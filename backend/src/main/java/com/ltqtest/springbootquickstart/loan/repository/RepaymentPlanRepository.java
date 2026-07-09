package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.RepaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepaymentPlanRepository extends JpaRepository<RepaymentPlan, Integer> {

    List<RepaymentPlan> findByApplicationId(Integer applicationId);
}
