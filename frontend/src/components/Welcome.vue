<template>
  <div class="dashboard-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>奶茶点餐系统</h2>
        <p class="welcome-text">欢迎，{{ username }}</p>
      </div>
      
      <nav class="sidebar-nav">
        <ul>
          <li 
            v-for="category in categories" 
            :key="category.id"
            :class="{ active: activeCategory === category.id }"
            @click="switchCategory(category.id)"
          >
            <span class="nav-icon">{{ category.icon }}</span>
            <span class="nav-text">{{ category.name }}</span>
          </li>
        </ul>
      </nav>
      
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <span class="logout-icon">🚪</span>
          退出登录
        </button>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- AI推荐页面 -->
      <div v-if="activeCategory === 'ai'" class="ai-recommendation-page">
        <div class="ai-page-header">
          <h1>🤖 AI智能推荐</h1>
          <p class="ai-page-subtitle">基于深度学习的个性化奶茶推荐，为你找到最适合的那一杯</p>
        </div>
        
        <div class="ai-content-layout">
          <!-- 左侧：表单和小贴士 -->
          <div class="ai-left-section">
            <div class="ai-form-section">
              <div class="recommendation-form">
                <div class="form-group">
                  <label for="preference">口味偏好：</label>
                  <select id="preference" v-model="aiForm.userPreference" class="form-select">
                    <option value="">请选择口味偏好</option>
                    <option value="甜味">喜欢甜味</option>
                    <option value="清淡">喜欢清淡</option>
                    <option value="浓郁">喜欢浓郁</option>
                    <option value="水果">喜欢水果味</option>
                    <option value="茶香">喜欢茶香</option>
                    <option value="奶香">喜欢奶香</option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="weather">当前天气：</label>
                  <select id="weather" v-model="aiForm.weather" class="form-select">
                    <option value="">请选择天气</option>
                    <option value="晴天">☀️ 晴天</option>
                    <option value="雨天">🌧️ 雨天</option>
                    <option value="阴天">☁️ 阴天</option>
                    <option value="雪天">❄️ 雪天</option>
                    <option value="炎热">🔥 炎热</option>
                    <option value="寒冷">🥶 寒冷</option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="mood">现在心情：</label>
                  <select id="mood" v-model="aiForm.mood" class="form-select">
                    <option value="">请选择心情</option>
                    <option value="开心">😊 开心</option>
                    <option value="放松">😌 放松</option>
                    <option value="兴奋">🎉 兴奋</option>
                    <option value="疲惫">😴 疲惫</option>
                    <option value="压力">😫 压力</option>
                    <option value="平静">🧘 平静</option>
                  </select>
                </div>

                <button 
                  @click="getAIRecommendation" 
                  :disabled="aiLoading || !isAIFormValid"
                  class="recommend-btn"
                >
                  {{ aiLoading ? '推荐中...' : '获取AI推荐' }}
                </button>
              </div>
            </div>

            <!-- 推荐小贴士 -->
            <div class="ai-tips-section">
              <div class="tips-card">
                <h3>💡 推荐小贴士</h3>
                <ul class="tips-list">
                  <li>选择准确的天气信息，获得更合适的饮品推荐</li>
                  <li>根据当前心情选择，AI会推荐匹配的饮品</li>
                  <li>口味偏好越具体，推荐结果越精准</li>
                  <li>可以多次尝试不同的组合</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 右侧：推荐结果 -->
          <div class="ai-right-section">
            <!-- 错误提示 -->
            <div v-if="aiError" class="error-message">
              {{ aiError }}
            </div>

            <!-- 推荐结果 -->
            <div v-if="aiRecommendation" class="recommendation-result">
              <div class="result-card">
                <div class="result-header">
                  <h3>🎯 为您推荐</h3>
                  <span class="ai-badge">AI推荐</span>
                </div>
                
                <div class="product-info">
                  <h4 class="product-name">{{ aiRecommendation.recommendedProduct }}</h4>
                  <div class="reasoning">
                    <strong>推荐理由：</strong>
                    <p>{{ aiRecommendation.reasoning }}</p>
                  </div>
                  <div class="description">
                    <strong>产品描述：</strong>
                    <p>{{ aiRecommendation.description }}</p>
                  </div>
                </div>

                <div class="result-actions">
                  <button @click="addAIToCart" class="add-cart-btn">
                    🛒 加入购物车
                  </button>
                  <button @click="getAnotherRecommendation" class="another-btn">
                    🔄 再推荐一个
                  </button>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="!aiLoading" class="empty-recommendation">
              <div class="empty-state">
                <span class="empty-icon">🤖</span>
                <p class="empty-text">填写左侧信息获取AI推荐</p>
                <p class="empty-subtext">AI将根据您的口味、天气和心情推荐最适合的奶茶</p>
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="aiLoading" class="loading-recommendation">
              <div class="loading-spinner"></div>
              <p>AI正在思考推荐...</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 地域推荐页面 -->
<div v-else-if="activeCategory === 'region'" class="region-recommendation-page">
  <div class="region-page-header">
    <h1>🗺️ 地域特色推荐</h1>
    <p class="region-page-subtitle">探索不同地域的特色茶饮，品味地方文化</p>
  </div>
  
  <!-- 直接内嵌地域推荐组件 -->
  <RegionRecommendation 
    :userId="userId"
    :username="username"
    style="margin-top: 20px;"
    @cart-updated="loadCart"
  />
</div>

<!-- 在 Welcome.vue 模板中 -->
<div v-else-if="activeCategory === 'festival-recommend'" class="festival-recommendation-page">
  <div class="festival-page-header">
    <h1>🎉 节日智能推荐</h1>
    <p class="festival-page-subtitle">根据当前时间智能推荐节日限定饮品</p>
  </div>
  
  <!-- 直接内嵌节日推荐组件 -->
  <FestivalRecommendation 
    :userId="userId"
    :username="username"
    style="margin-top: 20px;"
    @cart-updated="loadCart"
  />
</div>

      <!-- 原有商品页面 -->
      <div v-else class="product-page">
        <div class="content-header">
          <h1>{{ getCategoryTitle }}</h1>
          <p class="category-description">{{ getCategoryDescription }}</p>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-indicator">
            加载中...
          </div>
          
