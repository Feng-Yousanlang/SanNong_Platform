
package com.ltqtest.springbootquickstart.product.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.product.entity.Product;
import com.ltqtest.springbootquickstart.product.entity.Purchase;
import com.ltqtest.springbootquickstart.product.entity.ShoppingCart;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.entity.UserAddress;
import com.ltqtest.springbootquickstart.product.repository.ProductRepository;
import com.ltqtest.springbootquickstart.product.repository.PurchaseRepository;
import com.ltqtest.springbootquickstart.product.repository.ShoppingCartRepository;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import com.ltqtest.springbootquickstart.integration.payment.PayUtil;

import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import jakarta.servlet.http.HttpServletRequest;

import com.ltqtest.springbootquickstart.user.repository.UserAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private PayUtil payUtil;
    
    private static final Logger logger = Logger.getLogger(ProductController.class.getName());
    
    @Value("${file.upload.base-path}")
    private String uploadBasePath;
    @Value("${file.upload.product-img-path}")
    private String productImgPath;
    @Value("${file.upload.access-base-url}")
    private String accessBaseUrl;
    private final String productImageApiUrl = "/api/products/image/";
    
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }
    
    private String getExtensionByContentType(String contentType) {
        if (contentType == null) return "jpg";
        switch (contentType) {
            case "image/jpeg": return "jpg";
            case "image/png": return "png";
            case "image/gif": return "gif";
            default: return "jpg";
        }
    }
    
    public String convertLocalImageToUrl(String localImagePath, Integer userId) throws IOException {
        if (localImagePath == null || localImagePath.isEmpty()) {
            throw new IOException("本地图片路径不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new IOException("用户ID无效");
        }
        
        File sourceFile = new File(localImagePath);
        
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            throw new IOException("本地图片文件不存在：" + localImagePath);
        }
        
        String originalFilename = sourceFile.getName();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        
        if (extension == null || extension.isEmpty()) {
            throw new IOException("无法识别图片文件类型");
        }
        
        String newFilename = "product_" + userId + "_" + UUID.randomUUID() + "." + extension;
        
        String targetDirPath = uploadBasePath + productImgPath + userId + File.separator;
        File targetDir = new File(targetDirPath);
        
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("无法创建目标目录：" + targetDirPath);
            }
        }
        
        String targetFilePath = targetDirPath + newFilename;
        Path targetPath = Paths.get(targetFilePath);
        
        Files.copy(Paths.get(localImagePath), targetPath);
        
        String imageUrl = "/api/uploads/product_images/" + userId + "/" + newFilename;
        
        return imageUrl;
    }
    
    @PostMapping("/products/upload-image")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Integer userId) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error(400, "图片文件不能为空");
            }
            if (userId == null || userId <= 0) {
                return Result.error(400, "用户ID无效");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "用户不存在");
            }
            
            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                return Result.error(400, "无效的图片类型，只支持jpg、png、gif格式");
            }
            
            String userDirPath = uploadBasePath + productImgPath + userId + File.separator;
            File userDir = new File(userDirPath);
            if (!userDir.exists()) {
                if (!userDir.mkdirs()) {
                    return Result.error(500, "无法创建上传目录");
                }
            }
            
            String extension = getExtensionByContentType(contentType);
            String filename = "product_" + userId + "_" + UUID.randomUUID() + "." + extension;
            String filePath = userDirPath + filename;
            
            file.transferTo(new File(filePath));
            
            String imageUrl = accessBaseUrl + userId + "/" + filename;
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("imageUrl", imageUrl);
            responseData.put("filename", filename);
            
            return Result.success(200, "图片上传成功", responseData);
        } catch (IOException e) {
            return Result.error(500, "文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "上传图片失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/image/{userId}/{filename}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable("userId") Integer userId, @PathVariable("filename") String filename) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(null);
            }
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            String filePath = uploadBasePath + productImgPath + userId + File.separator + filename;
            File imageFile = new File(filePath);
            
            if (!imageFile.exists() || !imageFile.isFile()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageData = Files.readAllBytes(imageFile.toPath());
            
            String extension = StringUtils.getFilenameExtension(filename).toLowerCase();
            MediaType mediaType;
            switch (extension) {
                case "jpg":
                case "jpeg":
                    mediaType = MediaType.IMAGE_JPEG;
                    break;
                case "png":
                    mediaType = MediaType.IMAGE_PNG;
                    break;
                case "gif":
                    mediaType = MediaType.IMAGE_GIF;
                    break;
                default:
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(imageData.length);
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/convert-image")
    public Result<Map<String, String>> convertImageToUrl(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            String localImagePath = request.get("localImagePath") == null ? null : request.get("localImagePath").toString();
            Integer userId;
            
            try {
                userId = request.get("userId") == null ? null : Integer.parseInt(request.get("userId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "用户ID格式不正确");
            }
            
            String imageUrl = convertLocalImageToUrl(localImagePath, userId);
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("imageUrl", imageUrl);
            
            return Result.success(200, "图片转化成功", responseData);
        } catch (IOException e) {
            return Result.error(400, "图片转化失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/buyer")
    public Result<Map<String, Object>> getProducts(@RequestParam(required = false, defaultValue = "10") Integer nums) {
        try {
            if (nums == null || nums <= 0) {
                return Result.error(400, "参数错误：nums必须为正整数");
            }
            
            List<Product> products = productRepository.findAll();
            
            int actualSize = Math.min(nums, products.size());
            List<Product> limitedProducts = new ArrayList<>();
            if (actualSize > 0) {
                limitedProducts = products.subList(0, actualSize);
            }
            
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : limitedProducts) {
                if (product.getStatus() == 1) {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("productId", product.getProductId());
                    productMap.put("productName", product.getProductName());
                    productMap.put("price", product.getPrice());
                    productMap.put("producer", product.getProducer());
                    productMap.put("salesVolume", product.getSalesVolume());
                    
                    String productImg = product.getProductImg();
                    productMap.put("productImg", productImg);
                    
                    if (productImg != null && !productImg.isEmpty()) {
                        try {
                            if (productImg.startsWith(accessBaseUrl)) {
                                String relativePath = productImg.substring(accessBaseUrl.length());
                                if (relativePath.contains("/")) {
                                    String[] parts = relativePath.split("/");
                                    if (parts.length >= 2) {
                                        String userId = parts[0];
                                        String filename = parts[1];
                                        String directImageUrl = productImageApiUrl + userId + "/" + filename;
                                        productMap.put("directImageUrl", directImageUrl);
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    productMap.put("surplus", product.getSurplus());
                    productList.add(productMap);
                    }
                }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("products", productList);
            
            return Result.success(200, "获取商品列表成功", responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/buyer/{productId}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return Result.error(400, "参数错误：商品ID无效");
            }
            
            Product product = productRepository.findByProductId(productId);
            
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            if (product.getStatus() != 1) {
                return Result.error(400, "商品已下架，无法查看详情");
            }
            
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productId", product.getProductId());
            productMap.put("productName", product.getProductName());
            productMap.put("price", product.getPrice());
            productMap.put("producer", product.getProducer());
            productMap.put("salesVolume", product.getSalesVolume());
            productMap.put("productImg", product.getProductImg());
            productMap.put("surplus", product.getSurplus());
            
            return Result.success(200, "获取商品详情成功", productMap);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/products/farmer/newProduct")
    public Result<Map<String, Object>> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("price") double price,
            @RequestParam(value = "producer", required = false, defaultValue = "") String producer,
            @RequestParam("totalVolumn") int totalVolumn,
            @RequestParam("userId") int userId,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            if (productName == null || productName.trim().isEmpty()) {
                return Result.error(400, "参数错误：商品名称不能为空");
            }
            if (price <= 0) {
                return Result.error(400, "参数错误：商品价格必须大于0");
            }
            if (userId <= 0) {
                return Result.error(400, "参数错误：农户ID无效");
            }
            if (totalVolumn <= 0) {
                return Result.error(400, "参数错误：总数量必须大于0");
            }
            
            String productImg = "";
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                System.out.println(contentType);
                if (!isValidImageType(contentType)) {
                    return Result.error(400, "无效的图片类型，只支持jpg、png、gif格式");
                }
                String originalFilename = file.getOriginalFilename();
                String extension=StringUtils.getFilenameExtension(originalFilename);

                if (extension == null || extension.isEmpty()) {
                    extension = getExtensionByContentType(contentType);
                    logger.info("用户ID为" + userId + "的文件无扩展名，根据MIME类型推断为: " + extension);
                }
                String newFilename="product_"+userId+"_"+UUID.randomUUID()+"."+extension;
                String uploadRootDir=uploadBasePath + productImgPath;
                String userDir=uploadRootDir+userId+"/";
                System.out.println(newFilename);
                System.out.println(uploadRootDir);
                System.out.println(userDir);
                java.io.File dir=new java.io.File(userDir);
                if (!dir.getCanonicalPath().startsWith(new java.io.File(uploadRootDir).getCanonicalPath())) {
                    logger.severe("用户ID为" + userId + "尝试使用非法文件路径: " + userDir);
                    return Result.error(400, "无效的文件路径");
                }
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        logger.severe("无法创建用户ID为" + userId + "的农产品存储目录: " + userDir);
                        return Result.error(500, "创建文件存储目录失败");
                    }
                    logger.info("为用户ID" + userId + "创建了农产品存储目录: " + userDir);
                }
                String filePath = userDir + newFilename;
                java.io.File dest=new java.io.File(filePath);
                try {
                    file.transferTo(dest.getAbsoluteFile());
                    logger.info("用户ID为" + userId + "的农产品图片文件保存成功: " + dest.getAbsolutePath());
                } catch (IOException e) {
                    logger.severe("用户ID为" + userId + "的农产品图片文件保存失败: " + dest.getAbsolutePath());
                    return Result.error(500, "文件保存失败：" + e.getMessage());
                }
                
                productImg = accessBaseUrl + productImgPath + userId + "/" + newFilename;
                System.out.println(productImg);
                
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "农户不存在");
            }
            
            Product product = new Product();
            product.setProductName(productName);
            product.setPrice(price);
            product.setProducer(producer);
            product.setSalesVolume(0);
            product.setSurplus(totalVolumn);
            product.setProductImg(productImg);
            product.setTotalVolumn(totalVolumn);
            product.setUserId(userId);
            
            Product savedProduct = productRepository.save(product);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productId", savedProduct.getProductId());
            responseData.put("productImg", productImg);
    
            return Result.success(200, "商品添加成功", responseData);
            
        } catch (Exception e) {
            return Result.error(500, "文件保存失败：" + e.getMessage());
        } 
    }
    
    @GetMapping("/products/farmer/getMyProducts")
    public Result<List<Map<String, Object>>> getFarmerProducts(@RequestParam Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：用户ID无效");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "用户不存在");
            }
            
            User user = userOptional.get();
            if (user.getRoleType() != 1) {
                return Result.error(403, "您不是农户，不可看农户发布的商品");
            }
            
            List<Product> products = productRepository.findByUserId(userId);
            
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                if (product.getStatus() == 1) {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("productId", product.getProductId());
                    productMap.put("productName", product.getProductName());
                    productMap.put("price", product.getPrice());
                    productMap.put("producer", product.getProducer());
                    productMap.put("salesVolume", product.getSalesVolume());
                    productMap.put("productImg", product.getProductImg());
                    productMap.put("surplus", product.getSurplus());
                    productList.add(productMap);
                }
            }
            
            return Result.success(200, "获取农户商品列表成功", productList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("products/buyer/shop")
    public Result<Map<String, Object>> addToShoppingCart(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if (!request.containsKey("productId") || request.get("productId") == null) {
                return Result.error(400, "参数错误：商品ID不能为空");
            }
            if (!request.containsKey("userId") || request.get("userId") == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            if (!request.containsKey("amount") || request.get("amount") == null) {
                return Result.error(400, "参数错误：购买数量不能为空");
            }
            
            Integer productId;
            Integer userId;
            Integer amount;
            try {
                productId = Integer.parseInt(request.get("productId").toString());
                userId = Integer.parseInt(request.get("userId").toString());
                amount = Integer.parseInt(request.get("amount").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：数值类型格式不正确");
            }
            
            Product product = productRepository.findByProductId(productId);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            if (product.getStatus() != 1) {
                return Result.error(400, "商品已下架，无法购买");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "用户不存在");
            }
            
            if (product.getSurplus() < amount) {
                return Result.error(400, "商品库存不足，当前库存：" + product.getSurplus());
            }
            
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setProductId(productId);
            shoppingCart.setUserId(userId);
            shoppingCart.setAmount(amount);
            shoppingCart.setTotalPrice( amount * product.getPrice());
            shoppingCartRepository.save(shoppingCart);
        
            productRepository.save(product);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "添加到购物车成功");
            
            return Result.success(200, "添加成功", responseData);
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("products/buyer/purchase")
    public Result<Map<String, Object>> purchaseProduct(@RequestBody Map<String, Object> request) throws AlipayApiException {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if (!request.containsKey("productId") || request.get("productId") == null) {
                return Result.error(400, "参数错误：商品ID不能为空");
            }
            if (!request.containsKey("userId") || request.get("userId") == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            if (!request.containsKey("amount") || request.get("amount") == null) {
                return Result.error(400, "参数错误：购买数量不能为空");
            }
            if (!request.containsKey("getAddress") || request.get("getAddress") == null) {
                return Result.error(400, "参数错误：收货地址不能为空");
            }
            
            Integer productId;
            Integer userId;
            Integer amount;
            String getAddress;
            
            try {
                productId = Integer.parseInt(request.get("productId").toString());
                userId = Integer.parseInt(request.get("userId").toString());
                amount = Integer.parseInt(request.get("amount").toString());
                getAddress = request.get("getAddress").toString();
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：数值类型格式不正确");
            }
            
            Product product = productRepository.findByProductId(productId);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            if (product.getStatus() != 1) {
                return Result.error(400, "商品已下架，无法购买");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "用户不存在");
            }
            
            if (product.getSurplus() < amount) {
                return Result.error(400, "商品库存不足，当前库存：" + product.getSurplus());
            }
            
            Purchase purchase = new Purchase();
            purchase.setProductId(productId);
            purchase.setUserId(userId);
            purchase.setAmount(amount);
            purchase.setGetAddress(getAddress);
            purchase.setStatus(2);
            purchase.setTotalPrice((double) amount * product.getPrice());
            
            Purchase savedPurchase = purchaseRepository.save(purchase);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("purchaseId", savedPurchase.getPurchaseId());
            
            responseData.put("alipay", payUtil.sendRequestToAlipay(purchase.getTotalPrice(), savedPurchase.getPurchaseId(), "购买商品支付"));

            return Result.success(200, "请支付", responseData);
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("products/buyer/purchaseReturn")
    public Result<Map<String, Object>> purchaseReturn(HttpServletRequest request) throws ParseException {
        try {
            Log.get().info("=>支付宝异步回调");
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            String tradeStatus = params.get("trade_status");
            if (tradeStatus.equals("TRADE_SUCCESS")) {
                String tradeNo = params.get("out_trade_no");
                Purchase purchase = purchaseRepository.findByPurchaseId(Integer.parseInt(tradeNo));
                if (purchase != null) {
                    purchase.setStatus(3);
                    purchaseRepository.save(purchase);
                    Product product = productRepository.findByProductId(purchase.getProductId());
                    if (product != null) {
                        product.setSurplus(product.getSurplus() - purchase.getAmount());
                        product.setSalesVolume(product.getSalesVolume() + purchase.getAmount());
                        productRepository.save(product);
                    }
                }
                return Result.success(200, "支付成功");
            } else {
                return Result.error(400, "支付失败");
            }
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/products/farmer/soldout")
    public Result<List<Map<String, Object>>> getSoldOutProducts(@RequestParam Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：农户ID无效");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "农户不存在");
            }
            
            List<Product> farmerProducts = productRepository.findByUserId(userId);
            
            List<Integer> productIds = new ArrayList<>();
            for (Product product : farmerProducts) {
                productIds.add(product.getProductId());
            }
            
            List<Map<String, Object>> soldoutProducts = new ArrayList<>();
            
            if (!productIds.isEmpty()) {
                List<Purchase> allPurchases = purchaseRepository.findAll();
                
                for (Purchase purchase : allPurchases) {
                    if (productIds.contains(purchase.getProductId())) {
                        Product product = productRepository.findByProductId(purchase.getProductId());
                        if (product != null) {
                            Map<String, Object> productMap = new HashMap<>();
                            productMap.put("productId", product.getProductId());
                            productMap.put("productName", product.getProductName());
                            productMap.put("productImg", product.getProductImg());
                            productMap.put("amout", purchase.getAmount());
                            productMap.put("money", product.getPrice());
                            productMap.put("totalPrice", purchase.getTotalPrice());
                            productMap.put("sendAddress", purchase.getGetAddress());
                            productMap.put("createTime", purchase.getCreateTime());
                            
                            soldoutProducts.add(productMap);
                        }
                    }
                }
            }
            
            return Result.success(200, "成功", soldoutProducts);
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/products/buyer/showPurchase")
    public Result<List<Map<String, Object>>> showPurchase(@RequestParam Integer userId) {
        try {
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            List<Purchase> purchaseItems = purchaseRepository.findByUserId(userId.intValue());
            
            List<Map<String, Object>> purchaseList = new ArrayList<>();
            for (Purchase purchaseItem : purchaseItems) {
                Product product = productRepository.findByProductId(purchaseItem.getProductId());
                if (product != null) {
                    Map<String, Object> purchaseMap = new HashMap<>();
                    purchaseMap.put("purchaseId", purchaseItem.getPurchaseId());
                    purchaseMap.put("productName", product.getProductName());
                    purchaseMap.put("producer", product.getProducer());
                    purchaseMap.put("productImg", product.getProductImg());
                    purchaseMap.put("amount", purchaseItem.getAmount());
                    purchaseMap.put("price", product.getPrice());
                    purchaseMap.put("totalPrice", product.getPrice() * purchaseItem.getAmount());
                    purchaseMap.put("sendAddress", purchaseItem.getGetAddress());
                    purchaseMap.put("createTime", purchaseItem.getCreateTime());
                    purchaseMap.put("status", purchaseItem.getStatus());
                    purchaseList.add(purchaseMap);
                }
            }
            
            return Result.success(200, "成功", purchaseList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/products/buyer/showshop")
    public Result<List<Map<String, Object>>> showShoppingCart(@RequestParam Integer userId) {
        try {
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndStatus(userId.intValue(), 1);
            
            List<Map<String, Object>> cartList = new ArrayList<>();
            for (ShoppingCart cartItem : cartItems) {
                Product product = productRepository.findByProductId(cartItem.getProductId());
                if (product != null) {
                    Map<String, Object> cartMap = new HashMap<>();
                    cartMap.put("cartId", cartItem.getCartId());
                    cartMap.put("productName", product.getProductName());
                    cartMap.put("producer", product.getProducer());
                    cartMap.put("productImg", product.getProductImg());
                    cartMap.put("amount", cartItem.getAmount());
                    cartMap.put("price", product.getPrice());
                    cartMap.put("totalPrice", product.getPrice() * cartItem.getAmount());
                    cartList.add(cartMap);
                }
            }
            
            return Result.success(200, "成功", cartList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/products/buyer/buyshop")
    public Result<?> buyFromShoppingCart(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if(!request.containsKey("cartId") || request.get("cartId") == null) {
                return Result.error(400, "参数错误：购物车ID不能为空");
            }
            
            Integer cartId;
            
            try {
                cartId = Integer.parseInt(request.get("cartId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：数值类型格式不正确");
            }
            
            Optional<ShoppingCart> cartOptional = shoppingCartRepository.findByCartId(cartId);
            if (!cartOptional.isPresent()) {
                return Result.error(404, "购物车不存在");
            }
            
            Integer productId = cartOptional.get().getProductId();
            
            Product product = productRepository.findByProductId(productId);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            if (product.getStatus() != 1) {
                return Result.error(400, "商品已下架，无法购买");
            }
            Integer userId = cartOptional.get().getUserId();
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "用户不存在");
            }
            
            Optional<ShoppingCart> cartItemOptional = shoppingCartRepository.findByCartId(cartId);
            ShoppingCart cartItem = cartItemOptional.get();
            Integer amount = cartItem.getAmount();
            
            if (product.getSurplus() < amount) {
                return Result.error(400, "商品库存不足，当前库存：" + product.getSurplus());
            }
            
            Purchase purchase = new Purchase();
            purchase.setProductId(productId);
            purchase.setUserId(userId);
            purchase.setAmount(amount);
            purchase.setGetAddress(cartItem.getGetAddress());
            purchase.setTotalPrice((double) amount * product.getPrice());
            purchase.setStatus(3);

            purchaseRepository.save(purchase);
            
            product.setSurplus(product.getSurplus() - amount);
            product.setSalesVolume(product.getSalesVolume() + amount);
            productRepository.save(product);
            
            shoppingCartRepository.delete(cartItem);
            
            return Result.success(200, "购买成功");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/products/buyer/shop/delete")
    public Result<?> deleteShoppingCartItem(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if(!request.containsKey("cartId") || request.get("cartId") == null) {
                return Result.error(400, "参数错误：购物车ID不能为空");
            }
            
            Integer cartId;
            
            try {
                cartId = Integer.parseInt(request.get("cartId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：数值类型格式不正确");
            }
    
            Optional<ShoppingCart> cartItemOptional = shoppingCartRepository.findByCartId(cartId);
            
            if (!cartItemOptional.isPresent()) {
                return Result.error(404, "购物车中不存在该条记录");
            }
            
            ShoppingCart cartItem = cartItemOptional.get();
            
            shoppingCartRepository.delete(cartItem);
            
            return Result.success(200, "删除成功");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/farmer/showAllPurchase")
    public Result<List<Map<String, Object>>> showAllPurchase(@RequestParam Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：农户ID无效");
            }
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "农户不存在");
            }
            
            List<Product> farmerProducts = productRepository.findByUserId(userId);
            
            List<Integer> productIds = new ArrayList<>();
            for (Product product : farmerProducts) {
                if (product.getStatus() != 1) {
                    continue;
                }
                productIds.add(product.getProductId());
            }
            
            List<Map<String, Object>> purchaseList = new ArrayList<>();
            
            if (!productIds.isEmpty()) {
                List<Purchase> allPurchases = purchaseRepository.findAll();
                
                for (Purchase purchase : allPurchases) {
                    if (productIds.contains(purchase.getProductId()) && purchase.getStatus() == 3) {
                        Map<String, Object> purchaseMap = new HashMap<>();
                        purchaseMap.put("purchaseId", purchase.getPurchaseId());
                        purchaseMap.put("productId", purchase.getProductId());
                        purchaseMap.put("amount", purchase.getAmount());
                        purchaseMap.put("totalPrice", purchase.getTotalPrice());
                        purchaseMap.put("getAddress", purchase.getGetAddress());
                        purchaseMap.put("createTime", purchase.getCreateTime());
                        
                        purchaseList.add(purchaseMap);
                    }
                }
            }
            
            return Result.success(200, "成功", purchaseList);
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/products/farmer/sendProduct")
    public Result<Map<String, Object>> sendProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("purchase_id") || request.get("purchase_id") == null) {
                return Result.error(400, "参数错误：购买记录ID不能为空");
            }
            
            Integer purchase_id;
            try {
                if (request.get("purchase_id") instanceof Number) {
                    purchase_id = ((Number) request.get("purchase_id")).intValue();
                } else {
                    purchase_id = Integer.parseInt(request.get("purchase_id").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：购买记录ID格式不正确");
            }
            
            System.out.println("purchase_id:" + purchase_id);
            if (purchase_id <= 0) {
                return Result.error(400, "参数错误：购买记录ID无效");
            }
            
            Optional<Purchase> purchaseOptional = purchaseRepository.findById(purchase_id);
            if (!purchaseOptional.isPresent()) {
                return Result.error(404, "购买记录不存在");
            }
            
            Purchase purchase = purchaseOptional.get();
            
            if (purchase.getStatus() != 3) {
                return Result.error(400, "只能对已付款待发货的商品进行发货操作");
            }
            
            purchase.setStatus(4);
            purchaseRepository.save(purchase);
            
            return Result.success(200, "已成功发货");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/products/farmer/cancelPurchase")
    public Result<Map<String, Object>> farmerCancelPurchase(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("purchase_id") || request.get("purchase_id") == null) {
                return Result.error(400, "参数错误：购买记录ID不能为空");
            }
            
            Integer purchase_id;
            try {
                if (request.get("purchase_id") instanceof Number) {
                    purchase_id = ((Number) request.get("purchase_id")).intValue();
                } else {
                    purchase_id = Integer.parseInt(request.get("purchase_id").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：购买记录ID格式不正确");
            }
            
            if (purchase_id <= 0) {
                return Result.error(400, "参数错误：购买记录ID无效");
            }
            
            Optional<Purchase> purchaseOptional = purchaseRepository.findById(purchase_id);
            if (!purchaseOptional.isPresent()) {
                return Result.error(404, "购买记录不存在");
            }
            
            Purchase purchase = purchaseOptional.get();
            
            if (purchase.getStatus() != 3) {
                return Result.error(400, "只能对已付款待发货的订单进行取消操作");
            }
            
            purchase.setStatus(6);
            purchaseRepository.save(purchase);
            
            return Result.success(200, "已成功取消");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/farmer/showOneStatusAllProduct")
    public Result<List<Map<String, Object>>> showOneStatusAllProduct(@RequestParam Integer userId, @RequestParam Integer status) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：农户ID无效");
            }
            if (status == null || status < 0) {
                return Result.error(400, "参数错误：状态值无效");
            }
            
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (!userOptional.isPresent()) {
                return Result.error(404, "农户不存在");
            }
            
            List<Product> farmerProducts = productRepository.findByUserId(userId);
            
            List<Integer> productIds = new ArrayList<>();
            for (Product product : farmerProducts) {
                if (product.getStatus() != 1) {
                    continue;
                }
                productIds.add(product.getProductId());
            }
            
            List<Map<String, Object>> productList = new ArrayList<>();
            
            if (!productIds.isEmpty()) {
                List<Purchase> allPurchases = purchaseRepository.findAll();
                
                for (Purchase purchase : allPurchases) {
                    if (productIds.contains(purchase.getProductId()) && purchase.getStatus() == status) {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("product_id", purchase.getProductId());
                        productMap.put("amount", purchase.getAmount());
                        productMap.put("totalPrice", purchase.getTotalPrice());
                        productMap.put("getAddress", purchase.getGetAddress());
                        productMap.put("createTime", purchase.getCreateTime());
                        
                        productList.add(productMap);
                    }
                }
            }
            
            return Result.success(200, "已成功商品", productList);
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/products/buyer/receiveProduct")
    public Result<Map<String, Object>> receiveProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("purchase_id") || request.get("purchase_id") == null) {
                return Result.error(400, "参数错误：购买记录ID不能为空");
            }
            
            Integer purchase_id;
            try {
                if (request.get("purchase_id") instanceof Number) {
                    purchase_id = ((Number) request.get("purchase_id")).intValue();
                } else {
                    purchase_id = Integer.parseInt(request.get("purchase_id").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：购买记录ID格式不正确");
            }
            
            if (purchase_id <= 0) {
                return Result.error(400, "参数错误：购买记录ID无效");
            }
            
            Optional<Purchase> purchaseOptional = purchaseRepository.findById(purchase_id);
            if (!purchaseOptional.isPresent()) {
                return Result.error(404, "购买记录不存在");
            }
            
            Purchase purchase = purchaseOptional.get();
            
            if (purchase.getStatus() != 4) {
                return Result.error(400, "只能对已发货待收货的商品进行收货操作");
            }
            
            purchase.setStatus(5);
            purchaseRepository.save(purchase);
            
            return Result.success(200, "收货成功");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    @PostMapping("/products/buyer/cancelPurchase")
    public Result<Map<String, Object>> buyerCancelPurchase(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("purchase_id") || request.get("purchase_id") == null) {
                return Result.error(400, "参数错误：购买记录ID不能为空");
            }
            
            Integer purchase_id;
            try {
                if (request.get("purchase_id") instanceof Number) {
                    purchase_id = ((Number) request.get("purchase_id")).intValue();
                } else {
                    purchase_id = Integer.parseInt(request.get("purchase_id").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：购买记录ID格式不正确");
            }
            
            if (purchase_id <= 0) {
                return Result.error(400, "参数错误：购买记录ID无效");
            }
            Optional<Purchase> purchaseOptional = purchaseRepository.findById(purchase_id);
            if (!purchaseOptional.isPresent()) {
                return Result.error(404, "购买记录不存在");
            }
            
            Purchase purchase = purchaseOptional.get();
            
            if (purchase.getStatus() != 3) {
                return Result.error(400, "只能对已付款待发货的订单进行取消操作");
            }
            
            purchase.setStatus(6);
            purchaseRepository.save(purchase);
            Product product = productRepository.findByProductId(purchase.getProductId());
            product.setSurplus(product.getSurplus() + purchase.getAmount());
            product.setSalesVolume(product.getSalesVolume() - purchase.getAmount());
            productRepository.save(product);
            
            return Result.success(200, "已成功取消");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    @PostMapping("/products/buyer/returnProduct")
    public Result<Map<String, Object>> buyerReturnProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("purchase_id") || request.get("purchase_id") == null) {
                return Result.error(400, "参数错误：购买记录ID不能为空");
            }
            
            Integer purchase_id;
            try {
                if (request.get("purchase_id") instanceof Number) {
                    purchase_id = ((Number) request.get("purchase_id")).intValue();
                } else {
                    purchase_id = Integer.parseInt(request.get("purchase_id").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：购买记录ID格式不正确");
            }
            
            if (purchase_id <= 0) {
                return Result.error(400, "参数错误：购买记录ID无效");
            }
            
            Optional<Purchase> purchaseOptional = purchaseRepository.findById(purchase_id);
            if (!purchaseOptional.isPresent()) {
                return Result.error(404, "购买记录不存在");
            }
            
            Purchase purchase = purchaseOptional.get();
            
            if (purchase.getStatus() != 4&&purchase.getStatus()!=5) {
                return Result.error(400, "只能对已发货待收货或已收货的订单进行退货操作");
            }
            
            
            purchase.setStatus(7);
            purchaseRepository.save(purchase);
            Product product = productRepository.findByProductId(purchase.getProductId());
            product.setSurplus(product.getSurplus() + purchase.getAmount());
            product.setSalesVolume(product.getSalesVolume() - purchase.getAmount());
            productRepository.save(product);
            
            return Result.success(200, "已成功退货");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/products/farmer/deleteProduct")
    public Result<Map<String, Object>> deleteProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("productId") || request.get("productId") == null) {
                return Result.error(400, "参数错误：农产品ID不能为空");
            }
            Integer productId;
            try {
                if (request.get("productId") instanceof Number) {
                    productId = ((Number) request.get("productId")).intValue();
                } else {
                    productId = Integer.parseInt(request.get("productId").toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：农产品ID格式不正确");
            }
            
            if (productId <= 0) {
                return Result.error(400, "参数错误：农产品ID无效");
            }
            
            Optional<Product> productOptional = productRepository.findById(productId);
            if (!productOptional.isPresent()) {
                return Result.error(404, "农产品不存在");
            }
            
            Product product = productOptional.get();
            
            if (product.getStatus() != 1) {
                return Result.error(400, "只能下架已发布的农产品");
            }
            
            List<Purchase> allPurchases = purchaseRepository.findAll();
            List<Purchase> productPurchases = new ArrayList<>();
            
            for (Purchase purchase : allPurchases) {
                if (purchase.getProductId().equals(productId)) {
                    productPurchases.add(purchase);
                }
            }
            
            for (Purchase purchase : productPurchases) {
                if (purchase.getStatus() == 3 ) {
                    purchase.setStatus(6);
                    purchaseRepository.save(purchase);
                }
            }
            
            product.setStatus(2);
            productRepository.save(product);
            
            return Result.success(200, "下架成功");
            
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/products/buyer/getSavedAddress")
    public Result<?> getSavedAddress(@RequestParam Integer userId) {
        try {
            if (userId == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            if (userId <= 0) {
                return Result.error(400, "参数错误：用户ID无效");
            }
            
            if (!userRepository.existsById(userId)) {
                return Result.error(404, "用户不存在");
            }
            
            List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
            
            List<Map<String, Object>> addressList = new ArrayList<>();
            for (UserAddress address : addresses) {
                Map<String, Object> addressMap = new HashMap<>();
                addressMap.put("addressId", address.getAddressId());
                addressMap.put("address_name", address.getAddressName());
                addressList.add(addressMap);
            }
            
            return Result.success(200, "查看地址成功", addressList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

}
