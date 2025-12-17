package com.auth.config;

import com.auth.entity.Region;
import com.auth.entity.Product;
import com.auth.entity.RegionProduct;
import com.auth.repository.RegionRepository;
import com.auth.repository.ProductRepository;
import com.auth.repository.RegionProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@Order(2) // 在商品数据初始化之后执行
public class RegionDataInitializer implements CommandLineRunner {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RegionProductRepository regionProductRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 检查关联表是否为空
        long count = regionProductRepository.count();
        if (count == 0) {
            System.out.println("🚀 开始初始化地域-产品关联数据...");
            initRegionProductData();
            System.out.println("✅ 地域-产品关联数据初始化完成！");
        } else {
            System.out.println("✅ 地域-产品关联数据已存在 (共 " + count + " 条记录)");
        }
    }

    private void initRegionProductData() {
        // 获取所有地域
        List<Region> regions = regionRepository.findAll();
        List<Product> products = productRepository.findAll();

        if (regions.isEmpty() || products.isEmpty()) {
            System.out.println("⚠️ 地域或产品数据为空，跳过关联数据初始化");
            return;
        }

        System.out.println("📊 找到 " + regions.size() + " 个地域，准备关联产品...");

        // 为每个地域分配2-3个产品
        int productIndex = 0;
        int totalAssigned = 0;

        for (Region region : regions) {
            System.out.println("📍 为地域 [" + region.getName() + "] 添加产品...");

            // 每个地域分配2-3个产品
            int productsPerRegion = 2 + (region.getId().intValue() % 2); // 2或3个

            for (int i = 0; i < productsPerRegion; i++) {
                if (productIndex >= products.size()) {
                    productIndex = 0; // 循环使用产品
                }

                Product product = products.get(productIndex);
                productIndex++;

                // 创建关联
                RegionProduct regionProduct = new RegionProduct();
                regionProduct.setRegion(region);
                regionProduct.setProduct(product);
                regionProduct.setIsFeatured(true);
                regionProduct.setPopularityScore(80 + (int)(Math.random() * 20)); // 80-99
                regionProduct.setRecommendReason(getRecommendReason(region, product));
                regionProduct.setLocalName(getLocalName(region, product));
                regionProduct.setSeasonalMonth(getSeasonalMonth(region));
                regionProduct.setCreatedAt(new Date());
                regionProduct.setUpdatedAt(new Date());

                try {
                    regionProductRepository.save(regionProduct);
                    System.out.println("   ✅ " + region.getName() + " ← " + product.getName());
                    totalAssigned++;
                } catch (Exception e) {
                    System.out.println("   ❌ 关联失败: " + e.getMessage());
                }
            }
        }

        System.out.println("🎉 成功创建 " + totalAssigned + " 个地域-产品关联");
    }

    private String getRecommendReason(Region region, Product product) {
        Map<String, String> regionReasons = new HashMap<>();
        regionReasons.put("north", "北方寒冷气候，适合暖饮");
        regionReasons.put("northeast", "东北严寒地区，推荐热饮");
        regionReasons.put("northwest", "西北干燥气候，滋润饮品");
        regionReasons.put("south", "南方炎热天气，清凉解暑");
        regionReasons.put("southwest", "西南山区特色，风味独特");
        regionReasons.put("east", "华东都市风味，精致时尚");

        String baseReason = regionReasons.getOrDefault(region.getCode(), "当地特色推荐");

        // 根据产品类别添加
        if ("milktea".equals(product.getCategory())) {
            return baseReason + "，经典奶茶符合大众口味";
        } else if ("fruit".equals(product.getCategory())) {
            return baseReason + "，新鲜水果茶健康美味";
        } else if ("special".equals(product.getCategory())) {
            return baseReason + "，创意饮品独具风味";
        }

        return baseReason;
    }

    private String getLocalName(Region region, Product product) {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("north", "京味");
        prefixes.put("northeast", "东北");
        prefixes.put("northwest", "陕甘");
        prefixes.put("south", "粤式");
        prefixes.put("southwest", "川滇");
        prefixes.put("east", "沪上");

        String prefix = prefixes.getOrDefault(region.getCode(), "地方");

        // 取产品名前2个字
        String productName = product.getName();
        if (productName.length() >= 2) {
            return prefix + productName.substring(0, 2);
        } else {
            return prefix + productName;
        }
    }

    private String getSeasonalMonth(Region region) {
        switch (region.getCode()) {
            case "north":
            case "northeast":
                return "10,11,12,1,2"; // 秋冬季节
            case "south":
                return "4,5,6,7,8,9"; // 春夏季节
            case "southwest":
                return "3,4,5,9,10"; // 春秋季节
            default:
                return "1,2,3,4,5,6,7,8,9,10,11,12"; // 全年
        }
    }
}