<!-- ========== 天气显示和筛选 ========== -->
<div class="weather-widget" v-if="activeCategory === 'weather'">
  <!-- 天气信息展示区 -->
  <div class="weather-header">
    <!-- 自动获取的天气信息 -->
    <div v-if="weatherInfo.autoDetected" class="auto-weather-info">
      <div class="weather-status">
        <span class="weather-emoji">{{ weatherInfo.emoji }}</span>
        <div class="weather-text">
          <h3>{{ weatherInfo.city }} · {{ weatherInfo.temperature }}°C</h3>
          <p>{{ weatherInfo.description }}</p>
                    <!-- 🆕 新增：温度区间提示 -->
          <div class="temperature-tip" v-if="weatherInfo.temperature">
            <small>温度区间：{{ getTemperatureRange(weatherInfo.temperature) }}</small>
          </div>
        </div>
      </div>
      <div class="weather-source">
        <span class="api-badge">🌐 实时天气数据</span>
        <span class="update-time">{{ weatherInfo.time }}更新</span>
      </div>
    </div>
    
    <!-- 手动选择天气（备用） -->
    <div v-else class="manual-weather-info">
      <div class="weather-status">
        <span class="weather-emoji">📍</span>
        <div class="weather-text">
          <h3>请选择天气类型</h3>
          <p>我们将根据天气推荐最适合的饮品</p>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 天气筛选 -->
  <div class="weather-filter-section">
    <div class="filter-header">
      <h4>天气筛选</h4>
      <div class="filter-actions">
        <button 
          class="auto-detect-btn" 
          @click="autoDetectWeather"
          :disabled="weatherInfo.loading"
        >
          <span v-if="weatherInfo.loading">🔄 检测中...</span>
          <span v-else>🌐 自动检测天气</span>
        </button>
        <button 
          class="reset-btn" 
          @click="resetWeatherFilter"
          v-if="selectedWeather !== 'all'"
        >
          重置筛选
        </button>
      </div>
    </div>
    
    <div class="filter-buttons">
      <button
        v-for="weather in weatherTypes"
        :key="weather.value"
        :class="['weather-btn', { 'active': selectedWeather === weather.value }]"
        @click="selectWeather(weather.value)"
      >
        {{ weather.label }}
      </button>
    </div>
    
    <!-- 当前筛选状态 -->
    <div class="current-filter" v-if="selectedWeather !== 'all'">
      <div class="filter-status">
        <span class="filter-icon">🎯</span>
        <span class="filter-text">
          <span v-if="weatherInfo.autoDetected">根据当前天气，为您推荐：</span>
          <span v-else>已筛选：</span>
          <strong>{{ getWeatherLabel(selectedWeather) }}</strong>
        </span>
      </div>
      <button @click="clearWeatherFilter" class="clear-filter-btn">
        清除筛选
      </button>
    </div>
  </div>
  
  <!-- 推荐理由 -->
  <div class="weather-recommendation" v-if="weatherInfo.autoDetected && selectedWeather !== 'all'">
    <div class="recommendation-card">
      <div class="recommendation-header">
        <span class="recommend-icon">💡</span>
        <h4>推荐理由</h4>
      </div>
      <p class="recommendation-text">{{ getWeatherRecommendationText(selectedWeather) }}</p>
    </div>
  </div>

    <!-- 推荐理由（基于温度） -->
  <div class="weather-recommendation" v-if="weatherInfo.autoDetected && weatherInfo.temperature">
    <div class="recommendation-card">
      <div class="recommendation-header">
        <span class="recommend-icon">🌡️</span>
        <h4>温度推荐</h4>
      </div>
      <p class="recommendation-text">{{ getTemperatureRecommendation(weatherInfo.temperature) }}</p>
    </div>
  </div>
</div>
<!-- ========== 天气部分结束 ========== -->


          
          <!-- 节日显示和筛选 -->
          <div class="festival-widget" v-if="activeCategory === 'festival'">
            <div class="festival-info">
              <span class="festival-icon">🎉</span>
              <span class="festival-text">节日限定，错过就要等明年哦</span>
            </div>
            
            <!-- 添加节日筛选 -->
            <div class="festival-filter">
              <div class="filter-buttons">
                <button
                  v-for="season in festivalSeasons"
                  :key="season.value"
                  :class="['festival-btn', { 'active': selectedFestival === season.value }]"
                  @click="selectFestival(season.value)"
                >
                  {{ season.label }}
                </button>
              </div>
              
              <div class="current-filter" v-if="selectedFestival !== 'all'">
                <span>当前筛选：{{ getFestivalLabel(selectedFestival) }}</span>
                <button @click="clearFestivalFilter" class="clear-filter-btn">
                  清除筛选
                </button>
              </div>
            </div>
          </div>
          
        </div>
        
        <!-- 商品网格 -->
        <div class="products-grid">
          <div 
            v-for="(product, index) in filteredProducts" 
            :key="product.id"
            class="product-card"
            @click="selectProduct(product)"
          >
            <!-- 🎯 修改：使用getProductImage方法 -->
            <div class="product-image">
              <img 
                :src="getProductImage(product, index)" 
                :alt="product.name"
                class="product-img"
                @error="handleImageError"
              />
              <span class="product-emoji">
                {{ product.emoji || '🥤' }}
              </span>
            </div>
            
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-tags">
                <span 
                  v-for="tag in getProductTags(product)" 
                  :key="tag"
                  class="product-tag"
                >
                  {{ tag }}
                </span>
              </div>
              <div class="product-footer">
                <span class="product-price">¥{{ product.price }}</span>
                <button class="add-to-cart-btn" @click.stop="addToCart(product)">
                  +
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && filteredProducts.length === 0" class="empty-state">
          <span class="empty-icon">🥤</span>
          <p class="empty-text">该分类下暂无商品</p>
        </div>
      </div>
      
      <!-- 购物车 -->
      <div class="cart-sidebar" :class="{ active: showCart }">
        <div class="cart-header">
          <h3>购物车</h3>
          <button @click="toggleCart" class="close-cart">×</button>
        </div>
        <div class="cart-items">
          <div 
            v-for="(item, index) in cart" 
            :key="index"
            class="cart-item"
          >
            <span class="item-name">{{ item.name }}</span>
            <div class="item-controls">
              <button @click="decreaseQuantity(index)">-</button>
              <span class="item-quantity">{{ item.quantity }}</span>
              <button @click="increaseQuantity(index)">+</button>
            </div>
            <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            <button @click="removeFromCart(index)" class="remove-btn">×</button>
          </div>
        </div>
        <div class="cart-footer">
          <div class="cart-total">
            总计: ¥{{ cartTotal.toFixed(2) }}
          </div>
          <button 
            class="checkout-btn" 
            :disabled="cart.length === 0" 
            @click="createOrder"
          >
            {{ ordering ? '下单中...' : '立即下单' }}
          </button>
        </div>
      </div>
      
      <!-- 购物车浮动按钮 -->
      <button @click="toggleCart" class="cart-float-btn">
        🛒 {{ cartTotalQuantity }}
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

import RegionRecommendation from './RegionRecommendation.vue';
import FestivalRecommendation from './FestivalRecommendation.vue';

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api';

export default {
  name: 'Welcome',
    components: {
    RegionRecommendation , // 🆕 添加这行
     FestivalRecommendation, 
  },
  props: {
    username: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      activeCategory: 'recommend',
      showCart: false,
      cart: [],
      loading: false,
      ordering: false,
      categories: [
        { id: 'recommend', name: '个性推荐', icon: '⭐' },
        { id: 'ai', name: 'AI智能推荐', icon: '🤖' },
        { id: 'region', name: '地域特色', icon: '🗺️' },
        { id: 'weather', name: '天气特饮', icon: '☁️' },
        { id: 'festival', name: '节日限定', icon: '🎉' },
        { id: 'festival-recommend', name: '节日推荐', icon: '🎉' },
        { id: 'classic', name: '经典系列', icon: '🏆' },
        { id: 'fruit', name: '果茶系列', icon: '🍓' },
        { id: 'milktea', name: '奶茶系列', icon: '🥤' },
        { id: 'special', name: '特色系列', icon: '✨' }
      ],
      products: [],
      userId: 1,

            // ========== 新增的天气信息数据 ==========
      weatherInfo: {
        loading: false,
        autoDetected: false,
        city: '未知',
        temperature: 0,
        description: '天气信息',
        emoji: '☁️',
        time: '刚刚',
        humidity: 0,
        windSpeed: 0
      },

          // ========== 在这里添加天气筛选相关数据 ==========
    selectedWeather: 'all', // 默认显示全部
    weatherTypes: [
      { value: 'all', label: '全部展示' },
      { value: '晴天', label: '☀️ 晴天' },
      { value: '雨天', label: '🌧️ 雨天' },
      { value: '阴天', label: '☁️ 阴天' },
      { value: '雪天', label: '❄️ 雪天' },
      { value: '炎热', label: '🔥 炎热' },
      { value: '寒冷', label: '🥶 寒冷' }
    ],
    // ========== 添加结束 ==========

        // ========== 添加节日筛选数据 ==========
    selectedFestival: 'all', // 默认显示全部
    festivalSeasons: [
      { value: 'all', label: '全部展示' },
      { value: 'q1', label: '🎉 1-3月（元旦/春节/元宵）' },
      { value: 'q2', label: '🌸 4-6月（清明/端午/儿童节）' },
      { value: 'q3', label: '🎑 7-9月（七夕/中秋）' },
      { value: 'q4', label: '🎃 10-12月（国庆/万圣/圣诞）' }
    ],
      
      // AI推荐相关数据
      aiForm: {
        userPreference: '',
        weather: '',
        mood: ''
      },
      aiRecommendation: null,
      aiLoading: false,
      aiError: ''
    }
  },
