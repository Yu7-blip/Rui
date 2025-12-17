<template>
  <div class="user-management">
    <div class="management-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索用户名、邮箱..."
            class="search-input"
            @keyup.enter="searchUsers"
          >
          <button class="search-btn" @click="searchUsers">搜索</button>
        </div>
        <button class="refresh-btn" @click="loadUsers">刷新</button>
      </div>
    </div>

    <div class="management-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <span class="loading-text">数据加载中...</span>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-state">
        <span class="error-text">{{ error }}</span>
        <button @click="loadUsers" class="retry-btn">重试</button>
      </div>

      <!-- 用户列表 -->
      <div v-if="!loading && !error" class="user-list">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>邮箱</th>
                <th>注册时间</th>
                <th>订单数量</th>
                <th>总消费</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>
                  <div class="user-info">
                    <span class="user-avatar">👤</span>
                    {{ user.username }}
                  </div>
                </td>
                <td>{{ user.email }}</td>
                <td>{{ formatDateTime(user.registerTime) }}</td>
                <td>{{ user.orderCount }}</td>
                <td>¥{{ formatCurrency(user.totalSpent) }}</td>
                <td>
                  <span class="status-badge" :class="user.status">
                    {{ user.status === 'active' ? '活跃' : '禁用' }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button 
                      class="btn-view" 
                      @click="viewUserDetail(user)"
                      title="查看详情"
                    >
                      查看
                    </button>
                    <button 
                      v-if="user.status === 'active'"
                      class="btn-disable" 
                      @click="toggleUserStatus(user)"
                      title="禁用用户"
                      :disabled="updatingUser"
                    >
                      {{ updatingUser === user.id ? '处理中...' : '禁用' }}
                    </button>
                    <button 
                      v-else
                      class="btn-enable" 
                      @click="toggleUserStatus(user)"
                      title="启用用户"
                      :disabled="updatingUser"
                    >
                      {{ updatingUser === user.id ? '处理中...' : '启用' }}
                    </button>
                    <button 
                      class="btn-delete" 
                      @click="deleteUser(user)"
                      title="删除用户"
                      :disabled="deletingUser"
                    >
                      {{ deletingUser === user.id ? '删除中...' : '删除' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredUsers.length === 0">
                <td colspan="8" class="no-data">
                  {{ searchKeyword ? '未找到匹配的用户' : '暂无用户数据' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <button 
            :disabled="currentPage === 1 || loading" 
            @click="changePage(currentPage - 1)"
            class="page-btn"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ currentPage }} 页，共 {{ totalPages }} 页
          </span>
          <button 
            :disabled="currentPage === totalPages || loading" 
            @click="changePage(currentPage + 1)"
            class="page-btn"
          >
            下一页
          </button>
        </div>
      </div>

      <!-- 用户详情弹窗 -->
      <div v-if="showUserDetail" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>用户详情</h3>
            <button class="close-btn" @click="closeModal">×</button>
          </div>
          <div class="modal-body">
            <div v-if="selectedUser" class="user-detail">
              <div class="detail-row">
                <label>用户ID:</label>
                <span>{{ selectedUser.id }}</span>
              </div>
              <div class="detail-row">
                <label>用户名:</label>
                <span>{{ selectedUser.username }}</span>
              </div>
              <div class="detail-row">
                <label>邮箱:</label>
                <span>{{ selectedUser.email }}</span>
              </div>
              <div class="detail-row">
                <label>注册时间:</label>
                <span>{{ formatDateTime(selectedUser.registerTime) }}</span>
              </div>
              <div class="detail-row">
                <label>订单数量:</label>
                <span>{{ selectedUser.orderCount }}</span>
              </div>
              <div class="detail-row">
                <label>总消费:</label>
                <span>¥{{ formatCurrency(selectedUser.totalSpent) }}</span>
              </div>
              <div class="detail-row">
                <label>最近订单:</label>
                <div class="recent-orders">
                  <div 
                    v-for="order in selectedUser.recentOrders" 
                    :key="order.id"
                    class="recent-order"
                  >
                    {{ order.productName }} - ¥{{ formatCurrency(order.amount) }}
                  </div>
                  <div v-if="selectedUser.recentOrders.length === 0" class="no-orders">
                    暂无最近订单
                  </div>
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
  name: 'UserManagement',
  data() {
    return {
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      showUserDetail: false,
      selectedUser: null,
      loading: false,
      error: '',
      updatingUser: null, // 正在更新的用户ID
      deletingUser: null, // 正在删除的用户ID
      // 从后端获取的用户数据
      users: []
    }
  },
  computed: {
    filteredUsers() {
      let filtered = this.users
      
      // 搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(user => 
          user.username.toLowerCase().includes(keyword) ||
          user.email.toLowerCase().includes(keyword)
        )
      }
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return filtered.slice(start, end)
    },
    totalPages() {
      const total = this.searchKeyword 
        ? this.users.filter(user => 
            user.username.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
            user.email.toLowerCase().includes(this.searchKeyword.toLowerCase())
          ).length
        : this.users.length
      return Math.ceil(total / this.pageSize)
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      this.error = ''
      
      try {
        const response = await fetch('http://localhost:8081/api/admin/users')
        if (!response.ok) {
          throw new Error('获取用户数据失败')
        }
        const usersData = await response.json()
        
        // 转换数据格式
        this.users = usersData.map(user => ({
          id: user.id,
          username: user.username,
          email: user.email,
          registerTime: user.registerTime,
          orderCount: user.orderCount || 0,
          totalSpent: user.totalSpent || 0,
          status: 'active', // 后端暂时没有状态字段，默认为活跃
          recentOrders: user.recentOrders || []
        }))
        
      } catch (error) {
        console.error('加载用户数据失败:', error)
        this.error = '数据加载失败，请检查网络连接或稍后重试'
      } finally {
        this.loading = false
      }
    },

    async searchUsers() {
      // 重新加载所有用户数据，在前端进行搜索
      await this.loadUsers()
      this.currentPage = 1 // 搜索后回到第一页
    },

    async toggleUserStatus(user) {
      this.updatingUser = user.id
      try {
        // 模拟API调用 - 实际应该调用后端接口
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 更新本地状态
        const userIndex = this.users.findIndex(u => u.id === user.id)
        if (userIndex !== -1) {
          this.users[userIndex].status = user.status === 'active' ? 'inactive' : 'active'
          this.$message.success(`用户 ${user.username} 已${user.status === 'active' ? '禁用' : '启用'}`)
        }
      } catch (error) {
        console.error('更新用户状态失败:', error)
        this.$message.error('更新用户状态失败')
      } finally {
        this.updatingUser = null
      }
    },

    async deleteUser(user) {
      if (!confirm(`确定要删除用户 ${user.username} 吗？此操作不可恢复。`)) {
        return
      }

      this.deletingUser = user.id
      try {
        const response = await fetch(`http://localhost:8081/api/admin/users/${user.id}`, {
          method: 'DELETE'
        })

        if (!response.ok) {
          const result = await response.json()
          throw new Error(result.message || '删除用户失败')
        }

        // 从本地列表中移除
        this.users = this.users.filter(u => u.id !== user.id)
        this.$message.success('用户删除成功')
        
      } catch (error) {
        console.error('删除用户失败:', error)
        this.$message.error(error.message || '删除用户失败')
      } finally {
        this.deletingUser = null
      }
    },

    refreshData() {
      this.loadUsers()
      this.$message.success('数据已刷新')
    },

    changePage(page) {
      this.currentPage = page
    },

    viewUserDetail(user) {
      this.selectedUser = user
      this.showUserDetail = true
    },

    closeModal() {
      this.showUserDetail = false
      this.selectedUser = null
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
/* 原有的样式保持不变，只添加新的样式 */

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

.no-orders {
  color: #999;
  font-style: italic;
}

.action-buttons button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 其他原有样式保持不变 */
.user-management {
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

.search-box {
  display: flex;
  gap: 8px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px;
}

.search-btn, .refresh-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.search-btn:hover, .refresh-btn:hover {
  background: #0056b3;
}

.management-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  font-size: 16px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.btn-view, .btn-enable, .btn-disable, .btn-delete {
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

.btn-enable {
  background: #28a745;
  color: white;
}

.btn-disable {
  background: #ffc107;
  color: #212529;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-view:hover { background: #138496; }
.btn-enable:hover { background: #218838; }
.btn-disable:hover { background: #e0a800; }
.btn-delete:hover { background: #c82333; }

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 15px;
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
  width: 500px;
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

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.detail-row label {
  font-weight: 600;
  color: #333;
  min-width: 80px;
}

.detail-row span {
  color: #666;
  text-align: right;
}

.recent-orders {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-order {
  padding: 6px 10px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 14px;
}
</style>