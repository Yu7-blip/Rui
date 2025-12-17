<template>
  <div class="admin-dashboard">
    <!-- 顶部导航 -->
    <div class="admin-header">
      <div class="header-left">
        <h1>奶茶点餐管理系统</h1>
        <span class="admin-welcome">管理员：{{ adminName }}</span>
      </div>
      <div class="header-right">
        <button @click="handleLogout" class="logout-btn">退出系统</button>
      </div>
    </div>

    <!-- 侧边栏和主内容 -->
    <div class="admin-container">
      <!-- 侧边栏菜单 -->
      <div class="admin-sidebar">
        <nav class="sidebar-nav">
          <ul>
            <li 
              v-for="item in menuItems" 
              :key="item.key"
              :class="{ active: activeMenu === item.key }"
              @click="switchMenu(item.key)"
            >
              <span class="menu-icon">{{ item.icon }}</span>
              <span class="menu-text">{{ item.name }}</span>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 主内容区域 -->
      <div class="admin-main">
        <!-- 数据概览 -->
        <div v-if="activeMenu === 'dashboard'" class="dashboard-overview">
          <h2>数据概览</h2>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <span class="loading-text">数据加载中...</span>
          </div>

          <!-- 错误状态 -->
          <div v-if="error" class="error-state">
            <span class="error-text">{{ error }}</span>
            <button @click="loadDashboardData" class="retry-btn">重试</button>
          </div>

          <!-- 数据内容 -->
          <div v-if="!loading && !error">
            <div class="stats-cards">
              <div class="stat-card">
                <div class="stat-icon">👥</div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.totalUsers }}</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">📦</div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.totalOrders }}</div>
                  <div class="stat-label">总订单数</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">💰</div>
                <div class="stat-info">
                  <div class="stat-number">¥{{ formatCurrency(stats.totalRevenue) }}</div>
                  <div class="stat-label">总收入</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">🥤</div>
                <div class="stat-info">
                  <div class="stat-number">{{ stats.totalProducts }}</div>
                  <div class="stat-label">商品数量</div>
                </div>
              </div>
            </div>

            <!-- 最近订单 -->
            <div class="recent-orders">
              <div class="section-header">
                <h3>最近订单</h3>
                <button @click="loadRecentOrders" class="refresh-btn" :disabled="loadingOrders">
                  {{ loadingOrders ? '加载中...' : '刷新' }}
                </button>
              </div>
              <div class="orders-list">
                <div 
                  v-for="order in recentOrders" 
                  :key="order.id"
                  class="order-item"
                >
                  <div class="order-info">
                    <span class="order-number">订单号: {{ order.orderNumber }}</span>
                    <span class="order-user">用户: {{ order.username }}</span>
                    <span class="order-amount">金额: ¥{{ formatCurrency(order.totalAmount) }}</span>
                  </div>
                  <div class="order-status" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </div>
                </div>
                <div v-if="recentOrders.length === 0 && !loadingOrders" class="empty-orders">
                  暂无最近订单
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户管理 -->
        <UserManagement v-if="activeMenu === 'users'" />

        <!-- 订单管理 -->
        <OrderManagement v-if="activeMenu === 'orders'" />

        <!-- 商品管理 -->
        <ProductManagement v-if="activeMenu === 'products'" />

        <!-- 数据统计 -->
        <Statistics v-if="activeMenu === 'statistics'" />
      </div>
    </div>
  </div>
</template>

<script>
import UserManagement from './UserManagement.vue'
import OrderManagement from './OrderManagement.vue'
import ProductManagement from './ProductManagement.vue'
import Statistics from './Statistics.vue'

