package com.auth.controller;

import com.auth.dto.AdminStatsDTO;
import com.auth.dto.UserInfoDTO;
import com.auth.dto.OrderDetailDTO;
import com.auth.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    /**
     * 获取销售排行（格式化数据）
     */
    @GetMapping("/stats/sales-ranking-formatted")
    public ResponseEntity<List<Map<String, Object>>> getSalesRankingFormatted() {
        try {
            List<Object[]> ranking = adminService.getProductSalesRanking();
            List<Map<String, Object>> formattedRanking = ranking.stream().map(item -> {
                Map<String, Object> product = new HashMap<>();
                product.put("id", item[0]); // 商品ID
                product.put("name", item[1]); // 商品名称
                product.put("categoryId", item[2]); // 分类ID
                product.put("image", item[3]); // 商品emoji/图片
                product.put("salesCount", item[4]); // 销售数量
                product.put("revenue", item[5]); // 销售收入
                return product;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(formattedRanking);
        } catch (Exception e) {
            logger.error("获取销售排行数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取订单状态分布（格式化数据）
     */
    @GetMapping("/stats/order-status-formatted")
    public ResponseEntity<List<Map<String, Object>>> getOrderStatusDistributionFormatted() {
        try {
            List<Object[]> distribution = adminService.getOrderStatusDistribution();
            List<Map<String, Object>> formattedDistribution = distribution.stream().map(item -> {
                Map<String, Object> status = new HashMap<>();
                status.put("status", item[0]); // 状态
                status.put("count", ((Long)item[1]).intValue()); // 数量，转换为int
                return status;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(formattedDistribution);
        } catch (Exception e) {
            logger.error("获取订单状态分布数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取分类统计（格式化数据）
     */
    @GetMapping("/stats/categories-formatted")
    public ResponseEntity<List<Map<String, Object>>> getCategoryStatsFormatted() {
        try {
            List<Object[]> stats = adminService.getProductCategoryStats();
            List<Map<String, Object>> formattedStats = stats.stream().map(item -> {
                Map<String, Object> category = new HashMap<>();
                category.put("id", item[0]); // 分类ID
                category.put("productCount", item[1]); // 商品数量
                category.put("salesCount", item[2]); // 销售数量
                return category;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(formattedStats);
        } catch (Exception e) {
            logger.error("获取分类统计数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取管理面板统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        try {
            AdminStatsDTO stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取管理面板统计数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserInfoDTO>> getAllUsers() {
        try {
            List<UserInfoDTO> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有订单
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDetailDTO>> getAllOrders() {
        try {
            List<OrderDetailDTO> orders = adminService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取所有订单失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/orders/recent")
    public ResponseEntity<List<OrderDetailDTO>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<OrderDetailDTO> orders = adminService.getRecentOrders(limit);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取最近订单失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据状态获取订单
     */
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<OrderDetailDTO>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OrderDetailDTO> orders = adminService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("根据状态获取订单失败: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索订单
     */
    @GetMapping("/orders/search")
    public ResponseEntity<List<OrderDetailDTO>> searchOrders(@RequestParam String keyword) {
        try {
            List<OrderDetailDTO> orders = adminService.searchOrders(keyword);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("搜索订单失败: {}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        try {
            String newStatus = request.get("status");
            boolean success = adminService.updateOrderStatus(orderId, newStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "订单状态更新成功" : "订单状态更新失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新订单状态失败: orderId={}, status={}", orderId, request.get("status"), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            boolean success = adminService.deleteUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "用户删除成功" : "用户删除失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除用户失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取商品分类统计
     */
    @GetMapping("/stats/categories")
    public ResponseEntity<List<Object[]>> getCategoryStats() {
        try {
            List<Object[]> stats = adminService.getProductCategoryStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取商品分类统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品销售统计
     */
    @GetMapping("/stats/products")
    public ResponseEntity<List<Object[]>> getProductSalesStatistics() {
        try {
            List<Object[]> stats = adminService.getProductSalesStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取商品销售统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取销售排行
     */
    @GetMapping("/stats/sales-ranking")
    public ResponseEntity<List<Object[]>> getSalesRanking() {
        try {
            List<Object[]> ranking = adminService.getProductSalesRanking();
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            logger.error("获取销售排行失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取订单状态分布
     */
    @GetMapping("/stats/order-status")
    public ResponseEntity<List<Object[]>> getOrderStatusDistribution() {
        try {
            List<Object[]> distribution = adminService.getOrderStatusDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            logger.error("获取订单状态分布失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}