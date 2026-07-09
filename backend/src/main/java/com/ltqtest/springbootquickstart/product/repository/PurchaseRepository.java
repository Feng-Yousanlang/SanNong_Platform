package com.ltqtest.springbootquickstart.product.repository;

import com.ltqtest.springbootquickstart.product.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    
    Purchase findByPurchaseId(Integer purchaseId);
    
    List<Purchase> findByUserId(Integer userId);
    
    List<Purchase> findByUserIdAndStatus(Integer userId, Integer status);
}
