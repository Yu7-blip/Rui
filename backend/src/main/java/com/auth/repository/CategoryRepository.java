package com.auth.repository;

import com.auth.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 根据名称查找分类
    Optional<Category> findByName(String name);

    // 查找活跃分类
    List<Category> findByActiveTrue();

    // 根据排序查找分类
    List<Category> findByActiveTrueOrderBySortOrderAsc();

    // 统计分类使用情况
    @Query("SELECT c.name, COUNT(p) FROM Category c LEFT JOIN Product p ON c.name = p.category WHERE p.available = true GROUP BY c.name")
    List<Object[]> getCategoryUsageStats();

    // 查找包含商品的分类
    @Query("SELECT DISTINCT c FROM Category c JOIN Product p ON c.name = p.category WHERE p.available = true AND c.active = true")
    List<Category> findCategoriesWithProducts();
}