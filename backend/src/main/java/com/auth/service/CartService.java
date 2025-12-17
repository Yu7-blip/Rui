package com.auth.service;

import com.auth.dto.CartDTO;
import com.auth.dto.CartItemDTO;
import com.auth.entity.Cart;
import com.auth.entity.CartItem;
import com.auth.entity.Product;
import com.auth.entity.User;
import com.auth.repository.CartRepository;
import com.auth.repository.CartItemRepository;
import com.auth.repository.ProductRepository;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 获取用户的购物车 - 增强版：自动创建购物车
    public CartDTO getCartByUserId(Long userId) {
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            // 首先检查用户是否存在
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

            // 查找用户的购物车，如果不存在就创建一个
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> createCartForUser(user));

            return convertToDTO(cart);
        } catch (Exception e) {
            throw new RuntimeException("获取购物车失败: " + e.getMessage(), e);
        }
    }

    // 为用户创建购物车
    private Cart createCartForUser(User user) {
        try {
            Cart cart = new Cart(user);
            return cartRepository.save(cart);
        } catch (Exception e) {
            throw new RuntimeException("为用户创建购物车失败: " + e.getMessage(), e);
        }
    }

    // 添加商品到购物车
    public CartDTO addToCart(Long userId, Long productId, Integer quantity) {
        try {
            // 参数验证
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }
            if (productId == null) {
                throw new RuntimeException("商品ID不能为空");
            }
            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }

            // 确保购物车存在
            Cart cart = getOrCreateCart(userId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + productId));

            // 查找是否已存在该商品
            Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

            if (existingItem.isPresent()) {
                // 更新数量
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartItemRepository.save(item);
            } else {
                // 新增商品
                CartItem newItem = new CartItem(cart, product, quantity);
                cartItemRepository.save(newItem);
            }

            // 保存购物车以触发@PreUpdate重新计算总价
            cartRepository.save(cart);

            return convertToDTO(cart);
        } catch (Exception e) {
            throw new RuntimeException("添加商品到购物车失败: " + e.getMessage(), e);
        }
    }

    // 更新购物车商品数量
    public CartDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        try {
            // 参数验证
            if (userId == null || productId == null) {
                throw new RuntimeException("用户ID和商品ID不能为空");
            }
            if (quantity == null) {
                throw new RuntimeException("数量不能为空");
            }

            Cart cart = getOrCreateCart(userId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + productId));

            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                    .orElseThrow(() -> new RuntimeException("购物车中未找到该商品，商品ID: " + productId));

            if (quantity <= 0) {
                // 使用新的方法名
                cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
            } else {
                cartItem.setQuantity(quantity);
                cartItemRepository.save(cartItem);
            }

            // 保存购物车以触发@PreUpdate重新计算总价
            cartRepository.save(cart);

            return convertToDTO(cart);
        } catch (Exception e) {
            throw new RuntimeException("更新购物车商品数量失败: " + e.getMessage(), e);
        }
    }

    // 从购物车移除商品 - 修复版本
    public CartDTO removeFromCart(Long userId, Long productId) {
        try {
            // 参数验证
            if (userId == null || productId == null) {
                throw new RuntimeException("用户ID和商品ID不能为空");
            }

            Cart cart = getOrCreateCart(userId);

            // 检查商品是否存在
            productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + productId));

            // 使用新的方法名删除
            cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

            // 保存购物车以触发@PreUpdate重新计算总价
            cartRepository.save(cart);

            // 重新获取最新的购物车数据
            return getCartByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("从购物车移除商品失败: " + e.getMessage(), e);
        }
    }

    // 清空购物车
    public void clearCart(Long userId) {
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            Cart cart = getOrCreateCart(userId);
            cartItemRepository.deleteByCart(cart);

            // 保存购物车以触发@PreUpdate重新计算总价
            cartRepository.save(cart);
        } catch (Exception e) {
            throw new RuntimeException("清空购物车失败: " + e.getMessage(), e);
        }
    }

    // 获取或创建购物车
    private Cart getOrCreateCart(Long userId) {
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
                        return createCartForUser(user);
                    });
        } catch (Exception e) {
            throw new RuntimeException("获取或创建购物车失败: " + e.getMessage(), e);
        }
    }

    // 转换实体为DTO - 修复版：移除不存在的setCreatedAt/setUpdatedAt方法调用
    private CartDTO convertToDTO(Cart cart) {
        try {
            CartDTO dto = new CartDTO();
            dto.setId(cart.getId());

            // 安全获取用户信息
            if (cart.getUser() != null) {
                dto.setUserId(cart.getUser().getId());
                dto.setUsername(cart.getUser().getUsername() != null ? cart.getUser().getUsername() : "未知用户");
            } else {
                dto.setUserId(0L);
                dto.setUsername("未知用户");
            }

            // 获取总价
            Double totalPrice = cart.getTotalPrice();
            dto.setTotalPrice(totalPrice != null ? totalPrice : 0.0);

            // 使用Cart类的getTotalQuantity()方法计算总数量
            Integer totalQuantity = cart.getTotalQuantity();
            dto.setTotalQuantity(totalQuantity != null ? totalQuantity : 0);

            // 如果CartDTO有这些字段才设置，否则注释掉
            // dto.setCreatedAt(cart.getCreatedAt());
            // dto.setUpdatedAt(cart.getUpdatedAt());

            // 安全转换购物车项
            if (cart.getCartItems() != null) {
                cart.getCartItems().forEach(cartItem -> {
                    try {
                        CartItemDTO itemDTO = new CartItemDTO();
                        itemDTO.setId(cartItem.getId());

                        // 安全获取商品信息
                        if (cartItem.getProduct() != null) {
                            itemDTO.setProductId(cartItem.getProduct().getId());
                            itemDTO.setProductName(cartItem.getProduct().getName() != null ?
                                    cartItem.getProduct().getName() : "未知商品");
                            itemDTO.setProductEmoji(cartItem.getProduct().getEmoji() != null ?
                                    cartItem.getProduct().getEmoji() : "");

                            // 安全获取价格
                            Double productPrice = cartItem.getProduct().getPrice();
                            itemDTO.setProductPrice(productPrice != null ? productPrice : 0.0);
                        } else {
                            itemDTO.setProductId(0L);
                            itemDTO.setProductName("未知商品");
                            itemDTO.setProductEmoji("");
                            itemDTO.setProductPrice(0.0);
                        }

                        itemDTO.setQuantity(cartItem.getQuantity() != null ? cartItem.getQuantity() : 0);
                        dto.addCartItem(itemDTO);
                    } catch (Exception e) {
                        // 跳过有问题的购物车项，继续处理其他项
                        System.err.println("转换购物车项时出错: " + e.getMessage());
                    }
                });
            }

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("转换购物车DTO失败: " + e.getMessage(), e);
        }
    }
}