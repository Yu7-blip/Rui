package com.auth.repository;

import com.auth.entity.Order;
import com.auth.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 根据订单查找订单项
    List<OrderItem> findByOrder(Order order);

    // 根据订单ID查找订单项
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    // 根据商品ID查找订单项（用于统计商品销量）
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    // 商品销售排行
    @Query("SELECT p.name, p.category, p.emoji, SUM(oi.quantity) as sales, SUM(oi.quantity * oi.unitPrice) as revenue " +
            "FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.id, p.name, p.category, p.emoji " +
            "ORDER BY sales DESC")
    List<Object[]> getProductSalesRanking();

    // 根据时间段统计销售
    @Query("SELECT p.name, p.category, p.emoji, SUM(oi.quantity) as sales, SUM(oi.quantity * oi.unitPrice) as revenue " +
            "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name, p.category, p.emoji " +
            "ORDER BY sales DESC")
    List<Object[]> getProductSalesRankingByDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // 统计分类销售情况
    @Query("SELECT p.category, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
            "FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.category " +
            "ORDER BY totalRevenue DESC")
    List<Object[]> getCategorySalesStatistics();

    // 获取热门商品（按销量排序）
    @Query("SELECT p.id, p.name, p.category.id, p.emoji, " +
            "SUM(oi.quantity) as salesCount, SUM(oi.quantity * oi.unitPrice) as revenue " +
            "FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.id, p.name, p.category.id, p.emoji " +
            "ORDER BY salesCount DESC")
    List<Object[]> getTopSellingProducts();

    // 根据时间段获取热门商品
    @Query("SELECT p.id, p.name, p.category, p.emoji, p.price, " +
            "SUM(oi.quantity) as totalSales " +
            "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name, p.category, p.emoji, p.price " +
            "ORDER BY totalSales DESC")
    List<Object[]> getTopSellingProductsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // 统计每日销售数据 - 修复：使用CAST替代FUNCTION
    @Query("SELECT CAST(o.orderDate AS date), SUM(oi.quantity * oi.unitPrice) " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailySalesData(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // 统计每小时订单分布 - 修复：使用EXTRACT替代FUNCTION
    @Query("SELECT EXTRACT(HOUR FROM o.orderDate), COUNT(DISTINCT o.id) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(HOUR FROM o.orderDate) " +
            "ORDER BY EXTRACT(HOUR FROM o.orderDate)")
    List<Object[]> getHourlyOrderDistribution(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}