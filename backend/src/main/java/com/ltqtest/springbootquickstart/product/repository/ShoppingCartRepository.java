package com.ltqtest.springbootquickstart.product.repository;

import com.ltqtest.springbootquickstart.product.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCartId(Integer cartId);
    List<ShoppingCart> findByUserId(Integer userId);
    List<ShoppingCart> findByUserIdAndStatus(Integer userId, Integer status);
    Optional<ShoppingCart> findByUserIdAndProductIdAndStatus(Integer userId, Integer productId, Integer status);
}
