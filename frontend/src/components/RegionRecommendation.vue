<template>
  <div class="region-recommendation">
    <!-- 头部区域 -->
    <div class="region-header">
      <div class="header-content">
        <h1 class="region-title">
          <span class="region-icon">🗺️</span>
          地域特色茶饮
        </h1>
        <p class="region-subtitle">发现你所在地的特色奶茶，品味不同地域文化</p>
      </div>
      
      <!-- 定位控制 -->
      <div class="location-controls">
        <div class="current-location" :class="{ 'no-location': !currentLocation }">
          <span class="location-icon">📍</span>
          <span class="location-text">{{ locationDisplayText }}</span>
          <span v-if="matchedRegion" class="matched-region">
            {{ matchedRegion.name }}
          </span>
        </div>
        
        <div class="location-buttons">
          <button 
            @click="simulateLocation" 
            :disabled="isLocating"
            class="location-btn simulate-btn"
            :class="{ 'locating': isLocating }"
          >
            <span class="btn-icon">📍</span>
            <span v-if="isLocating" class="btn-text">定位中...</span>
            <span v-else class="btn-text">模拟定位</span>
          </button>
          
          <button 
            @click="getRealLocation" 
            :disabled="isLocating || !supportsGeolocation"
            class="location-btn real-btn"
            :title="supportsGeolocation ? '使用真实位置' : '浏览器不支持定位'"
          >
            <span class="btn-icon">🌐</span>
            <span class="btn-text">真实定位</span>
          </button>
          
          <button 
            @click="refreshRecommendation" 
            class="location-btn refresh-btn"
          >
            <span class="btn-icon">🔄</span>
            <span class="btn-text">刷新推荐</span>
          </button>
        </div>
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
      <p class="loading-text">正在探索地域特色...</p>
    </div>

    <!-- 推荐内容 -->
    <div v-else-if="recommendation" class="recommendation-content">
      <!-- 地域信息卡片 -->
      <div class="region-card">
        <div class="region-card-header">
          <div class="region-info">
            <span class="region-card-icon">{{ recommendation.region.icon }}</span>
            <div class="region-details">
              <h3 class="region-name">{{ recommendation.region.name }}</h3>
              <p class="region-specialty">{{ recommendation.region.specialtyDesc }}</p>
            </div>
          </div>
          <div class="match-badge" :class="recommendation.matchType">
            {{ getMatchTypeText(recommendation.matchType) }}
          </div>
        </div>

        <div class="region-card-body">
          <div class="region-stats">
            <div class="stat-item">
              <span class="stat-icon">🏙️</span>
              <div class="stat-content">
                <span class="stat-label">覆盖省份</span>
                <span class="stat-value">{{ recommendation.region.coverProvinces }}</span>
              </div>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🌤️</span>
              <div class="stat-content">
                <span class="stat-label">气候特点</span>
                <span class="stat-value">{{ recommendation.region.climateFeature }}</span>
              </div>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🥤</span>
              <div class="stat-content">
                <span class="stat-label">特色产品</span>
                <span class="stat-value">{{ recommendation.featuredProducts.length }}款</span>
              </div>
            </div>
          </div>

          <!-- 建议信息 -->
          <div class="suggestions">
            <div class="suggestion-item weather-suggestion">
              <span class="suggestion-icon">🌤️</span>
              <div class="suggestion-content">
                <span class="suggestion-title">天气建议</span>
                <span class="suggestion-text">{{ recommendation.weatherSuggestion }}</span>
              </div>
            </div>
            <div class="suggestion-item seasonal-suggestion">
              <span class="suggestion-icon">📅</span>
              <div class="suggestion-content">
                <span class="suggestion-title">季节建议</span>
                <span class="suggestion-text">{{ recommendation.seasonalSuggestion }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 地域筛选 -->
      <div class="region-filter">
        <div class="filter-header">
          <h4 class="filter-title">切换地域</h4>
          <p class="filter-subtitle">探索其他地区的特色茶饮</p>
        </div>
        <div class="filter-buttons">
          <button
            v-for="region in allRegions"
            :key="region.code"
            :class="['region-btn', { 'active': selectedRegion === region.code }]"
            @click="selectRegion(region.code)"
          >
            <span class="btn-region-icon">{{ region.icon }}</span>
            <span class="btn-region-name">{{ region.name }}</span>
          </button>
        </div>
      </div>

      <!-- 🆕 新增：特色茶叶板块 -->
      <div class="special-teas-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">🍃</span>
            当地特色茶叶
          </h3>
          <p class="section-subtitle">{{ getCurrentRegionName() }}的传统茶叶品种</p>
        </div>
        
        <div class="teas-container">
          <div 
            v-for="tea in getSpecialtyTeas()" 
            :key="tea.name" 
            class="tea-card"
            @click="showTeaDetail(tea)"
          >
            <div class="tea-icon">{{ getTeaIcon(tea.name) }}</div>
            <div class="tea-info">
              <h4 class="tea-name">{{ tea.name }}</h4>
              <p class="tea-origin">{{ tea.origin }}</p>
              <p class="tea-desc">{{ tea.description }}</p>
              <div class="tea-tags">
                <span class="tea-tag">{{ tea.type }}</span>
                <span class="tea-tag">{{ tea.season }}</span>
              </div>
            </div>
            <button class="tea-action-btn" @click.stop="addTeaToCart(tea)">
              🛒
            </button>
          </div>
        </div>
      </div>

      <!-- 特色产品推荐 -->
      <div class="featured-products-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">🌟</span>
            特色推荐
          </h3>
          <p class="section-subtitle">当地最受欢迎的饮品</p>
        </div>
        
        <div v-if="recommendation.featuredProducts.length > 0" class="products-grid">
          <div 
            v-for="product in recommendation.featuredProducts" 
            :key="product.id"
            class="product-card featured"
            @click="viewProductDetail(product)"
          >
            <div class="product-image">
              <img 
                :src="getProductImage(product)" 
                :alt="product.productName"
                class="product-img"
                @error="handleImageError"
              />
              <div class="featured-badge">
                <span class="badge-icon">⭐</span>
                <span class="badge-text">特色</span>
              </div>
              <div class="region-tag">
                <span class="tag-icon">{{ recommendation.region.icon }}</span>
              </div>
            </div>
            
            <div class="product-info">
              <h4 class="product-name">{{ product.productName }}</h4>
              <p class="product-local-name" v-if="product.localName">
                🏮 当地名称：{{ product.localName }}
              </p>
              <p class="product-description">{{ product.productDescription }}</p>
              
              <div class="product-reason" v-if="product.recommendReason">
                <span class="reason-icon">💡</span>
                <span class="reason-text">{{ product.recommendReason }}</span>
              </div>
              
              <div class="product-tags">
                <span class="product-tag popularity-tag">
                  <span class="tag-icon">🔥</span>
                  {{ product.popularityScore }}人气
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
                <span class="product-price">¥{{ product.productPrice.toFixed(2) }}</span>
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
            <span class="empty-icon">🥤</span>
            <p class="empty-text">该地区暂无特色产品</p>
            <p class="empty-subtext">正在努力开发中...</p>
          </div>
        </div>
      </div>

      <!-- 所有地域产品 -->
      <div class="all-products-section" v-if="recommendation.allProducts.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="section-icon">📋</span>
            所有产品
            <span class="product-count">({{ recommendation.allProducts.length }})</span>
          </h3>
        </div>
        
        <div class="products-list">
          <div 
            v-for="product in recommendation.allProducts"
            :key="product.id"
            class="product-item"
            :class="{ 'featured': product.isFeatured }"
          >
            <div class="item-image">
              <img 
                :src="getProductImage(product)" 
                :alt="product.productName"
                class="item-img"
                @error="handleImageError"
              />
              <span v-if="product.isFeatured" class="item-featured-badge">⭐</span>
            </div>
            
            <div class="item-info">
              <div class="item-header">
                <h5 class="item-name">{{ product.productName }}</h5>
                <span class="item-price">¥{{ product.productPrice.toFixed(2) }}</span>
              </div>
              <p class="item-desc">{{ truncateText(product.productDescription, 80) }}</p>
              
              <div class="item-tags">
                <span class="item-tag" v-if="product.isFeatured">特色</span>
                <span class="item-tag" v-if="product.localName">当地名称</span>
              </div>
              
              <div class="item-actions">
                <button 
                  @click="addToCart(product)"
                  class="item-action-btn cart-btn"
                >
                  🛒 加入购物车
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-content">
        <span class="empty-icon">🗺️</span>
        <h3 class="empty-title">开始探索地域特色</h3>
        <p class="empty-text">点击上方定位按钮，发现你所在地的特色奶茶</p>
        <button @click="simulateLocation" class="empty-action-btn">
          <span class="btn-icon">📍</span>
          开始探索
        </button>
      </div>
    </div>

    <!-- 🆕 茶叶详情模态框 -->
    <div v-if="selectedTea" class="tea-modal" @click.self="closeTeaModal">
      <div class="modal-content">
        <button @click="closeTeaModal" class="modal-close">×</button>
        <div class="tea-detail">
          <div class="detail-header">
            <span class="detail-icon">{{ getTeaIcon(selectedTea.name) }}</span>
            <h3>{{ selectedTea.name }}</h3>
            <span class="detail-origin">{{ selectedTea.origin }}</span>
          </div>
          <div class="detail-body">
            <div class="detail-section">
              <h4>茶叶介绍</h4>
              <p>{{ selectedTea.description }}</p>
            </div>
            <div class="detail-section">
              <h4>特点</h4>
              <ul>
                <li v-for="feature in selectedTea.features" :key="feature">
                  {{ feature }}
                </li>
              </ul>
            </div>
            <div class="detail-section">
              <h4>推荐搭配</h4>
              <p>{{ selectedTea.pairing }}</p>
            </div>
            <div class="detail-stats">
              <div class="stat">
                <span class="stat-label">香气</span>
                <div class="stat-bar">
                  <div class="stat-fill" :style="{width: selectedTea.scent + '%'}"></div>
                </div>
              </div>
              <div class="stat">
                <span class="stat-label">回甘</span>
                <div class="stat-bar">
                  <div class="stat-fill" :style="{width: selectedTea.aftertaste + '%'}"></div>
                </div>
              </div>
              <div class="stat">
                <span class="stat-label">浓厚度</span>
                <div class="stat-bar">
                  <div class="stat-fill" :style="{width: selectedTea.intensity + '%'}"></div>
                </div>
              </div>
            </div>
          </div>
          <div class="detail-actions">
            <button class="primary-btn" @click="addTeaToCart(selectedTea)">
              🛒 添加相关奶茶
            </button>
            <button class="secondary-btn" @click="closeTeaModal">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 产品详情模态框 -->
    <div v-if="selectedProduct" class="product-modal" @click.self="closeModal">
      <div class="modal-content">
        <button @click="closeModal" class="modal-close">×</button>
        <div class="product-detail">
          <h3>{{ selectedProduct.productName }}</h3>
          <p>{{ selectedProduct.productDescription }}</p>
          <button @click="handleAddToCart(selectedProduct)">加入购物车</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

export default {
  name: 'RegionRecommendation',
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
      // 定位相关
      currentLocation: null,
      isLocating: false,
      supportsGeolocation: false,
      
      // 数据相关
      allRegions: [],
      recommendation: null,
      selectedRegion: null,
      selectedProduct: null,
      
      // 状态相关
      loading: false,
      error: null,
      
      // 模拟城市列表
      mockCities: [
        { province: '贵州', city: '贵阳', region: 'southwest' },
        { province: '四川', city: '成都', region: 'southwest' },
        { province: '云南', city: '昆明', region: 'southwest' },
        { province: '黑龙江', city: '哈尔滨', region: 'northeast' },
        { province: '广东', city: '广州', region: 'south' },
        { province: '北京', city: '北京', region: 'north' },
        { province: '陕西', city: '西安', region: 'northwest' },
        { province: '上海', city: '上海', region: 'east' }
      ],
      
      // 🆕 添加特色茶叶数据
      specialtyTeas: {
        // 西南地区
        southwest: [
          {
            name: "贵州白茶",
            origin: "贵州",
            description: "贵州高山白茶，汤色清澈，滋味鲜爽甘甜，具有独特的毫香",
            type: "白茶",
            season: "春季",
            features: ["高山云雾滋养", "芽头肥壮", "毫香明显", "回甘持久"],
            pairing: "适合清饮，或搭配少量蜂蜜",
            scent: 85,
            aftertaste: 80,
            intensity: 70
          },
          {
            name: "云南普洱茶",
            origin: "云南",
            description: "陈年普洱，香气独特，越陈越香，具有降脂减肥功效",
            type: "黑茶",
            season: "四季",
            features: ["越陈越香", "茶汤红浓", "醇厚顺滑", "陈香独特"],
            pairing: "适合纯饮，或搭配茶点",
            scent: 90,
            aftertaste: 95,
            intensity: 85
          },
          {
            name: "四川蒙顶茶",
            origin: "四川",
            description: "蒙顶甘露，历史名茶，香气鲜嫩持久，汤色碧清微黄",
            type: "绿茶",
            season: "春季",
            features: ["历史名茶", "香气鲜嫩", "茶汤清澈", "口感醇和"],
            pairing: "适合清饮，搭配清淡点心",
            scent: 88,
            aftertaste: 82,
            intensity: 75
          }
        ],
        
        // 华东地区
        east: [
          {
            name: "杭州西湖龙井",
            origin: "浙江杭州",
            description: "中国十大名茶之一，色绿、香郁、味甘、形美，被誉为绿茶皇后",
            type: "绿茶",
            season: "春季",
            features: ["扁平光滑", "香气清高", "滋味鲜爽", "汤色碧绿"],
            pairing: "适合清饮，搭配江南糕点",
            scent: 95,
            aftertaste: 85,
            intensity: 80
          },
          {
            name: "福建铁观音",
            origin: "福建安溪",
            description: "乌龙茶代表，观音韵明显，回甘带蜜，七泡有余香",
            type: "乌龙茶",
            season: "春秋",
            features: ["条索卷曲", "兰花香明显", "回甘迅速", "耐泡度高"],
            pairing: "功夫茶泡法，搭配茶食",
            scent: 92,
            aftertaste: 90,
            intensity: 82
          },
          {
            name: "安徽黄山毛峰",
            origin: "安徽黄山",
            description: "黄山名茶，形似雀舌，白毫显露，香气如兰",
            type: "绿茶",
            season: "春季",
            features: ["白毫显露", "香气清鲜", "滋味醇厚", "汤色清澈"],
            pairing: "玻璃杯冲泡观赏",
            scent: 87,
            aftertaste: 83,
            intensity: 78
          }
        ],
        
        // 华南地区
        south: [
          {
            name: "广东凉茶",
            origin: "广东",
            description: "岭南传统草本茶饮，清热祛湿，适应炎热潮湿气候",
            type: "草本茶",
            season: "夏季",
            features: ["清热祛湿", "草药配方", "微苦回甘", "养生功效"],
            pairing: "适合热饮，少量多次",
            scent: 70,
            aftertaste: 75,
            intensity: 85
          },
          {
            name: "广西六堡茶",
            origin: "广西梧州",
            description: "黑茶代表，陈香明显，有槟榔香味，适合陈放",
            type: "黑茶",
            season: "四季",
            features: ["槟榔香味", "陈香明显", "茶汤红浓", "醇厚顺滑"],
            pairing: "煮茶饮用更佳",
            scent: 88,
            aftertaste: 90,
            intensity: 87
          },
          {
            name: "海南鹧鸪茶",
            origin: "海南",
            description: "海南特色茶叶，清热消暑，茶香独特，野生生长",
            type: "野生茶",
            season: "夏季",
            features: ["野生茶叶", "清热解暑", "独特香气", "天然无添加"],
            pairing: "冷泡热泡皆宜",
            scent: 82,
            aftertaste: 78,
            intensity: 76
          }
        ],
        
        // 华北地区
        north: [
          {
            name: "北京茉莉花茶",
            origin: "北京",
            description: "京味代表，茉莉花香浓郁，是北京人最爱的日常茶饮",
            type: "花茶",
            season: "四季",
            features: ["花香浓郁", "茶花结合", "口感鲜灵", "价格亲民"],
            pairing: "大碗茶泡法，搭配点心",
            scent: 95,
            aftertaste: 80,
            intensity: 75
          },
          {
            name: "山西沙棘茶",
            origin: "山西",
            description: "山西特产，富含维C，酸甜可口，具有保健功效",
            type: "保健茶",
            season: "四季",
            features: ["维C丰富", "酸甜口感", "保健养生", "山西特色"],
            pairing: "适合热饮，加点蜂蜜",
            scent: 78,
            aftertaste: 85,
            intensity: 72
          },
          {
            name: "蒙古奶茶",
            origin: "内蒙古",
            description: "草原风味奶茶，奶香浓郁，咸香可口，是牧民日常饮品",
            type: "奶茶",
            season: "四季",
            features: ["奶香浓郁", "咸香可口", "营养丰富", "驱寒暖身"],
            pairing: "搭配奶制品和肉干",
            scent: 90,
            aftertaste: 88,
            intensity: 92
          }
        ],
        
        // 东北地区
        northeast: [
          {
            name: "长白山人参茶",
            origin: "吉林长白山",
            description: "东北特产，人参入茶，滋补养生，提神醒脑",
            type: "保健茶",
            season: "冬季",
            features: ["人参滋补", "提神醒脑", "养生佳品", "东北特色"],
            pairing: "单独泡饮或加红枣",
            scent: 82,
            aftertaste: 85,
            intensity: 88
          },
          {
            name: "东北松子茶",
            origin: "黑龙江",
            description: "松子香气独特，口感醇厚，富含不饱和脂肪酸",
            type: "坚果茶",
            season: "秋冬",
            features: ["松子香气", "醇厚口感", "营养丰富", "暖身佳品"],
            pairing: "适合冬日热饮",
            scent: 85,
            aftertaste: 82,
            intensity: 80
          },
          {
            name: "黑森林红茶",
            origin: "辽宁",
            description: "东北地区改良红茶，茶性温和，适合寒冷气候",
            type: "红茶",
            season: "冬季",
            features: ["茶性温和", "汤色红亮", "暖身效果", "口感醇和"],
            pairing: "加奶或蜂蜜饮用",
            scent: 80,
            aftertaste: 78,
            intensity: 82
          }
        ],
        
        // 西北地区
        northwest: [
          {
            name: "宁夏枸杞茶",
            origin: "宁夏",
            description: "宁夏特产枸杞制成，明目养肝，带有天然甜味",
            type: "保健茶",
            season: "四季",
            features: ["明目养肝", "天然甜味", "营养价值高", "易于冲泡"],
            pairing: "单独泡饮或配菊花",
            scent: 75,
            aftertaste: 85,
            intensity: 70
          },
          {
            name: "新疆玫瑰花茶",
            origin: "新疆和田",
            description: "玫瑰花香气浓郁，美容养颜，是新疆特色花茶",
            type: "花茶",
            season: "春夏",
            features: ["玫瑰花香", "美容养颜", "新疆特色", "汤色粉红"],
            pairing: "单独泡饮或加蜂蜜",
            scent: 95,
            aftertaste: 80,
            intensity: 72
          },
          {
            name: "陕西茯砖茶",
            origin: "陕西咸阳",
            description: "古老黑茶，金花茂盛，陈香明显，助消化",
            type: "黑茶",
            season: "四季",
            features: ["金花茂盛", "陈香明显", "助消化", "越陈越香"],
            pairing: "煮茶饮用，配油茶面",
            scent: 90,
            aftertaste: 88,
            intensity: 85
          }
        ],
        
        // 华中地区
        central: [
          {
            name: "湖北恩施玉露",
            origin: "湖北恩施",
            description: "蒸青绿茶，茶汤清澈，鲜爽回甘，是中国传统蒸青茶",
            type: "绿茶",
            season: "春季",
            features: ["蒸青工艺", "汤色清澈", "鲜爽回甘", "硒含量高"],
            pairing: "玻璃杯冲泡观赏",
            scent: 88,
            aftertaste: 85,
            intensity: 78
          },
          {
            name: "湖南君山银针",
            origin: "湖南岳阳",
            description: "黄茶珍品，三起三落，茶舞动人，香气清纯",
            type: "黄茶",
            season: "春季",
            features: ["三起三落", "芽头挺直", "香气清纯", "汤色杏黄"],
            pairing: "玻璃杯冲泡观赏茶舞",
            scent: 90,
            aftertaste: 86,
            intensity: 80
          },
          {
            name: "河南信阳毛尖",
            origin: "河南信阳",
            description: "中国名茶，细圆光直，白毫显露，滋味醇厚",
            type: "绿茶",
            season: "春季",
            features: ["细圆光直", "白毫显露", "滋味醇厚", "栗香持久"],
            pairing: "清饮，搭配茶点",
            scent: 92,
            aftertaste: 87,
            intensity: 82
          }
        ],
        
        // 东南地区
        southeast: [
          {
            name: "台湾高山茶",
            origin: "台湾阿里山",
            description: "阿里山高山乌龙，高山韵味，清香持久，喉韵甘甜",
            type: "乌龙茶",
            season: "四季",
            features: ["高山韵味", "清香持久", "喉韵甘甜", "叶底肥厚"],
            pairing: "功夫茶泡法",
            scent: 93,
            aftertaste: 90,
            intensity: 83
          },
          {
            name: "港式奶茶",
            origin: "香港",
            description: "茶味浓郁，丝滑顺口，港式经典，使用拼配茶",
            type: "奶茶",
            season: "四季",
            features: ["茶味浓郁", "丝滑顺口", "港式经典", "拼配茶底"],
            pairing: "搭配菠萝油或蛋挞",
            scent: 88,
            aftertaste: 85,
            intensity: 90
          },
          {
            name: "澳门杏仁茶",
            origin: "澳门",
            description: "杏仁香气，口感细腻，传统甜品茶饮，滋补养生",
            type: "甜品茶",
            season: "四季",
            features: ["杏仁香气", "口感细腻", "滋补养生", "甜品茶饮"],
            pairing: "作为餐后甜品",
            scent: 87,
            aftertaste: 84,
            intensity: 79
          }
        ]
      },
      
      selectedTea: null // 当前选中的茶叶
    };
  },
  computed: {
    locationDisplayText() {
      if (!this.currentLocation) {
        return '等待定位...';
      }
      if (this.currentLocation.province && this.currentLocation.city) {
        return `${this.currentLocation.province} ${this.currentLocation.city}`;
      }
      return this.currentLocation.province || '未知位置';
    },
    
    matchedRegion() {
      if (!this.recommendation) return null;
      return this.recommendation.region;
    }
  },
  async created() {
    this.supportsGeolocation = 'geolocation' in navigator;
    await this.loadAllRegions();
  },
  methods: {
    // 加载所有地域
    async loadAllRegions() {
      try {
        const response = await axios.get(`${API_BASE_URL}/regions`);
        this.allRegions = response.data;
      } catch (error) {
        console.error('加载地域列表失败:', error);
        this.allRegions = [];
      }
    },
    
    // 模拟定位
    simulateLocation() {
      this.isLocating = true;
      this.error = null;
      
      setTimeout(() => {
        const randomCity = this.mockCities[Math.floor(Math.random() * this.mockCities.length)];
        this.currentLocation = {
          province: randomCity.province,
          city: randomCity.city,
          regionCode: randomCity.region,
          isMock: true
        };
        this.isLocating = false;
        
        // 获取推荐
        this.getRecommendationByLocation();
        
        this.$message.success(`已定位到：${randomCity.province} ${randomCity.city}`);
      }, 1500);
    },
    
    // 真实定位
    getRealLocation() {
      if (!this.supportsGeolocation) {
        this.error = '您的浏览器不支持定位功能';
        return;
      }
      
      this.isLocating = true;
      this.error = null;
      
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            // 这里应该调用地图API逆地理编码
            // 为简化，使用模拟数据
            const mockResult = await this.reverseGeocode(
              position.coords.latitude,
              position.coords.longitude
            );
            
            this.currentLocation = {
              province: mockResult.province,
              city: mockResult.city,
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
              isMock: false
            };
            
            this.getRecommendationByLocation();
            this.$message.success(`定位成功：${mockResult.city}`);
          } catch (error) {
            this.error = '获取位置信息失败';
          } finally {
            this.isLocating = false;
          }
        },
        (error) => {
          this.isLocating = false;
          switch(error.code) {
            case error.PERMISSION_DENIED:
              this.error = '定位权限被拒绝，请允许定位';
              break;
            case error.POSITION_UNAVAILABLE:
              this.error = '位置信息不可用';
              break;
            case error.TIMEOUT:
              this.error = '定位请求超时';
              break;
            default:
              this.error = '定位失败';
          }
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      );
    },
    
    // 模拟逆地理编码
    async reverseGeocode(lat, lng) {
      // 这里应该调用真实的地图API
      // 返回最近的城市
      const cities = [
        { province: '贵州', city: '贵阳', lat: 26.647, lng: 106.63 },
        { province: '四川', city: '成都', lat: 30.572, lng: 104.066 },
        { province: '云南', city: '昆明', lat: 24.88, lng: 102.832 },
        { province: '北京', city: '北京', lat: 39.904, lng: 116.407 },
        { province: '上海', city: '上海', lat: 31.230, lng: 121.473 },
        { province: '广东', city: '广州', lat: 23.129, lng: 113.264 }
      ];
      
      // 简化：计算距离最近的城市
      let nearest = cities[0];
      let minDistance = Infinity;
      
      cities.forEach(city => {
        const distance = Math.sqrt(
          Math.pow(city.lat - lat, 2) + Math.pow(city.lng - lng, 2)
        );
        if (distance < minDistance) {
          minDistance = distance;
          nearest = city;
        }
      });
      
      return nearest;
    },
    
    async getRecommendationByLocation() {
      if (!this.currentLocation) return;
      
      this.loading = true;
      this.error = null;

      // ✅ 确保定义了 request 变量
      const request = {
        province: this.currentLocation.province || '',
        city: this.currentLocation.city || '',
        useMock: this.currentLocation.isMock || false
      };
      
      console.log('🔄 发送定位推荐请求:', request);

      try {
        // ✅ 确保使用后端期望的字段名
        const request = {
          province: this.currentLocation.province || '',  // 必须要有
          city: this.currentLocation.city || '',
          useMock: this.currentLocation.isMock || false
          // 注意：后端LocationRequest类应该有这些字段
        };
        
        console.log('发送定位推荐请求:', JSON.stringify(request, null, 2));
        
        const response = await axios.post(
          `${API_BASE_URL}/regions/recommendation`,
          request,
          {
            headers: {
              'Content-Type': 'application/json'
            }
          }
        );
        
        console.log('收到完整响应:', JSON.stringify(response.data, null, 2));
        
        // ✅ 重要：检查后端返回的数据结构
        if (!response.data) {
          throw new Error('后端返回空数据');
        }
        
        if (!response.data.region) {
          console.warn('后端返回的region为null，使用默认地区');
          // 手动添加默认地区信息
          response.data.region = {
            code: 'southwest',
            name: '西南地区',
            icon: '🏔️',
            specialtyDesc: '贵州特色茶饮',
            coverProvinces: '贵州、四川、云南',
            climateFeature: '湿润多雨',
            productCount: response.data.featuredProducts?.length || 0
          };
        }
        
        this.recommendation = response.data;
        this.selectedRegion = this.recommendation.region.code;
        
      } catch (error) {
        console.error('获取推荐失败:', error);
        
        // ✅ 如果API失败，直接调用获取地区产品的API
        await this.fallbackToRegionProducts();
        
        if (error.response) {
          console.error('错误详情:', error.response.data);
          this.error = `请求失败: ${error.response.status}`;
        } else if (error.request) {
          this.error = '无法连接到服务器';
        } else {
          this.error = error.message;
        }
      } finally {
        this.loading = false;
      }
    },

    // ✅ 添加后备方法：直接获取地区产品
    async fallbackToRegionProducts() {
      try {
        const regionCode = 'southwest'; // 默认西南地区
        
        // 1. 获取地区信息
        const regionResponse = await axios.get(`${API_BASE_URL}/regions/${regionCode}`);
        
        // 2. 获取该地区的产品
        const productsResponse = await axios.get(`${API_BASE_URL}/regions/${regionCode}/products`);
        
        // 3. 获取特色产品
        const featuredResponse = await axios.get(`${API_BASE_URL}/regions/${regionCode}/featured-products`);
        
        // 4. 构建推荐数据
        this.recommendation = {
          region: regionResponse.data,
          matchType: 'fallback',
          detectedProvince: this.currentLocation?.province || '贵州',
          detectedCity: this.currentLocation?.city || '贵阳',
          featuredProducts: featuredResponse.data,
          allProducts: productsResponse.data,
          weatherSuggestion: '根据当地气候推荐温热饮品',
          seasonalSuggestion: this.generateSeasonalSuggestion()
        };
        
        this.selectedRegion = regionCode;
        
      } catch (fallbackError) {
        console.error('后备方案也失败:', fallbackError);
        // 最后使用模拟数据
        this.useMockRecommendation();
      }
    },
    
    // 选择特定地域
    async selectRegion(regionCode) {
      this.selectedRegion = regionCode;
      this.loading = true;
      
      try {
        // 获取该地域的产品
        const [regionResponse, productsResponse] = await Promise.all([
          axios.get(`${API_BASE_URL}/regions/${regionCode}`),
          axios.get(`${API_BASE_URL}/regions/${regionCode}/products`)
        ]);
        
        const region = regionResponse.data;
        const products = productsResponse.data;
        
        // 构建推荐数据
        this.recommendation = {
          region: region,
          matchType: 'manual',
          detectedProvince: '手动选择',
          detectedCity: '',
          featuredProducts: products.filter(p => p.isFeatured),
          allProducts: products,
          weatherSuggestion: this.generateWeatherSuggestion(region.climateFeature),
          seasonalSuggestion: this.generateSeasonalSuggestion()
        };
        
      } catch (error) {
        console.error('加载地域数据失败:', error);
        this.error = '加载地域数据失败';
      } finally {
        this.loading = false;
      }
    },
    
    // 刷新推荐
    refreshRecommendation() {
      if (this.currentLocation) {
        this.getRecommendationByLocation();
      } else {
        this.simulateLocation();
      }
    },
    
    // 添加购物车
    async addToCart(product) {
      try {
        const response = await axios.post(
          `${API_BASE_URL}/cart/${this.userId}/add`,
          null,
          {
            params: {
              productId: product.productId,
              quantity: 1
            }
          }
        );
        
        this.$message.success(`"${product.productName}" 已加入购物车`);
        this.$emit('cart-updated');
      } catch (error) {
        console.error('添加购物车失败:', error);
        this.$message.error('添加到购物车失败');
      }
    },
    
    // 查看产品详情
    viewProductDetail(product) {
      this.selectedProduct = product;
    },
    
    // 快速查看
    quickView(product) {
      // 简单预览，这里可以显示简略信息
      this.$message.info(`查看: ${product.productName}`);
    },
    
    // 处理添加到购物车（从模态框）
    handleAddToCart(product) {
      this.addToCart(product);
      this.closeModal();
    },
    
    // 关闭模态框
    closeModal() {
      this.selectedProduct = null;
    },
    
    // 清除错误
    clearError() {
      this.error = null;
    },
    
    // 生成天气建议
    generateWeatherSuggestion(climate) {
      if (climate.includes('寒冷')) {
        return '天气寒冷，推荐热饮暖身';
      } else if (climate.includes('炎热')) {
        return '天气炎热，推荐清凉饮品';
      } else if (climate.includes('干燥')) {
        return '气候干燥，推荐滋润饮品';
      } else {
        return '气候适宜，各种饮品都很合适';
      }
    },
    
    // 生成季节建议
    generateSeasonalSuggestion() {
      const month = new Date().getMonth() + 1;
      if (month >= 12 || month <= 2) {
        return '冬季推荐热饮，温暖身心';
      } else if (month >= 3 && month <= 5) {
        return '春季推荐清新果茶，感受生机';
      } else if (month >= 6 && month <= 8) {
        return '夏季推荐冰饮，消暑解渴';
      } else {
        return '秋季推荐温和奶茶，滋养润燥';
      }
    },
    
    // 获取匹配类型文本
    getMatchTypeText(type) {
      const types = {
        exact: '精确匹配',
        fuzzy: '模糊匹配',
        manual: '手动选择',
        default: '默认推荐'
      };
      return types[type] || type;
    },
    
    getProductImage(product) {
      console.log('🔍 检查商品图片:', product);
      
      // 尝试不同的字段名
      let imageUrl = null;
      
      // 1. 检查后端可能返回的不同字段名
      const possibleKeys = [
        'productImageUrl',
        'imageUrl',
        'productImage',
        'image',
        'url'
      ];
      
      for (const key of possibleKeys) {
        if (product[key] && product[key].trim() !== '') {
          imageUrl = product[key];
          console.log(`✅ 从字段 ${key} 找到图片:`, imageUrl);
          break;
        }
      }
      
      // 2. 如果找到图片URL
      if (imageUrl) {
        // 移除前导斜杠，使用相对路径
        let cleanUrl = imageUrl;
        if (cleanUrl.startsWith('/')) {
          cleanUrl = cleanUrl.substring(1); // 移除开头的斜杠
        }
        
        // 确保路径正确（图片应该在 public/images/ 目录下）
        return '/' + cleanUrl; // 这将指向 Vue 的 public/images/ 目录
      }
      
      // 3. 使用本地默认图片（基于产品ID）
      const imageId = product.productId ? ((product.productId % 60) + 1) : 1;
      return `/images/${imageId}.png`;
    },

    // 图片加载错误处理（强制显示emoji）
    handleImageError(event) {
      console.error('图片加载失败:', event.target.src);
      event.target.style.display = 'none';
      
      // 创建一个emoji显示
      const emojiDiv = document.createElement('div');
      emojiDiv.className = 'product-emoji';
      emojiDiv.innerHTML = '🥤';
      emojiDiv.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 3rem;
        opacity: 1;
        z-index: 2;
      `;
      
      event.target.parentElement.appendChild(emojiDiv);
    },
    
    // 获取产品标签
    getProductTags(product) {
      if (product.productTags) {
        return product.productTags.split(',').map(tag => tag.trim());
      }
      return [];
    },
    
    // 截断文本
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },
    
    // 🆕 获取当前地区名称
    getCurrentRegionName() {
      if (this.recommendation && this.recommendation.region) {
        return this.recommendation.region.name;
      } else if (this.selectedRegion) {
        const region = this.allRegions.find(r => r.code === this.selectedRegion);
        return region ? region.name : '当前地区';
      }
      return '当前地区';
    },
    
    // 🆕 获取特色茶叶列表
    getSpecialtyTeas() {
      const regionCode = this.selectedRegion || 
                       (this.recommendation && this.recommendation.region ? 
                        this.recommendation.region.code : 'southwest');
      
      return this.specialtyTeas[regionCode] || this.specialtyTeas.southwest;
    },
    
    // 🆕 获取茶叶图标
    getTeaIcon(teaName) {
      if (teaName.includes('龙井') || teaName.includes('毛尖') || teaName.includes('绿茶')) {
        return '🍃';
      } else if (teaName.includes('普洱') || teaName.includes('黑茶') || teaName.includes('茯砖')) {
        return '🫖';
      } else if (teaName.includes('奶茶')) {
        return '🥛';
      } else if (teaName.includes('花茶') || teaName.includes('茉莉') || teaName.includes('玫瑰')) {
        return '🌸';
      } else if (teaName.includes('凉茶')) {
        return '🌿';
      } else if (teaName.includes('人参') || teaName.includes('枸杞')) {
        return '🌱';
      } else if (teaName.includes('乌龙') || teaName.includes('铁观音')) {
        return '🍵';
      } else if (teaName.includes('白茶')) {
        return '⚪';
      } else if (teaName.includes('杏仁')) {
        return '🥜';
      } else {
        return '🍵';
      }
    },
    
    // 🆕 显示茶叶详情
    showTeaDetail(tea) {
      this.selectedTea = tea;
    },
    
    // 🆕 关闭茶叶模态框
    closeTeaModal() {
      this.selectedTea = null;
    },
    
    // 🆕 添加茶叶相关产品到购物车
// 🆕 添加茶叶本身到购物车
async addTeaToCart(tea) {
  try {
    console.log('添加茶叶到购物车:', tea);
    
    // 茶叶名称到产品ID的映射（与后端DataInitializer对应）
    const teaProductMap = {
      // 西南地区
      "贵州白茶": 61,
      "云南普洱茶": 62,
      "四川蒙顶茶": 63,
      
      // 华东地区
      "杭州西湖龙井": 64,
      "福建铁观音": 65,
      "安徽黄山毛峰": 66,
      
      // 华南地区
      "广东凉茶": 67,
      "广西六堡茶": 68,
      "海南鹧鸪茶": 69,
      
      // 华北地区
      "北京茉莉花茶": 70,
      "山西沙棘茶": 71,
      "蒙古奶茶": 72,
      
      // 东北地区
      "长白山人参茶": 73,
      "东北松子茶": 74,
      "黑森林红茶": 75,
      
      // 西北地区
      "宁夏枸杞茶": 76,
      "新疆玫瑰花茶": 77,
      "陕西茯砖茶": 78,
      
      // 华中地区
      "湖北恩施玉露": 79,
      "湖南君山银针": 80,
      "河南信阳毛尖": 81,
      
      // 东南地区
      "台湾高山茶": 82,
      "港式奶茶": 83,
      "澳门杏仁茶": 84
    };
    
    // 查找对应的产品ID
    let productId = teaProductMap[tea.name];
    
    if (!productId) {
      // 如果没有找到精确匹配，尝试根据茶叶类型推荐类似产品
      console.warn(`未找到茶叶"${tea.name}"的精确映射，尝试类型匹配`);
      
      const typeFallbackMap = {
        "白茶": 61,      // 贵州白茶
        "黑茶": 62,      // 云南普洱茶
        "绿茶": 64,      // 杭州西湖龙井
        "乌龙茶": 65,    // 福建铁观音
        "花茶": 70,      // 北京茉莉花茶
        "奶茶": 72,      // 蒙古奶茶
        "保健茶": 73,    // 长白山人参茶
        "草本茶": 67,    // 广东凉茶
        "黄茶": 80,      // 湖南君山银针
        "野生茶": 69,    // 海南鹧鸪茶
        "坚果茶": 74     // 东北松子茶
      };
      
      productId = typeFallbackMap[tea.type] || 61; // 默认贵州白茶
      
      this.$message.info(`为您推荐类似的 ${tea.type}：${this.getTeaNameById(productId)}`);
    }
    
    // 添加到购物车
    const response = await axios.post(
      `${API_BASE_URL}/cart/${this.userId}/add`,
      null,
      {
        params: {
          productId: productId,
          quantity: 1
        }
      }
    );
    
    this.$message.success(`"${tea.name}" 已加入购物车`);
    this.$emit('cart-updated');
    
  } catch (error) {
    console.error('添加茶叶到购物车失败:', error);
    
    if (error.response) {
      switch (error.response.status) {
        case 404:
          this.$message.error(`"${tea.name}" 产品暂时缺货，请稍后再试`);
          break;
        case 400:
          this.$message.error('产品参数错误');
          break;
        case 500:
          this.$message.error('服务器错误，请稍后再试');
          break;
        default:
          this.$message.error('添加到购物车失败');
      }
    } else {
      this.$message.error('网络连接失败，请检查网络');
    }
  }
},

// 🆕 辅助方法：根据ID获取茶叶名称
getTeaNameById(productId) {
  const teaIdNameMap = {
    61: "贵州白茶",
    62: "云南普洱茶",
    63: "四川蒙顶茶",
    64: "杭州西湖龙井",
    65: "福建铁观音",
    66: "安徽黄山毛峰",
    67: "广东凉茶",
    68: "广西六堡茶",
    69: "海南鹧鸪茶",
    70: "北京茉莉花茶",
    71: "山西沙棘茶",
    72: "蒙古奶茶",
    73: "长白山人参茶",
    74: "东北松子茶",
    75: "黑森林红茶",
    76: "宁夏枸杞茶",
    77: "新疆玫瑰花茶",
    78: "陕西茯砖茶",
    79: "湖北恩施玉露",
    80: "湖南君山银针",
    81: "河南信阳毛尖",
    82: "台湾高山茶",
    83: "港式奶茶",
    84: "澳门杏仁茶"
  };
  
  return teaIdNameMap[productId] || "特色茶叶";
},
  }
};
</script>

<style scoped>
.region-recommendation {
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

/* 头部样式 */
.region-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 25px;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-content {
  margin-bottom: 25px;
}

.region-title {
  font-size: 2.2rem;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 15px;
}

.region-icon {
  font-size: 2.5rem;
}

.region-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

/* 定位控制 */
.location-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.current-location {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.1rem;
  font-weight: 500;
}

.current-location.no-location {
  opacity: 0.7;
}

.location-icon {
  font-size: 1.5rem;
}

.matched-region {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
}

.location-buttons {
  display: flex;
  gap: 12px;
}

.location-btn {
  padding: 12px 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
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

.location-btn.locating {
  background: rgba(255, 255, 255, 0.2);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 0.7; }
  50% { opacity: 1; }
  100% { opacity: 0.7; }
}

.simulate-btn {
  background: rgba(255, 107, 107, 0.3);
  border-color: rgba(255, 107, 107, 0.5);
}

.real-btn {
  background: rgba(40, 167, 69, 0.3);
  border-color: rgba(40, 167, 69, 0.5);
}

.refresh-btn {
  background: rgba(108, 117, 125, 0.3);
  border-color: rgba(108, 117, 125, 0.5);
}

.btn-icon {
  font-size: 1.2rem;
}

/* 错误提示 */
.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 4px solid #dc3545;
}

.error-icon {
  margin-right: 10px;
  font-size: 1.2rem;
}

.error-close {
  background: none;
  border: none;
  color: #721c24;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e9ecef;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #666;
  font-size: 1.1rem;
}

/* 地域卡片 */
.region-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.region-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f8f9fa;
}

.region-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.region-card-icon {
  font-size: 3rem;
}

.region-details {
  flex: 1;
}

.region-name {
  font-size: 1.8rem;
  margin: 0 0 8px 0;
  color: #333;
}

.region-specialty {
  color: #666;
  margin: 0;
  font-size: 1.1rem;
}

.match-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: bold;
}

.match-badge.exact {
  background: #28a745;
  color: white;
}

.match-badge.fuzzy {
  background: #ffc107;
  color: #212529;
}

.match-badge.manual {
  background: #6c757d;
  color: white;
}

.match-badge.default {
  background: #17a2b8;
  color: white;
}

.region-card-body {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.region-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-icon {
  font-size: 2rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
}

.suggestion-icon {
  font-size: 1.8rem;
}

.suggestion-content {
  display: flex;
  flex-direction: column;
}

.suggestion-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.suggestion-text {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.4;
}

/* 地域筛选 */
.region-filter {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.filter-header {
  margin-bottom: 20px;
}

.filter-title {
  font-size: 1.4rem;
  margin: 0 0 8px 0;
  color: #333;
}

.filter-subtitle {
  color: #666;
  margin: 0;
}

.filter-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.region-btn {
  padding: 15px;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.region-btn:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.region-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.btn-region-icon {
  font-size: 2rem;
}

.btn-region-name {
  font-size: 0.95rem;
  font-weight: 500;
}

/* 产品区域 */
.featured-products-section,
.all-products-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.6rem;
  margin: 0 0 8px 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  font-size: 1.8rem;
}

.section-subtitle {
  color: #666;
  margin: 0;
}

.product-count {
  background: #f8f9fa;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.9rem;
  color: #666;
  margin-left: 10px;
}

/* 产品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.product-card {
  background: white;
  border: 2px solid #f0f0f0;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.product-card.featured {
  border-color: #ffc107;
}

.product-image {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.region-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(255, 255, 255, 0.9);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.tag-icon {
  font-size: 1.2rem;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 1.3rem;
  margin: 0 0 8px 0;
  color: #333;
}

.product-local-name {
  color: #e74c3c;
  font-size: 0.9rem;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-description {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 15px 0;
  line-height: 1.5;
}

.product-reason {
  background: #f8f9fa;
  padding: 10px 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reason-icon {
  font-size: 1rem;
  color: #667eea;
}

.reason-text {
  color: #495057;
  font-size: 0.9rem;
  font-style: italic;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.product-tag {
  background: #e9ecef;
  color: #495057;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.popularity-tag {
  background: #ffeaa7;
  color: #e17055;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 1.4rem;
  font-weight: bold;
  color: #e74c3c;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.add-cart-btn,
.quick-view-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s;
}

.add-cart-btn {
  background: #28a745;
  color: white;
}

.add-cart-btn:hover {
  background: #218838;
  transform: scale(1.1);
}

.quick-view-btn {
  background: #6c757d;
  color: white;
}

.quick-view-btn:hover {
  background: #545b62;
  transform: scale(1.1);
}

/* 产品列表 */
.products-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.product-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.product-item.featured {
  border-left-color: #ffc107;
}

.item-image {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-featured-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ffc107;
  color: #212529;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.item-info {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-name {
  font-size: 1.2rem;
  margin: 0;
  color: #333;
}

.item-price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #e74c3c;
}

.item-desc {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 15px 0;
  line-height: 1.5;
}

.item-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.item-tag {
  background: #dee2e6;
  color: #495057;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.item-action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.cart-btn {
  background: #28a745;
  color: white;
}

.cart-btn:hover {
  background: #218838;
}

/* 空状态 */
.empty-state,
.empty-products {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  font-size: 1.4rem;
  margin: 0 0 10px 0;
  color: #495057;
}

.empty-text {
  font-size: 1rem;
  margin: 0 0 20px 0;
}

.empty-subtext {
  font-size: 0.9rem;
  color: #888;
  margin: 5px 0 0 0;
}

.empty-action-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 模态框 */
.product-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from { transform: translateY(50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  font-size: 2rem;
  color: #666;
  cursor: pointer;
  z-index: 10;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .region-card-body {
    grid-template-columns: 1fr;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .region-header {
    padding: 20px;
  }
  
  .region-title {
    font-size: 1.8rem;
  }
  
  .location-controls {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .location-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .region-btn {
    min-width: 160px;
  }
  
  .filter-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .product-item {
    flex-direction: column;
  }
  
  .item-image {
    width: 100%;
    height: 150px;
  }
}

@media (max-width: 480px) {
  .region-filter {
    padding: 15px;
  }
  
  .filter-buttons {
    grid-template-columns: 1fr;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
}

/* 让页面可以滚动 */
.region-recommendation {
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
  overflow-y: auto; /* 添加这个 */
  height: calc(100vh - 60px); /* 减去可能的导航栏高度 */
}

/* 确保内容容器能滚动 */
.recommendation-content {
  max-height: none; /* 移除任何高度限制 */
  overflow: visible;
}

/* 新增样式 */
.special-teas-section {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 25px;
  border: 2px solid #90caf9;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-header {
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.6rem;
  margin: 0 0 8px 0;
  color: #1565c0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  font-size: 1.8rem;
}

.section-subtitle {
  color: #546e7a;
  margin: 0;
  font-size: 1rem;
}

.teas-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.tea-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s;
  border: 1px solid #e3f2fd;
  cursor: pointer;
  position: relative;
}

.tea-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(144, 202, 249, 0.3);
}

.tea-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.tea-info {
  flex: 1;
  min-width: 0;
}

.tea-name {
  font-size: 1.2rem;
  margin: 0 0 5px 0;
  color: #1565c0;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tea-origin {
  font-size: 0.85rem;
  color: #78909c;
  margin: 0 0 10px 0;
  font-style: italic;
}

.tea-desc {
  font-size: 0.9rem;
  color: #546e7a;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

   /* 修复兼容性问题 - 正确的多行文本截断写法 */
  display: -webkit-box;
  display: -moz-box;
  display: box;
  -webkit-line-clamp: 2;
  -moz-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical;
  box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  
  /* 备用方案：确保在非Webkit浏览器中也能正常工作 */
  max-height: 2.8em; /* 2行 x 1.4行高 */
}

.tea-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tea-tag {
  background: #e3f2fd;
  color: #1565c0;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.tea-action-btn {
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s;
  flex-shrink: 0;
}

.tea-action-btn:hover {
  background: #388e3c;
  transform: scale(1.1);
}

/* 茶叶详情模态框样式 */
.tea-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.3s;
}

.tea-detail {
  padding: 30px;
}

.detail-header {
  text-align: center;
  margin-bottom: 30px;
}

.detail-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 15px;
}

.detail-header h3 {
  font-size: 1.8rem;
  margin: 0 0 10px 0;
  color: #1565c0;
}

.detail-origin {
  color: #78909c;
  font-style: italic;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h4 {
  color: #1976d2;
  margin: 0 0 10px 0;
  font-size: 1.1rem;
}

.detail-section p {
  color: #546e7a;
  line-height: 1.6;
  margin: 0;
}

.detail-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detail-section li {
  padding: 8px 0;
  color: #546e7a;
  border-bottom: 1px solid #e3f2fd;
}

.detail-section li:last-child {
  border-bottom: none;
}

.detail-stats {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin: 25px 0;
}

.stat {
  margin-bottom: 15px;
}

.stat:last-child {
  margin-bottom: 0;
}

.stat-label {
  display: block;
  margin-bottom: 8px;
  color: #546e7a;
  font-weight: 500;
}

.stat-bar {
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.stat-fill {
  height: 100%;
  background: linear-gradient(90deg, #4fc3f7, #2196f3);
  border-radius: 4px;
}

.detail-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.primary-btn, .secondary-btn {
  padding: 12px 24px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.primary-btn {
  background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
  color: white;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.secondary-btn {
  background: #f5f5f5;
  color: #666;
}

.secondary-btn:hover {
  background: #e0e0e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teas-container {
    grid-template-columns: 1fr;
  }
  
  .tea-card {
    flex-direction: column;
    text-align: center;
  }
  
  .tea-info {
    text-align: center;
  }
  
  .tea-tags {
    justify-content: center;
  }
  
  .detail-actions {
    flex-direction: column;
  }
  
  .primary-btn, .secondary-btn {
    width: 100%;
  }
}

</style>