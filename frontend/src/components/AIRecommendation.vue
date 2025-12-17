<template>
  <div class="ai-recommendation">
    <div class="recommendation-header">
      <h2>🤖 AI智能奶茶推荐</h2>
      <p>告诉我你的喜好，让我为你推荐最适合的奶茶！</p>
    </div>

    <div class="recommendation-form">
      <div class="form-group">
        <label for="preference">口味偏好：</label>
        <select id="preference" v-model="formData.userPreference" class="form-select">
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
        <select id="weather" v-model="formData.weather" class="form-select">
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
        <select id="mood" v-model="formData.mood" class="form-select">
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
        @click="getRecommendation" 
        :disabled="loading || !isFormValid"
        class="recommend-btn"
      >
        {{ loading ? '推荐中...' : '获取AI推荐' }}
      </button>
    </div>

    <!-- 推荐结果 -->
    <div v-if="recommendation" class="recommendation-result">
      <div class="result-card">
        <div class="result-header">
          <h3>🎯 为您推荐</h3>
          <span class="ai-badge">AI推荐</span>
        </div>
        
        <div class="product-info">
          <div class="product-image" v-if="recommendation.productImage">
            <img :src="recommendation.productImage" :alt="recommendation.recommendedProduct" />
          </div>
          <h4 class="product-name">{{ recommendation.recommendedProduct }}</h4>
          <div class="product-price">¥{{ recommendation.productPrice }}</div>
          
          <div class="reasoning">
            <strong>推荐理由：</strong>
            <p>{{ recommendation.reasoning }}</p>
          </div>
          <div class="description">
            <strong>产品描述：</strong>
            <p>{{ recommendation.description }}</p>
          </div>
        </div>

        <div class="result-actions">
          <!-- 调试按钮 -->
          <button @click="forceDebug" class="debug-btn" style="background: #ffc107; color: black;">
            🐛 调试信息
          </button>
          <button @click="addToCart" class="add-cart-btn" :disabled="addingToCart">
            🛒 {{ addingToCart ? '添加中...' : '加入购物车' }}
          </button>
          <button @click="getAnotherRecommendation" class="another-btn">
            🔄 再推荐一个
          </button>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- 成功提示 -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'AIRecommendation',
  data() {
    return {
      formData: {
        userPreference: '',
        weather: '',
        mood: ''
      },
      recommendation: null,
      loading: false,
      addingToCart: false,
      error: '',
      successMessage: ''
    }
  },
  computed: {
    isFormValid() {
      return this.formData.userPreference && this.formData.weather && this.formData.mood;
    }
  },
  mounted() {
    console.log('=== 🎯 AIRecommendation 组件开始挂载 ===');
    
    // 确保在下一个tick设置，避免时机问题
    this.$nextTick(() => {
      window.aiRecommendationComponent = this;
      console.log('✅ 组件实例已设置为全局变量:', this);
      console.log('✅ 当前推荐数据:', this.recommendation);
      
      // 测试方法是否存在
      console.log('✅ addToCart方法:', typeof this.addToCart);
    });
  },
  methods: {
    async addToCart() {
      console.log('=== 🚨 addToCart方法被调用 ===');
      
      // 详细日志
      console.log('📊 完整推荐数据:', JSON.stringify(this.recommendation, null, 2));
      console.log('🎯 商品ID:', this.recommendation?.productId);
      console.log('🎯 商品名称:', this.recommendation?.recommendedProduct);
      
      if (!this.recommendation || !this.recommendation.productId) {
        console.error('❌ 错误：商品信息不完整，缺少productId');
        this.error = '无法添加商品到购物车：商品信息不完整';
        return;
      }

      console.log('📊 当前推荐数据:', this.recommendation);
      
      // 硬编码用户ID为3
      const userId = 3;
      const productId = this.recommendation.productId;
      const productName = this.recommendation.recommendedProduct;

      console.log('🎯 最终参数:', { userId, productId, productName });

      this.addingToCart = true;
      this.error = '';
      this.successMessage = '';

      try {
        const url = `http://localhost:8081/api/cart/${userId}/add?productId=${productId}&quantity=1`;
        console.log('🌐 发送请求到:', url);

        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          }
        });

        console.log('📡 响应状态:', response.status, response.statusText);

        if (!response.ok) {
          const errorText = await response.text();
          console.error('❌ 请求失败:', response.status, errorText);
          throw new Error(`添加到购物车失败: ${response.status}`);
        }

        const result = await response.json();
        console.log('✅ 添加成功，响应数据:', result);
        
        this.successMessage = `"${productName}" 已成功加入购物车！`;
        console.log('🎉 添加购物车流程完成');
        
      } catch (err) {
        console.error('❌ 添加购物车异常:', err);
        this.error = err.message || '添加到购物车失败，请稍后重试';
      } finally {
        this.addingToCart = false;
      }
    },

    async getRecommendation() {
      console.log('🔵 开始获取推荐');
      
      if (!this.isFormValid) {
        this.error = '请填写完整信息';
        return;
      }

      this.loading = true;
      this.error = '';
      this.successMessage = '';
      this.recommendation = null;

      try {
        const response = await fetch('http://localhost:8081/api/recommendations/ai-recommendation', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.formData)
        });
        
        console.log('🟢 请求完成');

        if (!response.ok) {
          throw new Error('推荐服务暂时不可用');
        }

        const data = await response.json();
        console.log('🟢 推荐数据:', data);
        
        if (data.success) {
          this.recommendation = data;
        } else {
          this.recommendation = data.recommendation;
        }
        
      } catch (err) {
        this.error = err.message || '获取推荐失败，请稍后重试';
        console.error('AI推荐错误:', err);
      } finally {
        this.loading = false;
      }
    },

    getAnotherRecommendation() {
      this.recommendation = null;
      this.successMessage = '';
      this.getRecommendation();
    },

    // 强制调试方法
    forceDebug() {
      console.log('=== 🐛 强制调试开始 ===');
      console.log('this实例:', this);
      console.log('推荐数据:', this.recommendation);
      console.log('商品ID:', this.recommendation?.productId);
      console.log('商品名称:', this.recommendation?.recommendedProduct);
      console.log('商品价格:', this.recommendation?.productPrice);
      
      alert(`调试信息：
商品: ${this.recommendation?.recommendedProduct}
ID: ${this.recommendation?.productId}
价格: ¥${this.recommendation?.productPrice}

点击确定后将继续执行加入购物车逻辑`);
      
      this.addToCart();
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.ai-recommendation {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

.recommendation-header {
  text-align: center;
  margin-bottom: 30px;
}

.recommendation-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.recommendation-header p {
  color: #666;
  font-size: 14px;
}

.recommendation-form {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-select {
  width: 100%;
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  transition: border-color 0.3s;
}

.form-select:focus {
  outline: none;
  border-color: #007bff;
}

.recommend-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
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

.recommendation-result {
  animation: fadeIn 0.5s ease-in;
}

.result-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f8f9fa;
}

.result-header h3 {
  color: #333;
  margin: 0;
}

.ai-badge {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  margin-bottom: 20px;
}

.product-image {
  text-align: center;
  margin-bottom: 15px;
}

.product-image img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #f0f0f0;
}

.product-name {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 10px;
  text-align: center;
}

.product-price {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 15px;
}

.reasoning, .description {
  margin-bottom: 15px;
}

.reasoning strong, .description strong {
  color: #333;
  display: block;
  margin-bottom: 5px;
}

.reasoning p, .description p {
  color: #666;
  line-height: 1.6;
  margin: 0;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.result-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-direction: column;
}

.debug-btn, .add-cart-btn, .another-btn {
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.debug-btn:hover {
  background: #e0a800 !important;
  transform: translateY(-1px);
}

.add-cart-btn {
  background: #28a745;
  color: white;
}

.add-cart-btn:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-1px);
}

.add-cart-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.another-btn {
  background: #6c757d;
  color: white;
}

.another-btn:hover {
  background: #545b62;
  transform: translateY(-1px);
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  margin-top: 15px;
}

.success-message {
  background: #d4edda;
  color: #155724;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  margin-top: 15px;
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
@media (max-width: 768px) {
  .ai-recommendation {
    padding: 15px;
  }
  
  .result-actions {
    flex-direction: column;
  }
  
  .product-name {
    font-size: 20px;
  }
  
  .product-image img {
    width: 100px;
    height: 100px;
  }
}
</style>