package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.Approver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproverRepository extends JpaRepository<Approver, Integer> {
}
