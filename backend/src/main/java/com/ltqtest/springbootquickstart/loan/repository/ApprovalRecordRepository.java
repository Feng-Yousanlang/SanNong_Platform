package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Integer> {
    
    List<ApprovalRecord> findByApplicationId(Integer applicationId);
    
    List<ApprovalRecord> findByApproverId(Integer approverId);
}