computed: {
  filteredProducts() {
  if (this.activeCategory === 'recommend') {
    // 1. 先获取默认的4个推荐商品
    const defaultProducts = this.getDefaultProducts().filter(
      product => product.category === 'recommend'
    );
    
    // 2. 获取新增的推荐商品（从API）
    const newProducts = this.products.filter(
      product => product.category === 'recommend'
    ).filter(apiProduct => 
      // 排除掉已经在默认商品中的（按名称判断）
      !defaultProducts.some(defaultProduct => 
        defaultProduct.name === apiProduct.name
      )
    );
    
    // 3. 合并：默认商品在前，新增商品在后
    const allProducts = [...defaultProducts, ...newProducts];
    
    console.log('🎯 默认商品:', defaultProducts.map(p => p.name));
    console.log('🎯 新增商品:', newProducts.map(p => p.name));
    console.log('🎯 合并后总数:', allProducts.length);
    
    // 4. 限制显示数量（比如最多显示6个）
    const maxDisplayCount = 6;
    return allProducts.slice(0, maxDisplayCount);
  }

// ========== 基于实际温度的天气筛选 ==========
if (this.activeCategory === 'weather') {
  let filtered = this.products.filter(product => product.category === 'weather');
  
  if (this.selectedWeather !== 'all') {
    filtered = filtered.filter(product => {
      const name = product.name || '';
      const desc = product.description || '';
      
      // 根据实际温度推荐不同的饮品
      const currentTemp = this.weatherInfo.temperature || 20;
      
      if (this.selectedWeather === '炎热' || currentTemp >= 28) {
        // 高温推荐冰爽解暑的饮品
        return name.includes('夏日') || name.includes('芒果') || 
               name.includes('冰沙') || name.includes('清爽') ||
               desc.includes('冰镇') || desc.includes('解暑');
      }
      else if (this.selectedWeather === '晴天' || (currentTemp >= 18 && currentTemp < 28)) {
        // 舒适温度推荐清爽果茶
        return name.includes('晴天') || name.includes('蜜桃') ||
               name.includes('阳光') || name.includes('橙子') ||
               desc.includes('清爽') || desc.includes('果茶');
      }
      else if (this.selectedWeather === '阴天' || (currentTemp >= 10 && currentTemp < 18)) {
        // 阴凉天气推荐温热饮品
        return name.includes('春风') || name.includes('秋日') ||
               name.includes('茉莉') || name.includes('桂花') ||
               desc.includes('温润') || desc.includes('暖身');
      }
      else if (this.selectedWeather === '寒冷' || currentTemp < 10) {
        // 寒冷天气推荐高热量的热饮
        return name.includes('冬日') || name.includes('红枣') ||
               name.includes('雪天') || name.includes('巧克力') ||
               desc.includes('温暖') || desc.includes('热饮');
      }
      else if (this.selectedWeather === '雨天') {
        // 雨天推荐姜茶等暖身饮品
        return name.includes('雨天') || name.includes('暖姜') ||
               desc.includes('暖身') || desc.includes('姜茶');
      }
      else if (this.selectedWeather === '雪天') {
        // 雪天推荐高热量的饮品
        return name.includes('雪天') || name.includes('巧克力') ||
               name.includes('坚果') || desc.includes('高热量');
      }
      return true;
    });
  }
  return filtered;
}

    // ========== 修改结束 ==========

    // ========== 新增：节日限定筛选 ==========
    if (this.activeCategory === 'festival') {
      let filtered = this.products.filter(product => product.category === 'festival');
      
      if (this.selectedFestival !== 'all') {
        filtered = filtered.filter(product => {
          const name = product.name || '';
          const desc = product.description || '';
          const searchText = (name + desc).toLowerCase();
          
          if (this.selectedFestival === 'q1') {
            // 1-3月：元旦、春节、元宵
            return searchText.includes('春节') || searchText.includes('元旦') || 
                   searchText.includes('福气') || searchText.includes('迎新');
          }
          if (this.selectedFestival === 'q2') {
            // 4-6月：清明、端午、儿童节
            return searchText.includes('端午') || searchText.includes('粽子') ||
                   searchText.includes('儿童') || searchText.includes('彩虹');
          }
          if (this.selectedFestival === 'q3') {
            // 7-9月：七夕、中秋
            return searchText.includes('七夕') || searchText.includes('星空') ||
                   searchText.includes('中秋') || searchText.includes('月饼');
          }
          if (this.selectedFestival === 'q4') {
            // 10-12月：国庆、万圣、圣诞
            return searchText.includes('圣诞') || searchText.includes('万圣') ||
                   searchText.includes('感恩') || searchText.includes('南瓜');
          }
          return true;
        });
      }
      return filtered;
    }
    // ========== 节日筛选结束 ==========

    return this.products.filter(product => product.category === this.activeCategory);
  },
  
  getCategoryTitle() {
    const category = this.categories.find(cat => cat.id === this.activeCategory);
    return category ? category.name : '个性推荐';
  },
  
  getCategoryDescription() {
    const descriptions = {
      recommend: '根据您的偏好智能推荐最适合的饮品',
      region: '各地特色茶饮，品味不同地域文化', // 🆕 新增
      weather: '根据当前天气状况推荐的特调饮品',
      festival: '节日限定特饮，错过就要等明年哦',
      classic: '经典永流传，品质有保证',
      fruit: '新鲜水果制作，健康又美味',
      milktea: '香醇奶茶，温暖你的心',
      special: '创意特色饮品，给你不一样的体验'
    };
    return descriptions[this.activeCategory] || '';
  },
  
  cartTotal() {
    return this.cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  },
  
  cartTotalQuantity() {
    return this.cart.reduce((total, item) => total + item.quantity, 0);
  },
  
  isAIFormValid() {
    return this.aiForm.userPreference && 
           this.aiForm.weather && 
           this.aiForm.mood;
  },
  
  // 🆕 新增：当前定位显示
  currentLocationDisplay() {
    if (!this.currentLocation) return '未定位';
    
    const region = this.regions.find(r => 
      r.provinces.includes(this.currentLocation.province)
    );
    
    if (region) {
      return `${this.currentLocation.city} (${region.label})`;
    }
    
    return this.currentLocation.city || this.currentLocation.province || '未知位置';
  }
},

