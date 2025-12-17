package com.auth.dto;

import java.time.LocalDateTime;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String emoji;
    private String tags;
    private Boolean available;
    private String imageUrl;  // 已添加
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 默认构造函数
    public ProductDTO() {}

    // 全参构造函数 - 更新：包含imageUrl
    public ProductDTO(Long id, String name, String description, Double price,
                      String category, String emoji, String tags, Boolean available,
                      String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.emoji = emoji;
        this.tags = tags;
        this.available = available;
        this.imageUrl = imageUrl;  // 新增
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 简化的构造函数（向后兼容）- 更新：包含imageUrl
    public ProductDTO(Long id, String name, String description, Double price,
                      String category, String emoji, String tags, Boolean available, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.emoji = emoji;
        this.tags = tags;
        this.available = available;
        this.imageUrl = imageUrl;  // 新增
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public String getImageUrl() { return imageUrl; }  // 新增
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }  // 新增

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // toString方法
    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +  // 新增
                ", available=" + available +
                '}';
    }
}