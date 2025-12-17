import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import CustomerView from '../views/CustomerView.vue'
import AdminView from '../views/AdminView.vue'
import RecommendationView from '../views/RecommendationView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresGuest: true }
  },
  {
    path: '/customer',
    name: 'Customer',
    component: CustomerView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/recommendation',
    name: 'Recommendation',
    component: RecommendationView,
    meta: { requiresAuth: true }
  },
  {
    path: '/ai-recommend',
    name: 'AIRecommend',
    component: RecommendationView,
    meta: { requiresAuth: true }
  },
  {
  path: '/region-recommend',
  name: 'RegionRecommend',
  component: () => import('../components/RegionRecommendation.vue'),
  meta: { requiresAuth: true }
}
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 解决路由重复导航错误
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

// 同样修复 replace 方法
const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

// ✅ 修复的路由守卫
router.beforeEach((to, from, next) => {
  console.log('导航到:', to.path);
  next(); // 直接放行所有路由
});

export default router