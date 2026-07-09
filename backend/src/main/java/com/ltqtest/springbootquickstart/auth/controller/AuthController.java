package com.ltqtest.springbootquickstart.auth.controller;

import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String passwordConfirm;
        private String identity;
        private String name;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String identity;
        private String id;
    }

    @Data
    public static class LogoutRequest {
        private String username;
    }

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        try {
            if (isBlank(request.getUsername())
                    || isBlank(request.getPassword())
                    || isBlank(request.getPasswordConfirm())
                    || isBlank(request.getIdentity())
                    || isBlank(request.getName())) {
                return Result.error(500, "注册失败");
            }

            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                return Result.error(300, "两次输入密码不一致");
            }

            Integer roleType;
            try {
                roleType = Integer.parseInt(request.getIdentity());
                if (roleType < 1 || roleType > 5) {
                    return Result.error(500, "注册失败");
                }
            } catch (NumberFormatException e) {
                return Result.error(500, "注册失败");
            }

            // 仅允许自助注册农户(1)与买家(2)，其余角色由平台分发
            if (roleType != 1 && roleType != 2) {
                return Result.error(403, "仅支持注册农户或买家，专家/银行/管理员账号由平台分发");
            }

            if (userService.findByUsername(request.getUsername()).isPresent()) {
                return Result.error(400, "该用户名已存在");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setRoleType(roleType);
            user.setRealName(request.getName());
            user.setLoginStatus(0);
            user.setExpertId(0);
            user.setApproverId(0);

            User savedUser = userService.register(user);
            return Result.success(savedUser);
        } catch (Exception e) {
            return Result.error(500, "注册失败");
        }
    }

    @PostMapping("/login/pwd")
    public Result<?> loginPwd(@RequestBody LoginRequest request) {
        try {
            if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
                return Result.error(400, "参数错误");
            }

            Optional<User> userOptional = userService.findByUsername(request.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 演示环境使用明文密码；生产环境应改为 PasswordEncoder 校验
                if (request.getPassword().equals(user.getPassword())) {
                    user.setLoginStatus(1);
                    userService.update(user);

                    LoginResponse response = new LoginResponse();
                    response.setToken("1");
                    response.setIdentity(String.valueOf(user.getRoleType()));
                    response.setId(String.valueOf(user.getUserId()));
                    return Result.success(response);
                }
            }

            return Result.error(401, "用户名或密码错误");
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody LogoutRequest request) {
        try {
            if (isBlank(request.getUsername())) {
                return Result.error(400, "参数错误");
            }

            Optional<User> userOptional = userService.findByUsername(request.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setLoginStatus(0);
                userService.update(user);
                return Result.success(null);
            }

            return Result.error(300, "用户不存在");
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}
