package com.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    // 坐标方式定位
    private Double latitude;
    private Double longitude;

    // 文本方式定位
    private String province;
    private String city;
    private String district;

    // 模拟定位
    private Boolean useMock = false;
    private String mockRegionCode; // 模拟的地域代码

    // 定位精度
    private String accuracy;

    // 用户偏好
    private Boolean showAllRegions = false; // 是否显示所有地域
    private String productCategory; // 产品分类筛选
    private Integer limit = 10; // 返回数量限制

    // 🆕 添加缺失的方法
    public Boolean isUseMock() {
        return useMock != null && useMock;
    }

    // 🆕 添加getter方法（Lombok可能未正确生成）
    public String getMockRegionCode() {
        return mockRegionCode;
    }

    // 🆕 修复valid方法，处理useMock为null的情况
    public boolean isValid() {
        return hasCoordinates() || hasTextLocation() || (useMock != null && useMock);
    }

    // 原有方法保持不变
    public boolean hasCoordinates() {
        return latitude != null && longitude != null &&
                Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180;
    }

    public boolean hasTextLocation() {
        return province != null && !province.trim().isEmpty();
    }

    // 获取定位描述
    public String getLocationDescription() {
        if (hasTextLocation()) {
            StringBuilder sb = new StringBuilder();
            if (province != null) sb.append(province);
            if (city != null) sb.append(city);
            if (district != null) sb.append(district);
            return sb.toString();
        }
        if (hasCoordinates()) {
            return String.format("坐标(%.4f, %.4f)", latitude, longitude);
        }
        return "未知位置";
    }
}

// 🆕 如果需要CoordinateResponse，可以创建单独文件
/*
package com.auth.dto;

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
*/