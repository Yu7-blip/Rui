<template>
  <div class="product-management">
    <div class="management-header">
      <h2>商品管理</h2>
      <div class="header-actions">
        <div class="filters">
          <select v-model="categoryFilter" class="filter-select">
            <option value="">全部分类</option>
            <option 
              v-for="category in categories" 
              :key="category.id"
              :value="category.id"
            >
              {{ category.name }}
            </option>
          </select>
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索商品名称..."
            class="search-input"
          >
        </div>
        <button class="add-btn" @click="showAddProduct">添加商品</button>
        <button class="refresh-btn" @click="loadProducts">刷新</button>
      </div>
    </div>

    <div class="management-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <span class="loading-text">商品数据加载中...</span>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-state">
        <span class="error-text">{{ error }}</span>
        <button @click="loadProducts" class="retry-btn">重试</button>
      </div>

      <!-- 商品列表 -->
      <div v-if="!loading && !error" class="product-list">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>商品图片</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>价格</th>
                <th>库存</th>
                <th>销量</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in filteredProducts" :key="product.id">
                <td class="product-id">{{ product.id }}</td>
                <td>
                  <div class="product-image">
                    <span class="product-emoji">{{ product.image || '🥤' }}</span>
                  </div>
                </td>
                <td>
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-description">{{ product.description }}</div>
                    <div class="product-tags">
                      <span 
                        v-for="tag in product.tags" 
                        :key="tag"
                        class="product-tag"
                      >
                        {{ tag }}
                      </span>
                    </div>
                  </div>
                </td>
<td>
  <span class="category-badge" :style="{ background: getCategoryColor(product.category) }">
    {{ getCategoryName(product.category) }}
  </span>
