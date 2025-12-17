package com.auth.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_price", precision = 10, scale = 2)
    private Double totalPrice = 0.0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    public void calculateTotals() {
        this.totalPrice = cartItems.stream()
                .mapToDouble(item -> {
                    // 安全计算：检查所有可能为null的字段
                    if (item == null || item.getProduct() == null ||
                            item.getProduct().getPrice() == null || item.getQuantity() == null) {
                        return 0.0;
                    }
                    return item.getProduct().getPrice() * item.getQuantity();
                })
                .sum();
    }

    public Integer getTotalQuantity() {
        return cartItems.stream()
                .mapToInt(item -> {
                    // 安全获取数量
                    return item != null && item.getQuantity() != null ? item.getQuantity() : 0;
                })
                .sum();
    }

    public void addItem(Product product, int quantity) {
        // 参数检查
        if (product == null || product.getId() == null || quantity <= 0) {
            return;
        }

        CartItem existingItem = cartItems.stream()
                .filter(item -> item != null && item.getProduct() != null &&
                        item.getProduct().getId() != null &&
                        item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(this, product, quantity);
            cartItems.add(newItem);
        }
        calculateTotals();
    }

    public void removeItem(Long productId) {
        cartItems.removeIf(item -> item != null && item.getProduct() != null &&
                item.getProduct().getId() != null &&
                item.getProduct().getId().equals(productId));
        calculateTotals();
    }

    public void clear() {
        cartItems.clear();
        calculateTotals();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<CartItem> getCartItems() {
        // 确保不返回null
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        return cartItems;
    }
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        calculateTotals();
    }
    public Double getTotalPrice() { return totalPrice != null ? totalPrice : 0.0; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}