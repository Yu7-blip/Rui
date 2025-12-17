package com.auth.dto;

public class RecommendationResponse {
    private String recommendedProduct;
    private String reasoning;
    private String description;
    private Long productId; // 新增字段

    // 默认构造函数
    public RecommendationResponse() {}

    // 带参数构造函数
    public RecommendationResponse(String recommendedProduct, String reasoning, String description) {
        this.recommendedProduct = recommendedProduct;
        this.reasoning = reasoning;
        this.description = description;
    }

    // 新增带productId的构造函数
    public RecommendationResponse(String recommendedProduct, String reasoning, String description, Long productId) {
        this.recommendedProduct = recommendedProduct;
        this.reasoning = reasoning;
        this.description = description;
        this.productId = productId;
    }

    // Getter和Setter
    public String getRecommendedProduct() {
        return recommendedProduct;
    }

    public void setRecommendedProduct(String recommendedProduct) {
        this.recommendedProduct = recommendedProduct;
    }

    // 在 RecommendationResponse.java 中添加
    private Double productPrice;

    // 添加getter和setter
    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }


    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "RecommendationResponse{" +
                "recommendedProduct='" + recommendedProduct + '\'' +
                ", reasoning='" + reasoning + '\'' +
                ", description='" + description + '\'' +
                ", productId=" + productId +
                '}';
    }
}