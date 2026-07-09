package com.ltqtest.springbootquickstart.comment.repository;

import com.ltqtest.springbootquickstart.comment.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {
    
    Optional<ProductComment> findByProductCommentId(Long productCommentId);
    
    List<ProductComment> findByRootCommentId(Long rootCommentId);

    Optional<ProductComment> findByProductCommentIdAndUserId(Long productCommentId, Integer userId);
    
    List<ProductComment> findByProductId(Integer productId);
    
    List<ProductComment> findByProductIdAndRootCommentIdIsNull(Integer productId);
}
