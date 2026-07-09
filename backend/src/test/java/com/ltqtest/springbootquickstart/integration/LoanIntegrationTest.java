package com.ltqtest.springbootquickstart.integration;

import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.loan.repository.FinancialProductRepository;
import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FinancialProductRepository financialProductRepository;

    @BeforeEach
    void seedLoanProducts() {
        FinancialProduct product = TestDataFactory.newFinancialProduct("惠农贷");
        financialProductRepository.save(product);
    }

    @Test
    void loanProducts_shouldReturnSeededList() throws Exception {
        mockMvc.perform(get("/api/loan/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].fpName").value("惠农贷"));
    }

    @Test
    void pendingLoans_shouldReturnEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/api/loan/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void applications_shouldRejectMissingUserId() throws Exception {
        mockMvc.perform(get("/api/loan/applications"))
                .andExpect(status().isBadRequest());
    }
}
