package com.auth.service;

import com.auth.dto.AdminStatsDTO;
import com.auth.dto.UserInfoDTO;
import com.auth.dto.OrderDetailDTO;
import com.auth.entity.User;
import com.auth.entity.Order;
import com.auth.entity.Product;
import com.auth.entity.OrderItem;
import com.auth.entity.Category;
import com.auth.repository.UserRepository;
import com.auth.repository.OrderRepository;
import com.auth.repository.ProductRepository;
import com.auth.repository.OrderItemRepository;
import com.auth.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取管理面板统计数据
     */
    public AdminStatsDTO getDashboardStats() {
        Long totalUsers = userRepository.count();
        Long totalOrders = orderRepository.count();

        // 计算总收入
        BigDecimal totalRevenue = BigDecimal.valueOf(orderRepository.findAll().stream()
                .mapToDouble(Order::getTotalPrice)
                .sum());

        Long totalProducts = productRepository.countByAvailable(true);

        // 计算平均订单价值
        BigDecimal avgOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        AdminStatsDTO stats = new AdminStatsDTO(totalUsers, totalOrders, totalRevenue, totalProducts, avgOrderValue);

        // 设置变化率 (这里使用模拟数据，实际应该与上期比较)
        stats.setRevenueChange(12.5);
        stats.setOrdersChange(8.2);
        stats.setUsersChange(5.7);
        stats.setAvgOrderChange(3.8);

        return stats;
    }

    /**
     * 获取所有用户信息
     */
    public List<UserInfoDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            UserInfoDTO dto = new UserInfoDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRegisterTime(user.getCreatedAt());
            dto.setStatus("active"); // 根据实际情况设置

            // 计算用户订单数量和总消费
            List<Order> userOrders = orderRepository.findByUserId(user.getId());
            dto.setOrderCount(userOrders.size());

            BigDecimal totalSpent = BigDecimal.valueOf(userOrders.stream()
                    .mapToDouble(Order::getTotalPrice)
                    .sum());
            dto.setTotalSpent(totalSpent);

            // 获取最近订单
            List<UserInfoDTO.RecentOrderDTO> recentOrders = userOrders.stream()
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .limit(2)
                    .map(order -> {
                        // 获取订单中的第一个商品作为代表
                        String productName = "暂无商品";
                        if (!order.getOrderItems().isEmpty()) {
                            productName = order.getOrderItems().get(0).getProduct().getName();
                        }
                        return new UserInfoDTO.RecentOrderDTO(
                                order.getId(),
                                productName,
                                BigDecimal.valueOf(order.getTotalPrice())
                        );
                    })
                    .collect(Collectors.toList());
            dto.setRecentOrders(recentOrders);

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有订单详情
     */
    public List<OrderDetailDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setTotalAmount(BigDecimal.valueOf(order.getTotalPrice()));
            dto.setOrderTime(order.getOrderDate());
            dto.setStatus(order.getStatus());

            // 获取用户信息
            User user = order.getUser();
            if (user != null) {
                dto.setUsername(user.getUsername());
            }

            // 获取订单项
            List<OrderDetailDTO.OrderItemDTO> items = order.getOrderItems().stream()
                    .map(item -> new OrderDetailDTO.OrderItemDTO(
                            item.getId(),
                            item.getProduct().getName(),
                            BigDecimal.valueOf(item.getUnitPrice()),
                            item.getQuantity()
                    ))
                    .collect(Collectors.toList());
            dto.setItems(items);

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取最近订单
     */
    public List<OrderDetailDTO> getRecentOrders(int limit) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();

        // 在Java代码中限制数量
        return orders.stream()
                .limit(limit)
                .map(this::convertToOrderDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态筛选订单
     */
    public List<OrderDetailDTO> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream().map(this::convertToOrderDetailDTO).collect(Collectors.toList());
    }

    /**
     * 搜索订单
     */
    public List<OrderDetailDTO> searchOrders(String keyword) {
        // 根据订单号搜索
        List<Order> ordersByNumber = orderRepository.findByOrderNumberContaining(keyword);

        // 根据用户名搜索
        List<Order> ordersByUsername = orderRepository.findByUserUsernameContainingIgnoreCase(keyword);

        // 合并结果并去重
        List<Order> allOrders = ordersByNumber;
        allOrders.addAll(ordersByUsername.stream()
                .filter(order -> !ordersByNumber.contains(order))
                .collect(Collectors.toList()));

        return allOrders.stream()
                .map(this::convertToOrderDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新订单状态
     */
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            order.setStatus(newStatus);

            // 如果是完成状态，设置完成时间
            if ("completed".equals(newStatus)) {
                order.setCompletedDate(LocalDateTime.now());
            }

            orderRepository.save(order);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Long userId) {
        try {
            // 检查用户是否有订单
            List<Order> userOrders = orderRepository.findByUserId(userId);
            if (!userOrders.isEmpty()) {
                throw new RuntimeException("用户有历史订单，无法删除");
            }

            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取商品分类统计
     */
    public List<Object[]> getProductCategoryStats() {
        return productRepository.getProductCountByCategory();
    }

    /**
     * 获取商品销售统计
     */
    public List<Object[]> getProductSalesStatistics() {
        return productRepository.getProductSalesStatistics();
    }

    /**
     * 获取销售排行
     */
// 在 AdminService.java 中修改这个方法
    public List<Object[]> getProductSalesRanking() {
        // 使用正确的方法名
        return orderItemRepository.getTopSellingProducts();
    }

    // 添加这个方法到 AdminService.java
    public List<Object[]> getHourlyOrderDistribution(LocalDateTime startDate, LocalDateTime endDate) {
        return orderItemRepository.getHourlyOrderDistribution(startDate, endDate);
    }
    /**
     * 获取订单状态分布
     */
    public List<Object[]> getOrderStatusDistribution() {
        return orderRepository.getOrderStatusDistribution();
    }

    /**
     * 将Order转换为OrderDetailDTO
     */
    private OrderDetailDTO convertToOrderDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(BigDecimal.valueOf(order.getTotalPrice()));
        dto.setOrderTime(order.getOrderDate());
        dto.setStatus(order.getStatus());

        User user = order.getUser();
        if (user != null) {
            dto.setUsername(user.getUsername());
        }

        List<OrderDetailDTO.OrderItemDTO> items = order.getOrderItems().stream()
                .map(item -> new OrderDetailDTO.OrderItemDTO(
                        item.getId(),
                        item.getProduct().getName(),
                        BigDecimal.valueOf(item.getUnitPrice()),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}