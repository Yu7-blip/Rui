// 将原有的复杂查询简化为基本的 LIKE 查询

package com.auth.repository;

import com.auth.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByIsActiveTrue();

    Optional<Region> findByCodeAndIsActiveTrue(String code);

    // 简化的查询方法 - 只保留必要的参数
    @Query("SELECT r FROM Region r WHERE r.isActive = true AND r.coverProvinces LIKE %:province%")
    List<Region> findByProvinces(@Param("province") String province);

    // 产品统计查询
    @Query("SELECT r.id, r.name, COUNT(rp) FROM Region r " +
            "LEFT JOIN RegionProduct rp ON r.id = rp.region.id " +
            "WHERE r.isActive = true " +
            "GROUP BY r.id, r.name")
    List<Object[]> countProductsByRegion();
}