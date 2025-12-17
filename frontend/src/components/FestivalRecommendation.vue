<template>
  <div class="festival-recommendation">
    <!-- 节日推荐页面头部 -->
    <div class="festival-header">
      <div class="header-content">
        <h1 class="festival-title">
          <span class="festival-icon">🎉</span>
          节日智能推荐
        </h1>
        <p class="festival-subtitle">根据当前时间智能推荐节日限定饮品，不错过每一份节日氛围</p>
      </div>
      
      <!-- 时间显示和控制 -->
      <div class="time-controls">
        <div class="current-time">
          <span class="time-icon">🕐</span>
          <span class="time-text">{{ formattedTime }}</span>
          <span class="date-text">{{ formattedDate }}</span>
        </div>
        
        <div class="time-buttons">
          <button 
            @click="refreshRecommendation" 
            class="time-btn refresh-btn"
            :class="{ 'refreshing': isRefreshing }"
          >
            <span class="btn-icon">🔄</span>
            <span class="btn-text">{{ isRefreshing ? '刷新中...' : '刷新推荐' }}</span>
          </button>
          
          <button 
            @click="toggleManualMode" 
            class="time-btn manual-btn"
            :class="{ 'active': isManualMode }"
          >
            <span class="btn-icon">👤</span>
            <span class="btn-text">{{ isManualMode ? '自动模式' : '手动选择' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 手动选择模式 -->
    <div v-if="isManualMode" class="manual-selector">
      <div class="selector-header">
        <h3>📅 手动选择季度</h3>
        <p>选择你想探索的季度特色饮品</p>
      </div>
      
      <div class="quarter-buttons">
        <button
          v-for="quarter in QUARTERS"
          :key="quarter.id"
          :class="['quarter-btn', { 'active': selectedQuarter === quarter.id }]"
          @click="selectQuarter(quarter.id)"
        >
          <span class="quarter-icon">{{ quarter.icon }}</span>
          <span class="quarter-name">{{ quarter.name }}</span>
          <span class="quarter-months">{{ quarter.months }}</span>
        </button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <span class="error-icon">⚠️</span>
      {{ error }}
      <button @click="clearError" class="error-close">×</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">正在分析时间数据，为您推荐节日饮品...</p>
      <p class="loading-subtext">智能推荐算法运行中...</p>
    </div>

    <!-- 推荐内容 -->
    <div v-else-if="currentFestival" class="recommendation-content">
      <!-- 节日信息卡片 -->
      <div class="festival-card">
        <div class="festival-card-header">
          <div class="festival-info">
            <span class="festival-card-icon">{{ currentFestival.icon }}</span>
            <div class="festival-details">
              <h3 class="festival-name">{{ currentFestival.name }}</h3>
              <p class="festival-description">{{ currentFestival.description }}</p>
            </div>
          </div>
          <div class="match-badge" :class="matchType">
            {{ getMatchTypeText(matchType) }}
          </div>
        </div>

        <div class="festival-card-body">
          <div class="festival-stats">
            <div class="stat-item">
              <span class="stat-icon">📅</span>
              <div class="stat-content">
                <span class="stat-label">时间范围</span>
                <span class="stat-value">{{ currentFestival.monthsDesc }}</span>
              </div>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🎯</span>
              <div class="stat-content">
                <span class="stat-label">推荐产品</span>
                <span class="stat-value">{{ festivalProducts.length }}款特色</span>
              </div>
            </div>
            <div class="stat-item">
              <span class="stat-icon">⏰</span>
              <div class="stat-content">
                <span class="stat-label">检测时间</span>
                <span class="stat-value">{{ detectionTime }}</span>
              </div>
            </div>
          </div>

          <!-- 推荐说明 -->
          <div class="recommendation-reason">
            <div class="reason-item">
              <span class="reason-icon">💡</span>
              <div class="reason-content">
                <span class="reason-title">推荐理由</span>
                <span class="reason-text">{{ getRecommendationReason() }}</span>
              </div>
            </div>
            <div class="reason-item">
              <span class="reason-icon">🎭</span>
              <div class="reason-content">
                <span class="reason-title">节日氛围</span>
                <span class="reason-text">{{ getFestivalAtmosphere() }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 所有季度切换 -->
      <div class="quarters-section">
        <div class="section-header">
          <h4 class="section-title">所有季度特色</h4>
          <p class="section-subtitle">探索其他季度的节日饮品</p>
        </div>
        <div class="quarters-grid">
          <div
            v-for="quarter in QUARTERS"
            :key="quarter.id"
            :class="['quarter-card', { 'active': currentFestival.id === quarter.id }]"
            @click="switchToQuarter(quarter.id)"
          >
            <div class="quarter-card-icon">{{ quarter.icon }}</div>
            <h5 class="quarter-card-name">{{ quarter.name }}</h5>
            <p class="quarter-card-months">{{ quarter.months }}</p>
            <p class="quarter-card-desc">{{ quarter.description }}</p>
            <div class="quarter-card-products">
              <span class="product-count">{{ getQuarterProductCount(quarter.id) }}款产品</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 节日特色产品推荐 -->
      <div class="festival-products-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">🌟</span>
            季度特色推荐
          </h3>
          <p class="section-subtitle">{{ currentFestival.name }}专属饮品</p>
        </div>
        
        <div v-if="festivalProducts.length > 0" class="products-grid">
          <div 
            v-for="product in festivalProducts" 
            :key="product.id"
            class="product-card festival"
            @click="viewProductDetail(product)"
          >
            <div class="product-image">
              <img 
                :src="getProductImage(product)" 
                :alt="product.name"
                class="product-img"
                @error="handleImageError"
              />
              <div class="festival-badge">
                <span class="badge-icon">{{ currentFestival.icon }}</span>
                <span class="badge-text">节日</span>
              </div>
              <div class="quarter-tag">
                <span class="tag-text">{{ currentFestival.name }}</span>
              </div>
            </div>
            
            <div class="product-info">
              <h4 class="product-name">{{ product.name }}</h4>
              <p class="product-festival-name" v-if="product.festivalName">
                🎭 节日名称：{{ product.festivalName }}
              </p>
              <p class="product-description">{{ product.description }}</p>
              
              <div class="product-reason" v-if="product.recommendReason">
                <span class="reason-icon">✨</span>
                <span class="reason-text">{{ product.recommendReason }}</span>
              </div>
              
              <div class="product-tags">
                <span class="product-tag festival-tag">
                  <span class="tag-icon">{{ currentFestival.icon }}</span>
                  {{ currentFestival.season }}
                </span>
                <span 
                  v-for="tag in getProductTags(product)" 
                  :key="tag"
                  class="product-tag"
                >
                  {{ tag }}
                </span>
              </div>
              
              <div class="product-footer">
                <span class="product-price">¥{{ product.price.toFixed(2) }}</span>
                <div class="product-actions">
                  <button 
                    @click.stop="addToCart(product)"
                    class="add-cart-btn"
                    title="加入购物车"
                  >
                    🛒
                  </button>
                  <button 
                    @click.stop="quickView(product)"
                    class="quick-view-btn"
                    title="快速查看"
                  >
                    👁️
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-products">
          <div class="empty-state">
            <span class="empty-icon">🎉</span>
            <p class="empty-text">该季度暂无特色产品</p>
            <p class="empty-subtext">正在开发更多节日饮品...</p>
          </div>
        </div>
      </div>

      <!-- 即将到来的节日 -->
      <div class="upcoming-festivals" v-if="upcomingFestivals.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">⏳</span>
            即将到来
          </h3>
          <p class="section-subtitle">提前预览下一个季度的特色</p>
        </div>
        
        <div class="upcoming-list">
          <div 
            v-for="festival in upcomingFestivals" 
            :key="festival.id"
            class="upcoming-item"
            @click="previewFestival(festival)"
          >
            <div class="upcoming-icon">{{ festival.icon }}</div>
            <div class="upcoming-info">
              <h5 class="upcoming-name">{{ festival.name }}</h5>
              <p class="upcoming-time">还有 {{ festival.daysLeft }} 天</p>
            </div>
            <button class="preview-btn" @click.stop="previewFestival(festival)">
              预览
            </button>
          </div>
        </div>
      </div>

      <!-- 时间线展示 -->
      <div class="timeline-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">📊</span>
            年度节日时间线
          </h3>
        </div>
        
        <div class="timeline">
          <div 
            v-for="quarter in QUARTERS"
            :key="quarter.id"
            :class="['timeline-item', { 'current': currentFestival.id === quarter.id }]"
          >
            <div class="timeline-marker">
              <span class="marker-icon">{{ quarter.icon }}</span>
            </div>
            <div class="timeline-content">
              <h5>{{ quarter.name }}</h5>
              <p>{{ quarter.months }}</p>
              <p class="timeline-desc">{{ quarter.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-content">
        <span class="empty-icon">🎉</span>
        <h3 class="empty-title">开始节日探索</h3>
        <p class="empty-text">系统将根据当前时间为您推荐合适的节日饮品</p>
        <button @click="autoRecommend" class="empty-action-btn">
          <span class="btn-icon">🤖</span>
          开始智能推荐
        </button>
      </div>
    </div>

    <!-- 产品详情模态框 -->
    <div v-if="selectedProduct" class="product-modal" @click.self="closeModal">
      <div class="modal-content">
        <button @click="closeModal" class="modal-close">×</button>
        <div class="product-detail">
          <h3>{{ selectedProduct.name }}</h3>
          <p>{{ selectedProduct.description }}</p>
          <button @click="handleAddToCart(selectedProduct)">加入购物车</button>
        </div>
      </div>
    </div>

    <!-- 节日预览模态框 -->
    <div v-if="previewingFestival" class="festival-modal" @click.self="closePreview">
      <div class="modal-content">
        <button @click="closePreview" class="modal-close">×</button>
        <div class="festival-preview">
          <h3>{{ previewingFestival.name }}</h3>
          <p>{{ previewingFestival.description }}</p>
          <button @click="switchToQuarter(previewingFestival.id)">查看该季度产品</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
    import axios from 'axios';
    const API_BASE_URL = 'http://localhost:8081/api';
// 节日数据定义
const FESTIVAL_DATA = {
  q1: {
    id: 'q1',
    name: '新春佳节季',
    icon: '🧧',
    season: '冬季',
    months: [1, 2, 3],
    monthsDesc: '1月 - 3月',
    description: '元旦春节元宵，团圆喜庆迎新',
    atmosphere: '温暖、团圆、喜庆',
    color: '#e74c3c',
    products: [
      {
        id: 6,
        name: '春节福气茶',
        description: '春节特饮，福气满满，年味十足',
        price: 28.00,
        festivalName: '春节限定',
        recommendReason: '红色包装象征吉祥，红枣桂圆寓意团圆',
        tags: '春节,福气,年味,暖饮',
        imageId: 12
      },
      {
        id: 18,
        name: '元旦迎新茶',
        description: '新年特调，迎接新的一年',
        price: 25.00,
        festivalName: '元旦限定',
        recommendReason: '清新口感象征新的开始',
        tags: '元旦,迎新,新年',
        imageId: 18
      },
      {
        id: 7,
        name: '端午粽子奶茶',
        description: '粽子风味创意奶茶，端午特色',
        price: 27.00,
        festivalName: '端午限定',
        recommendReason: '粽子风味创新，传统与时尚结合',
        tags: '端午,粽子,创意',
        imageId: 17
      }
    ]
  },
  q2: {
    id: 'q2',
    name: '春夏清新季',
    icon: '🌸',
    season: '春季',
    months: [4, 5, 6],
    monthsDesc: '4月 - 6月',
    description: '清明端午儿童节，清新自然活力',
    atmosphere: '清新、活力、自然',
    color: '#2ecc71',
    products: [
      {
        id: 19,
        name: '儿童节彩虹茶',
        description: '七彩彩虹分层，童趣满满',
        price: 23.00,
        festivalName: '儿童节限定',
        recommendReason: '七彩颜色吸引儿童，甜美口感',
        tags: '儿童节,彩虹,童趣',
        imageId: 19
      },
      {
        id: 7,
        name: '端午粽子奶茶',
        description: '粽子风味创意奶茶，端午特色',
        price: 27.00,
        festivalName: '端午限定',
        recommendReason: '传统粽子风味与现代奶茶结合',
        tags: '端午,粽子,创意',
        imageId: 17
      },
      {
        id: 8,
        name: '春风茉莉花茶',
        description: '清香茉莉花茶，春风般柔和',
        price: 16.00,
        festivalName: '春季特饮',
        recommendReason: '茉莉花香象征春天的气息',
        tags: '春风,花茶,清香',
        imageId: 6
      }
    ]
  },
  q3: {
    id: 'q3',
    name: '夏秋浪漫季',
    icon: '🎑',
    season: '夏季',
    months: [7, 8, 9],
    monthsDesc: '7月 - 9月',
    description: '七夕中秋团圆，浪漫温馨甜蜜',
    atmosphere: '浪漫、温馨、甜蜜',
    color: '#9b59b6',
    products: [
      {
        id: 16,
        name: '七夕星空茶',
        description: '梦幻星空渐变，七夕浪漫特饮',
        price: 35.00,
        festivalName: '七夕限定',
        recommendReason: '星空渐变象征牛郎织女的相会',
        tags: '七夕,星空,浪漫',
        imageId: 16
      },
      {
        id: 15,
        name: '中秋月饼奶茶',
        description: '月饼风味奶茶，中秋限定',
        price: 29.00,
        festivalName: '中秋限定',
        recommendReason: '月饼风味唤起中秋回忆',
        tags: '中秋,月饼,限定',
        imageId: 15
      },
      {
        id: 9,
        name: '秋日桂花乌龙',
        description: '桂花香乌龙茶，秋日浪漫',
        price: 19.00,
        festivalName: '秋季特饮',
        recommendReason: '桂花香是秋天的象征',
        tags: '秋日,桂花,乌龙',
        imageId: 7
      }
    ]
  },
  q4: {
    id: 'q4',
    name: '秋冬温馨季',
    icon: '🎃',
    season: '秋季',
    months: [10, 11, 12],
    monthsDesc: '10月 - 12月',
    description: '万圣感恩圣诞节，奇幻温暖惊喜',
    atmosphere: '奇幻、温暖、惊喜',
    color: '#e67e22',
    products: [
      {
        id: 11,
        name: '圣诞限定奶茶',
        description: '圣诞特调暖心奶茶，姜饼人造型',
        price: 30.00,
        festivalName: '圣诞限定',
        recommendReason: '姜饼人造型充满圣诞氛围',
        tags: '圣诞,限定,暖心',
        imageId: 11
      },
      {
        id: 14,
        name: '万圣节南瓜拿铁',
        description: '南瓜风味拿铁，万圣节特调',
        price: 26.00,
        festivalName: '万圣节限定',
        recommendReason: '南瓜风味适合万圣节主题',
        tags: '万圣节,南瓜,拿铁',
        imageId: 14
      },
      {
        id: 20,
        name: '感恩节南瓜派',
        description: '南瓜派风味奶茶，感恩温暖',
        price: 26.00,
        festivalName: '感恩节限定',
        recommendReason: '南瓜派风味唤起感恩心情',
        tags: '感恩节,南瓜,温暖',
        imageId: 20
      }
    ]
  }
};

const QUARTERS = [
  { id: 'q1', name: '第一季度', icon: '🧧', months: '1-3月', description: '新春佳节季' },
  { id: 'q2', name: '第二季度', icon: '🌸', months: '4-6月', description: '春夏清新季' },
  { id: 'q3', name: '第三季度', icon: '🎑', months: '7-9月', description: '夏秋浪漫季' },
  { id: 'q4', name: '第四季度', icon: '🎃', months: '10-12月', description: '秋冬温馨季' }
];

export default {
  name: 'FestivalRecommendation',
  props: {
    userId: {
      type: Number,
      default: 1
    },
    username: {
      type: String,
      default: '用户'
    }
  },
  data() {
    return {
      // 节日数据
      currentFestival: null,
      festivalProducts: [],
      upcomingFestivals: [],
      
      // 模式控制
      isManualMode: false,
      selectedQuarter: null,
      
      // 状态控制
      loading: false,
      isRefreshing: false,
      error: null,
      matchType: 'auto',
      
      // 选择控制
      selectedProduct: null,
      previewingFestival: null,
      
      // 时间相关
      currentTime: new Date(),
      timeInterval: null,
      
      // 常量
      FESTIVAL_DATA,
      QUARTERS
    };
  },
  computed: {
    formattedTime() {
      return this.currentTime.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    
    formattedDate() {
      return this.currentTime.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      });
    },
    
    detectionTime() {
      return this.currentTime.toLocaleString('zh-CN');
    },
    
    quarterNumber() {
      if (!this.currentFestival) return '';
      return this.currentFestival.id.charAt(1); // q1 -> 1
    }
  },
  created() {
    // 启动时间更新
    this.startTimeUpdate();
    // 自动推荐
    this.autoRecommend();
  },
  beforeDestroy() {
    // 清除定时器
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  },
  methods: {
    // 时间相关方法
    startTimeUpdate() {
      this.timeInterval = setInterval(() => {
        this.currentTime = new Date();
      }, 1000);
    },
    
    // 获取当前季度
    getCurrentQuarter() {
      const month = this.currentTime.getMonth() + 1; // 1-12
      if (month >= 1 && month <= 3) return 'q1';
      if (month >= 4 && month <= 6) return 'q2';
      if (month >= 7 && month <= 9) return 'q3';
      return 'q4';
    },
    
    // 自动推荐
    async autoRecommend() {
      this.loading = true;
      this.error = null;
      this.matchType = 'auto';
      
      try {
        // 模拟"智能分析"延迟
        await this.simulateLoading(1200);
        
        const quarterId = this.getCurrentQuarter();
        this.switchToQuarter(quarterId);
        
        // 计算即将到来的节日
        this.calculateUpcomingFestivals();
        
      } catch (err) {
        this.error = '节日推荐失败，请稍后重试';
        console.error('推荐失败:', err);
      } finally {
        this.loading = false;
      }
    },
    
    // 模拟加载
    simulateLoading(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
    
    // 切换季度
    switchToQuarter(quarterId) {
      this.currentFestival = this.FESTIVAL_DATA[quarterId];
      this.festivalProducts = this.currentFestival?.products || [];
      this.selectedQuarter = quarterId;
      this.matchType = this.isManualMode ? 'manual' : 'auto';
    },
    
    // 手动选择季度
    selectQuarter(quarterId) {
      this.selectedQuarter = quarterId;
      this.isManualMode = true;
      this.switchToQuarter(quarterId);
    },
    
    // 切换手动/自动模式
    toggleManualMode() {
      this.isManualMode = !this.isManualMode;
      if (!this.isManualMode) {
        // 切回自动模式，重新推荐
        this.autoRecommend();
      }
    },
    
    // 刷新推荐
    async refreshRecommendation() {
      this.isRefreshing = true;
      await this.autoRecommend();
      this.isRefreshing = false;
    },
    
    // 计算即将到来的节日
    calculateUpcomingFestivals() {
      const currentQuarter = this.getCurrentQuarter();
      const quarters = ['q1', 'q2', 'q3', 'q4'];
      const currentIndex = quarters.indexOf(currentQuarter);
      
      this.upcomingFestivals = [];
      
      // 获取下一个季度
      for (let i = 1; i <= 3; i++) {
        const nextIndex = (currentIndex + i) % 4;
        const nextQuarterId = quarters[nextIndex];
        const festival = this.FESTIVAL_DATA[nextQuarterId];
        
        if (festival) {
          // 简单计算天数（实际应该根据月份计算）
          const daysLeft = i * 90; // 每个季度约90天
          
          this.upcomingFestivals.push({
            ...festival,
            daysLeft: daysLeft
          });
        }
      }
    },
    
    // 预览节日
    previewFestival(festival) {
      this.previewingFestival = festival;
    },
    
    closePreview() {
      this.previewingFestival = null;
    },
    
    // 获取季度产品数量
    getQuarterProductCount(quarterId) {
      return this.FESTIVAL_DATA[quarterId]?.products?.length || 0;
    },
    
    // 获取推荐理由
    getRecommendationReason() {
      if (!this.currentFestival) return '';
      
      const reasons = {
        q1: '新年新气象，用温暖的饮品迎接新的一年，红色包装象征吉祥如意。',
        q2: '春暖花开，清新自然的饮品带来活力，适合户外活动和踏青。',
        q3: '夏末秋初，浪漫温馨的饮品伴随佳节，适合约会和家庭聚会。',
        q4: '岁末年终，奇幻温暖的饮品带来惊喜，适合节日派对和送礼。'
      };
      
      return reasons[this.currentFestival.id] || '根据当前季节特点精心推荐';
    },
    
    // 获取节日氛围
    getFestivalAtmosphere() {
      return this.currentFestival?.atmosphere || '节日氛围浓厚';
    },
    
    // 获取匹配类型文本
    getMatchTypeText(type) {
      const types = {
        auto: '自动推荐',
        manual: '手动选择',
        exact: '精确匹配'
      };
      return types[type] || type;
    },
    
    // 产品相关方法
    getProductImage(product) {
      const imageId = product.imageId || 1;
      return `/images/${imageId}.png`;
    },
    
    handleImageError(event) {
      event.target.style.display = 'none';
      const parent = event.target.parentElement;
      if (parent) {
        const fallback = document.createElement('div');
        fallback.className = 'image-fallback';
        fallback.innerHTML = this.currentFestival?.icon || '🎉';
        parent.appendChild(fallback);
      }
    },
    
    getProductTags(product) {
      if (product.tags && typeof product.tags === 'string') {
        return product.tags.split(',').map(tag => tag.trim());
      }
      return ['特色'];
    },
    
// 复制 Welcome.vue 中的 addToCart 方法
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
    
    // 这里可能需要触发父组件更新购物车
    this.$emit('cart-updated');
    
    this.$message.success('已添加到购物车');
  } catch (error) {
    console.error('❌ 添加到购物车失败:', error);
    
    if (error.response?.status === 400) {
      this.$message.error('用户不存在，请重新登录');
    } else {
      this.$message.error('添加到购物车失败');
    }
  }
},
    
    // 查看产品详情
    viewProductDetail(product) {
      this.selectedProduct = product;
    },
    
    quickView(product) {
      this.$message.info(`快速查看: ${product.name}`);
    },
    
    handleAddToCart(product) {
      this.addToCart(product);
      this.closeModal();
    },
    
    closeModal() {
      this.selectedProduct = null;
    },
    
    // 错误处理
    clearError() {
      this.error = null;
    },
    
    // 截断文本
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    }
  }
};
</script>

<style scoped>
.festival-recommendation {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: 100vh;
  overflow-y: auto;
}

/* 头部样式 */
.festival-header {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  color: white;
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
}

.header-content {
  margin-bottom: 25px;
  text-align: center;
}

.festival-title {
  font-size: 2.5rem;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.festival-icon {
  font-size: 3rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.festival-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0;
}

/* 时间控制 */
.time-controls {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.current-time {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 1.1rem;
}

.time-icon {
  font-size: 1.8rem;
}

.time-text {
  font-weight: bold;
  font-size: 1.3rem;
  font-family: monospace;
  background: rgba(0, 0, 0, 0.2);
  padding: 8px 15px;
  border-radius: 10px;
}

.date-text {
  opacity: 0.9;
}

.time-buttons {
  display: flex;
  gap: 15px;
}

.time-btn {
  padding: 12px 25px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
}

.time-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.time-btn.active {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}

.time-btn.refreshing {
  background: rgba(255, 255, 255, 0.2);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 0.7; }
  50% { opacity: 1; }
  100% { opacity: 0.7; }
}

/* 手动选择器 */
.manual-selector {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.selector-header {
  margin-bottom: 20px;
  text-align: center;
}

.selector-header h3 {
  font-size: 1.5rem;
  margin: 0 0 10px 0;
  color: #333;
}

.selector-header p {
  color: #666;
  margin: 0;
}

.quarter-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.quarter-btn {
  padding: 20px;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.quarter-btn:hover {
  border-color: #ff6b6b;
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(255, 107, 107, 0.15);
}

.quarter-btn.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-color: #ff6b6b;
  color: white;
}

.quarter-icon {
  font-size: 2.5rem;
}

.quarter-name {
  font-size: 1.1rem;
  font-weight: bold;
}

.quarter-months {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* 错误提示 */
.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px 25px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 5px solid #dc3545;
}

.error-icon {
  font-size: 1.5rem;
  margin-right: 15px;
}

.error-close {
  background: none;
  border: none;
  color: #721c24;
  font-size: 1.8rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 加载状态 */
.loading-container {
  background: white;
  border-radius: 16px;
  padding: 60px 30px;
  margin: 20px 0;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 5px solid #f1f3f5;
  border-top: 5px solid #ff6b6b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 25px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 10px;
}

.loading-subtext {
  color: #666;
  font-size: 0.95rem;
}

/* 节日卡片 */
.festival-card {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.1);
}

.festival-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 25px;
  border-bottom: 2px solid #f8f9fa;
}

.festival-info {
  display: flex;
  align-items: center;
  gap: 25px;
}

.festival-card-icon {
  font-size: 4rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.festival-details {
  flex: 1;
}

.festival-name {
  font-size: 2rem;
  margin: 0 0 10px 0;
  color: #333;
  /* 移除有兼容性问题的代码 */
  /* background: linear-gradient(135deg, #ff6b6b, #ee5a24); */
  /* -webkit-background-clip: text; */
  /* -webkit-text-fill-color: transparent; */
  
  /* 使用兼容性更好的方案 */
  position: relative;
  display: inline-block;
}

/* 添加渐变效果（兼容性更好） */
.festival-name::after {
  content: attr(data-text);
  position: absolute;
  left: 0;
  top: 0;
  color: transparent;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  -webkit-background-clip: text;
  background-clip: text;
  z-index: 2;
  opacity: 0.8;
}

.festival-description {
  color: #666;
  margin: 0;
  font-size: 1.1rem;
  line-height: 1.6;
}

.match-badge {
  padding: 10px 20px;
  border-radius: 25px;
  font-weight: bold;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.match-badge.auto {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  color: white;
}

.match-badge.manual {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.match-badge.exact {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  color: white;
}

/* 卡片主体 */
.festival-card-body {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.festival-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: #e9ecef;
  transform: translateY(-3px);
}

.stat-icon {
  font-size: 2.2rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

/* 推荐理由 */
.recommendation-reason {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reason-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.reason-icon {
  font-size: 1.8rem;
  margin-top: 5px;
}

.reason-content {
  flex: 1;
}

.reason-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  display: block;
  font-size: 1rem;
}

.reason-text {
  color: #666;
  line-height: 1.6;
  margin: 0;
  font-size: 0.95rem;
}

/* 所有季度 */
.quarters-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 25px;
  text-align: center;
}

.section-title {
  font-size: 1.6rem;
  margin: 0 0 10px 0;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.section-icon {
  font-size: 1.8rem;
}

.section-subtitle {
  color: #666;
  margin: 0;
}

.quarters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
}

.quarter-card {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 16px;
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.quarter-card:hover {
  border-color: #ff6b6b;
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(255, 107, 107, 0.15);
}

.quarter-card.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-color: #ff6b6b;
  color: white;
}

.quarter-card.active .quarter-card-desc,
.quarter-card.active .quarter-card-months {
  color: rgba(255, 255, 255, 0.9);
}

.quarter-card-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.quarter-card-name {
  font-size: 1.3rem;
  margin: 0 0 8px 0;
  font-weight: bold;
}

.quarter-card-months {
  color: #666;
  margin: 0 0 10px 0;
  font-size: 0.95rem;
}

.quarter-card-desc {
  color: #888;
  margin: 0 0 15px 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

.quarter-card-products {
  margin-top: 15px;
}

.product-count {
  background: #f8f9fa;
  color: #666;
  padding: 6px 15px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.quarter-card.active .product-count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* 产品区域 */
.festival-products-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
  margin-top: 25px;
}

.product-card.festival {
  background: white;
  border: 2px solid #f0f0f0;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.product-card.festival:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
  border-color: #ff6b6b;
}

.product-image {
  position: relative;
  height: 220px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.festival-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.quarter-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(255, 255, 255, 0.95);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
  color: #ff6b6b;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.product-info {
  padding: 25px;
}

.product-name {
  font-size: 1.4rem;
  margin: 0 0 10px 0;
  color: #333;
}

.product-festival-name {
  color: #ff6b6b;
  font-size: 0.9rem;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
}

.product-description {
  color: #666;
  font-size: 0.95rem;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.product-reason {
  background: #f8f9fa;
  padding: 12px 18px;
  border-radius: 10px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-left: 3px solid #ff6b6b;
}

.reason-icon {
  font-size: 1.2rem;
  color: #ff6b6b;
}

.reason-text {
  color: #495057;
  font-size: 0.9rem;
  font-style: italic;
  line-height: 1.5;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 25px;
}

.product-tag {
  background: #e9ecef;
  color: #495057;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
}

.festival-tag {
  background: #ffeaa7;
  color: #e17055;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e74c3c;
}

.product-actions {
  display: flex;
  gap: 12px;
}

.add-cart-btn,
.quick-view-btn {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 1.3rem;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-cart-btn {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: white;
}

.add-cart-btn:hover {
  background: linear-gradient(135deg, #27ae60 0%, #219653 100%);
  transform: scale(1.1);
}

.quick-view-btn {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
}

.quick-view-btn:hover {
  background: linear-gradient(135deg, #2980b9 0%, #1c7ed6 100%);
  transform: scale(1.1);
}

/* 即将到来的节日 */
.upcoming-festivals {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.upcoming-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.upcoming-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.upcoming-item:hover {
  background: #e9ecef;
  transform: translateX(10px);
}

.upcoming-icon {
  font-size: 2rem;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upcoming-info {
  flex: 1;
}

.upcoming-name {
  font-size: 1.1rem;
  margin: 0 0 5px 0;
  color: #333;
}

.upcoming-time {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

.preview-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.preview-btn:hover {
  background: linear-gradient(135deg, #8e44ad 0%, #7d3c98 100%);
  transform: scale(1.05);
}

/* 时间线 */
.timeline-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.timeline {
  position: relative;
  padding: 30px 0;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 60px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(to bottom, #ff6b6b, #ee5a24);
}

.timeline-item {
  display: flex;
  margin-bottom: 30px;
  position: relative;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-marker {
  width: 60px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.marker-icon {
  width: 50px;
  height: 50px;
  background: white;
  border: 3px solid #ff6b6b;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.timeline-item.current .marker-icon {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  animation: pulse 2s infinite;
}

.timeline-content {
  flex: 1;
  padding-left: 30px;
}

.timeline-content h5 {
  font-size: 1.2rem;
  margin: 0 0 5px 0;
  color: #333;
}

.timeline-content p {
  color: #666;
  margin: 0 0 5px 0;
  font-size: 0.95rem;
}

.timeline-desc {
  color: #888 !important;
  font-size: 0.9rem !important;
  line-height: 1.5;
}

/* 空状态 */
.empty-state,
.empty-products {
  background: white;
  border-radius: 20px;
  padding: 60px 30px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 25px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.empty-title {
  font-size: 1.5rem;
  margin: 0 0 15px 0;
  color: #495057;
}

.empty-text {
  font-size: 1rem;
  margin: 0 0 10px 0;
  color: #666;
}

.empty-subtext {
  font-size: 0.9rem;
  color: #888;
  margin: 5px 0 25px 0;
}

.empty-action-btn {
  padding: 12px 35px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.3);
}

/* 模态框 */
.product-modal,
.festival-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.3s;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
}

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  background: #f8f9fa;
  border: none;
  font-size: 1.8rem;
  color: #666;
  cursor: pointer;
  z-index: 10;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.modal-close:hover {
  background: #e9ecef;
  color: #333;
}

.festival-preview,
.product-detail {
  padding: 40px;
}

.festival-preview h3,
.product-detail h3 {
  font-size: 1.8rem;
  margin: 0 0 20px 0;
  color: #333;
}

.festival-preview p,
.product-detail p {
  color: #666;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.festival-preview button,
.product-detail button {
  padding: 12px 30px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.festival-preview button:hover,
.product-detail button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.3);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .festival-card-body {
    grid-template-columns: 1fr;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .festival-header {
    padding: 20px;
  }
  
  .festival-title {
    font-size: 2rem;
  }
  
  .time-controls {
    flex-direction: column;
    text-align: center;
  }
  
  .quarters-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .timeline::before {
    left: 25px;
  }
  
  .timeline-marker {
    width: 50px;
  }
  
  .marker-icon {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  .festival-recommendation {
    padding: 15px;
  }
  
  .quarters-grid {
    grid-template-columns: 1fr;
  }
  
  .quarter-buttons {
    grid-template-columns: 1fr;
  }
  
  .festival-stats {
    grid-template-columns: 1fr;
  }
}

.festival-recommendation {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: 100vh;
  overflow-y: auto; /* ✅ 确保可以滚动 */
  height: calc(100vh - 60px); /* 减去导航栏高度 */
}

/* 确保内容区域不会限制高度 */
.recommendation-content {
  max-height: none;
  overflow: visible;
}

</style>