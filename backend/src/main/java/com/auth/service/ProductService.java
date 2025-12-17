package com.auth.service;

import com.auth.dto.ProductDTO;
import com.auth.entity.Product;
import com.auth.repository.ProductRepository;
import com.auth.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取所有商品
     */
    public List<ProductDTO> getAllProducts() {
        try {
            List<Product> products = productRepository.findByAvailableTrue();
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取商品列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据分类获取商品
     */
    public List<ProductDTO> getProductsByCategory(String category) {
        try {
            List<Product> products = productRepository.findByCategoryAndAvailableTrue(category);
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取分类商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取商品
     */
    public ProductDTO getProductById(Long id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            return product.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("获取商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索商品 - 修复：使用Repository中存在的方法
     */
    public List<ProductDTO> searchProducts(String keyword) {
        try {
            // 使用Repository中存在的findByNameContainingIgnoreCase方法
            List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
            // 过滤可用商品
            products = products.stream()
                    .filter(Product::getAvailable)
                    .collect(Collectors.toList());
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("搜索商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建商品（管理员）
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        try {
            // 验证分类是否存在
            if (!categoryRepository.findByName(productDTO.getCategory()).isPresent()) {
                throw new RuntimeException("分类不存在: " + productDTO.getCategory());
            }

            // 验证商品名称是否已存在
            if (productRepository.existsByName(productDTO.getName())) {
                throw new RuntimeException("商品名称已存在: " + productDTO.getName());
            }

            Product product = convertToEntity(productDTO);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            product.setAvailable(true);

            Product savedProduct = productRepository.save(product);
            return convertToDTO(savedProduct);
        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("创建商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新商品（管理员）
     */
    public ProductDTO updateProduct(ProductDTO productDTO) {
        try {
            // 验证商品是否存在
            Product existingProduct = productRepository.findById(productDTO.getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + productDTO.getId()));

            // 验证分类是否存在
            if (productDTO.getCategory() != null &&
                    !categoryRepository.findByName(productDTO.getCategory()).isPresent()) {
                throw new RuntimeException("分类不存在: " + productDTO.getCategory());
            }

            // 验证商品名称是否重复（排除自身）
            if (productDTO.getName() != null &&
                    !existingProduct.getName().equals(productDTO.getName()) &&
                    productRepository.existsByName(productDTO.getName())) {
                throw new RuntimeException("商品名称已存在: " + productDTO.getName());
            }

            // 更新字段
            if (productDTO.getName() != null) {
                existingProduct.setName(productDTO.getName());
            }
            if (productDTO.getPrice() != null) {
                existingProduct.setPrice(productDTO.getPrice());
            }
            if (productDTO.getDescription() != null) {
                existingProduct.setDescription(productDTO.getDescription());
            }
            if (productDTO.getCategory() != null) {
                existingProduct.setCategory(productDTO.getCategory());
            }
            if (productDTO.getEmoji() != null) {
                existingProduct.setEmoji(productDTO.getEmoji());
            }
            if (productDTO.getAvailable() != null) {
                existingProduct.setAvailable(productDTO.getAvailable());
            }

            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(existingProduct);
            return convertToDTO(updatedProduct);
        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("更新商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除商品（管理员）- 软删除
     */
    public void deleteProduct(Long id) {
        try {
            // 验证商品是否存在
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + id));

            // 软删除：设置为不可用
            product.setAvailable(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

        } catch (RuntimeException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("删除商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 硬删除商品（管理员）- 谨慎使用
     */
    public void hardDeleteProduct(Long id) {
        try {
            if (!productRepository.existsById(id)) {
                throw new RuntimeException("商品不存在: " + id);
            }

            // TODO: 检查是否有订单关联该商品
            productRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("删除商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 启用/禁用商品
     */
    public ProductDTO toggleProductStatus(Long id, boolean available) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + id));

            product.setAvailable(available);
            product.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(product);
            return convertToDTO(updatedProduct);
        } catch (Exception e) {
            throw new RuntimeException("更新商品状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取热门商品 - 修复：使用Repository中存在的方法
     */
    public List<ProductDTO> getHotProducts(int limit) {
        try {
            // 使用Repository中存在的findAvailableProductsOrderByIdDesc方法，然后取前limit个
            List<Product> products = productRepository.findAvailableProductsOrderByIdDesc();
            if (products.size() > limit) {
                products = products.subList(0, limit);
            }
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取热门商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取商品数量统计
     */
    public Map<String, Object> getProductStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            long totalProducts = productRepository.count();
            long availableProducts = productRepository.countByAvailableTrue();

            stats.put("totalProducts", totalProducts);
            stats.put("availableProducts", availableProducts);
            stats.put("unavailableProducts", totalProducts - availableProducts);

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取商品统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换实体为DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setEmoji(product.getEmoji());
        dto.setAvailable(product.getAvailable());
        dto.setCreatedAt(product.getCreatedAt());
        // 注意：ProductDTO没有setUpdatedAt方法，所以不设置
        return dto;
    }

    /**
     * 转换DTO为实体
     */
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setEmoji(dto.getEmoji());
        product.setAvailable(dto.getAvailable());
        return product;
    }
}