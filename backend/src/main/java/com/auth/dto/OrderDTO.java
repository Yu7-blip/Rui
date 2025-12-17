package com.auth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String username;
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    private Double totalPrice;
    private String status;
    private String customerNotes;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedReadyTime;
    private LocalDateTime completedDate;
    private LocalDateTime createdAt;

    // 默认构造函数
    public OrderDTO() {}

    // 带参构造函数
    public OrderDTO(Long id, String orderNumber, Long userId, String username,
                    Double totalPrice, String status, LocalDateTime orderDate) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.username = username;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
    }

    // 添加订单项
    public void addOrderItem(OrderItemDTO orderItem) {
        this.orderItems.add(orderItem);
        calculateTotalPrice();
    }

    // 计算总价
    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .mapToDouble(OrderItemDTO::getItemTotalPrice)
                .sum();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
        calculateTotalPrice();
    }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LocalDateTime getEstimatedReadyTime() { return estimatedReadyTime; }
    public void setEstimatedReadyTime(LocalDateTime estimatedReadyTime) { this.estimatedReadyTime = estimatedReadyTime; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}