package com.auth.service;

import com.auth.entity.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 初始化默认用户（系统启动时运行）
     */
    @PostConstruct
    public void initDefaultUser() {
        try {
            System.out.println("=== 开始初始化默认用户 ===");

            // 创建默认普通用户
            if (!userRepository.existsByUsername("user")) {
                User defaultUser = new User();
                defaultUser.setUsername("user");
                defaultUser.setPassword("123456"); // ⭐ 使用明文
                defaultUser.setEmail("user@milktea.com");
                userRepository.save(defaultUser);
                System.out.println("✅ 默认用户已创建: user / 123456");
            }

            // 创建管理员用户
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword("password123"); // ⭐ 使用明文
                adminUser.setEmail("admin@example.com");
                userRepository.save(adminUser);
                System.out.println("✅ 管理员用户已创建: admin / password123");
            }

            // 创建测试用户
            if (!userRepository.existsByUsername("testuser")) {
                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setPassword("password123"); // ⭐ 使用明文
                testUser.setEmail("test@example.com");
                userRepository.save(testUser);
                System.out.println("✅ 测试用户已创建: testuser / password123");
            }

            System.out.println("=== 用户初始化完成 ===");

            // 打印所有用户
            List<User> allUsers = userRepository.findAll();
            System.out.println("当前数据库中的用户:");
            for (User user : allUsers) {
                System.out.println("  - " + user.getUsername() + " : " + user.getPassword());
            }

        } catch (Exception e) {
            System.err.println("初始化用户失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 用户注册
     */
    public User registerUser(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户 - ⭐ 使用明文密码
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // ⭐ 直接存储明文，不加密
        user.setEmail(email);

        return userRepository.save(user);
    }

    /**
     * 用户登录验证 - 使用明文验证（测试阶段）
     */
    public boolean validateUser(String username, String password) {
        System.out.println("=== 开始验证用户 ===");
        System.out.println("用户名: " + username);
        System.out.println("输入密码: " + password);

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String dbPassword = user.getPassword();

            System.out.println("数据库中的密码: " + dbPassword);
            System.out.println("用户邮箱: " + user.getEmail());
            System.out.println("用户ID: " + user.getId());

            // ⭐ 使用明文比较（测试阶段）
            boolean isValid = password.equals(dbPassword);
            System.out.println("验证结果: " + isValid);
            System.out.println("=== 验证结束 ===");

            return isValid;
        }

        System.out.println("用户不存在: " + username);
        System.out.println("=== 验证结束 ===");
        return false;
    }

    /**
     * 根据用户名获取用户信息
     */
    public Optional<User> getUserByUsername(String username) {
        System.out.println("获取用户信息: " + username);
        Optional<User> result = userRepository.findByUsername(username);
        System.out.println("用户是否存在: " + result.isPresent());
        return result;
    }

    /**
     * 根据ID获取用户信息
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * 更新用户信息
     */
    public User updateUser(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }

        return userRepository.save(user);
    }

    /**
     * 获取所有用户（调试用）
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}