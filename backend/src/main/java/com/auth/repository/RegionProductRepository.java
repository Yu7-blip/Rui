package com.auth.repository;

import com.auth.entity.Region;
import com.auth.entity.RegionProduct;
import com.auth.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionProductRepository extends JpaRepository<RegionProduct, Long> {

    // 根据地域ID查找
    List<RegionProduct> findByRegionId(Long regionId);

    // 根据产品ID查找
    List<RegionProduct> findByProductId(Long productId);

    // 查找特定地域和产品的关联
    Optional<RegionProduct> findByRegionIdAndProductId(Long regionId, Long productId);

    // 检查关联是否存在
    boolean existsByRegionIdAndProductId(Long regionId, Long productId);

    // 查找地域的特色产品
    List<RegionProduct> findByRegionIdAndIsFeaturedTrue(Long regionId);

    // 查找所有特色产品
    List<RegionProduct> findByIsFeaturedTrue();

    // 根据地域代码查找产品
    @Query("SELECT rp FROM RegionProduct rp " +
            "JOIN Region r ON rp.region.id = r.id " +
            "WHERE r.code = :regionCode AND r.isActive = true")
    List<RegionProduct> findByRegionCode(@Param("regionCode") String regionCode);

    // 根据地域代码查找特色产品
    @Query("SELECT rp FROM RegionProduct rp " +
            "JOIN Region r ON rp.region.id = r.id " +
            "WHERE r.code = :regionCode AND r.isActive = true AND rp.isFeatured = true")
    List<RegionProduct> findFeaturedByRegionCode(@Param("regionCode") String regionCode);

    // 根据产品分类查找地域产品
    @Query("SELECT rp FROM RegionProduct rp " +
            "JOIN Product p ON rp.product.id = p.id " +
            "JOIN Region r ON rp.region.id = r.id " +
            "WHERE p.category = :category AND r.isActive = true")
    List<RegionProduct> findByProductCategory(@Param("category") String category);

    // 根据人气分数排序
    @Query("SELECT rp FROM RegionProduct rp " +
            "JOIN Region r ON rp.region.id = r.id " +
            "WHERE r.isActive = true " +
            "ORDER BY rp.popularityScore DESC")
    List<RegionProduct> findAllOrderByPopularity();

    // 根据地域查找并按人气排序
    List<RegionProduct> findByRegionIdOrderByPopularityScoreDesc(Long regionId);

    // 统计地域的产品数量
    Long countByRegionId(Long regionId);

    // 统计特色产品数量
    Long countByRegionIdAndIsFeaturedTrue(Long regionId);

    // 根据季节月份查找
    @Query("SELECT rp FROM RegionProduct rp " +
            "WHERE rp.seasonalMonth LIKE %:month%")
    List<RegionProduct> findBySeasonalMonth(@Param("month") String month);

    // 批量删除地域产品关联
    void deleteByRegionId(Long regionId);

    void deleteByProductId(Long productId);

    // 批量检查关联是否存在
    @Query("SELECT rp.product.id FROM RegionProduct rp " +
            "WHERE rp.region.id = :regionId AND rp.product.id IN :productIds")
    List<Long> findExistingProductIds(@Param("regionId") Long regionId,
                                      @Param("productIds") List<Long> productIds);
}