package com.auth.config;

import com.auth.entity.Region;
import com.auth.entity.Product;
import com.auth.entity.RegionProduct;
import com.auth.repository.RegionRepository;
import com.auth.repository.ProductRepository;
import com.auth.repository.RegionProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class ImmediateRegionDataFix {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RegionProductRepository regionProductRepository;

    @PostConstruct
    public void init() {
        System.out.println("🚨 紧急修复：检查地域-产品关联数据...");

        // 检查是否有数据
        long count = regionProductRepository.count();
        if (count == 0) {
            System.out.println("⚠️ 发现地域-产品关联表为空，立即创建数据...");
            createImmediateData();
        } else {
            System.out.println("✅ 地域-产品关联数据已存在，共 " + count + " 条记录");
        }
    }

    private void createImmediateData() {
        try {
            // 获取西南地区 - 使用正确的方法名
            Region southwestRegion = null;
            List<Region> allRegions = regionRepository.findAll();
            for (Region region : allRegions) {
                if ("southwest".equals(region.getCode())) {
                    southwestRegion = region;
                    break;
                }
            }

            if (southwestRegion == null) {
                System.out.println("❌ 找不到西南地区");
                // 如果没有西南地区，使用第一个地区
                if (!allRegions.isEmpty()) {
                    southwestRegion = allRegions.get(0);
                    System.out.println("📝 使用第一个地区：" + southwestRegion.getName());
                } else {
                    System.out.println("❌ 没有任何地区数据");
                    return;
                }
            }

            // 获取前5个产品
            List<Product> products = productRepository.findAll();
            if (products.isEmpty()) {
                System.out.println("❌ 没有产品数据");
                return;
            }

            System.out.println("📊 找到 " + products.size() + " 个产品");

            // 为西南地区添加3-5个特色产品
            int maxProducts = Math.min(5, products.size());
            int successCount = 0;

            for (int i = 0; i < maxProducts; i++) {
                try {
                    Product product = products.get(i);

                    // 检查是否已存在关联
                    boolean exists = regionProductRepository.existsByRegionIdAndProductId(
                            southwestRegion.getId(), product.getId());

                    if (!exists) {
                        RegionProduct rp = new RegionProduct();
                        rp.setRegion(southwestRegion);
                        rp.setProduct(product);
                        rp.setIsFeatured(true);
                        rp.setPopularityScore(80 + (i * 5)); // 80, 85, 90, 95, 100

                        // 设置推荐理由
                        String reason = getRecommendReason(product, southwestRegion);
                        rp.setRecommendReason(reason);

                        // 设置当地名称
                        String localName = getLocalName(product, southwestRegion);
                        rp.setLocalName(localName);

                        rp.setSeasonalMonth(getSeasonalMonth(southwestRegion));
                        rp.setCreatedAt(new Date());
                        rp.setUpdatedAt(new Date());

                        regionProductRepository.save(rp);
                        System.out.println("✅ 添加：" + product.getName() + " → " + southwestRegion.getName());
                        successCount++;
                    } else {
                        System.out.println("⏩ 跳过：" + product.getName() + " (已存在关联)");
                    }

                } catch (Exception e) {
                    System.out.println("❌ 添加产品时出错：" + e.getMessage());
                }
            }

            System.out.println("🎉 紧急修复完成！共添加 " + successCount + " 个产品关联");

        } catch (Exception e) {
            System.out.println("❌ 紧急修复失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getRecommendReason(Product product, Region region) {
        String baseReason = region.getName() + "特色推荐，";

        if (product.getCategory() != null) {
            switch (product.getCategory()) {
                case "milktea":
                    return baseReason + "经典奶茶符合当地口味";
                case "fruit":
                    return baseReason + "新鲜水果茶清凉解渴";
                case "weather":
                    return baseReason + "适合当地气候特点";
                case "special":
                    return baseReason + "创意饮品独具风味";
                case "festival":
                    return baseReason + "节日氛围浓厚";
                case "classic":
                    return baseReason + "传统茶饮文化体现";
                default:
                    return baseReason + "口感独特值得一试";
            }
        }

        return baseReason + "深受当地人喜爱";
    }

    private String getLocalName(Product product, Region region) {
        String prefix = "";

        switch (region.getCode()) {
            case "north":
                prefix = "京味";
                break;
            case "northeast":
                prefix = "东北";
                break;
            case "northwest":
                prefix = "陕甘";
                break;
            case "southwest":
                prefix = "川滇";
                break;
            case "south":
                prefix = "粤式";
                break;
            case "east":
                prefix = "沪上";
                break;
            default:
                prefix = "地方";
        }

        // 取产品名前2-3个字
        String name = product.getName();
        if (name.length() >= 3) {
            return prefix + name.substring(0, 3);
        } else if (name.length() >= 2) {
            return prefix + name.substring(0, 2);
        } else {
            return prefix + name;
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