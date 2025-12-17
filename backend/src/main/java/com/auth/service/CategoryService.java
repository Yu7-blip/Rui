package com.auth.service;

import com.auth.entity.Category;
import com.auth.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 系统启动时初始化默认分类
     */
    @PostConstruct
    public void initDefaultCategories() {
        try {
            // 检查是否已有分类
            if (categoryRepository.count() == 0) {
                // 修复：使用Arrays.asList替代List.of
                List<Category> defaultCategories = Arrays.asList(
                        createCategory("recommend", "推荐", "⭐", "热销推荐商品", 1),
                        createCategory("weather", "天气限定", "☁️", "根据天气特供饮品", 2),
                        createCategory("festival", "节日限定", "🎉", "节日特别商品", 3),
                        createCategory("classic", "经典奶茶", "🥤", "经典口味奶茶", 4),
                        createCategory("fruit", "果茶系列", "🍓", "新鲜水果茶", 5),
                        createCategory("milktea", "奶茶系列", "🧋", "各类奶茶", 6),
                        createCategory("special", "特色饮品", "✨", "特色创新饮品", 7)
                );

                categoryRepository.saveAll(defaultCategories);
                System.out.println("✅ 默认分类初始化完成");
            }
        } catch (Exception e) {
            System.err.println("❌ 初始化默认分类失败: " + e.getMessage());
        }
    }

    private Category createCategory(String name, String displayName, String icon, String description, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDisplayName(displayName);
        category.setIcon(icon);
        category.setDescription(description);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        return category;
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        try {
            return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        } catch (Exception e) {
            throw new RuntimeException("获取分类列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有分类（包含不活跃的）
     */
    public List<Category> getAllCategoriesIncludeInactive() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("获取分类列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long id) {
        try {
            Optional<Category> category = categoryRepository.findById(id);
            return category.orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        } catch (Exception e) {
            throw new RuntimeException("获取分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据名称获取分类
     */
    public Category getCategoryByName(String name) {
        try {
            Optional<Category> category = categoryRepository.findByName(name);
            return category.orElseThrow(() -> new RuntimeException("分类不存在: " + name));
        } catch (Exception e) {
            throw new RuntimeException("获取分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建分类（管理员）
     */
    public Category createCategory(Category category) {
        try {
            // 验证分类名称是否已存在
            if (categoryRepository.findByName(category.getName()).isPresent()) {
                throw new RuntimeException("分类名称已存在: " + category.getName());
            }

            // 验证显示名称是否已存在
            if (category.getDisplayName() != null) {
                // 这里可以添加显示名称唯一性检查
            }

            // 设置默认值
            if (category.getActive() == null) {
                category.setActive(true);
            }
            if (category.getSortOrder() == null) {
                // 自动设置为最大排序值+1
                Integer maxOrder = categoryRepository.findByActiveTrue().stream()
                        .map(Category::getSortOrder)
                        .max(Integer::compare)
                        .orElse(0);
                category.setSortOrder(maxOrder + 1);
            }
            if (category.getIcon() == null) {
                category.setIcon("📦");
            }

            // 时间戳会自动由 @PrePersist 设置
            return categoryRepository.save(category);
        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("创建分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新分类（管理员）
     */
    public Category updateCategory(Category category) {
        try {
            // 验证分类是否存在
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("分类不存在: " + category.getId()));

            // 验证分类名称是否重复（排除自身）
            if (category.getName() != null && !existingCategory.getName().equals(category.getName())) {
                if (categoryRepository.findByName(category.getName()).isPresent()) {
                    throw new RuntimeException("分类名称已存在: " + category.getName());
                }
                existingCategory.setName(category.getName());
            }

            // 更新其他字段
            if (category.getDisplayName() != null) {
                existingCategory.setDisplayName(category.getDisplayName());
            }
            if (category.getIcon() != null) {
                existingCategory.setIcon(category.getIcon());
            }
            if (category.getDescription() != null) {
                existingCategory.setDescription(category.getDescription());
            }
            if (category.getSortOrder() != null) {
                existingCategory.setSortOrder(category.getSortOrder());
            }
            if (category.getActive() != null) {
                existingCategory.setActive(category.getActive());
            }

            // updatedAt 会自动由 @PreUpdate 更新
            return categoryRepository.save(existingCategory);
        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("更新分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除分类（管理员）- 软删除
     */
    public void deleteCategory(Long id) {
        try {
            // 验证分类是否存在
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("分类不存在: " + id));

            // 检查是否有商品使用该分类（这里需要根据你的Product实体来判断）
            // 如果有商品使用该分类，可以：
            // 1. 不允许删除
            // 2. 将分类置为不活跃
            // 这里采用方案2：软删除

            category.setActive(false);
            categoryRepository.save(category);

        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("删除分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 硬删除分类（管理员）- 真实删除，谨慎使用
     */
    public void hardDeleteCategory(Long id) {
        try {
            // 验证分类是否存在
            if (!categoryRepository.existsById(id)) {
                throw new RuntimeException("分类不存在: " + id);
            }

            // TODO: 这里应该检查是否有商品使用该分类
            // 如果有，不允许删除

            categoryRepository.deleteById(id);
        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("删除分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 启用/禁用分类
     */
    public Category toggleCategoryStatus(Long id, boolean active) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("分类不存在: " + id));

            category.setActive(active);
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("更新分类状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新分类排序
     */
    public void updateCategoryOrder(List<Map<String, Object>> orders) {
        try {
            for (Map<String, Object> order : orders) {
                Long categoryId = Long.parseLong(order.get("id").toString());
                Integer sortOrder = Integer.parseInt(order.get("sortOrder").toString());

                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("分类不存在: " + categoryId));

                category.setSortOrder(sortOrder);
                categoryRepository.save(category);
            }
        } catch (Exception e) {
            throw new RuntimeException("更新分类排序失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分类统计信息
     */
    public Map<String, Object> getCategoryStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long totalCategories = categoryRepository.count();
            long activeCategories = categoryRepository.findByActiveTrue().size();

            // 获取分类使用统计
            List<Object[]> usageStats = categoryRepository.getCategoryUsageStats();

            stats.put("totalCategories", totalCategories);
            stats.put("activeCategories", activeCategories);
            stats.put("inactiveCategories", totalCategories - activeCategories);
            stats.put("usageStats", usageStats);

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取分类统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取包含商品的分类
     */
    public List<Category> getCategoriesWithProducts() {
        try {
            return categoryRepository.findCategoriesWithProducts();
        } catch (Exception e) {
            throw new RuntimeException("获取包含商品的分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据状态获取分类
     */
    public List<Category> getCategoriesByStatus(boolean active) {
        try {
            List<Category> allCategories = categoryRepository.findAll();
            List<Category> result = new ArrayList<>();

            for (Category category : allCategories) {
                if (active && category.getActive()) {
                    result.add(category);
                } else if (!active && !category.getActive()) {
                    result.add(category);
                }
            }

            // 如果是活跃分类，按排序顺序排序
            if (active) {
                result.sort(Comparator.comparing(Category::getSortOrder));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索分类
     */
    public List<Category> searchCategories(String keyword) {
        try {
            List<Category> allCategories = categoryRepository.findAll();
            List<Category> result = new ArrayList<>();
            String lowerKeyword = keyword.toLowerCase();

            for (Category category : allCategories) {
                if (category.getName().toLowerCase().contains(lowerKeyword) ||
                        (category.getDisplayName() != null && category.getDisplayName().toLowerCase().contains(lowerKeyword)) ||
                        (category.getDescription() != null && category.getDescription().toLowerCase().contains(lowerKeyword))) {
                    result.add(category);
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("搜索分类失败: " + e.getMessage(), e);
        }
    }
}