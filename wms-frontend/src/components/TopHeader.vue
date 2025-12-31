<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Search, Bell, User, LogOut, ChevronDown, AlertTriangle } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { showToast } from '../utils/toast';

const router = useRouter();
const showUserMenu = ref(false);
const showNotifications = ref(false);
const searchQuery = ref('');
const notifications = ref([]);
const unreadCount = ref(0);

// 获取用户信息（从localStorage）
const username = localStorage.getItem('username') || '用户';
const role = localStorage.getItem('role') || '用户';

// 获取通知
const fetchNotifications = async () => {
  try {
    const res = await request.get('/dashboard/notifications');
    notifications.value = res.data || [];
    unreadCount.value = notifications.value.length;
  } catch (error) {
    console.error('获取通知失败', error);
  }
};

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
  if (showNotifications.value) {
    fetchNotifications();
    showUserMenu.value = false;
  }
};

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
  if (showUserMenu.value) {
    showNotifications.value = false;
  }
};

const handleSearch = () => {
  const query = searchQuery.value.trim();
  if (!query) {
    showToast('请输入搜索内容', 'warning');
    return;
  }
  
  // 导航到搜索结果页面，带上查询参数
  router.push({
    path: '/search',
    query: { q: query }
  });
  
  // 清空搜索框
  searchQuery.value = '';
};

const handleLogout = () => {
  if (confirm('确认退出登录？')) {
    // 清除本地存储的所有认证信息
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    localStorage.removeItem('account');
    localStorage.removeItem('roles');
    localStorage.removeItem('isLoggedIn');  // 重要：清除登录状态
    
    // 强制刷新并跳转到登录页
    window.location.href = '/login';
  }
};

// 点击外部关闭菜单
const closeMenu = () => {
  showUserMenu.value = false;
  showNotifications.value = false;
};

// 定时刷新通知 (每分钟)
let timer = null;
onMounted(() => {
  fetchNotifications();
  timer = setInterval(fetchNotifications, 60000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <header class="top-header">
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎回来, {{ username }}</h2>
      <p class="welcome-subtitle">WMS 智能仓储管理系统</p>
    </div>

    <div class="header-actions">
      <div class="search-wrapper">
        <Search :size="18" class="search-icon" />
        <input 
          type="text" 
          v-model="searchQuery"
          placeholder="搜索订单、货物..." 
          class="search-input"
          @keyup.enter="handleSearch"
        />
      </div>

      <div class="icon-btn notification-btn" @click.stop="toggleNotifications">
        <Bell :size="20" />
        <span class="badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
        
        <!-- 通知下拉框 -->
        <div v-if="showNotifications" class="notification-dropdown" @click.stop>
          <div class="dropdown-header">
            <h3>系统通知</h3>
            <span class="text-xs text-secondary">{{ unreadCount }} 条未读</span>
          </div>
          <div class="notification-list">
             <div v-if="notifications.length === 0" class="empty-notif">
                <Bell :size="24" class="text-gray-300 mb-2" />
                <p>暂无新通知</p>
             </div>
             <div v-else class="notif-item" v-for="item in notifications" :key="item.id">
                <div class="notif-icon warning">
                   <AlertTriangle :size="16" />
                </div>
                <div class="notif-content">
                   <h4 class="notif-title">{{ item.title }}</h4>
                   <p class="notif-desc">{{ item.content }}</p>
                   <span class="notif-time">{{ new Date(item.time).toLocaleString() }}</span>
                </div>
             </div>
          </div>
        </div>
      </div>

      <div class="user-profile" @click="toggleUserMenu">
        <div class="avatar">
          {{ username ? username.substring(0, 1).toUpperCase() : 'U' }}
        </div>
        <ChevronDown :size="16" class="chevron" :class="{ rotated: showUserMenu }" />
        
        <!-- 用户下拉菜单 -->
        <div v-if="showUserMenu" class="user-dropdown" @click.stop>
          <div class="dropdown-header">
            <div class="user-avatar-large">
              {{ username ? username.substring(0, 1).toUpperCase() : 'U' }}
            </div>
            <div class="user-info">
              <div class="user-name">{{ username }}</div>
              <div class="user-role">{{ role }}</div>
            </div>
          </div>
          
          <div class="dropdown-divider"></div>
          
          <div class="dropdown-items">
            <div class="dropdown-item logout" @click="handleLogout">
              <LogOut :size="18" />
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 遮罩层，点击关闭菜单 -->
    <div v-if="showUserMenu || showNotifications" class="overlay" @click="closeMenu"></div>
  </header>
</template>

<style scoped>
.top-header {
  height: 80px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  margin-bottom: 1rem;
  position: relative;
}

.welcome-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
}

.welcome-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-wrapper {
  position: relative;
  margin-right: 1rem;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
}

.search-input {
  padding: 0.625rem 1rem 0.625rem 2.5rem;
  border-radius: 2rem;
  border: 1px solid var(--border-color);
  width: 300px;
  font-size: 0.875rem;
  outline: none;
  background: white;
  transition: all 0.2s;
}

.search-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
}

.icon-btn:hover {
  background: var(--bg-color);
  color: var(--primary-color);
}

.user-profile {
  margin-left: 0.5rem;
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.25rem 0.75rem 0.25rem 0.25rem;
  border-radius: 2rem;
  transition: all 0.2s;
}

.user-profile:hover {
  background: var(--bg-color);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.875rem;
}

.chevron {
  color: var(--text-secondary);
  transition: transform 0.3s;
}

.chevron.rotated {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.user-dropdown {
  position: absolute;
  top: 60px;
  right: 0;
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04);
  min-width: 240px;
  z-index: 100;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--bg-color);
}

.user-avatar-large {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.125rem;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 700;
  color: var(--text-main);
  font-size: 0.9375rem;
}

.user-role {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-top: 0.125rem;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
}

.dropdown-items {
  padding: 0.5rem;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-radius: 0.5rem;
  transition: all 0.2s;
  color: var(--text-main);
  font-size: 0.875rem;
}

.dropdown-item:hover {
  background: var(--bg-color);
  color: var(--primary-color);
}

.dropdown-item.logout {
  color: #ef4444;
}

.dropdown-item.logout:hover {
  background: #fee2e2;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 90;
  background: transparent;
}

/* 通知下拉框样式 */
.notification-btn {
  position: relative;
}

.badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid white;
  font-weight: 700;
}

.notification-dropdown {
  position: absolute;
  top: 50px;
  right: -80px;
  width: 320px;
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04);
  z-index: 100;
  border: 1px solid var(--border-color);
  cursor: default;
}

.dropdown-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-main);
  margin: 0;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-notif {
  padding: 2rem;
  text-align: center;
  color: var(--text-secondary);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.notif-item {
  padding: 1rem;
  display: flex;
  gap: 0.75rem;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.2s;
}

.notif-item:hover {
  background: var(--bg-color);
}

.notif-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-icon.warning {
  background: #fef3c7;
  color: #f59e0b;
}

.notif-content {
  flex: 1;
}

.notif-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 0.25rem;
}

.notif-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.4;
  margin-bottom: 0.25rem;
}

.notif-time {
  font-size: 0.75rem;
  color: #9ca3af;
}
</style>