async created() {
  // 从本地存储获取用户信息
  const savedUser = localStorage.getItem('currentUser');
  if (savedUser) {
    const userData = JSON.parse(savedUser);
    
    // ⭐⭐⭐ 关键修复：使用userId而不是id ⭐⭐⭐
    // 因为你的AuthResponse返回的是userId字段
    this.userId = userData.userId;  // 改为userId
    
    this.username = userData.username || '用户';
    console.log('🎯 当前用户:', { 
      userId: this.userId, 
      username: this.username 
    });
    
    await this.loadCart();
  } else {
    this.$router.push('/login');
    return;
  }
  
  await this.loadProducts();
},
methods: {

      // ========== 新增的天气方法 ==========
// 1. 自动检测天气（调用后端获取真实天气）
async autoDetectWeather() {
  this.weatherInfo.loading = true;
  
  try {
    // 第一步：获取用户地理位置
    let position;
    try {
      position = await this.getUserLocation();
    } catch (error) {
      console.log('使用默认位置（贵阳）');
      position = {
        latitude: 26.6470,  // 贵阳纬度
        longitude: 106.6302 // 贵阳经度
      };
    }
    
    // 第二步：调用后端API获取真实天气数据
    const weatherData = await this.getRealWeatherData(position);
    
    // 第三步：根据实际温度判断天气类型
    const temperature = weatherData.temperature;
    let weatherType = this.determineWeatherType(temperature);
    
    // 第四步：更新天气信息
    this.weatherInfo = {
      ...this.weatherInfo,
      loading: false,
      autoDetected: true,
      city: weatherData.city || '贵阳',
      temperature: temperature,
      type: weatherType,
      description: weatherData.description || `${temperature}°C ${weatherType}`,
      emoji: this.getWeatherEmoji(weatherType),
      time: this.formatTime(new Date()),
      humidity: weatherData.humidity || 65,
      windSpeed: weatherData.windSpeed || 2.5
    };
    
    // 第五步：自动根据温度选择筛选条件
    this.selectedWeather = weatherType;
    
    this.$message.success(`已获取${this.weatherInfo.city}的天气数据（${temperature}°C）`);
    
  } catch (error) {
    console.error('天气检测失败:', error);
    // 失败时使用贵阳默认天气
    this.useGuiyangDefaultWeather();
    this.weatherInfo.loading = false;
  }
},

// 2. 调用后端API获取真实天气
async getRealWeatherData(position) {
  try {
    // 调用你的后端API获取贵阳天气
    const response = await axios.post(`${API_BASE_URL}/region/weather`, {
      city: "贵阳",
      latitude: position.latitude,
      longitude: position.longitude
    });
    
    if (response.data) {
      return response.data;
    }
  } catch (error) {
    console.log('调用天气API失败，使用模拟数据');
  }
  
  // 如果API失败，使用贵阳的模拟天气数据
  return this.getGuiyangMockWeather();
},

// 3. 根据实际温度判断天气类型
determineWeatherType(temperature) {
  if (temperature >= 30) {
    return '炎热';
  } else if (temperature >= 25) {
    return '晴天';
  } else if (temperature >= 15) {
    return '晴天'; // 适中温度也归为晴天
  } else if (temperature >= 5) {
    return '阴天';
  } else if (temperature >= 0) {
    return '寒冷';
  } else {
    return '寒冷'; // 零下
  }
},

// 4. 获取天气对应的emoji
getWeatherEmoji(weatherType) {
  const emojiMap = {
    '炎热': '🔥',
    '晴天': '☀️',
    '阴天': '☁️',
    '雨天': '🌧️',
    '雪天': '❄️',
    '寒冷': '🥶'
  };
  return emojiMap[weatherType] || '☁️';
},

// 5. 贵阳默认天气（后备方案）
useGuiyangDefaultWeather() {
  const now = new Date();
  const month = now.getMonth() + 1;
  
  // 根据季节设置贵阳默认温度
  let temperature, weatherType;
  
  if (month >= 6 && month <= 8) {
    // 夏季：贵阳平均22-28°C
    temperature = Math.floor(Math.random() * 7) + 22; // 22-28°C
    weatherType = temperature > 28 ? '炎热' : '晴天';
  } else if (month >= 12 || month <= 2) {
    // 冬季：贵阳平均3-8°C
    temperature = Math.floor(Math.random() * 6) + 3; // 3-8°C
    weatherType = temperature < 5 ? '寒冷' : '阴天';
  } else if (month >= 9 && month <= 11) {
    // 秋季：贵阳平均15-22°C
    temperature = Math.floor(Math.random() * 8) + 15; // 15-22°C
    weatherType = '晴天';
  } else {
    // 春季：贵阳平均12-20°C
    temperature = Math.floor(Math.random() * 9) + 12; // 12-20°C
    weatherType = '晴天';
  }
  
  this.weatherInfo = {
    ...this.weatherInfo,
    autoDetected: true,
    city: '贵阳',
    temperature: temperature,
    type: weatherType,
    description: `${temperature}°C ${weatherType}`,
    emoji: this.getWeatherEmoji(weatherType),
    time: this.formatTime(now),
    humidity: 75, // 贵阳湿度较高
    windSpeed: 2.0
  };
  
  this.selectedWeather = weatherType;
},

// 6. 贵阳模拟天气数据
getGuiyangMockWeather() {
  const now = new Date();
  const month = now.getMonth() + 1;
  const hour = now.getHours();
  
  let temperature, description;
  
  // 基于贵阳的气候特点
  if (month >= 6 && month <= 8) {
    // 夏季：凉爽，很少超过30°C
    temperature = Math.floor(Math.random() * 8) + 20; // 20-27°C
    description = hour >= 10 && hour <= 17 ? '温暖舒适' : '凉爽宜人';
  } else if (month >= 12 || month <= 2) {
    // 冬季：湿冷，很少零下
    temperature = Math.floor(Math.random() * 6) + 2; // 2-7°C
    description = '湿冷天气';
  } else {
    // 春秋季：非常舒适
    temperature = Math.floor(Math.random() * 10) + 12; // 12-21°C
    description = '气候宜人';
  }
  
  return {
    city: '贵阳',
    temperature: temperature,
    description: description,
    humidity: Math.floor(Math.random() * 20) + 70, // 70-90%
    windSpeed: Math.floor(Math.random() * 5) + 1 // 1-5 m/s
  };
},
    
    // 2. 获取用户地理位置
    getUserLocation() {
      return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
          reject(new Error('浏览器不支持地理位置'));
          return;
        }
        
        navigator.geolocation.getCurrentPosition(
          (position) => {
            resolve({
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            });
          },
          (error) => {
            // 模拟默认位置（北京）
            resolve({
              latitude: 39.9042,
              longitude: 116.4074
            });
          }
        );
      });
    },
    
    // 3. 模拟天气数据（看起来像后端返回）
    getMockWeatherData(position) {
      // 根据地理位置模拟城市
      const cities = [
        { name: '北京', lat: 39.9042, lon: 116.4074 },
        { name: '上海', lat: 31.2304, lon: 121.4737 },
        { name: '广州', lat: 23.1291, lon: 113.2644 },
        { name: '深圳', lat: 22.5431, lon: 114.0579 },
        { name: '杭州', lat: 30.2741, lon: 120.1551 },
        { name: '贵阳', lat: 30.5728, lon: 104.0668 }
      ];
      
      // 找到最近的城市
      let nearestCity = cities[0];
      let minDistance = Infinity;
      
      cities.forEach(city => {
        const distance = Math.sqrt(
          Math.pow(city.lat - position.latitude, 2) + 
          Math.pow(city.lon - position.longitude, 2)
        );
        if (distance < minDistance) {
          minDistance = distance;
          nearestCity = city;
        }
      });
      
      // 根据季节和时间模拟天气类型
      const now = new Date();
      const month = now.getMonth() + 1;
      const hour = now.getHours();
      
      // 天气类型映射
      let weatherType, emoji, description, temperature;
      
      if (month >= 6 && month <= 8) {
        // 夏季
        if (hour >= 10 && hour <= 17) {
          weatherType = '炎热';
          emoji = '🔥';
          description = '炎热晴朗';
          temperature = Math.floor(Math.random() * 5) + 30; // 30-35°C
        } else {
          weatherType = '晴天';
          emoji = '☀️';
          description = '晴朗';
          temperature = Math.floor(Math.random() * 5) + 25; // 25-30°C
        }
      } else if (month >= 12 || month <= 2) {
        // 冬季
        weatherType = '寒冷';
        emoji = '🥶';
        description = '寒冷';
        temperature = Math.floor(Math.random() * 10) - 5; // -5~5°C
      } else if (month >= 9 && month <= 11) {
        // 秋季
        weatherType = '阴天';
        emoji = '☁️';
        description = '多云';
        temperature = Math.floor(Math.random() * 5) + 15; // 15-20°C
      } else {
        // 春季
        weatherType = '晴天';
        emoji = '🌤️';
        description = '部分多云';
        temperature = Math.floor(Math.random() * 5) + 18; // 18-23°C
      }
      
      return {
        city: nearestCity.name,
        type: weatherType,
        emoji: emoji,
        description: description,
        temperature: temperature,
        time: this.formatTime(now),
        humidity: Math.floor(Math.random() * 30) + 50, // 50-80%
        windSpeed: Math.floor(Math.random() * 10) + 1 // 1-10 m/s
      };
    },
    
    // 4. 格式化时间
    formatTime(date) {
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    },
    
    // 5. 重置天气筛选
    resetWeatherFilter() {
      this.selectedWeather = 'all';
      this.weatherInfo.autoDetected = false;
    },
    
    // 6. 获取天气推荐文案
    getWeatherRecommendationText(weatherType) {
      const recommendations = {
        '晴天': '晴天时人体容易出汗，清爽的水果茶能补充水分和维生素，如蜜桃乌龙、柠檬绿茶等。',
        '雨天': '雨天潮湿阴冷，一杯温热的姜茶或奶茶能驱散寒意，温暖身心。',
        '阴天': '阴天心情容易沉闷，一杯色彩鲜艳、口感丰富的饮品能带来好心情。',
        '雪天': '雪天寒冷，高热量的巧克力或坚果饮品能提供充足能量和温暖。',
        '炎热': '炎热天气需要解暑降温，冰沙、气泡水等冰镇饮品是不错的选择。',
        '寒冷': '寒冷时适合热饮，红枣、桂圆等温补食材的饮品能增强抵抗力。'
      };
      return recommendations[weatherType] || '根据当前天气为您推荐合适的饮品。';
    },

    // 获取基于温度的推荐理由
