package com.ltqtest.springbootquickstart.comment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_comment")
public class ProductComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_comment_id", nullable = false, updatable = false)
    private Long productCommentId;
    
    @Column(name = "content", length = 1000, nullable = false)
    private String content;
    
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "comment_like_count")
    private Long commentLikeCount;
    
    @Column(name = "root_comment_id")
    private Long rootCommentId;

    @Column(name = "to_comment_id")
    private Long toCommentId;
    
  
}
