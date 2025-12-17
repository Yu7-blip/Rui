import axios from 'axios'

// 创建axios实例 - 去掉所有token验证
const API = axios.create({
  baseURL: 'http://localhost:8080/api', // 直接写死，简单
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 去掉所有拦截器！简单粗暴
export default {
  // ========== 认证相关 ==========
  login: (data) => API.post('/auth/login', data).then(res => res.data),
  register: (data) => API.post('/auth/register', data).then(res => res.data),
  logout: () => API.post('/auth/logout').then(res => res.data),
  getUserInfo: () => API.get('/auth/me').then(res => res.data),

  // ========== 用户管理 ==========
  getUsers: () => API.get('/admin/users').then(res => res.data),
  updateUserRole: (userId, role) => API.put(`/admin/users/${userId}/role`, { role }).then(res => res.data),
  deleteUser: (userId) => API.delete(`/admin/users/${userId}`).then(res => res.data),

  // ========== 产品管理 ==========
  getProducts: () => API.get('/products').then(res => res.data),
  getProductById: (id) => API.get(`/products/${id}`).then(res => res.data),
  createProduct: (data) => API.post('/products', data).then(res => res.data),
  updateProduct: (id, data) => API.put(`/products/${id}`, data).then(res => res.data),
  deleteProduct: (id) => API.delete(`/products/${id}`).then(res => res.data),

  // ========== 分类管理 ==========
  getCategories: () => API.get('/categories').then(res => res.data),
  createCategory: (data) => API.post('/categories', data).then(res => res.data),
  updateCategory: (id, data) => API.put(`/categories/${id}`, data).then(res => res.data),
  deleteCategory: (id) => API.delete(`/categories/${id}`).then(res => res.data),

  // ========== 购物车管理 ==========
  getCart: () => API.get('/cart').then(res => res.data),
  addToCart: (data) => API.post('/cart/add', data).then(res => res.data),
  updateCartItem: (itemId, quantity) => API.put(`/cart/items/${itemId}`, { quantity }).then(res => res.data),
  removeFromCart: (itemId) => API.delete(`/cart/items/${itemId}`).then(res => res.data),
  clearCart: () => API.post('/cart/clear').then(res => res.data),

  // ========== 订单管理 ==========
  getOrders: () => API.get('/orders').then(res => res.data),
  getOrderById: (id) => API.get(`/orders/${id}`).then(res => res.data),
  createOrder: (data) => API.post('/orders', data).then(res => res.data),
  updateOrderStatus: (id, status) => API.put(`/orders/${id}/status`, { status }).then(res => res.data),
  deleteOrder: (id) => API.delete(`/orders/${id}`).then(res => res.data),

  // ========== 统计分析 ==========
  getStatistics: () => API.get('/admin/statistics').then(res => res.data),

  // ========== AI推荐 ==========
  getRecommendations: (data) => API.post('/recommendation', data).then(res => res.data)
}