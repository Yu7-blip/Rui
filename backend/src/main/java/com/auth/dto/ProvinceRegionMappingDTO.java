package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 省份地域映射DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
class ProvinceRegionMappingDTO {
    private String province;
    private String regionCode;
    private String regionName;
    private Double similarity; // 匹配相似度
    private Boolean isExactMatch;
}