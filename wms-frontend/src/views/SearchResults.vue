<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { Search, Package, FileText, Loader } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const searchQuery = ref('');
const loading = ref(false);
const searchResults = ref({
  products: [],
  inboundOrders: [],
  outboundOrders: []
});

const performSearch = async (query) => {
  if (!query) return;
  
  loading.value = true;
  searchQuery.value = query;
  
  try {
    // 搜索产品
    const productRes = await request.get('/product/page', {
      params: { productName: query, pageNum: 1, pageSize: 10 }
    });
    searchResults.value.products = productRes.data?.records || [];

    // 搜索入库单
    const inboundRes = await request.get('/inbound/page', {
      params: { orderNo: query, pageNum: 1, pageSize: 10 }
    });
    searchResults.value.inboundOrders = inboundRes.data?.records || [];

    // 搜索出库单
    const outboundRes = await request.get('/outbound/page', {
      params: { orderNo: query, pageNum: 1, pageSize: 10 }
    });
    searchResults.value.outboundOrders = outboundRes.data?.records || [];
  } catch (error) {
    console.error('搜索失败:', error);
  } finally {
    loading.value = false;
  }
};

// 点击产品卡片，跳转到货物信息页面并筛选该产品
const handleProductClick = (product) => {
  router.push({
    path: '/cargo',
    query: { productName: product.productName }
  });
};

// 点击入库单卡片，跳转到入库单详情
const handleInboundClick = (order) => {
  router.push(`/inbound/detail/${order.inboundId}`);
};

// 点击出库单卡片，跳转到出库单详情
const handleOutboundClick = (order) => {
  router.push(`/outbound/detail/${order.outboundId}`);
};

onMounted(() => {
  const query = route.query.q;
  if (query) {
    performSearch(query);
  }
});

// 监听路由变化
watch(() => route.query.q, (newQuery) => {
  if (newQuery) {
    performSearch(newQuery);
  }
});

const getTotalResults = () => {
  return searchResults.value.products.length +
         searchResults.value.inboundOrders.length +
         searchResults.value.outboundOrders.length;
};
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <Search :size="28" />
        搜索结果
      </h1>
      <p class="page-subtitle" v-if="searchQuery">
        搜索关键词：<strong>"{{ searchQuery }}"</strong>
        <span v-if="!loading"> - 共找到 {{ getTotalResults() }} 条结果</span>
      </p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <Loader :size="48" class="spinner" />
      <p>正在搜索...</p>
    </div>

    <!-- 搜索结果 -->
    <div v-else-if="getTotalResults() > 0" class="results-container">
      <!-- 产品结果 -->
      <div v-if="searchResults.products.length > 0" class="result-section">
        <h2 class="section-title">
          <Package :size="20" />
          产品信息 ({{ searchResults.products.length }})
        </h2>
        <div class="result-cards">
          <div 
            v-for="product in searchResults.products" 
            :key="product.productId" 
            class="result-card"
            @click="handleProductClick(product)"
          >
            <div class="card-icon product-icon">
              <Package :size="24" />
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ product.productName }}</h3>
              <p class="card-meta">编码: {{ product.productCode }}</p>
              <p class="card-meta" v-if="product.categoryName">分类: {{ product.categoryName }}</p>
              <p class="card-meta" v-if="product.unit">单位: {{ product.unit }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 入库单结果 -->
      <div v-if="searchResults.inboundOrders.length > 0" class="result-section">
        <h2 class="section-title">
          <FileText :size="20" />
          入库单 ({{ searchResults.inboundOrders.length }})
        </h2>
        <div class="result-cards">
          <div 
            v-for="order in searchResults.inboundOrders" 
            :key="order.orderId" 
            class="result-card"
            @click="handleInboundClick(order)"
          >
            <div class="card-icon inbound-icon">
              <FileText :size="24" />
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ order.orderNo }}</h3>
              <p class="card-meta">供应商: {{ order.supplierName || '未知' }}</p>
              <p class="card-meta">状态: 
                <span :class="'status-' + order.status">{{ getOrderStatus(order.status) }}</span>
              </p>
              <p class="card-meta" v-if="order.createTime">创建时间: {{ order.createTime }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 出库单结果 -->
      <div v-if="searchResults.outboundOrders.length > 0" class="result-section">
        <h2 class="section-title">
          <FileText :size="20" />
          出库单 ({{ searchResults.outboundOrders.length }})
        </h2>
        <div class="result-cards">
          <div 
            v-for="order in searchResults.outboundOrders" 
            :key="order.orderId" 
            class="result-card"
            @click="handleOutboundClick(order)"
          >
            <div class="card-icon outbound-icon">
              <FileText :size="24" />
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ order.orderNo }}</h3>
              <p class="card-meta">客户: {{ order.customerName || '未知' }}</p>
              <p class="card-meta">状态: 
                <span :class="'status-' + order.status">{{ getOrderStatus(order.status) }}</span>
              </p>
              <p class="card-meta" v-if="order.createTime">创建时间: {{ order.createTime }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 无结果 -->
    <div v-else-if="!loading && searchQuery" class="empty-state">
      <Search :size="64" class="empty-icon" />
      <h2>未找到相关结果</h2>
      <p>请尝试使用其他关键词进行搜索</p>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    getOrderStatus(status) {
      const statusMap = {
        0: '待处理',
        1: '进行中',
        2: '已完成',
        3: '已取消'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style scoped>
.page-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.page-subtitle strong {
  color: var(--primary-color);
  font-weight: 600;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  color: var(--text-secondary);
}

.spinner {
  animation: spin 1s linear infinite;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.result-section {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid var(--bg-color);
}

.result-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.result-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: 0.75rem;
  transition: all 0.2s;
  cursor: pointer;
}

.result-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(132, 204, 22, 0.1);
  transform: translateY(-2px);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-icon {
  background: linear-gradient(135deg, #84cc16 0%, #65a30d 100%);
  color: white;
}

.inbound-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.outbound-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0.25rem 0;
}

.status-0 { color: #f59e0b; }
.status-1 { color: #3b82f6; }
.status-2 { color: #10b981; }
.status-3 { color: #ef4444; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  color: var(--text-secondary);
}

.empty-icon {
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-state h2 {
  font-size: 1.5rem;
  color: var(--text-main);
  margin-bottom: 0.5rem;
}

.empty-state p {
  font-size: 1rem;
}
</style>
