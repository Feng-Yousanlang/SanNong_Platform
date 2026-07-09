package com.ltqtest.springbootquickstart.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginLogout_flowForBuyer() throws Exception {
        String username = TestDataFactory.uniqueUsername("buyer_it_");

        Map<String, Object> registerBody = new HashMap<>();
        registerBody.put("username", username);
        registerBody.put("password", "123456");
        registerBody.put("passwordConfirm", "123456");
        registerBody.put("identity", "2");
        registerBody.put("name", "集成测试买家");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, String> loginBody = Map.of("username", username, "password", "123456");
        mockMvc.perform(post("/api/auth/login/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.identity").value("2"));

        Map<String, String> logoutBody = Map.of("username", username);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void register_shouldRejectAdminSelfRegistration() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", TestDataFactory.uniqueUsername("admin_it_"));
        body.put("password", "123456");
        body.put("passwordConfirm", "123456");
        body.put("identity", "5");
        body.put("name", "管理员");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(403, json.get("code").asInt());
        assertTrue(json.get("message").asText().contains("仅支持注册农户或买家"));
    }
}
