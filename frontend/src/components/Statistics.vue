<template>
  <div class="statistics">
    <div class="stats-header">
      <h2>数据统计</h2>
      <div class="time-filter">
        <select v-model="timeRange" class="filter-select" @change="loadStats">
          <option value="today">今日</option>
          <option value="week">本周</option>
          <option value="month">本月</option>
          <option value="year">今年</option>
        </select>
        <button class="refresh-btn" @click="loadStats">刷新</button>
      </div>
    </div>

    <div class="stats-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <span class="loading-text">统计数据加载中...</span>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-state">
        <span class="error-text">{{ error }}</span>
        <button @click="loadStats" class="retry-btn">重试</button>
      </div>

      <!-- 核心指标卡片 -->
      <div v-if="!loading && !error" class="metrics-cards">
        <div class="metric-card">
          <div class="metric-icon revenue">💰</div>
          <div class="metric-info">
            <div class="metric-value">¥{{ formatCurrency(metrics.totalRevenue) }}</div>
            <div class="metric-label">总收入</div>
            <div class="metric-change" :class="getChangeClass(metrics.revenueChange)">
              {{ metrics.revenueChange > 0 ? '+' : '' }}{{ metrics.revenueChange || 0 }}%
            </div>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon orders">📦</div>
          <div class="metric-info">
            <div class="metric-value">{{ metrics.totalOrders || 0 }}</div>
            <div class="metric-label">总订单</div>
            <div class="metric-change" :class="getChangeClass(metrics.ordersChange)">
              {{ metrics.ordersChange > 0 ? '+' : '' }}{{ metrics.ordersChange || 0 }}%
            </div>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon users">👥</div>
          <div class="metric-info">
            <div class="metric-value">{{ metrics.totalUsers || 0 }}</div>
            <div class="metric-label">总用户</div>
            <div class="metric-change" :class="getChangeClass(metrics.usersChange)">
              {{ metrics.usersChange > 0 ? '+' : '' }}{{ metrics.usersChange || 0 }}%
            </div>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon avg">📊</div>
          <div class="metric-info">
            <div class="metric-value">¥{{ formatCurrency(metrics.avgOrderValue) }}</div>
            <div class="metric-label">客单价</div>
            <div class="metric-change" :class="getChangeClass(metrics.avgOrderChange)">
              {{ metrics.avgOrderChange > 0 ? '+' : '' }}{{ metrics.avgOrderChange || 0 }}%
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div v-if="!loading && !error" class="charts-section">
        <!-- 销售排行 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>商品销售排行</h3>
          </div>
          <div class="chart-container">
            <div class="ranking-list">
              <div 
                v-for="(product, index) in topProducts" 
                :key="product.id"
                class="ranking-item"
              >
                <div class="rank-number" :class="getRankClass(index + 1)">
                  {{ index + 1 }}
                </div>
                <div class="product-info">
                  <span class="product-emoji">{{ product.image || '🥤' }}</span>
                  <div class="product-details">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-category">{{ getCategoryName(product.categoryId) }}</div>
                  </div>
                </div>
                <div class="sales-info">
                  <div class="sales-count">{{ product.salesCount || 0 }} 杯</div>
                  <div class="sales-revenue">¥{{ formatCurrency(product.revenue || 0) }}</div>
                </div>
                <div class="sales-progress">
                  <div 
                    class="progress-bar" 
                    :style="{ width: calculatePercentage(product, topProducts) + '%' }"
                  ></div>
                </div>
              </div>
              <div v-if="topProducts.length === 0" class="no-data">
                暂无销售数据
              </div>
            </div>
          </div>
        </div>

        <!-- 订单状态分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>订单状态分布</h3>
          </div>
          <div class="chart-container">
            <div class="orders-chart">
              <div 
                v-for="(item, index) in ordersData" 
                :key="index"
                class="order-chart-item"
              >
                <div class="order-info">
                  <span class="order-label">{{ getStatusText(item.status) }}</span>
                  <span class="order-count">{{ item.count || 0 }} 单</span>
                  <span class="order-percentage">{{ calculateOrderPercentage(item, ordersData) }}%</span>
                </div>
                <div class="order-progress">
                  <div 
                    class="progress-bar" 
                    :style="{ width: calculateOrderPercentage(item, ordersData) + '%' }"
                    :class="getStatusClass(item.status)"
                  ></div>
                </div>
              </div>
              <div v-if="ordersData.length === 0" class="no-data">
                暂无订单数据
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品分类统计 -->
      <div v-if="!loading && !error" class="category-stats">
        <div class="ranking-header">
          <h3>商品分类统计</h3>
        </div>
        <div class="category-list">
          <div 
            v-for="category in categoryStats" 
            :key="category.id"
            class="category-item"
          >
            <div class="category-info">
              <span class="category-color" :style="{ background: getCategoryColor(category.id) }"></span>
              <div class="category-details">
                <div class="category-name">{{ getCategoryName(category.id) }}</div>
                <div class="category-count">{{ category.productCount || 0 }} 个商品</div>
              </div>
            </div>
            <div class="category-sales">
              <div class="sales-count">{{ category.salesCount || 0 }} 杯</div>
              <div class="sales-percentage">{{ calculateCategoryPercentage(category, categoryStats) }}%</div>
            </div>
          </div>
          <div v-if="categoryStats.length === 0" class="no-data">
            暂无分类数据
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Statistics',
  data() {
    return {
      timeRange: 'week',
      loading: false,
      error: '',
      // 从后端获取的数据
      metrics: {
        totalRevenue: 0,
        totalOrders: 0,
        totalUsers: 0,
        avgOrderValue: 0,
        revenueChange: 0,
        ordersChange: 0,
        usersChange: 0,
        avgOrderChange: 0
      },
      topProducts: [],
      ordersData: [],
      categoryStats: [],
      categories: []
    }
  },
  mounted() {
    this.loadStats()
    this.loadCategories()
  },
  methods: {
    async loadStats() {
  this.loading = true
  this.error = ''
  
  try {
    const [statsRes, salesRes, ordersRes, categoriesRes] = await Promise.all([
      fetch('http://localhost:8081/api/admin/stats'),
      fetch('http://localhost:8081/api/admin/stats/sales-ranking-formatted'),
      fetch('http://localhost:8081/api/admin/stats/order-status-formatted'),
      fetch('http://localhost:8081/api/admin/stats/categories-formatted')
    ])

    if (!statsRes.ok) throw new Error('获取统计数据失败')
    if (!salesRes.ok) throw new Error('获取销售数据失败')
    if (!ordersRes.ok) throw new Error('获取订单数据失败')
    if (!categoriesRes.ok) throw new Error('获取分类数据失败')

    // 解析数据
    this.metrics = await statsRes.json()
    this.topProducts = await salesRes.json()
    this.ordersData = await ordersRes.json()
    this.categoryStats = await categoriesRes.json()

    // 添加调试日志
    console.log('分类统计数据:', this.categoryStats)
    console.log('销售排行数据:', this.topProducts)
    console.log('订单状态数据:', this.ordersData)

    // 确保数据格式正确
    this.metrics = {
      totalRevenue: this.metrics.totalRevenue || 0,
      totalOrders: this.metrics.totalOrders || 0,
      totalUsers: this.metrics.totalUsers || 0,
      avgOrderValue: this.metrics.avgOrderValue || 0,
      revenueChange: this.metrics.revenueChange || 0,
      ordersChange: this.metrics.ordersChange || 0,
      usersChange: this.metrics.usersChange || 0,
      avgOrderChange: this.metrics.avgOrderChange || 0
    }

  } catch (error) {
    console.error('加载统计数据失败:', error)
    this.error = '数据加载失败，请检查网络连接或稍后重试'
    this.useMockData()
  } finally {
    this.loading = false
  }
},

    async loadCategories() {
      try {
        const response = await fetch('http://localhost:8081/api/categories')
        if (response.ok) {
          this.categories = await response.json()
        } else {
          // 使用默认分类作为备选
          this.categories = [
            { id: 1, name: '经典系列', color: '#ff6b6b' },
            { id: 2, name: '果茶系列', color: '#51cf66' },
            { id: 3, name: '奶茶系列', color: '#339af0' },
            { id: 4, name: '特色系列', color: '#cc5de8' }
          ]
        }
      } catch (error) {
        console.error('加载分类数据失败:', error)
        this.categories = [
          { id: 1, name: '经典系列', color: '#ff6b6b' },
          { id: 2, name: '果茶系列', color: '#51cf66' },
          { id: 3, name: '奶茶系列', color: '#339af0' },
          { id: 4, name: '特色系列', color: '#cc5de8' }
        ]
      }
    },

    useMockData() {
      // 模拟数据作为备选
      this.metrics = {
        totalRevenue: 12450,
        totalOrders: 892,
        totalUsers: 156,
        avgOrderValue: 42.5,
        revenueChange: 12.5,
        ordersChange: 8.2,
        usersChange: 5.7,
        avgOrderChange: 3.8
      }

      this.topProducts = [
        { id: 1, name: '珍珠奶茶', categoryId: 1, salesCount: 156, revenue: 2808, image: '⚫' },
        { id: 2, name: '抹茶拿铁', categoryId: 3, salesCount: 134, revenue: 3082, image: '🍵' },
        { id: 3, name: '芋圆奶茶', categoryId: 1, salesCount: 112, revenue: 2240, image: '🟣' },
        { id: 4, name: '草莓果茶', categoryId: 2, salesCount: 89, revenue: 2136, image: '🍓' }
      ]

      this.ordersData = [
        { status: 'completed', count: 645 },
        { status: 'processing', count: 124 },
        { status: 'pending', count: 78 },
        { status: 'cancelled', count: 45 }
      ]

      this.categoryStats = [
        { id: 1, productCount: 12, salesCount: 345 },
        { id: 2, productCount: 8, salesCount: 234 },
        { id: 3, productCount: 15, salesCount: 456 },
        { id: 4, productCount: 6, salesCount: 123 }
      ]
    },

    getChangeClass(change) {
      return change >= 0 ? 'positive' : 'negative'
    },

    getStatusClass(status) {
      const classMap = {
        'completed': 'completed',
        'processing': 'processing',
        'pending': 'pending',
        'cancelled': 'cancelled'
      }
      return classMap[status] || ''
    },

    getStatusText(status) {
      const statusMap = {
        'completed': '已完成',
        'processing': '制作中',
        'pending': '待处理',
        'cancelled': '已取消'
      }
      return statusMap[status] || status
    },

    getRankClass(rank) {
      if (rank === 1) return 'rank-gold'
      if (rank === 2) return 'rank-silver'
      if (rank === 3) return 'rank-bronze'
      return 'rank-other'
    },

    getCategoryName(categoryId) {
  const categoryMap = {
    'classic': '经典系列',
    'milktea': '奶茶系列', 
    'fruit': '果茶系列',
    'special': '特色系列',
    'recommend': '推荐系列',
    'festival': '节日系列',
    'weather': '季节系列'
  };
  return categoryMap[categoryId] || categoryId;
},

getCategoryColor(categoryId) {
  const colorMap = {
    'classic': '#ff6b6b',
    'milktea': '#339af0',
    'fruit': '#51cf66', 
    'special': '#cc5de8',
    'recommend': '#f59f00',
    'festival': '#6741d9',
    'weather': '#20c997'
  };
  return colorMap[categoryId] || '#666';
},

    calculatePercentage(product, products) {
      if (!products.length) return 0
      const maxSales = Math.max(...products.map(p => p.salesCount || 0))
      return maxSales > 0 ? ((product.salesCount || 0) / maxSales) * 100 : 0
    },

    calculateOrderPercentage(item, orders) {
      const total = orders.reduce((sum, order) => sum + (order.count || 0), 0)
      return total > 0 ? Math.round(((item.count || 0) / total) * 100) : 0
    },

    calculateCategoryPercentage(category, categories) {
  const total = categories.reduce((sum, cat) => sum + (cat.salesCount || 0), 0);
  return total > 0 ? Math.round(((category.salesCount || 0) / total) * 100) : 0;
},

    formatCurrency(amount) {
      if (typeof amount === 'number') {
        return amount.toFixed(2)
      }
      if (typeof amount === 'string') {
        return parseFloat(amount).toFixed(2)
      }
      return '0.00'
    }
  }
}
</script>

