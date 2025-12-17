package com.auth.repository;

import com.auth.entity.Order;
import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 根据用户查找订单
    List<Order> findByUser(User user);

    // 根据用户ID查找订单
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    // 根据订单号查找
    Optional<Order> findByOrderNumber(String orderNumber);

    // 根据状态查找订单
    List<Order> findByStatus(String status);

    // 根据用户和状态查找订单
    List<Order> findByUserAndStatus(User user, String status);

    // 根据订单号模糊搜索
    List<Order> findByOrderNumberContaining(String orderNumber);

    // 根据用户名搜索
    @Query("SELECT o FROM Order o JOIN o.user u WHERE u.username LIKE %:username%")
    List<Order> findByUserUsernameContainingIgnoreCase(@Param("username") String username);

    // 根据时间段查找订单
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // 统计每日订单数量 - 修复：使用CAST替代FUNCTION
    @Query("SELECT CAST(o.orderDate AS date), COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailyOrderCount(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // 统计订单状态分布
    // 在 OrderRepository.java 中添加
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusDistribution();

    // 获取最近订单
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllByOrderByOrderDateDesc();

    // 统计总收入
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'completed'")
    Double getTotalRevenue();

    // 统计指定时间段的收入
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'completed' AND o.orderDate BETWEEN :startDate AND :endDate")
    Double getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    // 统计总订单数
    Long countBy();

    // 根据状态统计订单数
    Long countByStatus(String status);

    // 获取最近N个订单
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findTop10ByOrderByOrderDateDesc();
}