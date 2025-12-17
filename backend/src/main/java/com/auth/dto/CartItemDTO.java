package com.auth.dto;

public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productEmoji;
    private Double productPrice = 0.0;  // 设置默认值
    private Integer quantity = 0;       // 设置默认值
    private Double itemTotalPrice = 0.0; // 设置默认值

    // 默认构造函数
    public CartItemDTO() {}

    // 带参构造函数
    public CartItemDTO(Long id, Long productId, String productName, String productEmoji,
                       Double productPrice, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productEmoji = productEmoji;
        this.productPrice = productPrice != null ? productPrice : 0.0;
        this.quantity = quantity != null ? quantity : 0;
        this.itemTotalPrice = this.productPrice * this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity != null ? quantity : 0;
        calculateItemTotal();
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice != null ? productPrice : 0.0;
        calculateItemTotal();
    }

    private void calculateItemTotal() {
        this.itemTotalPrice = this.productPrice * this.quantity;
    }
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductEmoji() { return productEmoji; }
    public void setProductEmoji(String productEmoji) { this.productEmoji = productEmoji; }
    public Double getProductPrice() { return productPrice; }

    public Integer getQuantity() { return quantity; }

    public Double getItemTotalPrice() { return itemTotalPrice; }
    public void setItemTotalPrice(Double itemTotalPrice) { this.itemTotalPrice = itemTotalPrice; }
}