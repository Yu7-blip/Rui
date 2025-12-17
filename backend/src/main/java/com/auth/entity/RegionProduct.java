package com.auth.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "region_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "product_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false; // 是否特色推荐

    @Column(name = "recommend_reason")
    private String recommendReason; // 推荐理由

    @Column(name = "local_name")
    private String localName; // 当地名称

    @Column(name = "popularity_score")
    private Integer popularityScore = 0; // 人气分数

    @Column(name = "seasonal_month")
    private String seasonalMonth; // 最佳季节月份，逗号分隔

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }
}