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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FinancialProductRepository financialProductRepository;

    @BeforeEach
    void seedProducts() {
        FinancialProduct product = TestDataFactory.newFinancialProduct("春耕贷");
        financialProductRepository.save(product);
    }

    @Test
    void financingProducts_shouldReturnSeededProduct() throws Exception {
        mockMvc.perform(get("/api/financing/products/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.products[0].fpName").value("春耕贷"))
                .andExpect(jsonPath("$.data.products[0].tags[0]").value("低息"));
    }

    @Test
    void financialProductDetail_shouldReturnManagerInfo() throws Exception {
        FinancialProduct product = financialProductRepository.findAll().get(0);

        mockMvc.perform(post("/api/financial/products/")
                        .contentType("application/json")
                        .content("{\"fpId\":" + product.getFpId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fpManagerName").value("张经理"));
    }

    @Test
    void financialProductDetail_shouldRejectInvalidId() throws Exception {
        mockMvc.perform(post("/api/financial/products/")
                        .contentType("application/json")
                        .content("{\"fpId\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