getTemperatureRecommendation(temperature) {
  if (temperature >= 30) {
    return `当前温度${temperature}°C较高，推荐冰爽解暑的饮品，如芒果冰沙、柠檬绿茶等，帮助降温消暑。`;
  } else if (temperature >= 25) {
    return `当前温度${temperature}°C温暖舒适，推荐清爽果茶或奶茶，如蜜桃乌龙、珍珠奶茶等。`;
  } else if (temperature >= 18) {
    return `当前温度${temperature}°C宜人，推荐口感丰富的特色饮品，如芝士奶盖、芋圆奶茶等。`;
  } else if (temperature >= 10) {
    return `当前温度${temperature}°C稍凉，推荐温热饮品，如姜茶、红枣奶茶等温暖身心。`;
  } else if (temperature >= 0) {
    return `当前温度${temperature}°C较冷，推荐高热量的热饮，如巧克力、坚果奶茶等补充能量。`;
  } else {
    return `当前温度${temperature}°C寒冷，推荐特制的冬季热饮，温暖整个冬季。`;
  }
},

getTemperatureRange(temperature) {
  if (temperature >= 30) {
    return "炎热 (>30°C)";
  } else if (temperature >= 25) {
    return "温暖 (25-30°C)";
  } else if (temperature >= 18) {
    return "舒适 (18-25°C)";
  } else if (temperature >= 10) {
    return "凉爽 (10-18°C)";
  } else if (temperature >= 0) {
    return "寒冷 (0-10°C)";
  } else {
    return "严寒 (<0°C)";
  }
},

    // ========== 添加天气筛选相关方法 ==========
  selectWeather(weather) {
    this.selectedWeather = weather;
  },
  
  clearWeatherFilter() {
    this.selectedWeather = 'all';
  },
  
  getWeatherLabel(weatherValue) {
    const weather = this.weatherTypes.find(w => w.value === weatherValue);
    return weather ? weather.label.replace(/[^\u4e00-\u9fa5]/g, '') : '';
  },

    // ========== 添加节日筛选方法 ==========
  selectFestival(season) {
    this.selectedFestival = season;
  },
  
  clearFestivalFilter() {
    this.selectedFestival = 'all';
  },
  
  getFestivalLabel(seasonValue) {
    const season = this.festivalSeasons.find(s => s.value === seasonValue);
    return season ? season.label : '';
  },
  
  // 🎯 新增：获取本地图片URL（图片在public/images）
  getLocalImageUrl(index) {
    // 使用相对路径引用public/images中的图片
    // index从0开始，图片从1.png开始，所以加1
    const imageNumber = (index % 60) + 1;
    return `/images/${imageNumber}.png`;
  },
  
  // 🎯 新增：图片加载失败时的处理
  handleImageError(event) {
    console.log('❌ 图片加载失败，检查路径:', event.target.src);
    event.target.style.display = 'none';
    
    // 显示emoji
    const emoji = event.target.parentElement.querySelector('.product-emoji');
    if (emoji) {
      emoji.style.opacity = '1';
    }
  },

  // 🎯 新增：根据商品名称获取对应的图片ID
  getImageIdByName(productName, category) {
    const name = productName.toLowerCase();
    
    // 天气特饮 (1-10)
    if (category === 'weather') {
      if (name.includes('晴天') || name.includes('蜜桃')) return 1;
      if (name.includes('雨天') || name.includes('暖姜')) return 2;
      if (name.includes('雪天') || name.includes('巧克力')) return 3;
      if (name.includes('夏日') || name.includes('芒果')) return 4;
      if (name.includes('冬日') || name.includes('红枣')) return 5;
      if (name.includes('春风') || name.includes('茉莉')) return 6;
      if (name.includes('秋日') || name.includes('桂花')) return 7;
      if (name.includes('热带风暴')) return 8;
      if (name.includes('阳光') || name.includes('橙子')) return 9;
      if (name.includes('雾霾') || name.includes('清肺')) return 10;
    }
    
    // 节日限定 (11-20)
    else if (category === 'festival') {
      if (name.includes('圣诞')) return 11;
      if (name.includes('春节') || name.includes('福气')) return 12;
      if (name.includes('情人节') || name.includes('甜蜜')) return 13;
      if (name.includes('万圣节') || name.includes('南瓜')) return 14;
      if (name.includes('中秋') || name.includes('月饼')) return 15;
      if (name.includes('七夕') || name.includes('星空')) return 16;
      if (name.includes('端午') || name.includes('粽子')) return 17;
      if (name.includes('元旦') || name.includes('迎新')) return 18;
      if (name.includes('儿童节') || name.includes('彩虹')) return 19;
      if (name.includes('感恩节') || name.includes('南瓜派')) return 20;
    }
    
    // 特色创意 (21-30)
    else if (category === 'special') {
      if (name.includes('星空奶茶')) return 21;
      if (name.includes('泡泡浴')) return 22;
      if (name.includes('火山熔岩')) return 23;
      if (name.includes('海洋之心')) return 24;
      if (name.includes('森林迷雾')) return 25;
      if (name.includes('银河系')) return 26;
      if (name.includes('彩虹云朵')) return 27;
      if (name.includes('魔法药水')) return 28;
      if (name.includes('钻石冰晶')) return 29;
      if (name.includes('火焰山')) return 30;
    }
    
    // 奶茶系列 (31-45)
    else if (category === 'milktea') {
      if (name.includes('珍珠奶茶')) return 31;
      if (name.includes('芋圆奶茶')) return 32;
      if (name.includes('红豆奶茶')) return 33;
      if (name.includes('布丁奶茶')) return 34;
      if (name.includes('椰果奶茶')) return 35;
      if (name.includes('仙草冻')) return 36;
      if (name.includes('燕麦奶茶')) return 37;
      if (name.includes('黑糖珍珠')) return 38;
      if (name.includes('焦糖奶茶')) return 39;
      if (name.includes('丝袜奶茶')) return 40;
      if (name.includes('抹茶拿铁')) return 41;
      if (name.includes('巧克力奶茶')) return 42;
      if (name.includes('芝士奶盖')) return 43;
      if (name.includes('伯爵奶茶')) return 44;
      if (name.includes('阿华田')) return 45;
    }
    
    // 水果茶饮 (46-55)
    else if (category === 'fruit') {
      if (name.includes('草莓果茶')) return 46;
      if (name.includes('芒果冰沙')) return 47;
      if (name.includes('百香果')) return 48;
      if (name.includes('葡萄多多')) return 49;
      if (name.includes('桃子乌龙')) return 50;
      if (name.includes('柠檬绿茶')) return 51;
      if (name.includes('西瓜汁')) return 52;
      if (name.includes('菠萝冰茶')) return 53;
      if (name.includes('蓝莓酸奶')) return 54;
      if (name.includes('石榴气泡')) return 55;
    }
    
    // 经典茶饮 (56-60)
    else if (category === 'classic') {
      if (name.includes('四季春茶')) return 56;
      if (name.includes('铁观音')) return 57;
      if (name.includes('龙井绿茶')) return 58;
      if (name.includes('普洱熟茶')) return 59;
      if (name.includes('大红袍')) return 60;
    }
    
    // 默认返回1
    return 1;
  },

  // 🎯 修改：获取商品图片（优先使用后端返回的imageUrl，没有就根据商品名称匹配本地图片）
  getProductImage(product, index) {
    // 如果后端有返回imageUrl，使用后端图片
    if (product.imageUrl && product.imageUrl.trim() !== '') {
      return 'http://localhost:8081' + product.imageUrl;
    }
    
    // 否则根据商品名称匹配本地public/images中的图片
    const imageId = this.getImageIdByName(product.name, product.category);
    return `/images/${imageId}.png`;
  },

