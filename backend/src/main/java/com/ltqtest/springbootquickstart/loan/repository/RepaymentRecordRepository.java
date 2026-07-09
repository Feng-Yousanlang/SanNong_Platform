package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.RepaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRecordRepository extends JpaRepository<RepaymentRecord, Integer> {
    List<RepaymentRecord> findByUserId(Integer userId);
}
