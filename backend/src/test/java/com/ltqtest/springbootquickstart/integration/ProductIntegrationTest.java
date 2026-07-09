package com.ltqtest.springbootquickstart.integration;

import com.ltqtest.springbootquickstart.product.entity.Product;
import com.ltqtest.springbootquickstart.product.repository.ProductRepository;
import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void seedProducts() {
        User farmer = TestDataFactory.newFarmer(TestDataFactory.uniqueUsername("farmer_prod_"));
        farmer = userRepository.save(farmer);

        Product product = TestDataFactory.newProduct("有机大米", farmer.getUserId());
        productRepository.save(product);
    }

    @Test
    void buyerProducts_shouldReturnOnSaleItems() throws Exception {
        mockMvc.perform(get("/api/products/buyer").param("nums", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.products[0].productName").value("有机大米"));
    }

    @Test
    void buyerProductDetail_shouldReturn404ForMissingId() throws Exception {
        mockMvc.perform(get("/api/products/buyer/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void buyerProducts_shouldRejectInvalidNums() throws Exception {
        mockMvc.perform(get("/api/products/buyer").param("nums", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
