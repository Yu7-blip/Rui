package com.auth.service;

import com.auth.dto.*;
import com.auth.entity.Region;
import com.auth.entity.RegionProduct;
import com.auth.entity.Product;
import com.auth.repository.RegionRepository;
import com.auth.repository.RegionProductRepository;
import com.auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionProductRepository regionProductRepository;

    @Autowired
    private ProductRepository productRepository;

    // 省份到地域的映射关系
    private static final Map<String, String> PROVINCE_REGION_MAP = new HashMap<>();

    static {
        // 东北地区
        PROVINCE_REGION_MAP.put("黑龙江", "northeast");
        PROVINCE_REGION_MAP.put("吉林", "northeast");
        PROVINCE_REGION_MAP.put("辽宁", "northeast");

        // 华北地区
        PROVINCE_REGION_MAP.put("北京", "north");
        PROVINCE_REGION_MAP.put("天津", "north");
        PROVINCE_REGION_MAP.put("河北", "north");
        PROVINCE_REGION_MAP.put("山西", "north");
        PROVINCE_REGION_MAP.put("内蒙古", "north");

        // 西北地区
        PROVINCE_REGION_MAP.put("陕西", "northwest");
        PROVINCE_REGION_MAP.put("甘肃", "northwest");
        PROVINCE_REGION_MAP.put("青海", "northwest");
        PROVINCE_REGION_MAP.put("宁夏", "northwest");
        PROVINCE_REGION_MAP.put("新疆", "northwest");

        // 西南地区
        PROVINCE_REGION_MAP.put("四川", "southwest");
        PROVINCE_REGION_MAP.put("云南", "southwest");
        PROVINCE_REGION_MAP.put("贵州", "southwest");
        PROVINCE_REGION_MAP.put("重庆", "southwest");
        PROVINCE_REGION_MAP.put("西藏", "southwest");

        // 华中地区
        PROVINCE_REGION_MAP.put("河南", "central");
        PROVINCE_REGION_MAP.put("湖北", "central");
        PROVINCE_REGION_MAP.put("湖南", "central");
        PROVINCE_REGION_MAP.put("江西", "central");

        // 华东地区
        PROVINCE_REGION_MAP.put("上海", "east");
        PROVINCE_REGION_MAP.put("江苏", "east");
        PROVINCE_REGION_MAP.put("浙江", "east");
        PROVINCE_REGION_MAP.put("安徽", "east");
        PROVINCE_REGION_MAP.put("福建", "east");
        PROVINCE_REGION_MAP.put("山东", "east");

        // 华南地区
        PROVINCE_REGION_MAP.put("广东", "south");
        PROVINCE_REGION_MAP.put("广西", "south");
        PROVINCE_REGION_MAP.put("海南", "south");

        // 港澳台
        PROVINCE_REGION_MAP.put("台湾", "southeast");
        PROVINCE_REGION_MAP.put("香港", "southeast");
        PROVINCE_REGION_MAP.put("澳门", "southeast");
    }

    /**
     * 获取所有活跃地域
     */
    public List<RegionDTO> getAllActiveRegions() {
        return regionRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据代码获取地域
     */
    public RegionDTO getRegionByCode(String code) {
        return regionRepository.findByCodeAndIsActiveTrue(code)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * 根据定位获取推荐地域
     */
    public RegionRecommendationDTO getRegionRecommendation(LocationRequest request) {
        RegionRecommendationDTO response = new RegionRecommendationDTO();

        // 1. 检测用户位置
        String detectedProvince = detectProvince(request);
        String detectedCity = request.getCity();
        response.setDetectedProvince(detectedProvince);
        response.setDetectedCity(detectedCity);

        // 2. 匹配地域
        RegionDTO matchedRegion = matchRegion(detectedProvince);
        response.setRegion(matchedRegion);
        response.setMatchType("exact");

        if (matchedRegion == null) {
            // 如果没有精确匹配，返回默认地域或所有地域
            matchedRegion = getDefaultRegion();
            response.setMatchType("default");
        }

        // 3. 获取地域产品
        if (matchedRegion != null) {
            List<RegionProduct> products = regionProductRepository.findByRegionCode(matchedRegion.getCode());
            List<RegionProduct> featuredProducts = regionProductRepository.findFeaturedByRegionCode(matchedRegion.getCode());

            // 转换为DTO
            List<RegionProductDTO> productDTOs = convertToProductDTOs(products);
            List<RegionProductDTO> featuredDTOs = convertToProductDTOs(featuredProducts);

            response.setAllProducts(productDTOs);
            response.setFeaturedProducts(featuredDTOs);
        }

        // 4. 添加建议
        response.setWeatherSuggestion(generateWeatherSuggestion(detectedProvince));
        response.setSeasonalSuggestion(generateSeasonalSuggestion());

        return response;
    }

    /**
     * 根据省份检测地域
     */
    private String detectProvince(LocationRequest request) {
        if (request.isUseMock() && request.getMockRegionCode() != null) {
            // 模拟定位
            return "模拟定位";
        }

        if (request.getProvince() != null) {
            return request.getProvince();
        }

        if (request.hasCoordinates()) {
            // 这里应该调用地图API进行逆地理编码
            // 简化处理：随机返回一个省份
            List<String> provinces = new ArrayList<>(PROVINCE_REGION_MAP.keySet());
            return provinces.get(new Random().nextInt(provinces.size()));
        }

        return null;
    }

    /**
     * 根据省份匹配地域
     */
    private RegionDTO matchRegion(String province) {
        if (province == null) {
            return null;
        }

        // 直接从映射表获取地域代码
        String regionCode = PROVINCE_REGION_MAP.get(province);
        if (regionCode != null) {
            return getRegionByCode(regionCode);
        }

        // 模糊匹配
        for (Map.Entry<String, String> entry : PROVINCE_REGION_MAP.entrySet()) {
            if (province.contains(entry.getKey()) || entry.getKey().contains(province)) {
                return getRegionByCode(entry.getValue());
            }
        }

        return null;
    }

    /**
     * 获取默认地域（西南地区）
     */
    private RegionDTO getDefaultRegion() {
        return getRegionByCode("southwest"); // 默认西南地区
    }

    /**
     * 获取地域的所有产品
     */
    public List<RegionProductDTO> getRegionProducts(String regionCode) {
        List<RegionProduct> products = regionProductRepository.findByRegionCode(regionCode);
        return convertToProductDTOs(products);
    }

    /**
     * 获取地域的特色产品
     */
    public List<RegionProductDTO> getFeaturedProducts(String regionCode) {
        List<RegionProduct> products = regionProductRepository.findFeaturedByRegionCode(regionCode);
        return convertToProductDTOs(products);
    }

    /**
     * 添加产品到地域
     */
    @Transactional
    public boolean addProductToRegion(Long regionId, Long productId, boolean isFeatured, String reason) {
        try {
            if (regionProductRepository.existsByRegionIdAndProductId(regionId, productId)) {
                return false; // 已存在
            }

            Region region = regionRepository.findById(regionId).orElse(null);
            Product product = productRepository.findById(productId).orElse(null);

            if (region == null || product == null) {
                return false;
            }

            RegionProduct regionProduct = new RegionProduct();
            regionProduct.setRegion(region);
            regionProduct.setProduct(product);
            regionProduct.setIsFeatured(isFeatured);
            regionProduct.setRecommendReason(reason);

            regionProductRepository.save(regionProduct);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除地域产品关联
     */
    @Transactional
    public boolean removeProductFromRegion(Long regionId, Long productId) {
        try {
            Optional<RegionProduct> relation = regionProductRepository.findByRegionIdAndProductId(regionId, productId);
            if (relation.isPresent()) {
                regionProductRepository.delete(relation.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有地域及其产品统计
     */
    public List<Map<String, Object>> getAllRegionsWithStats() {
        List<Object[]> results = regionRepository.countProductsByRegion();
        List<Map<String, Object>> regionsWithStats = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("regionId", result[0]);
            map.put("regionName", result[1]);
            map.put("productCount", result[2]);
            regionsWithStats.add(map);
        }

        return regionsWithStats;
    }

    /**
     * 搜索地域产品
     */
    public List<RegionProductDTO> searchRegionProducts(String keyword, String regionCode) {
        // 简化实现，实际应该使用复杂查询
        List<RegionProduct> allProducts;
        if (regionCode != null && !regionCode.isEmpty()) {
            allProducts = regionProductRepository.findByRegionCode(regionCode);
        } else {
            allProducts = regionProductRepository.findAll();
        }

        return allProducts.stream()
                .filter(rp -> {
                    Product product = rp.getProduct();
                    return product.getName().contains(keyword) ||
                            product.getDescription().contains(keyword) ||
                            (rp.getRecommendReason() != null && rp.getRecommendReason().contains(keyword));
                })
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    /**
     * 初始化地域数据（用于首次运行）
     */
    @Transactional
    public void initializeRegions() {
        // 检查是否已初始化
        if (regionRepository.count() > 0) {
            return;
        }

        List<Region> regions = Arrays.asList(
                createRegion("northeast", "东北地区", "❄️", "黑龙江,吉林,辽宁",
                        "抗寒暖身系列", "寒冷干燥", "暖身,抗寒,高热量"),
                createRegion("north", "华北地区", "🏙️", "北京,天津,河北,山西,内蒙古",
                        "京味茶饮系列", "四季分明", "经典,传统,文化"),
                createRegion("northwest", "西北地区", "🏜️", "陕西,甘肃,青海,宁夏,新疆",
                        "草原奶茶系列", "干燥少雨", "浓郁,奶香,草原"),
                createRegion("southwest", "西南地区", "🏔️", "四川,云南,贵州,重庆,西藏",
                        "普洱茶系列", "温暖湿润", "普洱茶,香醇,山地"),
                createRegion("central", "华中地区", "🌉", "河南,湖北,湖南,江西",
                        "绿茶系列", "温和湿润", "清新,绿茶,自然"),
                createRegion("east", "华东地区", "🏯", "上海,江苏,浙江,安徽,福建,山东",
                        "功夫茶系列", "温和多雨", "精致,功夫茶,细腻"),
                createRegion("south", "华南地区", "🌴", "广东,广西,海南",
                        "凉茶系列", "炎热潮湿", "清凉,消暑,草本"),
                createRegion("southeast", "东南地区", "🌊", "台湾,香港,澳门",
                        "珍珠奶茶系列", "亚热带", "创意,流行,珍珠")
        );

        regionRepository.saveAll(regions);
    }

    /**
     * 根据省份查询地域（新增方法）
     */
    public List<RegionDTO> getRegionsByProvince(String province) {
        // 直接从映射表获取地域代码
        String regionCode = PROVINCE_REGION_MAP.get(province);
        if (regionCode != null) {
            RegionDTO region = getRegionByCode(regionCode);
            if (region != null) {
                return Collections.singletonList(region);
            }
        }

        // 如果没有精确匹配，返回空列表
        return Collections.emptyList();
    }

    // 辅助方法
    private Region createRegion(String code, String name, String icon, String provinces,
                                String specialty, String climate, String tags) {
        Region region = new Region();
        region.setCode(code);
        region.setName(name);
        region.setIcon(icon);
        region.setCoverProvinces(provinces);
        region.setSpecialtyDesc(specialty);
        region.setClimateFeature(climate);
        region.setRecommendTags(tags);
        region.setIsActive(true);
        return region;
    }

    private RegionDTO convertToDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setCode(region.getCode());
        dto.setName(region.getName());
        dto.setIcon(region.getIcon());
        dto.setCoverProvinces(region.getCoverProvinces());
        dto.setSpecialtyDesc(region.getSpecialtyDesc());
        dto.setClimateFeature(region.getClimateFeature());
        dto.setRecommendTags(region.getRecommendTags());
        dto.setIsActive(region.getIsActive());

        // 统计信息
        dto.setProductCount(Math.toIntExact(regionProductRepository.countByRegionId(region.getId())));
        dto.setFeaturedCount(Math.toIntExact(regionProductRepository.countByRegionIdAndIsFeaturedTrue(region.getId())));

        return dto;
    }

    private RegionProductDTO convertToProductDTO(RegionProduct regionProduct) {
        RegionProductDTO dto = new RegionProductDTO();
        dto.setId(regionProduct.getId());

        Region region = regionProduct.getRegion();
        dto.setRegionId(region.getId());
        dto.setRegionName(region.getName());
        dto.setRegionIcon(region.getIcon());

        Product product = regionProduct.getProduct();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductDescription(product.getDescription());
        dto.setProductPrice(product.getPrice());
        dto.setProductImageUrl(product.getImageUrl());
        dto.setProductTags(product.getTags());

        dto.setIsFeatured(regionProduct.getIsFeatured());
        dto.setRecommendReason(regionProduct.getRecommendReason());
        dto.setLocalName(regionProduct.getLocalName());
        dto.setPopularityScore(regionProduct.getPopularityScore());
        dto.setSeasonalMonth(regionProduct.getSeasonalMonth());

        return dto;
    }

    private List<RegionProductDTO> convertToProductDTOs(List<RegionProduct> regionProducts) {
        return regionProducts.stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    private String generateWeatherSuggestion(String province) {
        // 简化实现，根据省份生成天气建议
        if (province == null) return "根据当地天气选择合适的饮品";

        if (province.contains("黑龙江") || province.contains("吉林") || province.contains("辽宁")) {
            return "东北地区较寒冷，建议选择热饮暖身";
        } else if (province.contains("广东") || province.contains("广西") || province.contains("海南")) {
            return "南方天气炎热，推荐清凉解暑的饮品";
        } else {
            return "当前天气适中，各种饮品都适宜";
        }
    }

    private String generateSeasonalSuggestion() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;

        switch (month) {
            case 12:
            case 1:
            case 2:
                return "冬季推荐热饮，温暖身心";
            case 3:
            case 4:
            case 5:
                return "春季推荐清新果茶，感受生机";
            case 6:
            case 7:
            case 8:
                return "夏季推荐冰饮，消暑解渴";
            case 9:
            case 10:
            case 11:
                return "秋季推荐温和奶茶，滋养润燥";
            default:
                return "根据季节选择合适饮品";
        }
    }
}