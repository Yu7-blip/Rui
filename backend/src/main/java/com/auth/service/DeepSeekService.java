package com.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.auth.entity.Product;
import com.auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Autowired
    private ProductRepository productRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取AI奶茶推荐 - 适配 RecommendationService 的调用
     */
    public String getRecommendation(String userPreference, String weather, String mood) {
        try {
            String prompt = buildRecommendationPrompt(userPreference, weather, mood);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);
            requestBody.put("stream", false);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                System.err.println("DeepSeek API请求失败: " + response.getStatusCode());
                return getFallbackRecommendation(userPreference, weather, mood);
            }

        } catch (Exception e) {
            System.err.println("调用DeepSeek API异常: " + e.getMessage());
            e.printStackTrace();
            return getFallbackRecommendation(userPreference, weather, mood);
        }
    }

    /**
     * 从现有商品中获取推荐（限制AI只能选择数据库中的商品）
     */
    public String getRecommendationFromExistingProducts(String userPreference, String weather, String mood, String productList) {
        try {
            String prompt = buildRestrictedPrompt(userPreference, weather, mood, productList);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);
            requestBody.put("stream", false);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                System.err.println("DeepSeek API请求失败: " + response.getStatusCode());
                return getDatabaseFallbackRecommendation(userPreference, weather, mood);
            }

        } catch (Exception e) {
            System.err.println("调用DeepSeek API异常: " + e.getMessage());
            e.printStackTrace();
            return getDatabaseFallbackRecommendation(userPreference, weather, mood);
        }
    }

    /**
     * 构建限制性提示词，强制AI只从现有商品中选择
     */
    private String buildRestrictedPrompt(String userPreference, String weather, String mood, String productList) {
        return "你是一个奶茶推荐专家。用户信息：\n" +
                "口味偏好：" + (userPreference != null ? userPreference : "无") + "\n" +
                "天气：" + (weather != null ? weather : "无") + "\n" +
                "心情：" + (mood != null ? mood : "无") + "\n\n" +

                "重要限制：你只能从以下现有商品中选择推荐，不能推荐不存在的商品：\n" +
                productList + "\n\n" +

                "请严格按照以下格式回复：\n" +
                "推荐产品：[从上面列表中选择一个具体的商品名称]\n" +
                "推荐理由：[简要说明为什么推荐这个商品]\n" +
                "产品描述：[使用该商品的实际描述]\n\n" +

                "注意：商品名称必须完全匹配上面列表中的名称！";
    }

    /**
     * 构建推荐提示词
     */
    private String buildRecommendationPrompt(String userPreference, String weather, String mood) {
        return String.format(
                "你是一个专业的奶茶推荐专家，请根据以下用户信息推荐一款最适合的奶茶：\n\n" +
                        "## 用户信息：\n" +
                        "- 口味偏好：%s\n" +
                        "- 当前天气：%s\n" +
                        "- 心情状态：%s\n\n" +
                        "## 推荐要求：\n" +
                        "1. 综合考虑口味、天气和心情因素\n" +
                        "2. 推荐要具体到奶茶名称和配料\n" +
                        "3. 给出合理的推荐理由\n" +
                        "4. 描述奶茶的口感和特点\n\n" +
                        "## 回复格式（严格按照此格式）：\n" +
                        "推荐产品：{具体的奶茶名称}\n" +
                        "推荐理由：{详细的推荐理由，结合用户信息说明}\n" +
                        "产品描述：{奶茶的口感、配料、特点描述}\n\n" +
                        "请只返回上述格式的内容，不要添加任何其他文字。",
                userPreference, weather, mood
        );
    }

    /**
     * 解析API响应
     */
    private String parseResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                String content = (String) message.get("content");
                System.out.println("DeepSeek API返回: " + content);
                return content;
            }
        } catch (Exception e) {
            System.err.println("解析DeepSeek响应失败: " + e.getMessage());
        }
        return getDefaultRecommendation();
    }

    /**
     * 数据库回退推荐逻辑（当API调用失败时使用）
     */
    private String getDatabaseFallbackRecommendation(String userPreference, String weather, String mood) {
        System.out.println("使用数据库回退推荐逻辑");

        // 从数据库获取所有可用商品
        List<Product> availableProducts = productRepository.findByAvailableTrue();
        if (availableProducts.isEmpty()) {
            return getDefaultRecommendation();
        }

        // 根据条件筛选商品
        List<Product> filteredProducts = new ArrayList<>();

        for (Product product : availableProducts) {
            boolean matches = true;

            // 根据天气筛选
            if (weather != null && !weather.isEmpty()) {
                if (weather.contains("热") || weather.contains("夏") || weather.contains("晴")) {
                    matches = product.getTags() != null &&
                            (product.getTags().contains("清爽") || product.getTags().contains("冰沙"));
                } else if (weather.contains("冷") || weather.contains("冬") || weather.contains("寒")) {
                    matches = product.getTags() != null &&
                            (product.getTags().contains("暖饮") || product.getTags().contains("热饮"));
                } else if (weather.contains("雨")) {
                    matches = product.getTags() != null && product.getTags().contains("暖饮");
                }
            }

            if (matches) {
                filteredProducts.add(product);
            }
        }

        // 如果筛选后没有商品，使用所有可用商品
        if (filteredProducts.isEmpty()) {
            filteredProducts = availableProducts;
        }

        // 随机选择一个商品
        Collections.shuffle(filteredProducts);
        Product selectedProduct = filteredProducts.get(0);

        return String.format("推荐产品：%s\n推荐理由：根据您的偏好和当前情况推荐\n产品描述：%s",
                selectedProduct.getName(), selectedProduct.getDescription());
    }

    /**
     * 回退推荐逻辑（当API调用失败时使用）
     */
    private String getFallbackRecommendation(String userPreference, String weather, String mood) {
        System.out.println("使用回退推荐逻辑");

        // 基于天气的推荐
        if (weather != null) {
            if (weather.contains("热") || weather.contains("夏") || weather.contains("晴")) {
                return "推荐产品：芒果冰沙\n推荐理由：天气炎热，芒果的清爽口感能带来凉意\n产品描述：新鲜芒果果肉搭配Q弹波波，冰爽解渴，夏日必备";
            } else if (weather.contains("冷") || weather.contains("冬") || weather.contains("寒")) {
                return "推荐产品：雨天暖姜奶茶\n推荐理由：天气寒冷，红糖姜茶能温暖身体\n产品描述：红糖与老姜的完美结合，温暖香甜，驱寒暖胃";
            } else if (weather.contains("雨")) {
                return "推荐产品：经典珍珠奶茶\n推荐理由：雨天适合来一杯温暖的经典奶茶\n产品描述：香浓奶茶搭配Q弹珍珠，温暖舒适，经典不腻";
            }
        }

        // 基于心情的推荐
        if (mood != null) {
            if (mood.contains("开心") || mood.contains("高兴") || mood.contains("兴奋")) {
                return "推荐产品：芝士奶盖红茶\n推荐理由：心情愉快时适合享受丰富的口感层次\n产品描述：红茶底搭配绵密芝士奶盖，口感丰富，香甜浓郁";
            } else if (mood.contains("累") || mood.contains("疲惫") || mood.contains("困")) {
                return "推荐产品：咖啡奶茶\n推荐理由：咖啡因能帮助提神醒脑\n产品描述：咖啡与奶茶的完美融合，香浓提神，口感顺滑";
            } else if (mood.contains("压力") || mood.contains("紧张")) {
                return "推荐产品：春风茉莉花茶\n推荐理由：茉莉花茶有舒缓放松的效果\n产品描述：清香茉莉花茶，茶味醇厚，帮助放松心情";
            }
        }

        // 基于口味的推荐
        if (userPreference != null) {
            if (userPreference.contains("甜") || userPreference.contains("糖")) {
                return "推荐产品：黑糖珍珠奶茶\n推荐理由：满足对甜味的喜好\n产品描述：香浓奶茶与焦香黑糖珍珠，甜而不腻，口感丰富";
            } else if (userPreference.contains("水果") || userPreference.contains("清爽")) {
                return "推荐产品：百香果绿茶\n推荐理由：水果风味清新爽口\n产品描述：新鲜百香果搭配清香绿茶，酸甜清爽，果香四溢";
            } else if (userPreference.contains("茶") || userPreference.contains("清淡")) {
                return "推荐产品：四季春茶\n推荐理由：茶香清新，口感清淡\n产品描述：清香四季春茶，茶味醇厚，回甘清甜";
            }
        }

        return getDefaultRecommendation();
    }

    /**
     * 默认推荐
     */
    private String getDefaultRecommendation() {
        return "推荐产品：经典珍珠奶茶\n推荐理由：经典口味，适合大多数场合\n产品描述：香浓奶茶搭配Q弹珍珠，甜度适中，经典美味";
    }
}