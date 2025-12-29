<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import TopHeader from './components/TopHeader.vue';

const route = useRoute();
const isAuthLayout = computed(() => route.meta.layout === 'empty');
</script>

<template>
  <div class="app-layout">
    <Sidebar v-if="!isAuthLayout" />
    <main class="main-content" :class="{ 'full-width': isAuthLayout }">
      <TopHeader v-if="!isAuthLayout" />
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-content {
  margin-left: var(--sidebar-width);
  flex: 1;
  background-color: var(--bg-color);
  min-height: 100vh;
  transition: margin-left 0.3s;
}

.main-content.full-width {
  margin-left: 0;
}
</style>
