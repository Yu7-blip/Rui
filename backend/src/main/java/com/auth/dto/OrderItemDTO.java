package com.auth.dto;

public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productEmoji;
    private Integer quantity;
    private Double unitPrice;
    private Double itemTotalPrice;

    public OrderItemDTO() {}

    public OrderItemDTO(Long id, Long productId, String productName, String productEmoji,
                        Integer quantity, Double unitPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productEmoji = productEmoji;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemTotalPrice = unitPrice * quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductEmoji() {
        return productEmoji;
    }

    public void setProductEmoji(String productEmoji) {
        this.productEmoji = productEmoji;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null) {
            this.itemTotalPrice = this.unitPrice * quantity;
        }
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        if (this.quantity != null) {
            this.itemTotalPrice = unitPrice * this.quantity;
        }
    }

    public Double getItemTotalPrice() {
        return itemTotalPrice;
    }

    public void setItemTotalPrice(Double itemTotalPrice) {
        this.itemTotalPrice = itemTotalPrice;
    }
}