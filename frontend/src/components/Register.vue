<template>
  <div class="register-container">
    <div class="form-header">
      <h2>用户注册</h2>
      <p>创建您的新账户</p>
    </div>
    
    <form @submit.prevent="handleRegister" class="register-form">
      <div class="form-group">
        <label for="reg-username">用户名</label>
        <input
          id="reg-username"
          type="text"
          v-model="form.username"
          placeholder="请输入用户名（4-20位字符）"
          required
          :class="{ 'error': errors.username }"
          @input="clearError('username')"
        >
        <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
      </div>
      
      <div class="form-group">
        <label for="reg-password">密码</label>
        <input
          id="reg-password"
          type="password"
          v-model="form.password"
          placeholder="请输入密码（至少6位）"
          required
          :class="{ 'error': errors.password }"
          @input="clearError('password')"
        >
        <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
      </div>
      
      <div class="form-group">
        <label for="confirm-password">确认密码</label>
        <input
          id="confirm-password"
          type="password"
          v-model="form.confirmPassword"
          placeholder="请再次输入密码"
          required
          :class="{ 'error': errors.confirmPassword }"
          @input="clearError('confirmPassword')"
        >
        <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
      </div>
      
      <div class="form-group">
        <label for="email">邮箱（可选）</label>
        <input
          id="email"
          type="email"
          v-model="form.email"
          placeholder="请输入邮箱地址"
          :class="{ 'error': errors.email }"
          @input="clearError('email')"
        >
        <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
      </div>
      
      <button 
        type="submit" 
        class="submit-btn"
        :disabled="loading"
      >
        <span v-if="loading">注册中...</span>
        <span v-else>注册</span>
      </button>
    </form>
    
    <div class="form-footer">
      <p>已有账号？ 
        <a href="#" @click.prevent="switchToLogin" class="link">立即登录</a>
      </p>
    </div>
    
    <!-- 消息提示 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      },
      errors: {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      },
      loading: false,
      message: '',
      messageType: '' // success, error
    }
  },
  methods: {
    async handleRegister() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }
      
      this.loading = true
      this.message = ''
      
      try {
        const response = await axios.post('http://localhost:8081/api/auth/register', {
          username: this.form.username.trim(),
          password: this.form.password,
          email: this.form.email.trim() || null
        })
        
// ✅ 修改后的正确代码：
if (response.data.success) {
  // ⚠️ 关键修改：注册成功后不要保存用户信息到localStorage
  // ⚠️ 不要自动登录，让用户手动登录
  
  this.message = '注册成功！请登录您的账号'
  this.messageType = 'success'
  
  // 清空表单
  this.resetForm()
  
  // ⚠️ 关键修改：注册成功后跳转到登录页面，而不是直接登录
  setTimeout(() => {
    this.$router.push('/login')
  }, 1500)
} else {
          this.message = response.data.message
          this.messageType = 'error'
        }
      } catch (error) {
        console.error('注册错误:', error)
        if (error.response && error.response.data) {
          this.message = error.response.data.message || '注册失败'
        } else {
          this.message = '网络错误，请检查连接'
        }
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    
    validateForm() {
      let isValid = true
      this.errors = { username: '', password: '', confirmPassword: '', email: '' }
      
      // 用户名验证
      const username = this.form.username.trim()
      if (!username) {
        this.errors.username = '用户名不能为空'
        isValid = false
      } else if (username.length < 4 || username.length > 20) {
        this.errors.username = '用户名长度应为4-20位字符'
        isValid = false
      } else if (!/^[a-zA-Z0-9_]+$/.test(username)) {
        this.errors.username = '用户名只能包含字母、数字和下划线'
        isValid = false
      }
      
      // 密码验证
      if (!this.form.password) {
        this.errors.password = '密码不能为空'
        isValid = false
      } else if (this.form.password.length < 6) {
        this.errors.password = '密码长度不能少于6位'
        isValid = false
      }
      
      // 确认密码验证
      if (!this.form.confirmPassword) {
        this.errors.confirmPassword = '请确认密码'
        isValid = false
      } else if (this.form.password !== this.form.confirmPassword) {
        this.errors.confirmPassword = '两次输入的密码不一致'
        isValid = false
      }
      
      // 邮箱验证（可选）
      if (this.form.email.trim() && !this.isValidEmail(this.form.email.trim())) {
        this.errors.email = '请输入有效的邮箱地址'
        isValid = false
      }
      
      return isValid
    },
    
    isValidEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    },
    
    clearError(field) {
      this.errors[field] = ''
    },
    
    resetForm() {
      this.form = {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      }
      this.errors = {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      }
    },
    
    switchToLogin() {
      // 跳转到登录页面
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.register-container {
  padding: 40px 30px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.form-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #f8f9fa;
}

input:focus {
  outline: none;
  border-color: #667eea;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

input.error {
  border-color: #e74c3c;
  background-color: #fdf2f2;
}

.error-message {
  display: block;
  color: #e74c3c;
  font-size: 12px;
  margin-top: 5px;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.form-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.form-footer p {
  color: #666;
  font-size: 14px;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

.message {
  padding: 12px;
  border-radius: 6px;
  margin: 15px 0;
  text-align: center;
  font-size: 14px;
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
</style>