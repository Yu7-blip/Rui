// RecommendationController.java - 恢复原样，只删除错误添加的方法
package com.auth.controller;

import com.auth.dto.RecommendationRequest;
import com.auth.dto.RecommendationResponse;
import com.auth.entity.Product;
import com.auth.service.DeepSeekService;
import com.auth.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:8080")
public class RecommendationController {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/ai-recommendation")
    public ResponseEntity<?> getAIRecommendation(@RequestBody RecommendationRequest request) {
        try {
            System.out.println("收到AI推荐请求: " + request);

            // 调用DeepSeek服务获取AI推荐
            String aiResponse = deepSeekService.getRecommendation(
                    request.getUserPreference(),
                    request.getWeather(),
                    request.getMood()
            );

            System.out.println("AI响应: " + aiResponse);

            // 解析AI响应并转换为RecommendationResponse
            RecommendationResponse response = parseAIResponse(aiResponse);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("recommendedProduct", response.getRecommendedProduct());
            result.put("reasoning", response.getReasoning());
            result.put("description", response.getDescription());
            result.put("productId", response.getProductId());
            result.put("productPrice", response.getProductPrice()); // 新增价格字段

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("AI推荐服务异常: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "推荐服务暂时不可用");
            errorResult.put("recommendation", getFallbackResponse());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 解析AI返回的文本格式
     */
    private RecommendationResponse parseAIResponse(String aiResponse) {
        String recommendedProduct = "经典珍珠奶茶";
        String reasoning = "系统推荐";
        String description = "经典口味，适合大多数场合";
        Double productPrice = 18.00;

        if (aiResponse != null && !aiResponse.isEmpty()) {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.startsWith("推荐产品：")) {
                    recommendedProduct = line.substring(5).trim();
                } else if (line.startsWith("推荐理由：")) {
                    reasoning = line.substring(5).trim();
                } else if (line.startsWith("产品描述：")) {
                    description = line.substring(5).trim();
                }
            }
        }

        // 增强的商品匹配逻辑
        Long productId = recommendationService.findProductIdByName(recommendedProduct);

        // 如果找不到商品，使用默认商品
        if (productId == null) {
            System.out.println("未找到匹配商品: " + recommendedProduct + "，使用默认商品");
            RecommendationResponse defaultResponse = recommendationService.getDefaultRecommendationPublic();
            productId = defaultResponse.getProductId();
            recommendedProduct = defaultResponse.getRecommendedProduct();
            description = defaultResponse.getDescription();
        }

        // 获取商品价格
        Product product = recommendationService.findProductById(productId);
        if (product != null) {
            productPrice = product.getPrice();
        }

        RecommendationResponse response = new RecommendationResponse(recommendedProduct, reasoning, description, productId);
        response.setProductPrice(productPrice);
        return response;
    }

    /**
     * 获取回退推荐响应
     */
    private Map<String, Object> getFallbackResponse() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("recommendedProduct", "经典珍珠奶茶");
        fallback.put("reasoning", "系统推荐");
        fallback.put("description", "经典口味，适合大多数场合");
        fallback.put("productId", 1L);
        fallback.put("productPrice", 18.00);
        return fallback;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Recommendation service is healthy");
    }
}
// 删除末尾错误添加的 findProductIdByName 方法