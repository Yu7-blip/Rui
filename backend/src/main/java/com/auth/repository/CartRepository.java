package com.auth.repository;

import com.auth.entity.Cart;
import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 根据用户查找购物车
    Optional<Cart> findByUser(User user);

    // 根据用户ID查找购物车
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    // 检查用户是否有购物车
    boolean existsByUser(User user);
}