async addToCart(product) {
  try {
    console.log('🛒 添加商品到购物车:', product, '用户ID:', this.userId);
    
    const response = await axios.post(`${API_BASE_URL}/cart/${this.userId}/add`, null, {
      params: {
        productId: product.id,
        quantity: 1
      }
    });
    
    console.log('✅ 添加成功:', response.data);
    
    await this.loadCart();
    
    this.showCart = true;
    this.$message.success('已添加到购物车');
  } catch (error) {
    console.error('❌ 添加到购物车失败:', error);
    
    if (error.response?.status === 400) {
      // 如果是400错误，可能是用户ID无效
      this.$message.error('用户不存在，请重新登录');
      this.$router.push('/login');
    } else {
      this.$message.error('添加到购物车失败');
    }
  }
},

    getProductTags(product) {
      if (product.tags && typeof product.tags === 'string') {
        return product.tags.split(',').map(tag => tag.trim());
      }
      return ['特色'];
    },

async loadProducts() {
  this.loading = true;
  try {
    const response = await axios.get(`${API_BASE_URL}/products`);
    this.products = response.data;
    
    // 🎯 修改调试信息
    console.log('=== 图片调试信息 ===');
    this.products.slice(0, 3).forEach((product, index) => {
      console.log(`产品: ${product.name}`);
      console.log(`ID: ${product.id}`);
      console.log(`分类: ${product.category}`);
      console.log(`后端返回的imageUrl: ${product.imageUrl}`);
      console.log(`匹配的图片ID: ${this.getImageIdByName(product.name, product.category)}`);
      console.log(`最终图片路径: ${this.getProductImage(product, index)}`);
      console.log('---');
    });
    
  } catch (error) {
    console.error('加载商品失败:', error);
    this.products = this.getDefaultProducts();
  } finally {
    this.loading = false;
  }
},

    async loadCart() {
      try {
        console.log('🛒 加载购物车，用户ID:', this.userId);
        const response = await axios.get(`${API_BASE_URL}/cart/${this.userId}`);
        console.log('购物车响应:', response.data);
        const cartData = response.data;
        this.cart = cartData.cartItems.map(item => ({
          id: item.productId,
          name: item.productName,
          price: item.productPrice,
          quantity: item.quantity
        }));
      } catch (error) {
        console.error('❌ 加载购物车失败:', error);
        if (error.response) {
          console.error('错误详情:', error.response.data);
        }
        this.cart = [];
      }
    },

    switchCategory(categoryId) {
      this.activeCategory = categoryId;
          if (categoryId === 'weather') {
      this.selectedWeather = 'all';
    }
        if (categoryId === 'festival') {
      this.selectedFestival = 'all';
    }
      if (categoryId === 'ai') {
        this.aiRecommendation = null;
        this.aiError = '';
      }
    },

    selectProduct(product) {
      console.log('选中商品:', product);
    },

    async addAIToCart() {
      console.log('=== 🚨 addAIToCart方法被调用 ===');
      
      if (!this.aiRecommendation || !this.aiRecommendation.productId) {
        console.error('❌ 错误：商品信息不完整');
        this.$message.error('无法添加商品到购物车：商品信息不完整');
        return;
      }

      console.log('📊 AI推荐数据:', this.aiRecommendation);
      console.log('🎯 商品ID:', this.aiRecommendation.productId);
      console.log('🎯 用户ID:', this.userId);

      try {
        const response = await axios.post(`${API_BASE_URL}/cart/${this.userId}/add`, null, {
          params: {
            productId: this.aiRecommendation.productId,
            quantity: 1
          }
        });

        console.log('✅ 添加成功，响应数据:', response.data);
        
        await this.loadCart();
        
        this.$message.success(`"${this.aiRecommendation.recommendedProduct}" 已成功加入购物车！`);
        this.showCart = true;

      } catch (error) {
        console.error('❌ 添加购物车异常:', error);
        if (error.response) {
          console.error('错误详情:', error.response.data);
        }
        this.$message.error('添加到购物车失败，请稍后重试');
      }
    },

    async removeFromCart(index) {
      const item = this.cart[index];
      try {
        console.log('移除商品:', item.id, '用户:', this.userId);
        
        const response = await axios.delete(`${API_BASE_URL}/cart/${this.userId}/remove`, {
          params: {
            productId: item.id
          }
        });
        
        console.log('移除响应:', response.data);
        this.cart.splice(index, 1);
        this.$message.success('已从购物车移除');
        
        await this.loadCart();
        
      } catch (error) {
        console.error('移除商品失败:', error);
        if (error.response) {
          console.error('错误响应:', error.response.data);
        }
        this.$message.error('移除商品失败: ' + (error.response?.data?.message || error.message));
      }
    },

    async increaseQuantity(index) {
      const item = this.cart[index];
      try {
        await axios.put(`${API_BASE_URL}/cart/${this.userId}/update`, null, {
          params: {
            productId: item.id,
            quantity: item.quantity + 1
          }
        });
        item.quantity++;
      } catch (error) {
        console.error('更新数量失败:', error);
        this.$message.error('更新数量失败');
      }
    },

    async decreaseQuantity(index) {
      const item = this.cart[index];
      if (item.quantity > 1) {
        try {
          await axios.put(`${API_BASE_URL}/cart/${this.userId}/update`, null, {
            params: {
              productId: item.id,
              quantity: item.quantity - 1
            }
          });
          item.quantity--;
        } catch (error) {
          console.error('更新数量失败:', error);
          this.$message.error('更新数量失败');
        }
      } else {
        this.removeFromCart(index);
      }
    },

    async createOrder() {
      if (this.cart.length === 0) return;
      
      this.ordering = true;
      try {
        const response = await axios.post(`${API_BASE_URL}/orders/${this.userId}/create`);
        const order = response.data;
        
        this.$message.success(`订单创建成功！订单号: ${order.orderNumber}`);
        this.cart = [];
        this.showCart = false;
      } catch (error) {
        console.error('创建订单失败:', error);
        this.$message.error('创建订单失败');
      } finally {
        this.ordering = false;
      }
    },

    toggleCart() {
      this.showCart = !this.showCart;
    },

    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        this.$emit('logout');
      }
    },

    getDefaultProducts() {
      return [
        { id: 1, category: 'recommend', name: '智能推荐奶茶', emoji: '🤖', description: '根据您的口味偏好智能调配', price: 25, tags: '智能,个性化' },
        { id: 2, category: 'recommend', name: '心情特调', emoji: '💕', description: '根据今日心情特别调制', price: 28, tags: '心情,特调' },
        { id: 3, category: 'weather', name: '晴天蜜桃', emoji: '🍑', description: '清爽蜜桃搭配晴天气息', price: 22, tags: '晴天,果茶' },
        { id: 4, category: 'weather', name: '雨天暖姜', emoji: '☕', description: '温暖姜茶驱散雨天寒意', price: 20, tags: '雨天,暖饮' },
        { id: 5, category: 'weather', name: '雪天巧克力', emoji: '🍫', description: '浓郁巧克力温暖整个雪天', price: 26, tags: '雪天,热饮' },
        { id: 6, category: 'festival', name: '圣诞限定', emoji: '🎄', description: '圣诞特调暖心奶茶', price: 30, tags: '圣诞,限定' },
        { id: 7, category: 'festival', name: '春节福气茶', emoji: '🧧', description: '春节特饮，福气满满', price: 28, tags: '春节,福气' },
        { id: 8, category: 'festival', name: '情人节甜蜜', emoji: '❤️', description: '浪漫情人节限定饮品', price: 32, tags: '情人节,浪漫' },
        { id: 9, category: 'classic', name: '珍珠奶茶', emoji: '⚫', description: '经典珍珠奶茶，永不过时', price: 18, tags: '经典,珍珠' },
        { id: 10, category: 'classic', name: '芋圆奶茶', emoji: '🟣', description: '香糯芋圆搭配醇香奶茶', price: 20, tags: '经典,芋圆' },
        { id: 11, category: 'fruit', name: '草莓果茶', emoji: '🍓', description: '新鲜草莓搭配清茶', price: 24, tags: '果茶,草莓' },
        { id: 12, category: 'fruit', name: '芒果冰沙', emoji: '🥭', description: '香甜芒果制成冰沙', price: 26, tags: '果茶,冰沙' },
        { id: 13, category: 'milktea', name: '抹茶拿铁', emoji: '🍵', description: '日式抹茶搭配香醇拿铁', price: 23, tags: '奶茶,抹茶' },
        { id: 14, category: 'milktea', name: '焦糖布丁', emoji: '🍮', description: '焦糖布丁风味奶茶', price: 25, tags: '奶茶,布丁' },
        { id: 15, category: 'special', name: '星空奶茶', emoji: '🌌', description: '梦幻星空渐变色彩', price: 35, tags: '特色,星空' },
        { id: 16, category: 'special', name: '泡泡浴奶茶', emoji: '🛁', description: '创意泡泡浴造型奶茶', price: 38, tags: '特色,创意' }
      ];
    },

    async getAIRecommendation() {
      if (!this.isAIFormValid) {
        this.aiError = '请填写完整信息';
        return;
      }

      this.aiLoading = true;
      this.aiError = '';
      this.aiRecommendation = null;

      try {
        const response = await axios.post(`${API_BASE_URL}/recommendations/ai-recommendation`, this.aiForm);

        if (response.data) {
          this.aiRecommendation = response.data;
        } else {
          throw new Error('推荐服务暂时不可用');
        }
        
      } catch (err) {
        this.aiError = err.message || '获取推荐失败，请稍后重试';
        console.error('AI推荐错误:', err);
      } finally {
        this.aiLoading = false;
      }
    },

    getAnotherRecommendation() {
      this.aiRecommendation = null;
      this.getAIRecommendation();
    }
  }
}
</script>

