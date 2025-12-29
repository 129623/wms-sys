<script setup>
import { Search, Bell, Mail, User } from 'lucide-vue-next';
</script>

<template>
  <header class="top-header">
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎回来, 管理员</h2>
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

      <div class="user-profile">
        <div class="avatar">AD</div>
      </div>
    </div>
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
  cursor: pointer;
}
</style>
