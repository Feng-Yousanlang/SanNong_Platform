package com.ltqtest.springbootquickstart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import com.ltqtest.springbootquickstart.buyrequest.repository.BuyRequestRepository;
import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BuyRequestIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BuyRequestRepository buyRequestRepository;

    @Test
    void publishListAndDelete_shouldWorkEndToEnd() throws Exception {
        Map<String, Object> publishBody = Map.of(
                "title", "求购苹果",
                "content", "需要红富士苹果 100 斤",
                "contact", "13800001111"
        );

        mockMvc.perform(post("/api/buyRequest/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("求购苹果"));

        mockMvc.perform(get("/api/buyRequest/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("求购苹果"));

        BuyRequest saved = buyRequestRepository.findAll().get(0);
        mockMvc.perform(delete("/api/buyRequest/delete").param("id", String.valueOf(saved.getBuyRequestId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
