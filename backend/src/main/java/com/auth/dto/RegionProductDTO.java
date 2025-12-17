package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionProductDTO {
    private Long id;
    private Long regionId;
    private String regionName;
    private String regionIcon;

    private Long productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private String productImageUrl;
    private String productTags;

    private Boolean isFeatured;
    private String recommendReason;
    private String localName;
    private Integer popularityScore;
    private String seasonalMonth;

    // 完整产品信息（如果需要）
    private ProductDTO productDetail;
}