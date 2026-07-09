package com.ltqtest.springbootquickstart.buyrequest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import com.ltqtest.springbootquickstart.buyrequest.repository.BuyRequestRepository;
import com.ltqtest.springbootquickstart.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuyRequestController.class)
@Import(SecurityConfig.class)
class BuyRequestControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BuyRequestRepository buyRequestRepository;

    @Test
    void publish_shouldRejectEmptyContent() throws Exception {
        mockMvc.perform(post("/api/buyRequest/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "求购",
                                "content", "   ",
                                "contact", "13800000000"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void list_shouldReturnBuyRequests() throws Exception {
        BuyRequest request = new BuyRequest();
        request.setBuyRequestId(1);
        request.setTitle("求购土豆");
        request.setContent("需要100斤");
        when(buyRequestRepository.findAllByOrderByCreateTimeDesc()).thenReturn(List.of(request));

        mockMvc.perform(get("/api/buyRequest/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("求购土豆"));
    }

    @Test
    void publish_shouldPersistValidRequest() throws Exception {
        BuyRequest saved = new BuyRequest();
        saved.setBuyRequestId(2);
        saved.setTitle("求购苹果");
        saved.setContent("新鲜苹果");
        when(buyRequestRepository.save(any(BuyRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/buyRequest/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "求购苹果",
                                "content", "新鲜苹果",
                                "contact", "13800000000"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.buyRequestId").value(2));
    }
}
