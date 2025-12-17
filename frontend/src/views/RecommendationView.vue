<template>
  <div class="recommendation-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <span class="ai-icon">🤖</span>
          AI智能推荐
        </h1>
        <p class="page-subtitle">基于深度学习的个性化奶茶推荐，为你找到最适合的那一杯</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-grid">
        <!-- AI推荐组件 -->
        <div class="ai-section">
          <AIRecommendation />
        </div>

        <!-- 侧边栏 -->
        <div class="sidebar">
          <!-- 推荐小贴士 -->
          <div class="tips-card">
            <h3>💡 推荐小贴士</h3>
            <ul class="tips-list">
              <li>选择准确的天气信息，获得更合适的饮品推荐</li>
              <li>根据当前心情选择，AI会推荐匹配的饮品</li>
              <li>口味偏好越具体，推荐结果越精准</li>
              <li>可以多次尝试不同的组合</li>
            </ul>
          </div>

          <!-- 热门推荐 -->
          <div class="popular-card">
            <h3>🔥 热门推荐</h3>
            <div class="popular-list">
              <div 
                v-for="product in popularProducts" 
                :key="product.id"
                class="popular-item"
                @click="viewProduct(product)"
              >
                <span class="product-emoji">{{ product.emoji }}</span>
                <span class="product-name">{{ product.name }}</span>
                <span class="product-price">¥{{ product.price }}</span>
              </div>
            </div>
          </div>

          <!-- 天气推荐 -->
          <div class="weather-card">
            <h3>🌤️ 天气推荐</h3>
            <div class="weather-options">
              <button 
                v-for="weather in weatherOptions" 
                :key="weather.value"
                @click="quickWeatherRecommend(weather.value)"
                class="weather-btn"
              >
                {{ weather.emoji }} {{ weather.label }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 其他推荐类型 -->
      <div class="other-recommendations">
        <h2>更多推荐方式</h2>
        <div class="recommendation-cards">
          <div class="recommend-card" @click="navigateToPersonalized">
            <div class="card-icon">👤</div>
            <h4>个性化推荐</h4>
            <p>基于你的历史订单和偏好</p>
            <span class="card-arrow">→</span>
          </div>

          <div class="recommend-card" @click="navigateToSeasonal">
            <div class="card-icon">🍂</div>
            <h4>季节推荐</h4>
            <p>根据当前季节精选</p>
            <span class="card-arrow">→</span>
          </div>

          <div class="recommend-card" @click="navigateToFestival">
            <div class="card-icon">🎄</div>
            <h4>节日推荐</h4>
            <p>特殊节日的特色饮品</p>
            <span class="card-arrow">→</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AIRecommendation from '@/components/AIRecommendation.vue'

export default {
  name: 'RecommendationView',
  components: {
    AIRecommendation
  },
  data() {
    return {
      popularProducts: [
        { id: 1, name: '经典珍珠奶茶', emoji: '🧋', price: 18 },
        { id: 2, name: '芒果波波茶', emoji: '🥭', price: 22 },
        { id: 3, name: '芝士奶盖绿茶', emoji: '🍵', price: 20 },
        { id: 4, name: '草莓多多', emoji: '🍓', price: 24 }
      ],
      weatherOptions: [
        { label: '晴天推荐', value: '晴天', emoji: '☀️' },
        { label: '雨天推荐', value: '雨天', emoji: '🌧️' },
        { label: '炎热推荐', value: '炎热', emoji: '🔥' },
        { label: '寒冷推荐', value: '寒冷', emoji: '🥶' }
      ]
    }
  },
  methods: {
    viewProduct(product) {
      alert(`查看产品: ${product.name}`);
      // 实际项目中可以跳转到产品详情页
      // this.$router.push(`/product/${product.id}`);
    },

    quickWeatherRecommend(weather) {
      alert(`获取${weather}推荐`);
      // 实际项目中可以调用天气推荐API
      // this.$router.push(`/recommendations/weather?weather=${weather}`);
    },

    navigateToPersonalized() {
      alert('跳转到个性化推荐');
      // this.$router.push('/recommendations/personalized');
    },

    navigateToSeasonal() {
      alert('跳转到季节推荐');
      // this.$router.push('/recommendations/seasonal');
    },

    navigateToFestival() {
      alert('跳转到节日推荐');
      // this.$router.push('/recommendations/festival');
    }
  },

  mounted() {
    // 页面加载时可以获取实际的热门产品数据
    this.fetchPopularProducts();
  },

  methods: {
    async fetchPopularProducts() {
      try {
        // 实际项目中调用API获取热门产品
        // const response = await fetch('http://localhost:8081/api/recommendations/popular');
        // this.popularProducts = await response.json();
      } catch (error) {
        console.error('获取热门产品失败:', error);
      }
    },

    viewProduct(product) {
      this.$notify({
        title: '查看产品',
        message: `准备查看: ${product.name}`,
        type: 'info'
      });
    },

    quickWeatherRecommend(weather) {
      this.$notify({
        title: '天气推荐',
        message: `正在获取${weather}的推荐...`,
        type: 'success'
      });
    },

    navigateToPersonalized() {
      this.$notify({
        title: '个性化推荐',
        message: '跳转到个性化推荐页面',
        type: 'info'
      });
    },

    navigateToSeasonal() {
      this.$notify({
        title: '季节推荐',
        message: '跳转到季节推荐页面',
        type: 'info'
      });
    },

    navigateToFestival() {
      this.$notify({
        title: '节日推荐',
        message: '跳转到节日推荐页面',
        type: 'info'
      });
    }
  }
}
</script>

<style scoped>
.recommendation-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 0;
  text-align: center;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 2.5rem;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.ai-icon {
  font-size: 2.8rem;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
  margin-bottom: 50px;
}

.ai-section {
  background: white;
  border-radius: 16px;
  padding: 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tips-card, .popular-card, .weather-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tips-card h3, .popular-card h3, .weather-card h3 {
  color: #333;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tips-list {
  list-style: none;
  padding: 0;
}

.tips-list li {
  padding: 8px 0;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  padding-left: 20px;
}

.tips-list li:before {
  content: "•";
  color: #667eea;
  position: absolute;
  left: 0;
}

.tips-list li:last-child {
  border-bottom: none;
}

.popular-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.popular-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.popular-item:hover {
  background: #f8f9fa;
  border-color: #667eea;
  transform: translateX(5px);
}

.product-emoji {
  font-size: 1.2rem;
}

.product-name {
  flex: 1;
  color: #333;
  font-weight: 500;
}

.product-price {
  color: #e74c3c;
  font-weight: bold;
}

.weather-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.weather-btn {
  padding: 12px 8px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  text-align: center;
}

.weather-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.other-recommendations {
  margin-top: 40px;
}

.other-recommendations h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 2rem;
}

.recommendation-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 25px;
}

.recommend-card {
  background: white;
  border-radius: 16px;
  padding: 30px 25px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  border: 2px solid transparent;
}

.recommend-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.recommend-card h4 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.3rem;
}

.recommend-card p {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.card-arrow {
  color: #667eea;
  font-size: 1.5rem;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .ai-icon {
    font-size: 2.2rem;
  }
  
  .content-grid {
    gap: 20px;
  }
  
  .weather-options {
    grid-template-columns: 1fr;
  }
  
  .recommendation-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 30px 0;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .main-content {
    padding: 20px 15px;
  }
  
  .tips-card, .popular-card, .weather-card {
    padding: 20px;
  }
}
</style>