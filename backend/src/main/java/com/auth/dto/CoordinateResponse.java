package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 坐标响应DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
class CoordinateResponse {
    private Double latitude;
    private Double longitude;
    private String province;
    private String city;
    private String district;
    private String address;
    private String formattedAddress;
    private Integer accuracy; // 精度（米）
    private String source; // 数据来源：baidu/gaode/tencent
    private Long timestamp;
}

