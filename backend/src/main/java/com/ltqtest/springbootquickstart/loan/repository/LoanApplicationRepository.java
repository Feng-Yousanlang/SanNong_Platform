package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {
    List<LoanApplication> findByUserId(Integer userId);
    
    List<LoanApplication> findByUserIdAndStatus(Integer userId, Integer status);
    
    List<LoanApplication> findByStatus(Integer status);
}
