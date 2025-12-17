package com.auth.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // northeast/southwest等

    @Column(nullable = false)
    private String name; // 东北地区/西南地区

    @Column(nullable = false)
    private String icon; // 图标emoji

    @Column(name = "cover_provinces", nullable = false, length = 500)
    private String coverProvinces; // 覆盖省份，逗号分隔

    @Column(name = "specialty_desc", nullable = false)
    private String specialtyDesc; // 特色描述

    @Column(name = "climate_feature")
    private String climateFeature; // 气候特点

    @Column(name = "recommend_tags")
    private String recommendTags; // 推荐标签

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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