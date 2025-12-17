package com.auth.controller;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.entity.User;
import com.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 验证输入
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("用户名不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("密码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getPassword().length() < 6) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("密码长度不能少于6位");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 注册用户
            User user = userService.registerUser(
                    request.getUsername().trim(),
                    request.getPassword(),
                    request.getEmail() != null ? request.getEmail().trim() : null
            );

            // 返回成功响应
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("注册成功");
            response.setUsername(user.getUsername());
            response.setUserId(user.getId());
            response.setRole("USER");  // 新增：设置默认角色
            response.setToken("no-token-yet"); // 新增：设置占位符token

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 验证输入
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("用户名不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("密码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证用户凭据
            boolean isValid = userService.validateUser(
                    request.getUsername().trim(),
                    request.getPassword()
            );

            if (isValid) {
                Optional<User> userOptional = userService.getUserByUsername(request.getUsername().trim());

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // 返回成功响应 - 包含所有字段
                    AuthResponse response = new AuthResponse();
                    response.setSuccess(true);
                    response.setMessage("登录成功");
                    response.setUsername(user.getUsername());
                    response.setUserId(user.getId());
                    response.setRole("USER");  // 新增：设置角色
                    response.setToken("no-token-yet"); // 新增：设置占位符token

                    return ResponseEntity.ok(response);
                } else {
                    AuthResponse errorResponse = new AuthResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("用户信息不存在");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            } else {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("用户名或密码错误");
                return ResponseEntity.badRequest().body(errorResponse);
            }

        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取用户信息接口
     * 注意：现在不需要token验证，直接通过ID获取
     */
    @GetMapping("/user-info/{userId}")
    public ResponseEntity<AuthResponse> getUserInfo(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                AuthResponse response = new AuthResponse();
                response.setSuccess(true);
                response.setMessage("获取用户信息成功");
                response.setUsername(user.getUsername());
                response.setUserId(user.getId());
                response.setRole("USER");  // 新增：设置角色
                response.setToken("no-token-yet"); // 新增：设置占位符token

                return ResponseEntity.ok(response);
            } else {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("用户不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取用户信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth System is running");
    }

    /**
     * 简单的用户验证接口
     */
    @GetMapping("/check-user/{username}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String username) {
        try {
            Optional<User> userOptional = userService.getUserByUsername(username);
            return ResponseEntity.ok(userOptional.isPresent());
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * 管理员登录接口（与普通登录分开，设置不同角色）
     */
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            // 验证输入
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("管理员账号不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("管理员密码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证用户凭据
            boolean isValid = userService.validateUser(
                    request.getUsername().trim(),
                    request.getPassword()
            );

            if (isValid) {
                Optional<User> userOptional = userService.getUserByUsername(request.getUsername().trim());

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // 检查是否是管理员（简单判断：用户名为admin）
                    boolean isAdmin = "admin".equals(user.getUsername());

                    AuthResponse response = new AuthResponse();
                    response.setSuccess(isAdmin);

                    if (isAdmin) {
                        response.setMessage("管理员登录成功");
                        response.setUsername(user.getUsername());
                        response.setUserId(user.getId());
                        response.setRole("ADMIN");  // 设置为管理员角色
                        response.setToken("admin-token-placeholder");
                        return ResponseEntity.ok(response);
                    } else {
                        response.setMessage("非管理员账号");
                        return ResponseEntity.badRequest().body(response);
                    }
                } else {
                    AuthResponse errorResponse = new AuthResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("管理员账号不存在");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            } else {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("管理员账号或密码错误");
                return ResponseEntity.badRequest().body(errorResponse);
            }

        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}