<style scoped>
/* ============ 基础布局样式 ============ */
.dashboard-container {
  display: flex;
  height: 100vh;
  background: #f5f6fa;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  margin: 0 0 10px 0;
  font-size: 1.5rem;
  font-weight: bold;
}

.welcome-text {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-nav li {
  padding: 15px 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-nav li:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-nav li.active {
  background: rgba(255, 255, 255, 0.2);
  border-right: 3px solid white;
}

.nav-icon {
  font-size: 1.2rem;
}

.nav-text {
  font-size: 0.95rem;
  font-weight: 500;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 商品页面样式 */
.product-page {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-header {
  margin-bottom: 30px;
}

.content-header h1 {
  color: #333;
  font-size: 2.2rem;
  margin: 0 0 10px 0;
}

.category-description {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  margin-top: 20px;
}

.product-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* 产品图片容器 */
.product-image {
  position: relative;
  height: 180px;
  margin-bottom: 15px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

/* emoji - 绝对定位居中 */
.product-emoji {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 4rem;
  opacity: 0.3;
  z-index: 1;
  transition: opacity 0.3s ease;
}

/* 图片 - 覆盖整个容器 */
.product-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 关键：保持比例，覆盖容器 */
  z-index: 2;
  background: white;
  border-radius: 12px;
}

/* 当图片存在时，emoji完全透明 */
.product-img:not([src=""]) + .product-emoji {
  opacity: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .product-image {
    height: 100px; /* 手机端稍微小一点 */
  }
  
  .product-emoji {
    font-size: 2.5rem;
  }
}

.product-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.2rem;
}

.product-description {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 15px 0;
  line-height: 1.4;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 15px;
}

.product-tag {
  background: #e9ecef;
  color: #495057;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #e74c3c;
}

.add-to-cart-btn {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s;
}

.add-to-cart-btn:hover {
  background: #218838;
  transform: scale(1.1);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 1.2rem;
  margin: 0;
}

/* 加载状态 */
.loading-indicator {
  text-align: center;
  padding: 20px;
  color: #666;
}

/* 购物车样式 */
.cart-sidebar {
  position: fixed;
  right: -400px;
  top: 0;
  width: 380px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  transition: right 0.3s ease;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.cart-sidebar.active {
  right: 0;
}

.cart-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-header h3 {
  margin: 0;
  color: #333;
}

.close-cart {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item-name {
  flex: 1;
  color: #333;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 15px;
}

.item-controls button {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  width: 30px;
  height: 30px;
  border-radius: 4px;
  cursor: pointer;
}

.item-quantity {
  min-width: 30px;
  text-align: center;
}

.item-price {
  color: #e74c3c;
  font-weight: bold;
  min-width: 80px;
  text-align: right;
}

.remove-btn {
  background: #dc3545;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
}

.cart-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
}

.cart-total {
  font-size: 1.3rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.checkout-btn {
  width: 100%;
  padding: 15px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.checkout-btn:hover:not(:disabled) {
  background: #218838;
}

.checkout-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.cart-float-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 50px;
  padding: 15px 20px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  z-index: 999;
  transition: all 0.3s;
}

.cart-float-btn:hover {
  background: #218838;
  transform: scale(1.05);
}

/* ============ AI推荐页面样式 ============ */
.ai-recommendation-page {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.ai-page-header {
  text-align: center;
  margin-bottom: 40px;
}

.ai-page-header h1 {
  color: #333;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.ai-page-subtitle {
  color: #666;
  font-size: 1.1rem;
}

/* AI内容布局 */
.ai-content-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  align-items: start;
}

.ai-left-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-right-section {
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

/* 表单样式 */
.ai-form-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 20px;
}

.recommendation-form {
  width: 100%;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.form-select {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  transition: border-color 0.3s;
}

.form-select:focus {
  outline: none;
  border-color: #667eea;
}

.recommend-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.recommend-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.recommend-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 小贴士样式 */
.ai-tips-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tips-card h3 {
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
  padding: 10px 0;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  padding-left: 20px;
  line-height: 1.5;
}

.tips-list li:before {
  content: "•";
  color: #667eea;
  position: absolute;
  left: 0;
  font-weight: bold;
}

.tips-list li:last-child {
  border-bottom: none;
}

/* 推荐结果样式 */
.recommendation-result {
  flex: 1;
  animation: fadeIn 0.5s ease-in;
}

.result-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
  height: fit-content;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f8f9fa;
}

.result-header h3 {
  color: #333;
  margin: 0;
  font-size: 1.5rem;
}

.ai-badge {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 6px 15px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  margin-bottom: 25px;
}

.product-name {
  color: #2c3e50;
  font-size: 1.8rem;
  margin-bottom: 20px;
  text-align: center;
  font-weight: bold;
}

.reasoning, .description {
  margin-bottom: 20px;
}

.reasoning strong, .description strong {
  color: #333;
  display: block;
  margin-bottom: 8px;
  font-size: 1.1rem;
}

.reasoning p, .description p {
  color: #666;
  line-height: 1.6;
  margin: 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.result-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.add-cart-btn, .another-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.add-cart-btn {
  background: #28a745;
  color: white;
}

.add-cart-btn:hover {
  background: #218838;
  transform: translateY(-1px);
}

.another-btn {
  background: #6c757d;
  color: white;
}

.another-btn:hover {
  background: #545b62;
  transform: translateY(-1px);
}

/* 错误提示 */
.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 20px;
}

/* 空状态 */
.empty-recommendation {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
}

.empty-state {
  text-align: center;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 1.2rem;
  margin-bottom: 10px;
  font-weight: bold;
}

.empty-subtext {
  font-size: 0.9rem;
  color: #888;
}

/* 加载状态 */
.loading-recommendation {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .ai-content-layout {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .ai-form-section {
    position: static;
  }
  
  .ai-left-section {
    order: 2;
  }
  
  .ai-right-section {
    order: 1;
    min-height: auto;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    flex-direction: column;
    height: auto;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
  }
  
  .main-content {
    min-height: calc(100vh - 200px);
  }
  
  .ai-page-header h1 {
    font-size: 2rem;
  }
  
  .ai-form-section {
    padding: 20px;
  }
  
  .result-actions {
    flex-direction: column;
  }
  
  .product-name {
    font-size: 1.5rem;
  }
  
  .ai-content-layout {
    gap: 15px;
  }
  
  .cart-sidebar {
    width: 100%;
    right: -100%;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
}

/* ========== 天气筛选样式 ========== */
.weather-filter {
  margin-top: 20px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.weather-btn {
  padding: 8px 16px;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #495057;
}

.weather-btn:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.weather-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  font-weight: bold;
}

.weather-btn.active:first-child {
  background: #28a745;
}

.current-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 15px;
  border-top: 1px solid #e9ecef;
  color: #666;
  font-size: 14px;
}

.clear-filter-btn {
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.clear-filter-btn:hover {
  background: #e9ecef;
  color: #495057;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-buttons {
    gap: 8px;
  }
  
  .weather-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}

/* ========== 节日筛选样式 ========== */
.festival-filter {
  margin-top: 20px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.festival-btn {
  padding: 8px 16px;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #495057;
}

.festival-btn:hover {
  border-color: #ff6b6b;
  color: #ff6b6b;
  transform: translateY(-2px);
}

.festival-btn.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-color: transparent;
  color: white;
  font-weight: bold;
}

.festival-btn.active:first-child {
  background: #28a745;
}

/* ========== 地域定位样式 ========== */
.region-widget {
  margin-top: 20px;
}

.location-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 25px;
  color: white;
  margin-bottom: 20px;
}

.location-header h2 {
  margin: 0 0 15px 0;
  font-size: 1.8rem;
}

.location-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 15px;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
}

.location-icon {
  font-size: 1.5rem;
}

.location-text {
  font-weight: bold;
}

.location-text.no-location {
  opacity: 0.7;
  font-weight: normal;
}

.location-buttons {
  display: flex;
  gap: 10px;
}

.location-btn {
  padding: 10px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.location-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.location-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.location-error {
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 15px;
  border-radius: 8px;
  margin-top: 10px;
  border-left: 4px solid #ff6b6b;
}

.region-filter {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  margin-top: 15px;
  backdrop-filter: blur(10px);
}

.filter-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 15px;
}

.region-btn {
  padding: 12px 15px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.region-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.region-btn.active {
  background: white;
  color: #667eea;
  font-weight: bold;
  border-color: white;
}

.region-icon {
  font-size: 1.2rem;
}

.region-name {
  font-size: 0.9rem;
}

.current-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.filter-info {
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  opacity: 0.8;
}

.filter-value {
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}

.specialty-tag {
  background: #ff6b6b;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.clear-filter-btn {
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-filter-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .location-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .location-buttons {
    justify-content: center;
  }
  
  .current-filter {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
    text-align: center;
  }
}

/* ========== 新增的天气样式 ========== */
.weather-widget {
  margin-top: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 25px;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
}

.weather-header {
  margin-bottom: 20px;
}

.auto-weather-info, .manual-weather-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.weather-status {
  display: flex;
  align-items: center;
  gap: 15px;
}

.weather-emoji {
  font-size: 3rem;
}

.weather-text h3 {
  margin: 0 0 5px 0;
  font-size: 1.3rem;
}

.weather-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.weather-source {
  text-align: right;
}

.api-badge {
  display: block;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  margin-bottom: 5px;
}

.update-time {
  font-size: 0.8rem;
  opacity: 0.7;
}

.weather-filter-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-header h4 {
  margin: 0;
  font-size: 1.1rem;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.auto-detect-btn, .reset-btn {
  padding: 8px 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.auto-detect-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}

.auto-detect-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reset-btn {
  background: rgba(255, 107, 107, 0.2);
  border-color: rgba(255, 107, 107, 0.3);
}

.reset-btn:hover {
  background: rgba(255, 107, 107, 0.3);
}

.filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.weather-btn {
  padding: 8px 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  flex: 1;
  min-width: 120px;
  text-align: center;
}

.weather-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.weather-btn.active {
  background: white;
  color: #667eea;
  font-weight: bold;
  border-color: white;
}

.weather-btn.active:first-child {
  background: #28a745;
  color: white;
  border-color: #28a745;
}

.current-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.filter-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-icon {
  font-size: 1.2rem;
}

.filter-text {
  opacity: 0.9;
}

.filter-text strong {
  opacity: 1;
  font-weight: bold;
}

.clear-filter-btn {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.clear-filter-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.weather-recommendation {
  margin-top: 20px;
  animation: fadeIn 0.5s ease;
}

.recommendation-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #ff6b6b;
}

.recommendation-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.recommend-icon {
  font-size: 1.2rem;
}

.recommendation-header h4 {
  margin: 0;
  font-size: 1rem;
}

.recommendation-text {
  margin: 0;
  line-height: 1.5;
  opacity: 0.9;
  font-size: 0.9rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .auto-weather-info, .manual-weather-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .weather-source {
    text-align: left;
  }
  
  .filter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .weather-btn {
    min-width: 100px;
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .current-filter {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 温度提示样式 */
.temperature-tip {
  margin-top: 5px;
  padding: 3px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: inline-block;
  font-size: 0.8rem;
}

/* 天气按钮根据温度高亮 */
.weather-btn[data-temp="hot"] {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.weather-btn[data-temp="cold"] {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

</style>