package com.ltqtest.springbootquickstart.loan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;

public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Integer> {
    FinancialProduct findByFpName(String fpName);
}
