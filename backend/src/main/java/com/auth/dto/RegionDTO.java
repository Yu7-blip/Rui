package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String coverProvinces;
    private String specialtyDesc;
    private String climateFeature;
    private String recommendTags;
    private Boolean isActive;

    // 统计信息
    private Integer productCount; // 关联产品数量
    private Integer featuredCount; // 特色产品数量

    // 格式化显示的省份列表
    public String getFormattedProvinces() {
        if (coverProvinces == null || coverProvinces.trim().isEmpty()) {
            return "";
        }
        return coverProvinces.replace(",", ", ");
    }

    // 获取省份数组
    public String[] getProvinceArray() {
        if (coverProvinces == null || coverProvinces.trim().isEmpty()) {
            return new String[0];
        }
        return coverProvinces.split(",");
    }
}

