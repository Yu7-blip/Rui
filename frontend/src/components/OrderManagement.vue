<template>
  <div class="order-management">
    <div class="management-header">
      <h2>订单管理</h2>
      <div class="header-actions">
        <div class="filters">
          <select v-model="statusFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待处理</option>
            <option value="processing">制作中</option>
            <option value="completed">已完成</option>
            <option value="cancelled">已取消</option>
          </select>
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索订单号、用户名..."
            class="search-input"
          >
        </div>
        <button class="refresh-btn" @click="loadOrders">刷新</button>
      </div>
    </div>

    <div class="management-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <span class="loading-text">订单数据加载中...</span>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-state">
        <span class="error-text">{{ error }}</span>
        <button @click="loadOrders" class="retry-btn">重试</button>
      </div>

      <!-- 订单列表 -->
      <div v-if="!loading && !error" class="order-list">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>商品详情</th>
                <th>总金额</th>
                <th>下单时间</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredOrders" :key="order.id">
                <td class="order-number">{{ order.orderNumber }}</td>
                <td>
                  <div class="user-info">
                    <span class="user-avatar">👤</span>
                    {{ order.username || '未知用户' }}
                  </div>
                </td>
                <td>
                  <div class="order-items">
                    <div 
                      v-for="item in order.items" 
                      :key="item.id"
                      class="order-item"
                    >
                      {{ item.name }} × {{ item.quantity }}
                    </div>
                  </div>
                </td>
                <td class="order-amount">¥{{ formatCurrency(order.totalAmount) }}</td>
                <td class="order-time">{{ formatDateTime(order.orderTime) }}</td>
                <td>
                  <span class="status-badge" :class="order.status">
                    {{ getStatusText(order.status) }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button 
                      class="btn-view" 
                      @click="viewOrderDetail(order)"
                      title="查看详情"
                    >
                      详情
                    </button>
                    <button 
                      v-if="order.status === 'pending'"
                      class="btn-process" 
                      @click="updateOrderStatus(order, 'processing')"
                      :disabled="updatingOrder === order.id"
                      title="开始制作"
                    >
                      {{ updatingOrder === order.id ? '处理中...' : '接单' }}
                    </button>
                    <button 
                      v-if="order.status === 'processing'"
                      class="btn-complete" 
                      @click="updateOrderStatus(order, 'completed')"
                      :disabled="updatingOrder === order.id"
                      title="完成订单"
                    >
                      {{ updatingOrder === order.id ? '处理中...' : '完成' }}
                    </button>
                    <button 
                      v-if="order.status === 'pending' || order.status === 'processing'"
                      class="btn-cancel" 
                      @click="updateOrderStatus(order, 'cancelled')"
                      :disabled="updatingOrder === order.id"
                      title="取消订单"
                    >
                      {{ updatingOrder === order.id ? '处理中...' : '取消' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredOrders.length === 0">
                <td colspan="7" class="no-data">
                  {{ searchKeyword || statusFilter ? '未找到匹配的订单' : '暂无订单数据' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="filteredOrders.length > 0" class="pagination">
          <button 
            :disabled="currentPage === 1" 
            @click="changePage(currentPage - 1)"
            class="page-btn"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ currentPage }} 页，共 {{ totalPages }} 页
          </span>
          <button 
            :disabled="currentPage === totalPages" 
            @click="changePage(currentPage + 1)"
            class="page-btn"
          >
            下一页
          </button>
        </div>
      </div>

      <!-- 订单详情弹窗 -->
      <div v-if="showOrderDetail" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>订单详情</h3>
            <button class="close-btn" @click="closeModal">×</button>
          </div>
          <div class="modal-body">
            <div v-if="selectedOrder" class="order-detail">
              <div class="detail-section">
                <h4>基本信息</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>订单号:</label>
                    <span>{{ selectedOrder.orderNumber }}</span>
                  </div>
                  <div class="detail-item">
                    <label>用户:</label>
                    <span>{{ selectedOrder.username || '未知用户' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>下单时间:</label>
                    <span>{{ formatDateTime(selectedOrder.orderTime) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>订单状态:</label>
                    <span class="status-badge" :class="selectedOrder.status">
                      {{ getStatusText(selectedOrder.status) }}
                    </span>
                  </div>
                  <div class="detail-item">
                    <label>总金额:</label>
                    <span class="amount">¥{{ formatCurrency(selectedOrder.totalAmount) }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>商品清单</h4>
                <div class="items-list">
                  <div 
                    v-for="item in selectedOrder.items" 
                    :key="item.id"
                    class="item-row"
                  >
                    <div class="item-info">
                      <span class="item-name">{{ item.name }}</span>
                      <span class="item-price">¥{{ formatCurrency(item.price) }}</span>
                    </div>
                    <div class="item-quantity">× {{ item.quantity }}</div>
                    <div class="item-total">¥{{ formatCurrency(item.price * item.quantity) }}</div>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>订单操作</h4>
                <div class="action-buttons">
                  <button 
                    v-if="selectedOrder.status === 'pending'"
                    class="btn-process" 
                    @click="updateOrderStatus(selectedOrder, 'processing')"
                    :disabled="updatingOrder === selectedOrder.id"
                  >
                    {{ updatingOrder === selectedOrder.id ? '处理中...' : '接单制作' }}
                  </button>
                  <button 
                    v-if="selectedOrder.status === 'processing'"
                    class="btn-complete" 
                    @click="updateOrderStatus(selectedOrder, 'completed')"
                    :disabled="updatingOrder === selectedOrder.id"
                  >
                    {{ updatingOrder === selectedOrder.id ? '处理中...' : '完成订单' }}
                  </button>
                  <button 
                    v-if="selectedOrder.status === 'pending' || selectedOrder.status === 'processing'"
                    class="btn-cancel" 
                    @click="updateOrderStatus(selectedOrder, 'cancelled')"
                    :disabled="updatingOrder === selectedOrder.id"
                  >
                    {{ updatingOrder === selectedOrder.id ? '处理中...' : '取消订单' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  data() {
    return {
      searchKeyword: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 8,
      showOrderDetail: false,
      selectedOrder: null,
      loading: false,
      error: '',
      updatingOrder: null,
      // 从后端获取的订单数据
      orders: []
    }
  },
  computed: {
    filteredOrders() {
      let filtered = this.orders
      
      // 状态过滤
      if (this.statusFilter) {
        filtered = filtered.filter(order => order.status === this.statusFilter)
      }
      
      // 搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(order => 
          order.orderNumber.toLowerCase().includes(keyword) ||
          (order.username && order.username.toLowerCase().includes(keyword))
        )
      }
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return filtered.slice(start, end)
    },
    totalPages() {
      const total = this.orders.filter(order => {
        const statusMatch = this.statusFilter ? order.status === this.statusFilter : true
        const searchMatch = this.searchKeyword 
          ? order.orderNumber.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
            (order.username && order.username.toLowerCase().includes(this.searchKeyword.toLowerCase()))
          : true
        return statusMatch && searchMatch
      }).length
      return Math.ceil(total / this.pageSize)
    }
  },
  mounted() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      this.error = ''
      
      try {
        const response = await fetch('http://localhost:8081/api/admin/orders')
        if (!response.ok) {
          throw new Error('获取订单数据失败')
        }
        const ordersData = await response.json()
        
        // 转换数据格式
        this.orders = ordersData.map(order => ({
          id: order.id,
          orderNumber: order.orderNumber,
          username: order.username,
          totalAmount: order.totalAmount || 0,
          orderTime: order.orderTime,
          status: order.status || 'pending',
          items: order.items || []
        }))
        
      } catch (error) {
        console.error('加载订单数据失败:', error)
        this.error = '数据加载失败，请检查网络连接或稍后重试'
      } finally {
        this.loading = false
      }
    },

    async updateOrderStatus(order, newStatus) {
      this.updatingOrder = order.id
      try {
        const response = await fetch(`http://localhost:8081/api/admin/orders/${order.id}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ status: newStatus })
        })

        if (!response.ok) {
          const result = await response.json()
          throw new Error(result.message || '更新订单状态失败')
        }

        // 更新本地状态
        order.status = newStatus
        const statusText = this.getStatusText(newStatus)
        this.$message.success(`订单 ${order.orderNumber} 已${statusText}`)
        
        // 关闭弹窗
        if (this.showOrderDetail) {
          this.closeModal()
        }

      } catch (error) {
        console.error('更新订单状态失败:', error)
        this.$message.error(error.message || '更新订单状态失败')
      } finally {
        this.updatingOrder = null
      }
    },

    changePage(page) {
      this.currentPage = page
    },

    getStatusText(status) {
      const statusMap = {
        pending: '待处理',
        processing: '制作中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || '未知'
    },

    viewOrderDetail(order) {
      this.selectedOrder = order
      this.showOrderDetail = true
    },

    closeModal() {
      this.showOrderDetail = false
      this.selectedOrder = null
    },

    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      try {
        const date = new Date(dateTime)
        return date.toLocaleString('zh-CN')
      } catch {
        return dateTime
      }
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
.order-management {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.management-header h2 {
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filters {
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

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px;
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

.management-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.order-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-container {
  overflow-x: auto;
  flex: 1;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e1e5e9;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table tbody tr:hover {
  background: #f8f9fa;
}

.order-number {
  font-weight: 600;
  color: #007bff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  font-size: 16px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-item {
  padding: 4px 8px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;
}

.order-amount {
  font-weight: 600;
  color: #e74c3c;
}

.order-time {
  color: #666;
  font-size: 14px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.processing {
  background: #cce7ff;
  color: #004085;
}

.status-badge.completed {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.btn-view, .btn-process, .btn-complete, .btn-cancel {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-view {
  background: #17a2b8;
  color: white;
}

.btn-process {
  background: #ffc107;
  color: #212529;
}

.btn-complete {
  background: #28a745;
  color: white;
}

.btn-cancel {
  background: #dc3545;
  color: white;
}

.btn-view:hover { background: #138496; }
.btn-process:hover { background: #e0a800; }
.btn-complete:hover { background: #218838; }
.btn-cancel:hover { background: #c82333; }

.action-buttons button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.no-data {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 15px;
  border-top: 1px solid #e1e5e9;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-info {
  color: #666;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.detail-item span {
  color: #333;
}

.detail-item .amount {
  font-weight: 600;
  color: #e74c3c;
  font-size: 16px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.item-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-weight: 500;
}

.item-price {
  color: #666;
  font-size: 14px;
}

.item-quantity {
  margin: 0 15px;
  color: #666;
}

.item-total {
  font-weight: 600;
  color: #e74c3c;
  min-width: 60px;
  text-align: right;
}

.detail-section .action-buttons {
  display: flex;
  gap: 10px;
}

.detail-section .btn-process,
.detail-section .btn-complete,
.detail-section .btn-cancel {
  padding: 8px 16px;
  font-size: 14px;
}
</style>