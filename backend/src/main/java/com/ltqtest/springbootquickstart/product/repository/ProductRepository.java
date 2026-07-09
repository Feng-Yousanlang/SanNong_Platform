package com.ltqtest.springbootquickstart.product.repository;
import com.ltqtest.springbootquickstart.product.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    List<Product> findAll();
    
    Product findByProductId(Integer productId);
    List<Product> findByUserId(Integer userId);
}
