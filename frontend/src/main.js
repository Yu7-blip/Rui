import Vue from 'vue'
import App from './App.vue'
import router from './router'

Vue.config.productionTip = false

// 添加消息提示
Vue.prototype.$message = {
  success: (message) => {
    alert('✅ ' + message)
  },
  error: (message) => {
    alert('❌ ' + message)
  },
  warning: (message) => {
    alert('⚠️ ' + message)
  }
}

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')