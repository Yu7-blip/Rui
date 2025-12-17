<template>
  <div class="admin-view">
    <AdminDashboard />
  </div>
</template>

<script>
import AdminDashboard from '../components/AdminDashboard.vue'

export default {
  name: 'AdminView',
  components: {
    AdminDashboard
  },
  created() {
    console.log('进入管理界面')
  }
}
</script>

<style scoped>
.admin-view {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>