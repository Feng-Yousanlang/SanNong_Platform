package com.ltqtest.springbootquickstart.comment.controller;

import com.ltqtest.springbootquickstart.product.entity.Product;
import com.ltqtest.springbootquickstart.product.repository.ProductRepository;
import com.ltqtest.springbootquickstart.comment.entity.ProductComment;
import com.ltqtest.springbootquickstart.comment.repository.ProductCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ltqtest.springbootquickstart.common.Result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/")
public class ProductCommentController {
    
    @Autowired
    private ProductCommentRepository productCommentRepository;
    @Autowired
    private ProductRepository productRepository;
    
    @PostMapping("comment/like")
    public Result<Map<String, Long>> likeComment(@RequestBody Map<String, Object> request) {
        Long productCommentId = Long.valueOf(request.get("productCommentId").toString());
        if (productCommentId == null) {
            return Result.error(400, "评论ID不能为空");
        }
        
        Optional<ProductComment> commentOpt = productCommentRepository.findByProductCommentId(productCommentId);
        if (!commentOpt.isPresent()) {
            return Result.error(404, "评论不存在");
        }
        
        ProductComment comment = commentOpt.get();
        Long currentLikeCount = comment.getCommentLikeCount();
        comment.setCommentLikeCount(currentLikeCount != null ? currentLikeCount + 1 : 1);
        productCommentRepository.save(comment);
        
        Map<String, Long> data = new HashMap<>();
        data.put("commentLikeCount", comment.getCommentLikeCount());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        return Result.success(200, "点赞成功", data);
    }

     @PostMapping("comment/dislike")
    public Result<Map<String, Long>> dislikeComment(@RequestBody Map<String, Object> request) {
        Long productCommentId = Long.valueOf(request.get("productCommentId").toString());
        if (productCommentId == null) {
            return Result.error(400, "评论ID不能为空");
        }
        
        Optional<ProductComment> commentOpt = productCommentRepository.findByProductCommentId(productCommentId);
        if (!commentOpt.isPresent()) {
            return Result.error(404, "评论不存在");
        }
        
        ProductComment comment = commentOpt.get();
        Long currentLikeCount = comment.getCommentLikeCount();
        comment.setCommentLikeCount(currentLikeCount != null ? currentLikeCount - 1 : 1);
        productCommentRepository.save(comment);
        
        Map<String, Long> data = new HashMap<>();
        data.put("commentLikeCount", comment.getCommentLikeCount());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        return Result.success(200, "点赞成功", data);
    }

    @PostMapping("comment/delete")
    public Result<Map<String, Object>> deleteComment(Long productCommentId,Integer userId) {
        if (productCommentId == null || userId == null) {
            return Result.error(400, "评论ID和用户ID不能为空");
        }
        
        Optional<ProductComment> commentOpt = productCommentRepository.findByProductCommentIdAndUserId(productCommentId, userId);
        if (!commentOpt.isPresent()) {
            Optional<ProductComment> checkOpt = productCommentRepository.findByProductCommentId(productCommentId);
            if (!checkOpt.isPresent()) {
                return Result.error(404, "评论不存在");
            } else {
                return Result.error(403, "您无权删除该评论");
            }
        }
        
        deleteCommentAndChildren(productCommentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除成功");
        
        return Result.success(200, "删除成功", response);
    }
    
    private void deleteCommentAndChildren(Long commentId) {
        Optional<ProductComment> commentOpt = productCommentRepository.findByProductCommentId(commentId);
        if (commentOpt.isPresent()) {
            List<ProductComment> childCommentsByRoot = productCommentRepository.findByRootCommentId(commentId);

            List<ProductComment> allChildComments = new ArrayList<>();
            allChildComments.addAll(childCommentsByRoot);
            for (ProductComment child : allChildComments) {
                deleteCommentAndChildren(child.getProductCommentId());
            }
            
            productCommentRepository.delete(commentOpt.get());
        }
    }
    
    @PostMapping("comment")
    public Result<Map<String, Object>> addComment(@RequestBody CommentRequest request) {
        try {
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return Result.error(400, "评论内容不能为空");
            }
            
            if (request.getUserId() == null || request.getUserId() <= 0) {
                return Result.error(400, "用户ID不能为空且必须大于0");
            }
            
            if (request.getProductId() == null || request.getProductId() <= 0) {
                return Result.error(400, "商品ID不能为空且必须大于0");
            }
            
            ProductComment comment = new ProductComment();
            comment.setContent(request.getContent());
            comment.setSendTime(LocalDateTime.now());
            comment.setUserId(request.getUserId());
            comment.setProductId(request.getProductId());
            comment.setRootCommentId(request.getRootCommentId());
            comment.setCommentLikeCount(0L);
            comment.setToCommentId(request.getToCommentId());
            productCommentRepository.save(comment);
    
            Map<String, Object> response = new HashMap<>();
            response.put("productCommentId", comment.getProductCommentId());
            response.put("content", comment.getContent());
            response.put("rootCommentId", comment.getRootCommentId());
            response.put("sendTime", comment.getSendTime());
            response.put("toCommentId", comment.getToCommentId());
            return Result.success(200, "成功", response);
        } catch (Exception e) {
            return Result.error(500, "评论保存失败: " + e.getMessage());
        }
    }
    
