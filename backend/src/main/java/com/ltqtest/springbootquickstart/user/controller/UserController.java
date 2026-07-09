package com.ltqtest.springbootquickstart.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.multipart.MultipartFile;
import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.expert.entity.ExpertUserChatRecord;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.entity.UserAddress;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import com.ltqtest.springbootquickstart.expert.repository.ExpertUserChatRecordRepository;
import com.ltqtest.springbootquickstart.user.repository.UserAddressRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.File;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private ExpertUserChatRecordRepository expertUserChatRecordRepository;
    
    @Value("${file.upload.base-path}")
    private String uploadBasePath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @Value("${file.upload.access-base-url}")
    private String accessBaseUrl;
    
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif");
    }
    
    private String getExtensionByContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            default:
                return "jpg";
        }
    }
    
    @GetMapping("/user/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(null);
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            String imageUrl = user.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String fileName;
            if (imageUrl.startsWith(accessBaseUrl)) {
                fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            } else if (imageUrl.startsWith(avatarPath)) {
                fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            } else {
                fileName = imageUrl;
            }
            
            String filePath = uploadBasePath + avatarPath + userId + "/" + fileName;
            File imageFile = new File(filePath);
            
            if (!imageFile.exists() || !imageFile.isFile()) {
                logger.warning("用户ID为" + userId + "的头像文件不存在: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageData = Files.readAllBytes(Paths.get(filePath));
            
            String extension = StringUtils.getFilenameExtension(fileName);
            MediaType mediaType;
            if ("png".equalsIgnoreCase(extension)) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("gif".equalsIgnoreCase(extension)) {
                mediaType = MediaType.IMAGE_GIF;
            } else {
                mediaType = MediaType.IMAGE_JPEG;
            }
            
            logger.info("成功返回用户ID为" + userId + "的头像图片");
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageData);
                    
        } catch (IOException e) {
            logger.severe("读取用户ID为" + userId + "的头像文件时出错: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            logger.severe("处理用户ID为" + userId + "的头像请求时发生未预期错误: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/user/profile")
    public Result<Map<String, Object>> getUser(@RequestParam("userId") Integer userId,
                                            @RequestParam(value = "imageReturnFormat", defaultValue = "url") String imageReturnFormat) {
        try {
            if(userId == null || userId <= 0) {
                return Result.error(400, "参数错误，userId不能为空");
            }
            User user = userRepository.findByUserId(userId).orElse(null);
            
            if(user == null) {
                return Result.error(404, "用户不存在");
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("real_name", user.getRealName());
            userData.put("role_type", user.getRoleType());
            userData.put("phone", user.getPhone());
            userData.put("email", user.getEmail());
            
            String imageUrl = user.getImageUrl();
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if ("base64".equalsIgnoreCase(imageReturnFormat)) {
                    String base64Image = convertImageToBase64(userId, imageUrl);
                    if (base64Image != null) {
                        userData.put("image_data", base64Image);
                        userData.put("image_type", "base64");
                    } else {
                        userData.put("image_data", "");
                        userData.put("image_type", "base64");
                        logger.warning("用户ID为" + userId + "的头像无法转换为Base64格式");
                    }
                } else {
                    if (!imageUrl.startsWith(accessBaseUrl)) {
                        if (imageUrl.startsWith(avatarPath)) {
                            imageUrl = accessBaseUrl + imageUrl;
                        } else {
                            imageUrl = accessBaseUrl + avatarPath + userId + "/" + imageUrl;
                        }
                    }
                    userData.put("image_url", imageUrl);
                }
            } else {
                if ("base64".equalsIgnoreCase(imageReturnFormat)) {
                    userData.put("image_data", "");
                    userData.put("image_type", "base64");
                } else {
                    userData.put("image_url", imageUrl);
                }
            }
            
            userData.put("expert_id", user.getExpertId() != null ? user.getExpertId() : 0);
            userData.put("approver_id", user.getApproverId() != null ? user.getApproverId() : 0);
            userData.put("create_time", user.getCreateTime());
            return Result.success(200, "成功", userData);
            
        } catch (Exception e) {
            logger.severe("获取用户信息失败: " + e.getMessage());
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    private String convertImageToBase64(Integer userId, String imageUrl) {
        try {
            String fileName;
            if (imageUrl.startsWith(accessBaseUrl)) {
                fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            } else if (imageUrl.startsWith(avatarPath)) {
                fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            } else {
                fileName = imageUrl;
            }
            
            String filePath = uploadBasePath + avatarPath + userId + "/" + fileName;
            File imageFile = new File(filePath);

            if (!imageFile.exists() || !imageFile.isFile()) {
                logger.warning("用户ID为" + userId + "的头像文件不存在: " + filePath);
                return null;
            }
            
            byte[] imageData = Files.readAllBytes(Paths.get(filePath));
            String base64 = java.util.Base64.getEncoder().encodeToString(imageData);
            
            String extension = StringUtils.getFilenameExtension(fileName);
            String mimeType;
            if ("png".equalsIgnoreCase(extension)) {
                mimeType = "image/png";
            } else if ("gif".equalsIgnoreCase(extension)) {
                mimeType = "image/gif";
            } else {
                mimeType = "image/jpeg";
            }
            
            return "data:" + mimeType + ";base64," + base64;
            
        } catch (Exception e) {
            logger.severe("用户ID为" + userId + "的头像转换为Base64时出错: " + e.getMessage());
            return null;
        }
    }
    
    @PutMapping("/user/profile/update")
    public Result<Map<String, Object>> updateUserProfile(
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "real_name", required = false) String realName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误，userId不能为空且必须大于0");
            }
            
           
            User user = userRepository.findByUserId(userId).orElse(null);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            if (realName != null && !realName.isEmpty()) {
                user.setRealName(realName);
            }
            if (phone != null && !phone.isEmpty()) {
                user.setPhone(phone);
            }
            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
            }
            User updatedUser = userRepository.save(user);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userId", updatedUser.getUserId());
            resultMap.put("realName", updatedUser.getRealName());
            resultMap.put("phone", updatedUser.getPhone());
            resultMap.put("email", updatedUser.getEmail());
            return Result.success(200, "更新成功", resultMap);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数无效：" + e.getMessage());
        } catch (Exception e) {
            System.err.println("更新用户信息时发生错误：" + e.getMessage());
            e.printStackTrace();
            
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @PostMapping("/user/password/update")
    public Result<?> updatePassword(
            @RequestParam("userId") Integer userId,
            @RequestParam("old_password") String oldPassword,
            @RequestParam("new_password") String newPassword) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误，userId不能为空且必须大于0");
            }
            if (oldPassword.isEmpty()||oldPassword == null) {
                return Result.error(400, "旧密码不能为空");
            }
            if (newPassword.isEmpty()||newPassword == null) {
                return Result.error(400, "新密码不能为空");
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            String storedPassword = user.getPassword();
            
            boolean isOldPasswordCorrect = oldPassword.equals(storedPassword);
            
            if (!isOldPasswordCorrect) {
                return Result.error(400, "旧密码不正确");
            }
            
            if (newPassword.equals(oldPassword)) {
                return Result.error(400, "新密码不能与旧密码相同");
            }
            
            user.setPassword(newPassword);
            
            userRepository.save(user);
            
            logger.info("用户ID为" + userId + "的密码修改成功");
            return Result.success(200, "密码修改成功");
        } catch (Exception e) {
            logger.severe("用户ID为" + userId + "的密码修改失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }

    @PostMapping("/user/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("userId") Integer userId,
            @RequestPart("file") MultipartFile file) {
        
        try {
            if (userId == null || userId <= 0) {
                logger.warning("用户ID参数无效: " + userId);
                return Result.error(400, "参数错误，userId不能为空且必须大于0");
            }
            
            if (file == null || file.isEmpty()) {
                logger.warning("用户ID为" + userId + "未选择上传文件");
                return Result.error(400, "请选择要上传的头像文件");
            }
            
            String contentType = file.getContentType();
            if (contentType == null) {
                logger.warning("用户ID为" + userId + "上传的文件无法识别类型");
                return Result.error(400, "无法识别文件类型");
            }
            
            if (!isValidImageType(contentType)) {
                logger.warning("用户ID为" + userId + "上传了不支持的文件类型: " + contentType);
                return Result.error(400, "只支持JPG、PNG、GIF格式的图片文件上传");
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                logger.warning("未找到用户ID为" + userId + "的用户信息");
                return Result.error(404, "用户不存在");
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            
            if (extension == null || extension.isEmpty()) {
                extension = getExtensionByContentType(contentType);
                logger.info("用户ID为" + userId + "的文件无扩展名，根据MIME类型推断为: " + extension);
            }
            
            String newFilename = "avatar_" + userId + "_" + UUID.randomUUID() + "." + extension;
            
            String uploadRootDir = uploadBasePath + avatarPath;
            String userDir = uploadRootDir + userId + "/";
            
            java.io.File dir = new java.io.File(userDir);
            
            if (!dir.getCanonicalPath().startsWith(new java.io.File(uploadRootDir).getCanonicalPath())) {
                logger.severe("用户ID为" + userId + "尝试使用非法文件路径: " + userDir);
                return Result.error(400, "无效的文件路径");
            }
            
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    logger.severe("无法创建用户ID为" + userId + "的头像存储目录: " + userDir);
                    return Result.error(500, "创建文件存储目录失败");
                }
                logger.info("为用户ID" + userId + "创建了头像存储目录: " + userDir);
            }
            
            String filePath = userDir + newFilename;
            java.io.File dest = new java.io.File(filePath);
            try {
                file.transferTo(dest.getAbsoluteFile());
                logger.info("用户ID为" + userId + "的头像文件保存成功: " + dest.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("用户ID为" + userId + "的头像文件保存失败: " + e.getMessage());
                throw e;
            }
            
            String imageUrl = accessBaseUrl + avatarPath + userId + "/" + newFilename;
            
            String oldImageUrl = user.getImageUrl();
            
            user.setImageUrl(imageUrl);
            
            try {
                userRepository.save(user);
                logger.info("成功更新用户ID为" + userId + "的头像URL: " + imageUrl);
            } catch (DataAccessException e) {
                logger.severe("更新用户ID为" + userId + "的头像URL到数据库失败: " + e.getMessage());
                if (dest.exists()) {
                    boolean deleted = dest.delete();
                    logger.info("由于数据库更新失败，" + (deleted ? "成功" : "失败") + "删除已上传的头像文件: " + dest.getAbsolutePath());
                }
                return Result.error(500, "数据库更新失败，请稍后再试");
            }
            
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                try {
                    deleteOldAvatarFile(oldImageUrl, uploadRootDir, userId);
                } catch (Exception e) {
                    logger.warning("清理用户ID为" + userId + "的旧头像文件失败: " + e.getMessage());
                }
            }
            
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("image_url", imageUrl);
            
            logger.info("用户ID为" + userId + "的头像上传请求处理完成");
            return Result.success(200, "上传成功", resultMap);
            
        } catch (IllegalArgumentException e) {
            logger.warning("用户ID为" + userId + "的头像上传参数错误: " + e.getMessage());
            return Result.error(400, "参数错误：" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.severe("用户ID为" + userId + "的头像文件上传失败: " + e.getMessage());
            return Result.error(500, "文件上传失败，请检查文件是否可用");
        } catch (Exception e) {
            logger.severe("用户ID为" + userId + "的头像上传过程中发生未预期错误: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    private void deleteOldAvatarFile(String oldImageUrl, String uploadRootDir, Integer userId) {
        try {
            String fileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
            if (fileName.contains(userId.toString())) {
                java.io.File oldFile = new java.io.File(uploadRootDir + userId + "/" + fileName);
                if (oldFile.exists() && oldFile.isFile()) {
                    boolean deleted = oldFile.delete();
                    logger.info("" + (deleted ? "成功" : "失败") + "删除用户ID为" + userId + "的旧头像文件: " + oldFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("清理旧头像文件失败", e);
        }
    }
    
    @PostMapping("/user/upload/addAddress")
    public Result<Map<String, String>> addAddress(@RequestBody Map<String, String> requestBody) {
        try {
            Integer userId = Integer.parseInt(requestBody.get("userId"));
            String newAddress = requestBody.get("newAddress");
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：userId不能为空且必须大于0");
            }
            if (newAddress == null || newAddress.trim().isEmpty()) {
                return Result.error(400, "参数错误：新地址不能为空");
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            UserAddress userAddress = new UserAddress();
            userAddress.setUserId(userId);
            userAddress.setAddressName(newAddress);
            
            userAddressRepository.save(userAddress);
            
            return Result.success(200, "新增地址成功！");
        } catch (Exception e) {
            logger.severe("新增地址失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @PostMapping("/user/upload/modifyAddress")
    public Result<?> modifyAddress(
           @RequestBody Map<String, String> requestBody) {
        try {
            Integer addressId = Integer.parseInt(requestBody.get("addressId"));
            String newAddress = requestBody.get("newAddress");
            if (addressId == null || addressId <= 0) {
                return Result.error(400, "参数错误：addressId不能为空且必须大于0");
            }
            if (newAddress == null || newAddress.trim().isEmpty()) {
                return Result.error(400, "参数错误：新地址不能为空");
            }
            
            UserAddress userAddress = userAddressRepository.findById(addressId).orElse(null);
            if (userAddress == null) {
                return Result.error(404, "地址不存在");
            }
            
            userAddress.setAddressName(newAddress);
            userAddressRepository.save(userAddress);
            
            return Result.success(200, "修改地址成功！");
        } catch (Exception e) {
            logger.severe("修改地址失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @DeleteMapping("/user/upload/deleteAddress")
    public Result<Map<String, String>> deleteAddress(@RequestParam("addressId") Integer addressId) {
        try {
            if (addressId == null || addressId <= 0) {
                return Result.error(400, "参数错误：addressId不能为空且必须大于0");
            }
            
            if (!userAddressRepository.existsById(addressId)) {
                return Result.error(404, "地址不存在");
            }
            
            userAddressRepository.deleteById(addressId);
            
            return Result.success(200, "删除地址成功！");
        } catch (Exception e) {
            logger.severe("删除地址失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @GetMapping("/user/upload/address")
    public Result<Map<String, Object>> getAddress(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：userId不能为空且必须大于0");
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
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
            
            Map<String, Object> data = new HashMap<>();
            data.put("addressList", addressList);
            
            return Result.success(200, "查看个人地址成功！", data);
        } catch (Exception e) {
            logger.severe("查看个人地址失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }

    @PostMapping("/user/question")
    public Result<Map<String, Object>> userQuestion(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Integer expertId = (Integer) request.get("expertId");
            String question = (String) request.get("question");
            if (userId == null || userId <= 0 || expertId == null || expertId <= 0 || question == null || question.trim().isEmpty()) {
                return Result.error(400, "参数错误，userId、expertId不能为空且必须大于0，question不能为空");
            }
            

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("expertId", expertId);
            responseData.put("userId", userId);
            responseData.put("question", question);

            ExpertUserChatRecord chatRecord = new ExpertUserChatRecord();
            chatRecord.setUserId(userId);
            chatRecord.setExpertId(expertId);
            chatRecord.setQuestion(question);
            LocalDateTime now = LocalDateTime.now();
            chatRecord.setSendTime(now);
            expertUserChatRecordRepository.save(chatRecord);

            return Result.success(200, "提问成功");
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/user/list")
    public Result<List<Map<String, Object>>> getUserList(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：userId无效");
            }

            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            List<User> users;
            if (user.getRoleType() != null && user.getRoleType() == 5) {
                users = userRepository.findAll();
            } else {
                users = List.of(user);
            }

            List<Map<String, Object>> userList = new ArrayList<>();
            for (User item : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", item.getUserId());
                userMap.put("username", item.getUsername());
                userMap.put("realName", item.getRealName());
                userMap.put("roleType", item.getRoleType());
                userMap.put("phone", item.getPhone());
                userMap.put("email", item.getEmail());
                userList.add(userMap);
            }

            return Result.success(userList);
        } catch (Exception e) {
            logger.severe("获取用户列表失败: " + e.getMessage());
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/user/auth/update")
    public Result<Void> updateUserRole(@RequestBody Map<String, Object> request) {
        try {
            Integer adminId = toInteger(request.get("userId"));
            Integer newRole = toInteger(request.get("roleType"));
            Integer targetUserId = toInteger(request.get("userId_change"));

            if (adminId == null || newRole == null || targetUserId == null) {
                return Result.error(400, "参数错误");
            }
            if (newRole < 1 || newRole > 5) {
                return Result.error(400, "角色类型无效");
            }

            User admin = userRepository.findByUserId(adminId).orElse(null);
            if (admin == null || admin.getRoleType() == null || admin.getRoleType() != 5) {
                return Result.error(403, "仅平台管理员可修改用户权限");
            }

            User target = userRepository.findByUserId(targetUserId).orElse(null);
            if (target == null) {
                return Result.error(404, "目标用户不存在");
            }

            target.setRoleType(newRole);
            userRepository.save(target);
            return Result.success(200, "用户权限修改成功", null);
        } catch (Exception e) {
            logger.severe("修改用户权限失败: " + e.getMessage());
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/user/auth/delete")
    public Result<Void> deleteUserByAdmin(@RequestBody Map<String, Object> request) {
        try {
            Integer adminId = toInteger(request.get("userId"));
            Integer targetUserId = toInteger(request.get("userId_delete"));

            if (adminId == null || targetUserId == null) {
                return Result.error(400, "参数错误");
            }
            if (adminId.equals(targetUserId)) {
                return Result.error(400, "不能删除当前登录的管理员账号");
            }

            User admin = userRepository.findByUserId(adminId).orElse(null);
            if (admin == null || admin.getRoleType() == null || admin.getRoleType() != 5) {
                return Result.error(403, "仅平台管理员可删除用户");
            }

            User target = userRepository.findByUserId(targetUserId).orElse(null);
            if (target == null) {
                return Result.error(404, "目标用户不存在");
            }

            userRepository.deleteById(targetUserId);
            return Result.success(200, "用户删除成功", null);
        } catch (Exception e) {
            logger.severe("删除用户失败: " + e.getMessage());
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
