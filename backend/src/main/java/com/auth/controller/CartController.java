package com.auth.controller;

import com.auth.dto.CartDTO;
import com.auth.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:8080")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        try {
            System.out.println("🛒 获取购物车请求，用户ID: " + userId);
            CartDTO cart = cartService.getCartByUserId(userId);
            System.out.println("✅ 购物车数据: " + cart);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.err.println("❌ 获取购物车异常 - 用户ID: " + userId);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取购物车失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            System.out.println("➕ 添加商品到购物车 - 用户ID: " + userId + ", 商品ID: " + productId + ", 数量: " + quantity);
            CartDTO cart = cartService.addToCart(userId, productId, quantity);
            System.out.println("✅ 添加成功，购物车数据: " + cart);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            System.err.println("❌ 添加购物车业务异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 添加购物车系统异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("添加商品到购物车失败");
        }
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            CartDTO cart = cartService.updateCartItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long userId,
            @RequestParam Long productId) {
        try {
            CartDTO cart = cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}