export default {
  name: 'AdminDashboard',
  components: {
    UserManagement,
    OrderManagement,
    ProductManagement,
    Statistics
  },
  data() {
    return {
      adminName: '管理员',
      activeMenu: 'dashboard',
      menuItems: [
        { key: 'dashboard', name: '数据概览', icon: '📊' },
        { key: 'users', name: '用户管理', icon: '👥' },
        { key: 'orders', name: '订单管理', icon: '📦' },
        { key: 'products', name: '商品管理', icon: '🥤' },
        { key: 'statistics', name: '数据统计', icon: '📈' }
      ],
      loading: false,
      loadingOrders: false,
      error: '',
      // 从后端获取的数据
      stats: {
        totalUsers: 0,
        totalOrders: 0,
        totalRevenue: 0,
        totalProducts: 0,
        avgOrderValue: 0
      },
      recentOrders: []
    }
  },
  mounted() {
    this.loadDashboardData()
  },
  methods: {
// 修改 loadDashboardData 方法：
async loadDashboardData() {
  this.loading = true
  this.error = ''
  
  try {
    // 1. 获取统计数据 - 修改路径
    const statsResponse = await fetch('http://localhost:8081/api/admin/stats')
    if (!statsResponse.ok) {
      throw new Error('获取统计数据失败')
    }
    const statsData = await statsResponse.json()
    
    // 更新统计数据
    this.stats = {
      totalUsers: statsData.totalUsers || 0,
      totalOrders: statsData.totalOrders || 0,
      totalRevenue: statsData.totalRevenue || 0,
      totalProducts: statsData.totalProducts || 0,
      avgOrderValue: statsData.avgOrderValue || 0
    }
    
    // 2. 获取最近订单 - 修改路径
    await this.loadRecentOrders()
  } catch (error) {
    console.error('加载数据失败:', error)
    this.error = '数据加载失败，请检查网络连接或稍后重试'
  } finally {
    this.loading = false
  }
},

// 修改 loadRecentOrders 方法：
async loadRecentOrders() {
  this.loadingOrders = true
  try {
    // 修改为不需要token验证的路径
    const response = await fetch('http://localhost:8081/api/admin/orders')
    if (!response.ok) {
      throw new Error('获取最近订单失败')
    }
    const orders = await response.json()
    
    // 只取前5条作为最近订单
    this.recentOrders = orders.slice(0, 5).map(order => ({
      id: order.id,
      orderNumber: order.orderNumber,
      username: order.username || '未知用户',
      totalAmount: order.totalAmount || 0,
      status: order.status || 'pending'
    }))
  } catch (error) {
    console.error('加载最近订单失败:', error)
    this.$message.error('加载最近订单失败')
  } finally {
    this.loadingOrders = false
  }
},

    async loadRecentOrders() {
      this.loadingOrders = true
      try {
        const response = await fetch('http://localhost:8080/api/admin/orders/recent?limit=5')
        if (!response.ok) {
          throw new Error('获取最近订单失败')
        }
        const orders = await response.json()
        this.recentOrders = orders.map(order => ({
          id: order.id,
          orderNumber: order.orderNumber,
          username: order.username || '未知用户',
          totalAmount: order.totalAmount || 0,
          status: order.status || 'pending'
        }))
      } catch (error) {
        console.error('加载最近订单失败:', error)
        this.$message.error('加载最近订单失败')
      } finally {
        this.loadingOrders = false
      }
    },

    switchMenu(menuKey) {
      this.activeMenu = menuKey
    },

    getStatusClass(status) {
      const statusMap = {
        pending: 'status-pending',
        processing: 'status-processing',
        completed: 'status-completed',
        cancelled: 'status-cancelled'
      }
      return statusMap[status] || 'status-pending'
    },

    getStatusText(status) {
      const statusMap = {
        pending: '待处理',
        processing: '制作中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || '待处理'
    },

    formatCurrency(amount) {
      if (typeof amount === 'number') {
        return amount.toFixed(2)
      }
      if (typeof amount === 'string') {
        return parseFloat(amount).toFixed(2)
      }
      return '0.00'
    },

    handleLogout() {
      if (confirm('确定要退出管理系统吗？')) {
        // 清除管理员登录状态
        localStorage.removeItem('adminToken')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  background: #f8f9fa;
}

.admin-header {
  background: white;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-bottom: 1px solid #e1e5e9;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.admin-welcome {
  color: #666;
  font-size: 14px;
}

.logout-btn {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  background: #c82333;
}

.admin-container {
  display: flex;
  height: calc(100vh - 70px);
}

.admin-sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e1e5e9;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-nav li {
  padding: 15px 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  border-left: 4px solid transparent;
}

.sidebar-nav li:hover {
  background: #f8f9fa;
}

.sidebar-nav li.active {
  background: #e3f2fd;
  border-left-color: #2196f3;
  color: #2196f3;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}

.menu-text {
  font-size: 14px;
  font-weight: 500;
}

.admin-main {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

/* 数据概览样式 */
.dashboard-overview h2 {
  margin-bottom: 20px;
  color: #333;
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 32px;
  margin-right: 15px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.recent-orders {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.refresh-btn {
  padding: 6px 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.refresh-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.refresh-btn:hover:not(:disabled) {
  background: #218838;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.order-info {
  display: flex;
  gap: 20px;
}

.order-number {
  font-weight: 500;
  color: #333;
}

.order-user, .order-amount {
  color: #666;
}

.order-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-processing {
  background: #cce7ff;
  color: #004085;
}

.status-completed {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.empty-orders {
  text-align: center;
  color: #666;
  padding: 20px;
}
</style>