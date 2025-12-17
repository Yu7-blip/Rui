package com.auth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartDTO {
    private Long id;
    private Long userId;
    private String username;
    private List<CartItemDTO> cartItems = new ArrayList<>();
    private Double totalPrice;
    private Integer totalQuantity;
    private LocalDateTime updatedAt;

    // 默认构造函数
    public CartDTO() {}

    // 带参构造函数
    public CartDTO(Long id, Long userId, String username, Double totalPrice, Integer totalQuantity) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    // 添加购物车项
    public void addCartItem(CartItemDTO cartItem) {
        this.cartItems.add(cartItem);
        calculateTotals();
    }

    // 计算总计
    public void calculateTotals() {
        this.totalQuantity = cartItems.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
        this.totalPrice = cartItems.stream()
                .mapToDouble(CartItemDTO::getItemTotalPrice)
                .sum();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<CartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
        calculateTotals();
    }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}