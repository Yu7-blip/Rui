package com.auth.service;

import com.auth.dto.OrderDTO;
import com.auth.dto.OrderItemDTO;
import com.auth.dto.CartDTO;
import com.auth.dto.CartItemDTO;
import com.auth.entity.*;
import com.auth.repository.OrderRepository;
import com.auth.repository.OrderItemRepository;
import com.auth.repository.UserRepository;
import com.auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    // 从购物车创建订单
    public OrderDTO createOrderFromCart(Long userId, String customerNotes) {
        try {
            // 获取用户购物车 - 这里需要修改，因为CartService返回的是CartDTO
            com.auth.dto.CartDTO cart = cartService.getCartByUserId(userId);

            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("购物车为空，无法创建订单");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 创建订单
            Order order = new Order(user, null);
            order.setCustomerNotes(customerNotes);

            // 添加订单项
            for (com.auth.dto.CartItemDTO cartItem : cart.getCartItems()) {
                Product product = productRepository.findById(cartItem.getProductId())
                        .orElseThrow(() -> new RuntimeException("商品不存在: " + cartItem.getProductId()));

                OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
                order.addOrderItem(orderItem);
            }

            order.calculateTotalPrice();
            Order savedOrder = orderRepository.save(order);

            // 清空购物车
            cartService.clearCart(userId);

            return convertToDTO(savedOrder);
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
        }
    }

    // 获取用户订单
    public List<OrderDTO> getUserOrders(Long userId) {
        try {
            List<Order> orders = orderRepository.findByUserId(userId);
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取用户订单失败: " + e.getMessage(), e);
        }
    }

    // 根据ID获取订单
    public OrderDTO getOrderById(Long orderId) {
        try {
            Optional<Order> order = orderRepository.findById(orderId);
            return order.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("获取订单失败: " + e.getMessage(), e);
        }
    }

    // 根据订单号获取订单
    public OrderDTO getOrderByNumber(String orderNumber) {
        try {
            Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
            return order.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("获取订单失败: " + e.getMessage(), e);
        }
    }

    // 更新订单状态
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));

            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);

            return convertToDTO(updatedOrder);
        } catch (Exception e) {
            throw new RuntimeException("更新订单状态失败: " + e.getMessage(), e);
        }
    }

    // 取消订单
    public OrderDTO cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    // 获取所有订单（管理员用）
    public List<OrderDTO> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取所有订单失败: " + e.getMessage(), e);
        }
    }

    // 根据状态获取订单
    public List<OrderDTO> getOrdersByStatus(String status) {
        try {
            List<Order> orders = orderRepository.findByStatus(status);
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("根据状态获取订单失败: " + e.getMessage(), e);
        }
    }

    // 转换实体为DTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCustomerNotes(order.getCustomerNotes());
        dto.setOrderDate(order.getOrderDate());
        dto.setEstimatedReadyTime(order.getEstimatedReadyTime());
        dto.setCompletedDate(order.getCompletedDate());
        dto.setCreatedAt(order.getCreatedAt());

        // 转换订单项
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(orderItem.getId());
            itemDTO.setProductId(orderItem.getProduct().getId());
            itemDTO.setProductName(orderItem.getProduct().getName());
            itemDTO.setProductEmoji(orderItem.getProduct().getEmoji());
            itemDTO.setQuantity(orderItem.getQuantity());
            itemDTO.setUnitPrice(orderItem.getUnitPrice());
            dto.addOrderItem(itemDTO);
        }

        return dto;
    }
}