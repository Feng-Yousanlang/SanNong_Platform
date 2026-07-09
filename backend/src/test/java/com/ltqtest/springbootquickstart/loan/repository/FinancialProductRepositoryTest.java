package com.ltqtest.springbootquickstart.loan.repository;

import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class FinancialProductRepositoryTest {

    @Autowired
    private FinancialProductRepository financialProductRepository;

    @Test
    void saveAndFindAll_shouldPersistProductFields() {
        FinancialProduct product = TestDataFactory.newFinancialProduct("仓储贷");
        financialProductRepository.save(product);

        List<FinancialProduct> all = financialProductRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("仓储贷", all.get(0).getFpName());
        assertTrue(all.get(0).getAnnualRate() > 0);
    }
}
