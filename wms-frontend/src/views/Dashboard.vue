<script setup>
import { ref, onMounted, computed } from 'vue';
import { ArrowRight, Box, Settings, Package, ArrowDown, ArrowUp, RotateCw, TrendingUp } from 'lucide-vue-next';
import request from '../utils/request';
import { useRouter } from 'vue-router';

const router = useRouter();

// 数据状态
const loading = ref(true);
const dashboardData = ref({
  basicStats: {
    pendingInbound: 0,
    pendingOutbound: 0
  },
  adminStats: null,
  permissions: []
});

const inventoryInfo = ref({
  topInventory: [],
  zoneCapacity: []
});

const activities = ref([]);

// 获取当前用户信息
const username = localStorage.getItem('username') || '用户';
const role = localStorage.getItem('role') || '普通用户';

// 判断是否是管理员
const isAdmin = computed(() => {
  return dashboardData.value.permissions.includes('ROLE_ADMIN');
});

// 判断是否有库存权限
const hasInventoryPermission = computed(() => {
  return dashboardData.value.permissions.some(p => 
    p.includes('wms:inventory:list') || p.includes('ROLE_ADMIN')
  );
});

// 获取仪表盘数据
const fetchDashboardStats = async () => {
  try {
    const res = await request.get('/dashboard/stats');
    if (res.code === 200) {
      dashboardData.value = res.data;
    }
  } catch (error) {
    console.error('获取仪表盘统计失败:', error);
  }
};

// 获取库存信息
const fetchInventoryInfo = async () => {
  if (!hasInventoryPermission.value) return;
  
  try {
    const res = await request.get('/dashboard/inventory');
    if (res.code === 200) {
      inventoryInfo.value = res.data;
    }
  } catch (error) {
    console.error('获取库存信息失败:', error);
  }
};

// 获取最近动态
const fetchRecentActivities = async () => {
  try {
    const res = await request.get('/dashboard/activities');
    if (res.code === 200) {
      activities.value = res.data.activities || [];
    }
  } catch (error) {
    console.error('获取最近动态失败:', error);
  }
};

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return '';
  
  const date = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000); // 秒

  if (diff < 60) return '刚刚';
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;
  return `${Math.floor(diff / 86400)}天前`;
};

// 获取活动图标类型
const getActivityIconClass = (type) => {
  if (type === 'inbound') return 'in';
  if (type === 'outbound') return 'out';
  return 'audit';
};

// 初始化
onMounted(async () => {
  loading.value = true;
  await fetchDashboardStats();
  
  // 根据权限加载其他数据
  if (hasInventoryPermission.value) {
    await fetchInventoryInfo();
  }
  
  await fetchRecentActivities();
  loading.value = false;
});

// 跳转到任务中心
const goToTasks = () => {
  router.push('/inbound');
};
</script>

