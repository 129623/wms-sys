<script setup>
import { ref } from 'vue';
import { 
  LayoutDashboard, 
  ArrowDownToLine, 
  ArrowUpFromLine, 
  Search, 
  Database, 
  Settings, 
  Box, 
  Users,
  ChevronDown,
  ChevronRight,
  Warehouse
} from 'lucide-vue-next';
import { useRoute } from 'vue-router';

const route = useRoute();

const menuItems = [
  { name: '仪表盘', path: '/', icon: LayoutDashboard },
  { name: '入库管理', path: '/inbound', icon: ArrowDownToLine },
  { name: '出库管理', path: '/outbound', icon: ArrowUpFromLine },
  { name: '库存查询', path: '/inventory', icon: Search },
];

const basicDataItems = [
  { name: '货物资料', path: '/cargo', icon: Box },
  { name: '分类管理', path: '/category', icon: Database },
  { name: '单位资料', path: '/unit', icon: Database },
  { name: '存储类型', path: '/storage-type', icon: Database },
  { name: '货物标签', path: '/product-label', icon: Database },
  { name: '客户/供应商', path: '/customer', icon: Users },
];

// 仓储管理下拉项
const warehouseItems = [
  { name: '仓库管理', path: '/warehouse', icon: Database },
  { name: '库区管理', path: '/zone', icon: Database },
  { name: '货架管理', path: '/rack', icon: Database },
  { name: '库位管理', path: '/location', icon: Database },
];

const systemItems = [
  { name: '用户管理', path: '/system/user', icon: Users },
  { name: '角色管理', path: '/system/role', icon: Settings },
];

// 下拉菜单展开状态
const warehouseExpanded = ref(true);

const toggleWarehouse = () => {
  warehouseExpanded.value = !warehouseExpanded.value;
};

const isActive = (path) => route.path === path;

// 检查仓储管理组是否有激活项
const isWarehouseGroupActive = () => {
  return warehouseItems.some(item => isActive(item.path));
};
</script>

<template>
  <aside class="sidebar">
    <div class="logo-area">
      <div class="logo-icon">
        <Box class="text-white" />
      </div>
      <h1 class="logo-text">WMS 仓储系统</h1>
    </div>

    <nav class="nav-menu">
      <div class="nav-section">
        <div class="nav-label">业务操作</div>
        <router-link 
          v-for="item in menuItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <component :is="item.icon" :size="20" />
          <span>{{ item.name }}</span>
        </router-link>
      </div>

      <div class="nav-section">
        <div class="nav-label">基础资料</div>
        <router-link 
          v-for="item in basicDataItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <component :is="item.icon" :size="20" />
          <span>{{ item.name }}</span>
        </router-link>

        <!-- 仓储管理下拉菜单 -->
        <div class="nav-submenu">
          <div 
            class="nav-item nav-parent" 
            :class="{ active: isWarehouseGroupActive() }"
            @click="toggleWarehouse"
          >
            <Warehouse :size="20" />
            <span>库区架位</span>
            <ChevronDown 
              :size="16" 
              class="chevron" 
              :class="{ rotated: !warehouseExpanded }"
            />
          </div>
          
          <transition name="slide">
            <div v-show="warehouseExpanded" class="submenu-items">
              <router-link 
                v-for="item in warehouseItems" 
                :key="item.path" 
                :to="item.path"
                class="nav-item nav-subitem"
                :class="{ active: isActive(item.path) }"
              >
                <component :is="item.icon" :size="18" />
                <span>{{ item.name }}</span>
              </router-link>
            </div>
          </transition>
        </div>
      </div>

       <div class="nav-section">
        <div class="nav-label">系统管理</div>
        <router-link 
          v-for="item in systemItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <component :is="item.icon" :size="20" />
          <span>{{ item.name }}</span>
        </router-link>
      </div>
    </nav>

  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background: white;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.logo-area {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-bottom: 1px solid transparent;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--primary-dark);
}

.nav-menu {
  flex: 1;
  padding: 1.5rem 1rem;
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 1.5rem;
}

.nav-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
  padding-left: 0.75rem;
  font-weight: 600;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  color: var(--text-secondary);
  border-radius: 0.5rem;
  transition: all 0.2s;
  font-weight: 500;
  margin-bottom: 0.25rem;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.nav-item.active {
  background: var(--primary-light);
  color: var(--primary-dark);
}

/* 下拉菜单样式 */
.nav-submenu {
  margin-top: 0.25rem;
}

.nav-parent {
  position: relative;
  justify-content: space-between;
  font-weight: 600;
}

.chevron {
  margin-left: auto;
  transition: transform 0.3s ease;
}

.chevron.rotated {
  transform: rotate(-90deg);
}

.submenu-items {
  margin-left: 0.5rem;
  padding-left: 1rem;
  border-left: 2px solid var(--border-color);
  margin-top: 0.25rem;
}

.nav-subitem {
  padding: 0.6rem 0.75rem;
  font-size: 0.9rem;
  margin-bottom: 0.2rem;
}

/* 下拉动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  max-height: 300px;
  transform: translateY(0);
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid var(--border-color);
}

.status-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: var(--primary-dark);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.status-btn:hover {
  background: #4d7c0f;
}
</style>
