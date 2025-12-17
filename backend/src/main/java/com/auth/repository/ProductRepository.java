package com.auth.repository;

import com.auth.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 在 ProductRepository.java 中添加这个方法
    Long countByAvailable(Boolean available);
    // 根据分类查找商品
    List<Product> findByCategory(String category);

    // 查找可用商品
    List<Product> findByAvailableTrue();

    // 根据分类查找可用商品
    List<Product> findByCategoryAndAvailableTrue(String category);

    // 根据名称模糊搜索
    List<Product> findByNameContainingIgnoreCase(String name);

    // 根据多个分类查找
    @Query("SELECT p FROM Product p WHERE p.category IN :categories AND p.available = true")
    List<Product> findByCategories(@Param("categories") List<String> categories);

    // 根据价格范围查找
    List<Product> findByPriceBetweenAndAvailableTrue(Double minPrice, Double maxPrice);

    // 统计可用商品数量
    Long countByAvailableTrue();

    // 按分类统计商品数量 - 修改这个方法
    @Query("SELECT p.category, COUNT(p), COALESCE(SUM(oi.quantity), 0) " +
            "FROM Product p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "WHERE p.available = true " +
            "GROUP BY p.category")
    List<Object[]> getProductCountByCategory();

    // 根据状态查找商品
    List<Product> findByAvailable(Boolean available);

    // 根据分类和状态查找商品
    List<Product> findByCategoryAndAvailable(String category, Boolean available);

    // 获取商品销售统计 - 修复：使用正确的JOIN语法
    @Query("SELECT p.id, p.name, p.category, p.emoji, p.price, " +
            "COALESCE(SUM(oi.quantity), 0), " +
            "COALESCE(SUM(oi.quantity * oi.unitPrice), 0) " +
            "FROM Product p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "WHERE p.available = true " +
            "GROUP BY p.id, p.name, p.category, p.emoji, p.price " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getProductSalesStatistics();

    // 获取热门商品（简化版本）
    @Query("SELECT p FROM Product p WHERE p.available = true ORDER BY p.id DESC")
    List<Product> findAvailableProductsOrderByIdDesc();

    // 根据分类统计销售金额
    @Query("SELECT p.category, SUM(oi.quantity * oi.unitPrice) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE p.available = true " +
            "GROUP BY p.category")
    List<Object[]> getCategoryRevenueStatistics();

    // ============ 新增方法 ============

    // 根据名称和可用状态查找商品
    Optional<Product> findByNameAndAvailableTrue(String name);

    // 根据名称包含关键字和可用状态查找商品
    List<Product> findByNameContainingAndAvailableTrue(String name);

    // ============ 新增AI推荐需要的方法 ============

    // 根据名称精确查找商品（不限制可用状态）
    Optional<Product> findByName(String name);

    // 检查商品名称是否存在
    boolean existsByName(String name);
}