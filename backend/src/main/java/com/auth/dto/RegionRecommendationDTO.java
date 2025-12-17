package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionRecommendationDTO {
    private RegionDTO region; // 匹配的地域信息
    private String detectedProvince; // 检测到的省份
    private String detectedCity; // 检测到的城市
    private String matchType; // 匹配类型：exact/fuzzy/default/manual

    private List<RegionProductDTO> featuredProducts; // 特色产品列表
    private List<RegionProductDTO> allProducts; // 所有地域产品列表

    private String weatherSuggestion; // 天气建议
    private String seasonalSuggestion; // 季节建议
}