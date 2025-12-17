<template>
  <Welcome 
    :username="username"
    @logout="handleLogout" 
  />
</template>

<script>
import Welcome from '../components/Welcome.vue'

export default {
  name: 'CustomerView',
  components: {
    Welcome
  },
  data() {
    return {
      username: '用户'
    }
  },
created() {
  // 从本地存储获取用户信息
  const savedUser = localStorage.getItem('currentUser')
  if (savedUser) {
    const userData = JSON.parse(savedUser)
    this.username = userData.username || '用户'
    
    // 如果是管理员，应该跳转到管理员界面
    if (userData.role === 'admin') {
      this.$router.push('/admin')
      return
    }
  } else {
    // 如果没有用户信息，跳转到登录页
    this.$router.push('/login')
  }
},
  methods: {
    handleLogout() {
      // 清除本地存储
      localStorage.removeItem('currentUser')
      // 跳转到登录页
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
/* 不需要额外样式，Welcome组件已经有完整样式 */
</style>