</td>
                <td class="product-price">¥{{ formatCurrency(product.price) }}</td>
                <td>
                  <span :class="{ 'low-stock': product.stock < 10 }">
                    {{ product.stock }}
                  </span>
                </td>
                <td class="sales-count">{{ product.salesCount || 0 }}</td>
                <td>
                  <span class="status-badge" :class="product.available ? 'active' : 'inactive'">
                    {{ product.available ? '上架' : '下架' }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button 
                      class="btn-edit" 
                      @click="editProduct(product)"
                      title="编辑商品"
                    >
                      编辑
                    </button>
                    <button 
                      v-if="product.available"
                      class="btn-disable" 
                      @click="toggleProductStatus(product)"
                      :disabled="updatingProduct === product.id"
                      title="下架商品"
                    >
                      {{ updatingProduct === product.id ? '处理中...' : '下架' }}
                    </button>
                    <button 
                      v-else
                      class="btn-enable" 
                      @click="toggleProductStatus(product)"
                      :disabled="updatingProduct === product.id"
                      title="上架商品"
                    >
                      {{ updatingProduct === product.id ? '处理中...' : '上架' }}
                    </button>
                    <button 
                      class="btn-delete" 
                      @click="deleteProduct(product)"
                      :disabled="deletingProduct === product.id"
                      title="删除商品"
                    >
                      {{ deletingProduct === product.id ? '删除中...' : '删除' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredProducts.length === 0">
                <td colspan="9" class="no-data">
                  {{ searchKeyword || categoryFilter ? '未找到匹配的商品' : '暂无商品数据' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="filteredProducts.length > 0" class="pagination">
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

      <!-- 添加/编辑商品弹窗 -->
      <div v-if="showProductModal" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ isEditing ? '编辑商品' : '添加商品' }}</h3>
            <button class="close-btn" @click="closeModal">×</button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveProduct" class="product-form">
              <div class="form-row">
                <div class="form-group">
                  <label>商品名称 *</label>
                  <input 
                    v-model="formData.name" 
                    type="text" 
                    required
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>分类 *</label>
                  <select v-model="formData.categoryId" required class="form-select">
                    <option value="">请选择分类</option>
                    <option 
                      v-for="category in categories" 
                      :key="category.id"
                      :value="category.id"
                    >
                      {{ category.name }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>价格 *</label>
                  <input 
                    v-model.number="formData.price" 
                    type="number" 
                    step="0.01"
                    min="0"
                    required
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>库存 *</label>
                  <input 
                    v-model.number="formData.stock" 
                    type="number" 
                    min="0"
                    required
                    class="form-input"
                  >
                </div>
              </div>

              <div class="form-group">
                <label>商品描述</label>
                <textarea 
                  v-model="formData.description" 
                  rows="3"
                  class="form-textarea"
                ></textarea>
              </div>

              <div class="form-group">
                <label>标签</label>
                <div class="tags-input">
                  <input 
                    v-model="newTag" 
                    type="text" 
                    placeholder="输入标签后按回车添加"
                    @keydown.enter.prevent="addTag"
                    class="form-input"
                  >
                  <div class="tags-list">
                    <span 
                      v-for="(tag, index) in formData.tags" 
                      :key="index"
                      class="tag"
                    >
                      {{ tag }}
                      <button type="button" @click="removeTag(index)" class="tag-remove">
                        ×
                      </button>
                    </span>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label>商品图标</label>
                <input 
                  v-model="formData.image" 
                  type="text" 
                  placeholder="输入表情符号，如: 🥤"
                  class="form-input"
                >
              </div>

              <div class="form-actions">
                <button type="button" @click="closeModal" class="btn-cancel">
                  取消
                </button>
                <button type="submit" class="btn-save" :disabled="savingProduct">
                  {{ savingProduct ? '保存中...' : (isEditing ? '更新' : '添加') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductManagement',
  data() {
    return {
      searchKeyword: '',
      categoryFilter: '',
      currentPage: 1,
      pageSize: 100,
      showProductModal: false,
      isEditing: false,
      newTag: '',
      loading: false,
      error: '',
      updatingProduct: null,
      deletingProduct: null,
      savingProduct: false,
      // 表单数据
      formData: {
        id: null,
        name: '',
        categoryId: '',
        price: 0,
        stock: 0,
        description: '',
        tags: [],
        image: '🥤',
        available: true
      },
      // 从后端获取的数据
      categories: [],
      products: []
    }
  },
  computed: {
    filteredProducts() {
      let filtered = this.products
      
      // 分类过滤
      if (this.categoryFilter) {
        filtered = filtered.filter(product => product.categoryId === this.categoryFilter)
      }
      
      // 搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(product => 
          product.name.toLowerCase().includes(keyword) ||
          (product.description && product.description.toLowerCase().includes(keyword)) ||
          (product.tags && product.tags.some(tag => tag.toLowerCase().includes(keyword)))
        )
      }
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return filtered.slice(start, end)
    },
    totalPages() {
      const total = this.products.filter(product => {
        const categoryMatch = this.categoryFilter ? product.categoryId === this.categoryFilter : true
        const searchMatch = this.searchKeyword 
          ? product.name.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
            (product.description && product.description.toLowerCase().includes(this.searchKeyword.toLowerCase())) ||
            (product.tags && product.tags.some(tag => tag.toLowerCase().includes(this.searchKeyword.toLowerCase())))
          : true
        return categoryMatch && searchMatch
      }).length
      return Math.ceil(total / this.pageSize)
    }
  },
  mounted() {
    this.loadProducts()
    this.loadCategories()
  },
  methods: {
async loadProducts() {
  this.loading = true
  this.error = ''
  
  try {
    const response = await fetch('http://localhost:8081/api/products')
    if (!response.ok) {
      throw new Error('获取商品数据失败')
    }
    const productsData = await response.json()
    
    console.log('📦 加载到的商品数据:', productsData)
    
    // 转换数据格式
    this.products = productsData.map(product => {
      console.log(`商品: ${product.name}, 分类: ${product.category}`)
      
      return {
        id: product.id,
        name: product.name,
        categoryId: product.category, // 保留原始分类字段，方便编辑时使用
        category: product.category,   // 添加category字段用于显示
        price: product.price,
        stock: product.stock || 100,
        salesCount: product.salesCount || 0,
        description: product.description,
        tags: product.tags ? product.tags.split(',') : [],
        image: product.emoji || '🥤',
        available: product.available !== false
      }
    })
    
  } catch (error) {
    console.error('加载商品数据失败:', error)
    this.error = '数据加载失败，请检查网络连接或稍后重试'
  } finally {
    this.loading = false
  }
},

async loadCategories() {
  try {
    const response = await fetch('http://localhost:8081/api/categories')
    if (response.ok) {
      const categoriesData = await response.json()
      
      // 🚨 确保分类顺序和Welcome.vue一致
      const categoryOrder = {
        'recommend': 1,
        'weather': 2,
        'festival': 3,
        'classic': 4,
        'fruit': 5,
        'milktea': 6,
        'special': 7,
        'ai': 8,
        'region': 9,
        'festival-recommend': 10
      }
      
      // 按指定顺序排序
      this.categories = categoriesData
        .sort((a, b) => {
          const orderA = categoryOrder[a.name] || 99;
          const orderB = categoryOrder[b.name] || 99;
          return orderA - orderB;
        })
        .map(category => ({
          id: categoryOrder[category.name] || category.id,
          name: category.displayName || category.name,
          originalName: category.name,
          color: this.getRandomColor()
        }))
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
    // 使用和Welcome.vue一致的默认分类
    this.categories = [
      { id: 1, name: '推荐', originalName: 'recommend', color: '#ff6b6b' },
      { id: 2, name: '天气特饮', originalName: 'weather', color: '#51cf66' },
      { id: 3, name: '节日限定', originalName: 'festival', color: '#339af0' },
      { id: 4, name: '经典系列', originalName: 'classic', color: '#cc5de8' },
      { id: 5, name: '果茶系列', originalName: 'fruit', color: '#ff922b' },
      { id: 6, name: '奶茶系列', originalName: 'milktea', color: '#20c997' },
      { id: 7, name: '特色系列', originalName: 'special', color: '#868e96' },
      { id: 8, name: 'AI推荐', originalName: 'ai', color: '#ffd43b' },
      { id: 9, name: '地域特色', originalName: 'region', color: '#9775fa' },
      { id: 10, name: '节日推荐', originalName: 'festival-recommend', color: '#f783ac' }
    ]
  }
},

async saveProduct() {
  this.savingProduct = true
  try {
    const url = this.isEditing 
      ? `http://localhost:8081/api/products/${this.formData.id}`
      : 'http://localhost:8081/api/products'
    
    const method = this.isEditing ? 'PUT' : 'POST'
    
    // 🚨 关键修复：直接使用分类ID对应的分类名映射
    const categoryMap = {
      1: 'recommend',      // 推荐
      2: 'weather',        // 天气特饮
      3: 'festival',       // 节日限定
      4: 'classic',        // 经典系列
      5: 'fruit',          // 果茶系列
      6: 'milktea',        // 奶茶系列
      7: 'special',        // 特色系列
      8: 'ai',             // AI推荐（如果需要）
      9: 'region',         // 地域特色（如果需要）
      10: 'festival-recommend' // 节日推荐（如果需要）
    }
    
    // 获取对应的英文分类名
    const categoryName = categoryMap[this.formData.categoryId] || ''
    
    if (!categoryName) {
      throw new Error('请选择有效的分类')
    }
    
    console.log('🎯 保存商品分类信息:');
    console.log('分类ID:', this.formData.categoryId);
    console.log('映射的分类名:', categoryName);
    
    // 准备提交数据
    const submitData = {
      name: this.formData.name,
      category: categoryName, // 使用映射后的英文分类名
      price: this.formData.price,
      stock: this.formData.stock,
      description: this.formData.description,
      tags: this.formData.tags.join(','),
      emoji: this.formData.image,
      available: this.formData.available
    }
    
    // 如果是编辑，添加ID
    if (this.isEditing) {
      submitData.id = this.formData.id
    }
    
    console.log('📤 提交的数据:', submitData);
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(submitData)
    })
    
    if (!response.ok) {
      const result = await response.json()
      throw new Error(result.message || '保存商品失败')
    }
    
    // 重新加载数据
    await this.loadProducts()
    this.$message.success(this.isEditing ? '商品更新成功' : '商品添加成功')
    this.closeModal()
    
  } catch (error) {
    console.error('保存商品失败:', error)
    this.$message.error(error.message || '保存商品失败')
  } finally {
    this.savingProduct = false
  }
},

    async toggleProductStatus(product) {
      this.updatingProduct = product.id
      try {
        const newStatus = !product.available
        const response = await fetch(`http://localhost:8081/api/products/${product.id}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ available: newStatus })
        })

        if (!response.ok) {
          throw new Error('更新商品状态失败')
        }

        // 更新本地状态
        product.available = newStatus
        this.$message.success(`商品 ${product.name} 已${newStatus ? '上架' : '下架'}`)
        
      } catch (error) {
        console.error('更新商品状态失败:', error)
        this.$message.error('更新商品状态失败')
      } finally {
        this.updatingProduct = null
      }
    },

    async deleteProduct(product) {
      if (!confirm(`确定要删除商品 "${product.name}" 吗？此操作不可恢复。`)) {
        return
      }

      this.deletingProduct = product.id
      try {
        const response = await fetch(`http://localhost:8081/api/products/${product.id}`, {
          method: 'DELETE'
        })

        if (!response.ok) {
          const result = await response.json()
          throw new Error(result.message || '删除商品失败')
        }

        // 从本地列表中移除
        this.products = this.products.filter(p => p.id !== product.id)
        this.$message.success('商品删除成功')
        
      } catch (error) {
        console.error('删除商品失败:', error)
        this.$message.error(error.message || '删除商品失败')
      } finally {
        this.deletingProduct = null
      }
    },

    changePage(page) {
      this.currentPage = page
    },

getCategoryName(categoryId) {
  const category = this.categories.find(cat => cat.id == categoryId)
  return category ? category.name : '未知'
},

    getCategoryColor(categoryId) {
      const category = this.categories.find(cat => cat.id === categoryId)
      return category ? category.color : '#666'
    },

    showAddProduct() {
      this.isEditing = false
      this.formData = {
        id: null,
        name: '',
        categoryId: '',
        price: 0,
        stock: 0,
        description: '',
        tags: [],
        image: '🥤',
        available: true
      }
      this.showProductModal = true
    },

editProduct(product) {
  this.isEditing = true
  
  // 🚨 修复：找到对应的分类ID
  const categoryMapReverse = {
    'recommend': 1,
    'weather': 2,
    'festival': 3,
    'classic': 4,
    'fruit': 5,
    'milktea': 6,
    'special': 7,
    'ai': 8,
    'region': 9,
    'festival-recommend': 10
  }
  
  // 根据商品的category字段找到对应的分类ID
  const categoryId = categoryMapReverse[product.category] || product.categoryId
  
  this.formData = {
    ...product,
    categoryId: categoryId, // 设置正确的分类ID
    tags: Array.isArray(product.tags) ? product.tags : 
          (product.tags ? product.tags.split(',') : [])
  }
  
  console.log('📝 编辑商品信息:');
  console.log('原始category:', product.category);
  console.log('找到的categoryId:', categoryId);
  console.log('表单数据:', this.formData);
  
  this.showProductModal = true
},

    closeModal() {
      this.showProductModal = false
      this.newTag = ''
    },

    addTag() {
      if (this.newTag.trim() && !this.formData.tags.includes(this.newTag.trim())) {
        this.formData.tags.push(this.newTag.trim())
        this.newTag = ''
      }
    },

    removeTag(index) {
      this.formData.tags.splice(index, 1)
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
getRandomColor() {
  const colors = ['#ff6b6b', '#51cf66', '#339af0', '#cc5de8', '#ff922b', '#20c997', '#868e96']
  return colors[Math.floor(Math.random() * colors.length)]
},

// 根据分类名获取显示名称
getCategoryDisplayName(categoryName) {
  const categoryMap = {
    'recommend': '推荐',
    'weather': '天气特饮',
    'festival': '节日限定',
    'classic': '经典系列',
    'fruit': '果茶系列',
    'milktea': '奶茶系列',
    'special': '特色系列',
    'ai': 'AI推荐',
    'region': '地域特色',
    'festival-recommend': '节日推荐'
  }
  return categoryMap[categoryName] || categoryName
}
  }
}
</script>


<style scoped>
.product-management {
  height: 100vh; /* 🎯 全屏高度 */
  display: flex;
  flex-direction: column;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0; /* 防止被压缩 */
}

.management-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.product-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 🎯 重要 */
}

/* 🎯 关键修改：表格容器 */
.table-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto; /* 🎯 启用垂直滚动 */
  max-height: calc(100vh - 250px); /* 🎯 动态高度 */
}

