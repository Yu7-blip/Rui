<template>
  <div class="login-wrapper">
    <!-- 左侧图片 -->
    <div class="left-image-section">
      <img src="/images/qingtian-tea.png" alt="晴天主题款奶茶">
      <div class="image-text dynamic-text" data-text="晴天主题款">晴天主题款</div>
    </div>

    <!-- 中间登录框 -->
    <div class="login-section">
      <div class="login-background"></div>
      <div class="login-content">
        <div class="system-title">
          <h1 class="gradient-text" :class="currentTitleStyle">个性化奶茶智能点餐式系统</h1>
        </div>

        <div class="form-header">
          <h2 class="color-changing-text">用户登录</h2>
          <p class="subtitle-text">欢迎回来，请登录您的账户</p>
        </div>
        
        <!-- 消息提示 -->
        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username" class="form-label">用户名</label>
            <input
              id="username"
              type="text"
              v-model="form.username"
              placeholder="请输入用户名"
              required
              class="form-input"
            >
          </div>
          
          <div class="form-group">
            <label for="password" class="form-label">密码</label>
            <input
              id="password"
              type="password"
              v-model="form.password"
              placeholder="请输入密码"
              required
              class="form-input"
            >
          </div>
          
          <button type="submit" class="submit-btn" :class="{ loading: loading }">
            <span class="btn-content">
              <span class="btn-text">{{ loading ? '登录中...' : '登录' }}</span>
              <span class="btn-wave"></span>
            </span>
          </button>
        </form>
        
        <div class="form-footer">
          <p>还没有账号？ 
            <a href="#" @click.prevent="switchToRegister" class="link">立即注册</a>
          </p>
        </div>

        <!-- 管理员快速登录入口 -->
        <div class="admin-login-section">
          <div class="divider">
            <span class="divider-text">管理员入口</span>
          </div>
          <div class="admin-buttons">
            <button type="button" @click="showAdminLogin = true" class="admin-btn rui-btn">
              <span class="btn-icon">👑</span>
              <span class="btn-text">管理员登录</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧图片 -->
    <div class="right-image-section">
      <img src="/images/spring-tea.png" alt="春节节日限定款奶茶">
      <div class="image-text dynamic-text" data-text="春节节日限定款">春节节日限定款</div>
    </div>

    <!-- 浮动气泡 -->
    <div class="bubbles">
      <div v-for="i in 15" :key="i" class="bubble" :style="bubbleStyle(i)"></div>
    </div>

    <!-- 管理员登录弹窗 -->
    <div v-if="showAdminLogin" class="admin-modal-overlay" @click.self="showAdminLogin = false">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>管理员登录</h3>
          <button class="close-btn" @click="showAdminLogin = false">×</button>
        </div>
        
        <div class="admin-modal-body">
          <!-- 消息提示 -->
          <div v-if="adminMessage" :class="['message', adminMessageType]">
            {{ adminMessage }}
          </div>
          
          <form @submit.prevent="handleAdminLogin" class="admin-login-form">
            <div class="form-group">
              <label for="admin-username" class="form-label">管理员账号</label>
              <input
                id="admin-username"
                type="text"
                v-model="adminForm.username"
                placeholder="请输入管理员账号"
                required
                class="form-input"
              >
            </div>
            
            <div class="form-group">
              <label for="admin-password" class="form-label">管理员密码</label>
              <input
                id="admin-password"
                type="password"
                v-model="adminForm.password"
                placeholder="请输入管理员密码"
                required
                class="form-input"
              >
            </div>
            
            <button type="submit" class="submit-btn admin-submit-btn" :class="{ loading: adminLoading }">
              <span class="btn-content">
                <span class="btn-text">{{ adminLoading ? '登录中...' : '管理员登录' }}</span>
              </span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      adminForm: {
        username: '',
        password: ''
      },
      errors: {
        username: '',
        password: ''
      },
      loading: false,
      adminLoading: false,
      message: '',
      messageType: '',
      adminMessage: '',
      adminMessageType: '',
      showAdminLogin: false,
      // 颜色变换相关
      colorIndex: 0,
      colorStyles: [
        'title-style-1', // 奶茶色系
        'title-style-2', // 水果色系
        'title-style-3', // 清新色系
        'title-style-4'  // 渐变色系
      ]
    }
  },
  computed: {
    currentTitleStyle() {
      return this.colorStyles[this.colorIndex]
    }
  },
  methods: {
async handleLogin() {
  // 表单验证
  if (!this.validateForm()) {
    return
  }
  
  this.loading = true
  this.message = ''
  
  try {
    const requestData = {
      username: this.form.username.trim(),
      password: this.form.password
    }
    console.log('发送的请求数据:', JSON.stringify(requestData))
    
    const response = await axios.post('http://localhost:8081/api/auth/login', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('登录成功响应:', response.data)
    
    if (response.data.success) {
      // ✅ 新增：保存用户信息、角色和Token到localStorage
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userId', response.data.userId)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('role', response.data.role)  // 保存用户角色
      
      this.showMessage('登录成功！正在跳转...', 'success')
      
      // ✅ 修改：保留原来的currentUser存储（兼容现有代码）
      const userData = {
        username: response.data.username,
        userId: response.data.userId,
        role: response.data.role  // 使用实际的角色
      }
      localStorage.setItem('currentUser', JSON.stringify(userData))
      
      setTimeout(() => {
        // ✅ 修改：根据用户角色跳转到不同页面
        if (response.data.role === 'ADMIN') {
          this.$router.push('/admin')
        } else {
          this.$router.push('/customer')
        }
      }, 1000)
      
    } else {
      this.showMessage(response.data.message, 'error')
    }
  } catch (error) {
    console.error('完整的错误信息:', error)
    
    if (error.response && error.response.data) {
      const errorData = error.response.data
      this.showMessage(errorData.message || JSON.stringify(errorData) || '登录失败', 'error')
    } else {
      this.showMessage('网络错误，请检查连接', 'error')
    }
  } finally {
    this.loading = false
  }
},

async handleAdminLogin() {
  if (!this.adminForm.username.trim() || !this.adminForm.password) {
    this.showAdminMessage('请输入管理员账号和密码', 'error')
    return
  }
  
  this.adminLoading = true
  this.adminMessage = ''
  
  try {
    const requestData = {
      username: this.adminForm.username.trim(),
      password: this.adminForm.password
    }
    console.log('发送管理员登录请求数据:', JSON.stringify(requestData))
    
    // ⭐ 修改：调用专门的管理员登录接口
    const response = await axios.post('http://localhost:8081/api/auth/admin/login', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('管理员登录响应:', response.data)
    
    if (response.data.success) {
      this.showAdminMessage('管理员登录成功！正在跳转...', 'success')
      
      // ⭐ 修改：使用后端返回的角色
      const adminUser = {
        username: response.data.username,
        userId: response.data.userId,
        role: response.data.role
      }
      localStorage.setItem('currentUser', JSON.stringify(adminUser))
      
      setTimeout(() => {
        this.showAdminLogin = false
        this.$router.push('/admin')
      }, 1000)
      
    } else {
      this.showAdminMessage(response.data.message, 'error')
    }
  } catch (error) {
    console.error('管理员登录错误:', error)
    
    if (error.response && error.response.data) {
      const errorData = error.response.data
      this.showAdminMessage(errorData.message || JSON.stringify(errorData) || '管理员登录失败', 'error')
    } else {
      this.showAdminMessage('网络错误，请检查连接', 'error')
    }
  } finally {
    this.adminLoading = false
  }
},
    
    validateForm() {
      let isValid = true
      this.errors = { username: '', password: '' }
      
      if (!this.form.username.trim()) {
        this.errors.username = '用户名不能为空'
        isValid = false
        this.shakeElement('username')
      }
      
      if (!this.form.password) {
        this.errors.password = '密码不能为空'
        isValid = false
        this.shakeElement('password')
      } else if (this.form.password.length < 6) {
        this.errors.password = '密码长度不能少于6位'
        isValid = false
        this.shakeElement('password')
      }
      
      return isValid
    },
    
    showMessage(text, type) {
      this.message = text
      this.messageType = type
      setTimeout(() => {
        this.message = ''
      }, 3000)
    },

    showAdminMessage(text, type) {
      this.adminMessage = text
      this.adminMessageType = type
      setTimeout(() => {
        this.adminMessage = ''
      }, 3000)
    },
    
    shakeElement(elementId) {
      const element = document.getElementById(elementId)
      if (element) {
        element.classList.add('shake')
        setTimeout(() => {
          element.classList.remove('shake')
        }, 500)
      }
    },
    
    switchToRegister() {
      this.$router.push('/register')
    },
    
    // 颜色变换效果
    startColorRotation() {
      setInterval(() => {
        this.colorIndex = (this.colorIndex + 1) % this.colorStyles.length
      }, 3000) // 每3秒切换一次颜色
    },
    
    // 气泡背景效果
    bubbleStyle(index) {
      const size = Math.random() * 60 + 20
      const left = Math.random() * 100
      const animationDelay = Math.random() * 20
      const animationDuration = Math.random() * 10 + 10
      
      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${left}%`,
        animationDelay: `${animationDelay}s`,
        animationDuration: `${animationDuration}s`,
        background: this.getRandomBubbleColor()
      }
    },
    
    getRandomBubbleColor() {
      const colors = [
        'rgba(255, 182, 193, 0.3)', // 浅粉色
        'rgba(255, 218, 185, 0.3)', // 桃色
        'rgba(173, 216, 230, 0.3)', // 浅蓝色
        'rgba(152, 251, 152, 0.3)', // 浅绿色
        'rgba(255, 250, 205, 0.3)'  // 浅黄色
      ]
      return colors[Math.floor(Math.random() * colors.length)]
    },
    
    initEffects() {
      this.startColorRotation()
    }
  },
  
  mounted() {
    this.form.username = 'admin'
    this.form.password = '123456'
    this.initEffects()
  }
}
</script>

<style scoped>
/* 基础布局 */
.login-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 左右图片区域 */
.left-image-section, .right-image-section {
  flex: 1;
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.left-image-section img, .right-image-section img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0.9;
}

.image-text {
  position: absolute;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.8);
  background: rgba(0,0,0,0.6);
  padding: 15px 40px;
  border-radius: 15px;
  text-align: center;
  z-index: 10;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

/* 中间登录区域 - 磨砂玻璃效果 */
.login-section {
  width: 480px;
  min-width: 480px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.login-background {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 80px rgba(255, 255, 255, 0.2);
}

.login-content {
  width: 100%;
  padding: 50px 40px;
  position: relative;
  z-index: 101;
}

/* 系统标题 */
.system-title {
  text-align: center;
  margin-bottom: 40px;
  padding: 25px 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.system-title h1 {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
  background-clip: text;
  -webkit-background-clip: text;
  transition: all 0.5s ease;
}

/* 标题颜色样式 */
.title-style-1 {
  background: linear-gradient(135deg, #d4a574, #8b4513);
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-style-2 {
  background: linear-gradient(135deg, #ff6b6b, #ffa726, #66bb6a);
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-style-3 {
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-style-4 {
  background: linear-gradient(135deg, #ff9a9e, #fad0c4, #fad0c4);
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 表单标题 */
.form-header {
  text-align: center;
  margin-bottom: 35px;
}

.form-header h2 {
  margin-bottom: 12px;
  font-size: 28px;
  font-weight: 600;
  animation: colorChange 4s ease-in-out infinite;
}

@keyframes colorChange {
  0%, 100% {
    color: #667eea;
  }
  25% {
    color: #ff6b6b;
  }
  50% {
    color: #4ecdc4;
  }
  75% {
    color: #ffa726;
  }
}

.subtitle-text {
  color: #666;
  font-size: 15px;
  opacity: 0.8;
}

/* 消息提示样式 */
.message {
  padding: 12px 20px;
  border-radius: 8px;
  margin: 0 0 20px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表单样式 */
.login-form {
  margin-bottom: 25px;
}

.form-group {
  margin-bottom: 25px;
}

.form-label {
  display: block;
  margin-bottom: 10px;
  color: #555;
  font-weight: 500;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 15px 20px;
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.form-input.shake {
  animation: shake 0.5s ease-in-out;
  border-color: #ff6b6b;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn.loading {
  pointer-events: none;
  opacity: 0.8;
}

.btn-wave {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: all 0.6s ease;
}

.submit-btn:hover .btn-wave {
  width: 300px;
  height: 300px;
}

/* 底部链接 */
.form-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.form-footer p {
  color: #666;
  font-size: 14px;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 管理员区域 */
.admin-login-section {
  margin-top: 30px;
  padding-top: 25px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.divider {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.divider-text {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 20px;
  color: #888;
  font-size: 13px;
  font-weight: 500;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(0, 0, 0, 0.1);
  z-index: -1;
}

.admin-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-btn {
  padding: 14px;
  border: 2px solid #e1e8ed;
  background: white;
  color: #555;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-weight: 500;
}

.admin-btn:hover {
  border-color: #667eea;
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.rui-btn {
  border-color: #ff6b6b;
  color: #ff6b6b;
}

.rui-btn:hover {
  background: #ff6b6b;
  border-color: #ff6b6b;
  color: white;
}

/* 气泡背景 */
.bubbles {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.bubble {
  position: absolute;
  bottom: -100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: floatUp linear infinite;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.3;
  }
  90% {
    opacity: 0.3;
  }
  100% {
    transform: translateY(-100vh) rotate(360deg);
    opacity: 0;
  }
}

/* 管理员登录弹窗样式 */
.admin-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.admin-modal {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

.admin-modal-header {
  padding: 25px 30px 20px;
  border-bottom: 1px solid #e1e8ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.admin-modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.admin-modal-body {
  padding: 30px;
}

.admin-login-form {
  margin-top: 10px;
}

.admin-submit-btn {
  background: linear-gradient(135deg, #ff6b6b, #ffa726) !important;
  margin-top: 10px;
}

.admin-submit-btn:hover {
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4) !important;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-section {
    width: 420px;
    min-width: 420px;
  }
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }
  
  .left-image-section,
  .right-image-section {
    display: none;
  }
  
  .login-section {
    width: 100%;
    min-width: auto;
  }
  
  .login-background {
    top: 10px;
    left: 10px;
    right: 10px;
    bottom: 10px;
  }
  
  .login-content {
    padding: 30px 20px;
  }

  .admin-modal {
    width: 95%;
    margin: 20px;
  }

  .admin-modal-header,
  .admin-modal-body {
    padding: 20px;
  }
}
</style>