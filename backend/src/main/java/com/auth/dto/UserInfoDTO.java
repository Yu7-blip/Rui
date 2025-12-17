package com.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserInfoDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime registerTime;
    private Integer orderCount;
    private BigDecimal totalSpent;
    private String status;
    private List<RecentOrderDTO> recentOrders;

    // 构造器
    public UserInfoDTO() {}

    // getter和setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getRegisterTime() { return registerTime; }
    public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<RecentOrderDTO> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<RecentOrderDTO> recentOrders) { this.recentOrders = recentOrders; }

    // 内部类用于最近订单
    public static class RecentOrderDTO {
        private Long id;
        private String productName;
        private BigDecimal amount;

        public RecentOrderDTO() {}

        public RecentOrderDTO(Long id, String productName, BigDecimal amount) {
            this.id = id;
            this.productName = productName;
            this.amount = amount;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}