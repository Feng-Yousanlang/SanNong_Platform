package com.ltqtest.springbootquickstart.integration;

import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpertIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void expertsList_shouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/experts/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void expertSearch_shouldRejectEmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/experts/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
