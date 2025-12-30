<script setup>
import { ref } from 'vue';
import { Search, Bell, Mail, User, LogOut, Settings, ChevronDown } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

const router = useRouter();
const showUserMenu = ref(false);

// 获取用户名（从localStorage）
const username = localStorage.getItem('username') || '管理员';

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
};

const handleLogout = () => {
  if (confirm('确认退出登录？')) {
    // 清除本地存储的所有认证信息
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    localStorage.removeItem('isLoggedIn');  // 重要：清除登录状态
    
    // 强制刷新并跳转到登录页
    window.location.href = '/login';
  }
};

// 点击外部关闭菜单
const closeMenu = () => {
  showUserMenu.value = false;
};
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
        <input type="text" placeholder="搜索订单、货物..." class="search-input" />
      </div>

      <div class="icon-btn">
        <Bell :size="20" />
      </div>
      <div class="icon-btn">
        <Mail :size="20" />
      </div>

      <div class="user-profile" @click="toggleUserMenu">
        <div class="avatar">
          {{ username.substring(0, 2).toUpperCase() }}
        </div>
        <ChevronDown :size="16" class="chevron" :class="{ rotated: showUserMenu }" />
        
        <!-- 用户下拉菜单 -->
        <div v-if="showUserMenu" class="user-dropdown" @click.stop>
          <div class="dropdown-header">
            <div class="user-avatar-large">
              {{ username.substring(0, 2).toUpperCase() }}
            </div>
            <div class="user-info">
              <div class="user-name">{{ username }}</div>
              <div class="user-role">管理员</div>
            </div>
          </div>
          
          <div class="dropdown-divider"></div>
          
          <div class="dropdown-items">
            <div class="dropdown-item" @click="router.push('/system')">
              <Settings :size="18" />
              <span>系统设置</span>
            </div>
            <div class="dropdown-item logout" @click="handleLogout">
              <LogOut :size="18" />
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 遮罩层，点击关闭菜单 -->
    <div v-if="showUserMenu" class="overlay" @click="closeMenu"></div>
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
  z-index: 99;
  background: transparent;
}
</style>
