package com.auth.dto;

import java.math.BigDecimal;

public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalProducts;
    private Double revenueChange;
    private Double ordersChange;
    private Double usersChange;
    private BigDecimal avgOrderValue;
    private Double avgOrderChange;

    // 构造器
    public AdminStatsDTO() {}

    public AdminStatsDTO(Long totalUsers, Long totalOrders, BigDecimal totalRevenue,
                         Long totalProducts, BigDecimal avgOrderValue) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalProducts = totalProducts;
        this.avgOrderValue = avgOrderValue;
    }

    // getter和setter
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }

    public Double getRevenueChange() { return revenueChange; }
    public void setRevenueChange(Double revenueChange) { this.revenueChange = revenueChange; }

    public Double getOrdersChange() { return ordersChange; }
    public void setOrdersChange(Double ordersChange) { this.ordersChange = ordersChange; }

    public Double getUsersChange() { return usersChange; }
    public void setUsersChange(Double usersChange) { this.usersChange = usersChange; }

    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(BigDecimal avgOrderValue) { this.avgOrderValue = avgOrderValue; }

    public Double getAvgOrderChange() { return avgOrderChange; }
    public void setAvgOrderChange(Double avgOrderChange) { this.avgOrderChange = avgOrderChange; }
}