<style scoped>
.statistics {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-header h2 {
  color: #333;
  margin: 0;
}

.time-filter {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.refresh-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.refresh-btn:hover {
  background: #0056b3;
}

.stats-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 加载和错误状态 */
.loading-state, .error-state {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  margin-bottom: 20px;
}

.loading-text {
  color: #666;
}

.error-text {
  color: #dc3545;
  margin-bottom: 10px;
  display: block;
}

.retry-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.retry-btn:hover {
  background: #0056b3;
}

/* 核心指标卡片 */
.metrics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.metric-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.metric-icon.revenue {
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
}

.metric-icon.orders {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.metric-icon.users {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
}

.metric-icon.avg {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.metric-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 6px;
}

.metric-change {
  font-size: 12px;
  font-weight: 600;
}

.metric-change.positive {
  color: #28a745;
}

.metric-change.negative {
  color: #dc3545;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.chart-header {
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.chart-container {
  height: 300px;
  overflow-y: auto;
}

/* 销售排行 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.3s ease;
}

.ranking-item:hover {
  background: #e9ecef;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  font-size: 14px;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ff6b00);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #808080);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #8c5310);
}

.rank-other {
  background: #6c757d;
}

.product-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-emoji {
  font-size: 24px;
}

.product-details {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.product-category {
  font-size: 12px;
  color: #666;
}

.sales-info {
  text-align: right;
  min-width: 80px;
}

.sales-count {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.sales-revenue {
  font-size: 12px;
  color: #666;
}

.sales-progress {
  width: 100px;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* 订单统计图 */
.orders-chart {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-chart-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.order-label {
  color: #333;
  font-weight: 500;
}

.order-count {
  color: #666;
}

.order-percentage {
  color: #333;
  font-weight: 600;
}

.order-progress {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar.completed {
  background: #28a745;
}

.progress-bar.processing {
  background: #ffc107;
}

.progress-bar.pending {
  background: #17a2b8;
}

.progress-bar.cancelled {
  background: #dc3545;
}

/* 分类统计 */
.category-stats {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.ranking-header {
  margin-bottom: 20px;
}

.ranking-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.3s ease;
}

.category-item:hover {
  background: #e9ecef;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.category-details {
  display: flex;
  flex-direction: column;
}

.category-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.category-count {
  font-size: 12px;
  color: #666;
}

.category-sales {
  text-align: right;
}

.sales-count {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.sales-percentage {
  font-size: 12px;
  color: #666;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .metrics-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .metrics-cards {
    grid-template-columns: 1fr;
  }
}
</style>