<template>
  <div class="dashboard">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 仪表盘内容 -->
    <div v-else>
      <div class="hero-section">
        <div class="hero-card">
          <div class="hero-content">
            <h1>智能仓储管理系统</h1>
            <p>实时追踪库存与作业状态</p>
            <div class="hero-actions">
              <button 
                v-if="hasInventoryPermission" 
                class="btn btn-primary bg-white text-primary"
                @click="router.push('/inventory')"
              >
                查看库存
              </button>
              <button 
                class="btn btn-outline"
                style="background: rgba(0,0,0,0.3); color: white; border: 1px solid rgba(255,255,255,0.5);"
                @click="router.push('/cargo')"
              >
                货物管理
              </button>
            </div>
          </div>
          <img src="https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" alt="Warehouse" class="hero-bg" />
          <div class="hero-overlay"></div>
        </div>

        <div class="stats-card-main">
          <h3>快速统计</h3>
          <div class="stat-row">
            <div class="big-stat">
              <span class="number">{{ dashboardData.basicStats.pendingInbound }}</span>
              <span class="label">待入库任务</span>
            </div>
            <div class="big-stat">
              <span class="number">{{ dashboardData.basicStats.pendingOutbound }}</span>
              <span class="label">待出库任务</span>
            </div>
          </div>
          <a href="#" class="link-arrow" @click.prevent="goToTasks">
            前往任务中心 <ArrowRight :size="16" />
          </a>
          <div class="decoration-icon">
            <Box :size="64" />
          </div>
        </div>
      </div>

      <!-- 管理员专属统计 -->
      <div v-if="isAdmin && dashboardData.adminStats" class="admin-stats">
        <div class="stat-card">
          <div class="stat-icon" style="background: #dbeafe; color: #1e40af;">
            <Package :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">总产品种类</div>
            <div class="stat-number">{{ dashboardData.adminStats.totalProducts }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #fef3c7; color: #b45309;">
            <TrendingUp :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日入库</div>
            <div class="stat-number">{{ dashboardData.adminStats.todayInbound }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #d1fae5; color: #047857;">
            <ArrowDown :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日出库</div>
            <div class="stat-number">{{ dashboardData.adminStats.todayOutbound }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #e0e7ff; color: #4338ca;">
            <Box :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">总库存数</div>
            <div class="stat-number">{{ dashboardData.adminStats.totalInventory }}</div>
          </div>
        </div>
      </div>

      <div class="content-grid">
        <div class="left-col">
          <!-- 库存信息（需要权限） -->
          <div v-if="hasInventoryPermission">
            <div class="section-header">
              <h3>最新库存</h3>
              <a href="#" @click.prevent="router.push('/inventory')">查看全部 ></a>
            </div>
            <div class="inventory-cards" v-if="inventoryInfo.topInventory.length > 0">
              <div 
                v-for="(item, index) in inventoryInfo.topInventory.slice(0, 3)" 
                :key="index"
                class="card"
              >
                <div class="icon-box"><Box :size="32" /></div>
                <h4>{{ item.productName || '未知产品' }}</h4>
                <p>数量: {{ item.totalQty || 0 }}</p>
              </div>
            </div>
            <div v-else class="empty-state-small">
              <p>暂无库存数据</p>
            </div>

            <div class="capacity-section">
              <h3>仓库容量</h3>
              <div class="capacity-cards">
                <div 
                  v-for="(zone, index) in inventoryInfo.zoneCapacity" 
                  :key="index"
                  class="cap-card"
                >
                  <div class="cap-info">
                    <h4>{{ zone.zoneName }}</h4>
                    <span>{{ zone.current }} / {{ zone.total }}</span>
                  </div>
                  <div class="progress-bar">
                    <div 
                      class="progress" 
                      :class="{ 'warning': zone.percentage < 50 }"
                      :style="{ width: zone.percentage + '%' }"
                    ></div>
                  </div>
                  <span class="muted">{{ zone.percentage }}% 已使用</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 无权限提示 -->
          <div v-else class="no-permission">
            <Package :size="48" class="icon" />
            <h3>暂无库存查看权限</h3>
            <p>请联系管理员开通相应权限</p>
          </div>
        </div>

        <div class="right-col">
          <div class="section-header">
            <h3>最近动态</h3>
            <a href="#">查看全部</a>
          </div>
          <div class="activity-list" v-if="activities.length > 0">
            <div 
              v-for="(activity, index) in activities" 
              :key="index"
              class="activity-item"
            >
              <div class="act-icon" :class="getActivityIconClass(activity.type)">
                <ArrowDown v-if="activity.type === 'inbound'" :size="16" />
                <ArrowUp v-else-if="activity.type === 'outbound'" :size="16" />
                <RotateCw v-else :size="16" />
              </div>
              <div class="act-details">
                <h4>{{ activity.title }}</h4>
                <p>{{ activity.description }}</p>
              </div>
              <span class="time">{{ formatTime(activity.time) }}</span>
            </div>
          </div>
          <div v-else class="empty-state-small">
            <p>暂无最近动态</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0 2rem 2rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-secondary);
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.hero-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.hero-card {
  position: relative;
  background: #111827;
  border-radius: 1rem;
  overflow: hidden;
  height: 240px;
  display: flex;
  align-items: center;
  padding: 2rem;
  color: white;
}

.hero-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 60%;
  height: 100%;
  object-fit: cover;
  opacity: 0.5;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, #111827 40%, transparent);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 60%;
}

.hero-content h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.hero-content p {
  color: #9ca3af;
  margin-bottom: 1.5rem;
}

.hero-actions {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.625rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-outline {
  border: 1px solid;
}

.text-primary { color: var(--primary-dark) !important; }
.bg-white-10 { background: rgba(255,255,255,0.1); }
.border-white { border-color: rgba(255,255,255,0.3) !important; color: white !important;}

.stats-card-main {
  background: #bef264;
  border-radius: 1rem;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin: 1.5rem 0;
}

.big-stat {
  display: flex;
  flex-direction: column;
}

.big-stat .number {
  font-size: 2.5rem;
  font-weight: 800;
  color: #365314;
  line-height: 1;
}

.big-stat .label {
  font-size: 0.875rem;
  color: #4d7c0f;
  font-weight: 500;
}

.link-arrow {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #365314;
  font-size: 0.875rem;
  cursor: pointer;
}

.decoration-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.2;
  color: #365314;
  transform: rotate(-15deg);
}

/* 管理员统计卡片 */
.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 0.75rem;
  padding: 1.25rem;
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  font-size: 1.125rem;
  font-weight: 700;
}

.section-header a {
  font-size: 0.875rem;
  color: var(--text-secondary);
  cursor: pointer;
}

.inventory-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.card {
  background: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
  text-align: center;
  border: 1px solid var(--border-color);
  transition: transform 0.2s;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.icon-box {
  background: var(--bg-color);
  width: 64px;
  height: 64px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: var(--text-secondary);
}

.card h4 {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.card p {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.capacity-section {
  margin-top: 2rem;
}

.capacity-cards {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cap-card {
  background: white;
  padding: 1.25rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
}

.cap-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.cap-info h4 { font-weight: 600; font-size: 0.875rem; }
.cap-info span { font-size: 0.875rem; font-weight: 700; }

.progress-bar {
  background: var(--bg-color);
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress {
  background: var(--primary-color);
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress.warning {
  background: #facc15;
}

.muted {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.right-col {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px solid var(--border-color);
  height: fit-content;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.activity-item {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.act-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.act-icon.in { background: #dcfce7; color: #16a34a; }
.act-icon.out { background: #fee2e2; color: #dc2626; }
.act-icon.audit { background: #fff7ed; color: #ea580c; }

.act-details h4 {
  font-size: 0.875rem;
  font-weight: 600;
}

.act-details p {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.time {
  margin-left: auto;
  font-size: 0.75rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.no-permission {
  background: white;
  border-radius: 1rem;
  padding: 3rem;
  text-align: center;
  border: 1px solid var(--border-color);
}

.no-permission .icon {
  color: var(--text-secondary);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.no-permission h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 0.5rem;
}

.no-permission p {
  color: var(--text-secondary);
}

.empty-state-small {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}



</style>