    private static class CommentRequest {
        private String content;
        private Integer userId;
        private Integer productId;
        private Long rootCommentId;
        private Long toCommentId;
        
        public String getContent() {
            return content;
        }

        public Integer getUserId() {
            return userId;
        }
        public Integer getProductId() {
            return productId;
        }
        public Long getRootCommentId() {
            return rootCommentId;
        }
        public Long getToCommentId() {
            return toCommentId;
        }
    }
    @GetMapping("/commentarea")
    public Result<Map<String, Object>> getProductWithComments(@RequestParam Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return Result.error(400, "参数错误：商品ID无效");
            }
            
            Product product = productRepository.findByProductId(productId);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            List<ProductComment> comments = productCommentRepository.findByProductIdAndRootCommentIdIsNull(productId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", product.getUserId());
            responseData.put("productName", product.getProductName());
            responseData.put("price", product.getPrice());
            responseData.put("producer", product.getProducer());
            responseData.put("salesVolumn", product.getSalesVolume());
            responseData.put("productImg", product.getProductImg());
            responseData.put("surplus", product.getSurplus());
            
            List<Map<String, Object>> commentList = new ArrayList<>();
            for (ProductComment comment : comments) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("productCommentId", comment.getProductCommentId());
                commentMap.put("content", comment.getContent());
                commentMap.put("sendTime", comment.getSendTime());
                commentMap.put("userId", comment.getUserId());
                commentMap.put("commentLikeCount", comment.getCommentLikeCount()); 
                commentList.add(commentMap);
            }
            
            responseData.put("productComment", commentList);
            
            return Result.success(200, "获取成功", responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

      @GetMapping("/comment/childcomment")
    public Result<List<Map<String, Object>>> getChildComments(@RequestParam Long product_comment_id) {
        try {
            if (product_comment_id == null || product_comment_id <= 0) {
                return Result.error(400, "参数错误：评论ID无效");
            }
            
            List<ProductComment> childComments = productCommentRepository.findByRootCommentId(product_comment_id);
            
            List<Map<String, Object>> commentList = new ArrayList<>();
            
            for (ProductComment comment : childComments) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("productCommentId", comment.getProductCommentId());
                commentMap.put("toCommentId", comment.getToCommentId());
                commentMap.put("content", comment.getContent());
                commentMap.put("sendTime", comment.getSendTime());
                commentMap.put("userId", comment.getUserId());
                commentMap.put("commentLikeCount", comment.getCommentLikeCount());
                commentList.add(commentMap);
            }
            
            return Result.success(200, "成功返回子评论", commentList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
}
