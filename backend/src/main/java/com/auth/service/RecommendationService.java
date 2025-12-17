package com.auth.service;

import com.auth.dto.ProductDTO;
import com.auth.dto.RecommendationRequest;
import com.auth.dto.RecommendationResponse;
import com.auth.entity.Product;
import com.auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DeepSeekService deepSeekService;

    // ============ 商品名称映射表 - 解决AI推荐与数据库商品名称不匹配问题 ============
    private final Map<String, String> productNameMapping = createProductNameMapping();

    private Map<String, String> createProductNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        // 添加常见的AI推荐名称到数据库商品名称的映射
        mapping.put("经典珍珠奶茶", "经典珍珠奶茶");
        mapping.put("珍珠奶茶", "经典珍珠奶茶");
        mapping.put("冰爽芒果波波茶", "芒果冰沙");
        mapping.put("芒果波波茶", "芒果冰沙");
        mapping.put("暖心红糖姜茶", "雨天暖姜奶茶");
        mapping.put("红糖姜茶", "雨天暖姜奶茶");
        mapping.put("芝士奶盖草莓奶茶", "芝士奶盖红茶");
        mapping.put("芝士草莓奶茶", "芝士奶盖红茶");
        mapping.put("提神咖啡奶茶", "咖啡奶茶");
        mapping.put("咖啡奶茶", "咖啡奶茶");
        mapping.put("薰衣草奶茶", "春风茉莉花茶");
        mapping.put("黑糖珍珠鲜奶", "黑糖珍珠奶茶");
        mapping.put("百香果绿茶", "百香果绿茶");
        mapping.put("四季春茶", "四季春茶");
        mapping.put("冰爽柠檬茶", "柠檬绿茶");
        mapping.put("柠檬绿茶", "柠檬绿茶");
        mapping.put("芝士奶盖绿茶", "芝士奶盖红茶");
        mapping.put("草莓多多", "草莓果茶");
        mapping.put("芋圆奶茶", "芋圆奶茶");
        mapping.put("红豆奶茶", "红豆奶茶");
        mapping.put("布丁奶茶", "布丁奶茶");
        mapping.put("椰果奶茶", "椰果奶茶");
        mapping.put("仙草冻奶茶", "仙草冻奶茶");
        mapping.put("抹茶拿铁", "抹茶拿铁");
        mapping.put("巧克力奶茶", "巧克力奶茶");
        mapping.put("芋泥波波茶", "芋圆奶茶");
        mapping.put("葡萄多多", "葡萄多多");
        mapping.put("桃子乌龙茶", "桃子乌龙");
        return mapping;
    }

    // ============ 以下是你的原有代码，完全保持不变 ============

    // 获取个性推荐商品
    public List<ProductDTO> getPersonalizedRecommendations(Long userId) {
        // 这里可以根据用户的历史订单、偏好等实现个性化推荐
        // 目前先实现一个基础版本：混合推荐

        List<ProductDTO> recommendations = new ArrayList<>();

        // 1. 热门推荐（销量高的商品）
        recommendations.addAll(getPopularProducts());

        // 2. 新品推荐
        recommendations.addAll(getNewProducts());

        // 3. 根据季节推荐
        recommendations.addAll(getSeasonalProducts());

        // 去重并限制数量
        return removeDuplicatesAndLimit(recommendations, 8);
    }

    // 根据天气推荐
    public List<ProductDTO> getWeatherBasedRecommendations(String weather) {
        List<Product> products = productRepository.findByAvailableTrue();

        return products.stream()
                .filter(product -> isSuitableForWeather(product, weather))
                .map(this::convertToDTO)
                .limit(6)
                .collect(Collectors.toList());
    }

    // 获取节日推荐
    public List<ProductDTO> getFestivalRecommendations(String festival) {
        List<Product> products = productRepository.findByAvailableTrue();

        return products.stream()
                .filter(product -> isSuitableForFestival(product, festival))
                .map(this::convertToDTO)
                .limit(6)
                .collect(Collectors.toList());
    }

    // 获取热门商品（模拟实现）
    private List<ProductDTO> getPopularProducts() {
        // 在实际应用中，这里应该查询订单数据计算销量
        // 目前随机选择一些商品作为热门商品
        List<Product> allProducts = productRepository.findByAvailableTrue();
        Collections.shuffle(allProducts);

        return allProducts.stream()
                .limit(4)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取新品
    private List<ProductDTO> getNewProducts() {
        List<Product> allProducts = productRepository.findByAvailableTrue();

        // 使用传统的比较器写法
        Collections.sort(allProducts, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return p2.getCreatedAt().compareTo(p1.getCreatedAt());
            }
        });

        return allProducts.stream()
                .limit(3)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取季节性商品
    private List<ProductDTO> getSeasonalProducts() {
        int month = LocalDateTime.now().getMonthValue();
        String season = getCurrentSeason(month);

        List<Product> products = productRepository.findByAvailableTrue();

        return products.stream()
                .filter(product -> isSuitableForSeason(product, season))
                .map(this::convertToDTO)
                .limit(3)
                .collect(Collectors.toList());
    }

    // 判断商品是否适合当前天气
    private boolean isSuitableForWeather(Product product, String weather) {
        String category = product.getCategory();
        String tags = product.getTags() != null ? product.getTags().toLowerCase() : "";
        String name = product.getName().toLowerCase();

        if ("sunny".equalsIgnoreCase(weather) || "晴天".equals(weather)) {
            return "fruit".equals(category) ||
                    "weather".equals(category) ||
                    tags.contains("清爽") ||
                    tags.contains("冰沙");
        } else if ("rainy".equalsIgnoreCase(weather) || "雨天".equals(weather)) {
            return "weather".equals(category) ||
                    tags.contains("暖饮") ||
                    tags.contains("热饮") ||
                    name.contains("姜");
        } else if ("snowy".equalsIgnoreCase(weather) || "雪天".equals(weather)) {
            return tags.contains("热饮") ||
                    tags.contains("巧克力") ||
                    name.contains("热");
        } else {
            return "recommend".equals(category) || "classic".equals(category);
        }
    }

    // 判断商品是否适合节日
    private boolean isSuitableForFestival(Product product, String festival) {
        String category = product.getCategory();
        String name = product.getName().toLowerCase();
        String tags = product.getTags() != null ? product.getTags().toLowerCase() : "";

        if ("christmas".equalsIgnoreCase(festival) || "圣诞".equals(festival)) {
            return "festival".equals(category) ||
                    name.contains("圣诞") ||
                    tags.contains("圣诞");
        } else if ("spring festival".equalsIgnoreCase(festival) || "春节".equals(festival)) {
            return "festival".equals(category) ||
                    name.contains("春节") ||
                    name.contains("福气") ||
                    tags.contains("春节");
        } else if ("valentine".equalsIgnoreCase(festival) || "情人节".equals(festival)) {
            return "festival".equals(category) ||
                    name.contains("情人") ||
                    name.contains("甜蜜") ||
                    tags.contains("浪漫");
        } else {
            return "festival".equals(category);
        }
    }

    // 判断商品是否适合季节
    private boolean isSuitableForSeason(Product product, String season) {
        String tags = product.getTags() != null ? product.getTags().toLowerCase() : "";

        if ("spring".equals(season)) { // 春季
            return tags.contains("清爽") || tags.contains("花香");
        } else if ("summer".equals(season)) { // 夏季
            return tags.contains("冰沙") || tags.contains("清爽") || tags.contains("果茶");
        } else if ("autumn".equals(season)) { // 秋季
            return tags.contains("温暖") || tags.contains("坚果");
        } else if ("winter".equals(season)) { // 冬季
            return tags.contains("热饮") || tags.contains("巧克力") || tags.contains("暖饮");
        } else {
            return true;
        }
    }

    // 获取当前季节
    private String getCurrentSeason(int month) {
        if (month >= 3 && month <= 5) return "spring";
        if (month >= 6 && month <= 8) return "summer";
        if (month >= 9 && month <= 11) return "autumn";
        return "winter";
    }

    // 去重并限制数量（替代Java 11+的distinct()）
    private List<ProductDTO> removeDuplicatesAndLimit(List<ProductDTO> list, int limit) {
        Set<Long> seenIds = new HashSet<>();
        List<ProductDTO> result = new ArrayList<>();

        for (ProductDTO dto : list) {
            if (!seenIds.contains(dto.getId())) {
                seenIds.add(dto.getId());
                result.add(dto);
                if (result.size() >= limit) {
                    break;
                }
            }
        }

        return result;
    }

    // 转换实体为DTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setEmoji(product.getEmoji());
        dto.setTags(product.getTags());
        dto.setAvailable(product.getAvailable());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }

    // 获取相关推荐（基于当前商品）
    public List<ProductDTO> getRelatedProducts(Long productId) {
        Optional<Product> currentProduct = productRepository.findById(productId);
        if (!currentProduct.isPresent()) {
            return new ArrayList<>();
        }

        Product product = currentProduct.get();
        String category = product.getCategory();

        // 推荐同分类的其他商品
        List<ProductDTO> related = productRepository.findByCategoryAndAvailableTrue(category)
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .map(this::convertToDTO)
                .limit(4)
                .collect(Collectors.toList());

        // 如果同分类商品不足，补充其他推荐
        if (related.size() < 4) {
            List<ProductDTO> additional = getPopularProducts();
            for (ProductDTO dto : additional) {
                if (related.size() >= 4) break;

                boolean exists = false;
                for (ProductDTO existing : related) {
                    if (existing.getId().equals(dto.getId())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    related.add(dto);
                }
            }
        }

        return related;
    }

    // ============ 修改后的AI推荐方法 ============

    /**
     * AI智能奶茶推荐 - 只在数据库商品中推荐
     */
    public RecommendationResponse getAIRecommendation(RecommendationRequest request) {
        try {
            // 首先从数据库获取所有可用商品
            List<Product> availableProducts = productRepository.findByAvailableTrue();
            if (availableProducts.isEmpty()) {
                return getDefaultRecommendation();
            }

            // 构建商品列表供AI选择
            String productList = buildProductListForAI(availableProducts);

            // 调用AI服务，但限制它只能从现有商品中选择
            String recommendation = deepSeekService.getRecommendationFromExistingProducts(
                    request.getUserPreference(),
                    request.getWeather(),
                    request.getMood(),
                    productList
            );

            return parseAndMatchRecommendation(recommendation);

        } catch (Exception e) {
            System.err.println("AI推荐服务异常: " + e.getMessage());
            // 如果AI服务失败，返回基于数据库的回退推荐
            return getDatabaseFallbackRecommendation(request);
        }
    }

    /**
     * 构建供AI选择的商品列表
     */
    private String buildProductListForAI(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("请从以下商品中选择推荐（只能选择这些商品）：\n");

        for (Product product : products) {
            sb.append("- ").append(product.getName())
                    .append("（分类：").append(product.getCategory())
                    .append("，标签：").append(product.getTags() != null ? product.getTags() : "无")
                    .append("，描述：").append(product.getDescription())
                    .append("）\n");
        }

        return sb.toString();
    }

    /**
     * 解析AI推荐并匹配数据库商品
     */
    private RecommendationResponse parseAndMatchRecommendation(String recommendation) {
        String productName = "经典珍珠奶茶";
        String reasoning = "这是一款经典的选择，适合大多数口味";
        String description = "香浓奶茶搭配Q弹珍珠，甜度适中";
        Long matchedProductId = null;

        try {
            // 解析AI返回的推荐内容
            String[] lines = recommendation.split("\n");
            for (String line : lines) {
                if (line.startsWith("推荐产品：")) {
                    productName = line.substring(5).trim();
                } else if (line.startsWith("推荐理由：")) {
                    reasoning = line.substring(5).trim();
                } else if (line.startsWith("产品描述：")) {
                    description = line.substring(5).trim();
                }
            }

            // 尝试匹配数据库中的商品
            matchedProductId = findProductIdByNamePrivate(productName);

            // 如果找不到匹配的商品，使用默认商品
            if (matchedProductId == null) {
                System.out.println("未找到匹配商品: " + productName + "，使用默认商品");
                Product defaultProduct = getDefaultProduct();
                if (defaultProduct != null) {
                    matchedProductId = defaultProduct.getId();
                    productName = defaultProduct.getName();
                    description = defaultProduct.getDescription();
                }
            }

        } catch (Exception e) {
            System.err.println("解析AI推荐响应失败: " + e.getMessage());
            // 解析失败时返回默认推荐
            return getDefaultRecommendation();
        }

        // 使用新的构造函数
        RecommendationResponse response = new RecommendationResponse(productName, reasoning, description);
        response.setProductId(matchedProductId);
        return response;
    }

    /**
     * 基于数据库的回退推荐
     */
    private RecommendationResponse getDatabaseFallbackRecommendation(RecommendationRequest request) {
        try {
            List<Product> availableProducts = productRepository.findByAvailableTrue();
            if (availableProducts.isEmpty()) {
                return getDefaultRecommendation();
            }

            // 简单逻辑：随机选择一个可用商品
            Collections.shuffle(availableProducts);
            Product selectedProduct = availableProducts.get(0);
            return createRecommendationFromProduct(selectedProduct, request);

        } catch (Exception e) {
            return getDefaultRecommendation();
        }
    }

    /**
     * 根据商品名称查找商品ID - 私有方法
     */
    private Long findProductIdByNamePrivate(String productName) {
        // 首先尝试直接匹配
        Optional<Product> product = productRepository.findByNameAndAvailableTrue(productName);
        if (product.isPresent()) {
            return product.get().getId();
        }

        // 尝试使用映射表匹配
        String mappedName = productNameMapping.get(productName);
        if (mappedName != null) {
            Optional<Product> mappedProduct = productRepository.findByNameAndAvailableTrue(mappedName);
            if (mappedProduct.isPresent()) {
                return mappedProduct.get().getId();
            }
        }

        // 尝试模糊匹配 - 使用现有的方法
        List<Product> similarProducts = productRepository.findByNameContainingIgnoreCase(
                productName.length() > 2 ? productName.substring(0, 2) : productName);
        if (!similarProducts.isEmpty()) {
            return similarProducts.get(0).getId();
        }

        return null;
    }

    /**
     * 从商品创建推荐响应
     */
    private RecommendationResponse createRecommendationFromProduct(Product product, RecommendationRequest request) {
        String reasoning = generateReasoning(product, request);
        RecommendationResponse response = new RecommendationResponse(
                product.getName(),
                reasoning,
                product.getDescription()
        );
        response.setProductId(product.getId());
        return response;
    }

    /**
     * 生成推荐理由
     */
    private String generateReasoning(Product product, RecommendationRequest request) {
        StringBuilder reason = new StringBuilder();

        if (request.getUserPreference() != null && !request.getUserPreference().isEmpty()) {
            reason.append("根据您的口味偏好「").append(request.getUserPreference()).append("」推荐");
        }

        if (request.getWeather() != null && !request.getWeather().isEmpty()) {
            if (reason.length() > 0) reason.append("，");
            reason.append("结合「").append(request.getWeather()).append("」天气");
        }

        if (request.getMood() != null && !request.getMood().isEmpty()) {
            if (reason.length() > 0) reason.append("，");
            reason.append("匹配您「").append(request.getMood()).append("」的心情");
        }

        if (reason.length() == 0) {
            reason.append("为您推荐这款受欢迎的商品");
        }

        return reason.toString();
    }

    /**
     * 获取默认商品
     */
    private Product getDefaultProduct() {
        List<Product> availableProducts = productRepository.findByAvailableTrue();
        if (!availableProducts.isEmpty()) {
            return availableProducts.get(0);
        }
        return null;
    }

    /**
     * 默认推荐
     */
    private RecommendationResponse getDefaultRecommendation() {
        Product defaultProduct = getDefaultProduct();
        if (defaultProduct != null) {
            RecommendationResponse response = new RecommendationResponse(
                    defaultProduct.getName(),
                    "经典口味，适合大多数场合",
                    defaultProduct.getDescription()
            );
            response.setProductId(defaultProduct.getId());
            return response;
        }
        RecommendationResponse response = new RecommendationResponse(
                "珍珠奶茶",
                "经典口味，适合大多数场合",
                "香浓奶茶搭配Q弹珍珠，经典美味"
        );
        response.setProductId(null);
        return response;
    }

    // ============ 匹配辅助方法 ============

    private boolean matchesPreference(Product product, String preference) {
        if (preference == null || preference.isEmpty()) return true;

        String tags = product.getTags() != null ? product.getTags().toLowerCase() : "";
        String name = product.getName().toLowerCase();
        String description = product.getDescription() != null ? product.getDescription().toLowerCase() : "";

        if (preference.contains("甜")) {
            return tags.contains("甜") || name.contains("糖") || name.contains("蜜");
        } else if (preference.contains("水果")) {
            return tags.contains("水果") || name.contains("果") || description.contains("水果");
        } else if (preference.contains("茶")) {
            return tags.contains("茶") || name.contains("茶") || description.contains("茶");
        } else if (preference.contains("奶")) {
            return tags.contains("奶") || name.contains("奶") || description.contains("奶");
        }
        return true;
    }

    private boolean matchesWeather(Product product, String weather) {
        if (weather == null || weather.isEmpty()) return true;
        return isSuitableForWeather(product, weather);
    }

    private boolean matchesMood(Product product, String mood) {
        if (mood == null || mood.isEmpty()) return true;

        String tags = product.getTags() != null ? product.getTags().toLowerCase() : "";
        String name = product.getName().toLowerCase();

        if (mood.contains("开心") || mood.contains("高兴")) {
            return tags.contains("庆祝") || name.contains("欢乐") || tags.contains("甜蜜");
        } else if (mood.contains("疲惫") || mood.contains("累")) {
            return tags.contains("提神") || name.contains("咖啡") || tags.contains("能量");
        } else if (mood.contains("压力") || mood.contains("紧张")) {
            return tags.contains("放松") || tags.contains("舒缓") || name.contains("薰衣草");
        }
        return true;
    }

    private boolean matchesPreferenceRelaxed(Product product, String preference) {
        return matchesPreference(product, preference);
    }

    private boolean matchesWeatherRelaxed(Product product, String weather) {
        return matchesWeather(product, weather);
    }

    private boolean matchesMoodRelaxed(Product product, String mood) {
        return matchesMood(product, mood);
    }


    /**
     * 根据ID查找商品
     */
    public Product findProductById(Long productId) {
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId).orElse(null);
    }

    // ============ 新增公共方法供Controller调用 ============

    /**
     * 根据产品名称查找产品ID - 公共方法供控制器调用
     */
    public Long findProductIdByName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return 1L; // 默认ID
        }

        // 首先尝试直接匹配
        Optional<Product> product = productRepository.findByNameAndAvailableTrue(productName.trim());
        if (product.isPresent()) {
            return product.get().getId();
        }

        // 尝试使用映射表匹配
        String mappedName = productNameMapping.get(productName.trim());
        if (mappedName != null) {
            Optional<Product> mappedProduct = productRepository.findByNameAndAvailableTrue(mappedName);
            if (mappedProduct.isPresent()) {
                return mappedProduct.get().getId();
            }
        }

        // 尝试模糊匹配 - 更宽松的匹配
        List<Product> similarProducts = productRepository.findByNameContainingIgnoreCase(productName.trim());
        if (!similarProducts.isEmpty()) {
            return similarProducts.get(0).getId();
        }

        // 如果还是找不到，返回默认商品ID
        return 1L; // 默认ID
    }

    /**
     * 获取默认推荐 - 公共方法
     */
    public RecommendationResponse getDefaultRecommendationPublic() {
        Product defaultProduct = getDefaultProduct();
        if (defaultProduct != null) {
            RecommendationResponse response = new RecommendationResponse(
                    defaultProduct.getName(),
                    "经典口味，适合大多数场合",
                    defaultProduct.getDescription()
            );
            response.setProductId(defaultProduct.getId());
            return response;
        }
        RecommendationResponse response = new RecommendationResponse(
                "珍珠奶茶",
                "经典口味，适合大多数场合",
                "香浓奶茶搭配Q弹珍珠，经典美味"
        );
        response.setProductId(1L);
        return response;
    }
}