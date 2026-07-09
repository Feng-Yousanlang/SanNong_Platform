package com.ltqtest.springbootquickstart.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltqtest.springbootquickstart.config.SecurityConfig;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void login_shouldReturnIdentityWhenPasswordMatches() throws Exception {
        User user = TestDataFactory.newFarmer("farmer_login");
        user.setUserId(11);
        when(userService.findByUsername("farmer_login")).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("farmer_login");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("1"))
                .andExpect(jsonPath("$.data.identity").value("1"))
                .andExpect(jsonPath("$.data.id").value("11"));
    }

    @Test
    void register_shouldRejectExpertRole() throws Exception {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("expert_try");
        request.setPassword("123456");
        request.setPasswordConfirm("123456");
        request.setIdentity("3");
        request.setName("专家");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void register_shouldAcceptFarmerWhenUsernameAvailable() throws Exception {
        when(userService.findByUsername("farmer_new")).thenReturn(Optional.empty());
        when(userService.register(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(99);
            return saved;
        });

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("farmer_new");
        request.setPassword("123456");
        request.setPasswordConfirm("123456");
        request.setIdentity("1");
        request.setName("新农户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void register_shouldRejectPasswordMismatch() throws Exception {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("buyer_try");
        request.setPassword("123456");
        request.setPasswordConfirm("654321");
        request.setIdentity("2");
        request.setName("买家");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(300));
    }

    @Test
    void login_shouldRejectWrongPassword() throws Exception {
        User user = TestDataFactory.newFarmer("farmer_wrong");
        when(userService.findByUsername("farmer_wrong")).thenReturn(Optional.of(user));

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("farmer_wrong");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logout_shouldReturnSuccessForExistingUser() throws Exception {
        User user = TestDataFactory.newBuyer("buyer_logout");
        when(userService.findByUsername("buyer_logout")).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthController.LogoutRequest request = new AuthController.LogoutRequest();
        request.setUsername("buyer_logout");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
