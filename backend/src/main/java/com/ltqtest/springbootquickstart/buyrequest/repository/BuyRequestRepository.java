package com.ltqtest.springbootquickstart.buyrequest.repository;

import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BuyRequestRepository extends JpaRepository<BuyRequest, Integer> {
    
    @Query("SELECT b FROM BuyRequest b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword%")
    List<BuyRequest> searchByKeyword(@Param("keyword") String keyword);
    
    List<BuyRequest> findAllByOrderByCreateTimeDesc();
    
    List<BuyRequest> findAllByOrderByCreateTimeAsc();
    void deleteById(Integer buyRequestId);
    boolean existsById(Integer buyRequestId);
}