/* 表格样式 */
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
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
  position: sticky;
  top: 0;
  z-index: 10;
}

.data-table tbody tr:hover {
  background: #f8f9fa;
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

.filter-select, .search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.search-input {
  width: 200px;
}

.add-btn {
  padding: 8px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.refresh-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover { background: #218838; }
.refresh-btn:hover { background: #0056b3; }

.management-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.product-list {
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
  min-width: 1000px;
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

.product-id {
  font-weight: 600;
  color: #666;
}

.product-image {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-emoji {
  font-size: 20px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 600;
  color: #333;
}

.product-description {
  color: #666;
  font-size: 12px;
  line-height: 1.3;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.product-tag {
  background: #f0f2f5;
  color: #666;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
}

.category-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.product-price {
  font-weight: 600;
  color: #e74c3c;
}

.low-stock {
  color: #dc3545;
  font-weight: 600;
}

.sales-count {
  color: #666;
  text-align: center;
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

.btn-edit, .btn-enable, .btn-disable, .btn-delete {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-edit {
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

.btn-edit:hover { background: #138496; }
.btn-enable:hover { background: #218838; }
.btn-disable:hover { background: #e0a800; }
.btn-delete:hover { background: #c82333; }

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

.product-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.form-input, .form-select, .form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  border-color: #007bff;
  outline: none;
}

.tags-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.tag-remove {
  background: none;
  border: none;
  color: #1976d2;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #e1e5e9;
}

.btn-cancel, .btn-save {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-save {
  background: #007bff;
  color: white;
}

.btn-cancel:hover { background: #5a6268; }
.btn-save:hover { background: #0056b3; }

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 在 ProductManagement.vue 的 <style> 部分添加 */
.management-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 150px); /* 限制最大高度 */
}

.table-container {
  overflow-x: auto;
  overflow-y: auto; /* 添加垂直滚动 */
  flex: 1;
}

.data-table thead {
  position: sticky;
  top: 0;
  background: #f8f9fa;
  z-index: 10;
}

</style>