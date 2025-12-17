import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8081/api', // 确保这个地址正确
  timeout: 10000
})

// 请求拦截器 - 添加Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理401错误
request.interceptors.response.use(
  response => {
    return response.data || response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token过期或无效
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      localStorage.removeItem('currentUser')
      // 跳转到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request