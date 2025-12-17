package com.auth.controller;

import com.auth.dto.*;
import com.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regions")
@CrossOrigin(origins = "http://localhost:8080")
public class RegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 获取所有地域
     */
    @GetMapping
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        try {
            List<RegionDTO> regions = regionService.getAllActiveRegions();
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据代码获取地域
     */
    @GetMapping("/{code}")
    public ResponseEntity<RegionDTO> getRegionByCode(@PathVariable String code) {
        try {
            RegionDTO region = regionService.getRegionByCode(code);
            if (region != null) {
                return ResponseEntity.ok(region);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取地域推荐（根据定位）
     */
    @PostMapping("/recommendation")
    public ResponseEntity<RegionRecommendationDTO> getRegionRecommendation(
            @RequestBody LocationRequest request) {
        try {
            if (!request.isValid()) {
                return ResponseEntity.badRequest().body(null);
            }

            RegionRecommendationDTO recommendation = regionService.getRegionRecommendation(request);
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 获取地域的所有产品
     */
    @GetMapping("/{regionCode}/products")
    public ResponseEntity<List<RegionProductDTO>> getRegionProducts(
            @PathVariable String regionCode) {
        try {
            List<RegionProductDTO> products = regionService.getRegionProducts(regionCode);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取地域的特色产品
     */
    @GetMapping("/{regionCode}/featured-products")
    public ResponseEntity<List<RegionProductDTO>> getFeaturedProducts(
            @PathVariable String regionCode) {
        try {
            List<RegionProductDTO> products = regionService.getFeaturedProducts(regionCode);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 添加产品到地域
     */
    @PostMapping("/{regionId}/products/{productId}")
    public ResponseEntity<Map<String, Object>> addProductToRegion(
            @PathVariable Long regionId,
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "false") boolean isFeatured,
            @RequestParam(required = false) String reason) {
        try {
            boolean success = regionService.addProductToRegion(regionId, productId, isFeatured, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "添加成功" : "产品已存在或参数错误");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 从地域移除产品
     */
    @DeleteMapping("/{regionId}/products/{productId}")
    public ResponseEntity<Map<String, Object>> removeProductFromRegion(
            @PathVariable Long regionId,
            @PathVariable Long productId) {
        try {
            boolean success = regionService.removeProductFromRegion(regionId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "移除成功" : "关联不存在");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取地域统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getRegionStats() {
        try {
            List<Map<String, Object>> stats = regionService.getAllRegionsWithStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索地域产品
     */
    @GetMapping("/search")
    public ResponseEntity<List<RegionProductDTO>> searchRegionProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) String regionCode) {
        try {
            List<RegionProductDTO> results = regionService.searchRegionProducts(keyword, regionCode);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有省份的地域映射
     */
    @GetMapping("/province-mapping")
    public ResponseEntity<Map<String, String>> getProvinceRegionMapping() {
        try {
            Map<String, String> mapping = new HashMap<>();
            mapping.put("黑龙江", "northeast");
            mapping.put("吉林", "northeast");
            mapping.put("辽宁", "northeast");
            mapping.put("北京", "north");
            mapping.put("天津", "north");
            mapping.put("河北", "north");
            mapping.put("山西", "north");
            mapping.put("内蒙古", "north");
            mapping.put("陕西", "northwest");
            mapping.put("甘肃", "northeast");
            mapping.put("青海", "northwest");
            mapping.put("宁夏", "northwest");
            mapping.put("新疆", "northwest");
            mapping.put("四川", "southwest");
            mapping.put("云南", "southwest");
            mapping.put("贵州", "southwest");
            mapping.put("重庆", "southwest");
            mapping.put("西藏", "southwest");
            mapping.put("河南", "central");
            mapping.put("湖北", "central");
            mapping.put("湖南", "central");
            mapping.put("江西", "central");
            mapping.put("上海", "east");
            mapping.put("江苏", "east");
            mapping.put("浙江", "east");
            mapping.put("安徽", "east");
            mapping.put("福建", "east");
            mapping.put("山东", "east");
            mapping.put("广东", "south");
            mapping.put("广西", "south");
            mapping.put("海南", "south");
            mapping.put("台湾", "southeast");
            mapping.put("香港", "southeast");
            mapping.put("澳门", "southeast");

            return ResponseEntity.ok(mapping);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 初始化地域数据（仅开发环境使用）
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeRegions() {
        try {
            regionService.initializeRegions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "地域数据初始化成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "初始化失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RegionService");
        response.put("timestamp", System.currentTimeMillis());

        try {
            List<RegionDTO> regions = regionService.getAllActiveRegions();
            response.put("regionCount", regions.size());
            response.put("message", "服务运行正常");
        } catch (Exception e) {
            response.put("message", "服务异常: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}