package com.auth;

import com.auth.entity.Category;
import com.auth.entity.Product;
import com.auth.repository.CategoryRepository;
import com.auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class AuthApplication {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // 初始化分类数据
            initCategories();

            // 初始化商品数据
            if (productRepository.count() == 0) {
                initProducts();
            }
        };
    }

    private void initCategories() {
        // 检查是否已经有分类数据
        if (categoryRepository.count() == 0) {
            Category[] categories = {
                    createCategory("classic", "经典系列", "🧋", 1),
                    createCategory("fruit", "果茶系列", "🍹", 2),
                    createCategory("tea", "茶饮系列", "🍵", 3),
                    createCategory("premium", "特色系列", "🌟", 4),
                    createCategory("coffee", "咖啡系列", "☕", 5),
                    createCategory("hot", "热饮系列", "🔥", 6),
                    createCategory("special", "特调系列", "🎯", 7)
            };

            for (Category category : categories) {
                categoryRepository.save(category);
            }
            System.out.println("分类数据初始化完成，共添加 " + categories.length + " 个分类");
        }
    }

    private Category createCategory(String name, String displayName, String icon, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDisplayName(displayName);
        category.setIcon(icon);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        category.setDescription(displayName + "，包含多种口味选择");
        return category;
    }

    private void initProducts() {
        // 获取分类
        Category classicCat = categoryRepository.findByName("classic").orElse(null);
        Category fruitCat = categoryRepository.findByName("fruit").orElse(null);
        Category premiumCat = categoryRepository.findByName("premium").orElse(null);
        Category coffeeCat = categoryRepository.findByName("coffee").orElse(null);
        Category hotCat = categoryRepository.findByName("hot").orElse(null);
        Category specialCat = categoryRepository.findByName("special").orElse(null);
        Category teaCat = categoryRepository.findByName("tea").orElse(null);

        // AI推荐的商品列表
        Product[] aiProducts = {
                createProduct("经典珍珠奶茶", "香浓奶茶搭配Q弹珍珠，经典美味", 18.00, classicCat, "🧋", "经典,珍珠,奶茶"),
                createProduct("冰爽芒果波波茶", "新鲜芒果果肉搭配Q弹波波，冰爽解渴", 22.00, fruitCat, "🥭", "芒果,波波,冰爽"),
                createProduct("暖心红糖姜茶", "红糖与老姜的完美结合，温暖香甜，驱寒暖胃", 16.00, hotCat, "☕", "红糖,姜茶,暖饮"),
                createProduct("芝士奶盖草莓奶茶", "香甜草莓与香浓奶茶，topped with 绵密芝士奶盖", 25.00, premiumCat, "🍓", "芝士,草莓,奶盖"),
                createProduct("提神咖啡奶茶", "咖啡与奶茶的完美融合，香浓提神，口感顺滑", 20.00, coffeeCat, "☕", "咖啡,提神,奶茶"),
                createProduct("薰衣草奶茶", "淡淡薰衣草香与奶茶结合，香甜舒缓，帮助放松", 19.00, specialCat, "🌿", "薰衣草,舒缓,放松"),
                createProduct("黑糖珍珠鲜奶", "香浓鲜奶与焦香黑糖珍珠，甜而不腻，口感丰富", 21.00, classicCat, "🥛", "黑糖,珍珠,鲜奶"),
                createProduct("百香果绿茶", "新鲜百香果搭配清香绿茶，酸甜清爽，果香四溢", 17.00, fruitCat, "🍹", "百香果,绿茶,清爽"),
                createProduct("四季春茶", "清香四季春茶，茶味醇厚，回甘清甜", 15.00, teaCat, "🍵", "四季春,茶香,清淡"),
                createProduct("冰爽柠檬茶", "新鲜柠檬搭配绿茶，酸甜清爽", 16.00, fruitCat, "🍋", "柠檬,清爽,冰爽"),
                createProduct("芝士奶盖绿茶", "清香绿茶搭配绵密芝士奶盖", 23.00, premiumCat, "🍵", "芝士,奶盖,绿茶"),
                createProduct("草莓多多", "新鲜草莓果肉，酸甜可口", 24.00, fruitCat, "🍓", "草莓,果茶,多多"),
                createProduct("芋圆奶茶", "香浓奶茶搭配软糯芋圆", 19.00, classicCat, "🍠", "芋圆,奶茶,软糯"),
                createProduct("红豆奶茶", "经典红豆与奶茶的完美结合", 18.00, classicCat, "❤️", "红豆,奶茶,经典"),
                createProduct("布丁奶茶", "滑嫩布丁与香浓奶茶", 20.00, classicCat, "🍮", "布丁,奶茶,滑嫩"),
                createProduct("椰果奶茶", "Q弹椰果搭配香浓奶茶", 17.00, classicCat, "🥥", "椰果,奶茶,Q弹"),
                createProduct("仙草冻奶茶", "清爽仙草冻与奶茶结合", 18.00, classicCat, "🌿", "仙草,奶茶,清爽"),
                createProduct("抹茶拿铁", "日式抹茶与香浓拿铁", 22.00, premiumCat, "🍵", "抹茶,拿铁,日式"),
                createProduct("巧克力奶茶", "浓郁巧克力与香浓奶茶", 21.00, premiumCat, "🍫", "巧克力,奶茶,浓郁"),
                createProduct("芋泥波波茶", "香浓芋泥搭配Q弹波波", 23.00, premiumCat, "🍠", "芋泥,波波,香浓"),
                createProduct("葡萄多多", "新鲜葡萄果肉，清爽可口", 22.00, fruitCat, "🍇", "葡萄,果茶,多多"),
                createProduct("桃子乌龙茶", "香甜桃子与清香乌龙茶", 19.00, teaCat, "🍑", "桃子,乌龙,果茶")
        };

        for (Product product : aiProducts) {
            productRepository.save(product);
        }

        System.out.println("AI推荐商品数据初始化完成，共添加 " + aiProducts.length + " 个商品");
    }

    private Product createProduct(String name, String description, double price,
                                  Category category, String emoji, String tags) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category.getName()); // 使用分类名称
        product.setEmoji(emoji);
        product.setTags(tags);
        product.setAvailable(true);
        product.setImageUrl("/images/" + name + ".jpg");
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }
}