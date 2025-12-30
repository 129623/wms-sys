import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/Dashboard.vue';
import InboundManagement from '../views/InboundManagement.vue';
import OutboundManagement from '../views/OutboundManagement.vue';
import InventoryQuery from '../views/InventoryQuery.vue';
import CargoInfo from '../views/CargoInfo.vue';
import CategoryManagement from '../views/CategoryManagement.vue';
import WarehouseManagement from '../views/WarehouseManagement.vue';
import SystemManagement from '../views/SystemManagement.vue';
import Login from '../views/Login.vue';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { layout: 'empty' } // Optional marker for layout handling
  },
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/inbound',
    name: 'InboundManagement',
    component: InboundManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/inbound/add',
    name: 'InboundAdd',
    component: () => import('../views/InboundForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inbound/detail/:id',
    name: 'InboundDetail',
    component: () => import('../views/InboundDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/outbound',
    name: 'OutboundManagement',
    component: OutboundManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/outbound/add',
    name: 'OutboundAdd',
    component: () => import('../views/OutboundForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/outbound/detail/:id',
    name: 'OutboundDetail',
    component: () => import('../views/OutboundDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/customer',
    name: 'CustomerManagement',
    component: () => import('../views/CustomerManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/zone',
    name: 'ZoneManagement',
    component: () => import('../views/ZoneManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/rack',
    name: 'RackManagement',
    component: () => import('../views/RackManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/location',
    name: 'LocationManagement',
    component: () => import('../views/LocationManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inventory',
    name: 'InventoryQuery',
    component: InventoryQuery,
    meta: { requiresAuth: true }
  },
  {
    path: '/cargo',
    name: 'CargoInfo',
    component: CargoInfo,
    meta: { requiresAuth: true }
  },
  {
    path: '/category',
    name: 'CategoryManagement',
    component: CategoryManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/warehouse',
    name: 'WarehouseManagement',
    component: WarehouseManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/unit',
    name: 'UnitManagement',
    component: () => import('../views/UnitManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/user',
    name: 'UserManagement',
    component: () => import('../views/system/UserManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/role',
    name: 'RoleManagement',
    component: () => import('../views/system/RoleManagement.vue'),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn');
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      next({ name: 'Login' });
    } else {
      next();
    }
  } else {
    // If going to Login while logged in, redirect to Dashboard
    if (to.name === 'Login' && isLoggedIn) {
      next({ name: 'Dashboard' });
    } else {
      next();
    }
  }
});

export default router;
