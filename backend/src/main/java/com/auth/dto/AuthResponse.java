package com.auth.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private String username;
    private Long userId;
    private String role;      // 添加role字段
    private String token;     // 添加token字段

    // 构造器
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String username, Long userId, String role, String token) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.token = token;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public String toString() {
        return "AuthResponse{success=" + success +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                ", token='" + token + '\'' + '}';
    }
}