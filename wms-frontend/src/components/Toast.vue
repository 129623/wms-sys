<script setup>
import { ref, onMounted } from 'vue';
import { X, AlertCircle, CheckCircle, Info, AlertTriangle } from 'lucide-vue-next';

const props = defineProps({
  message: { type: String, required: true },
  type: { type: String, default: 'info' }, // success, error, warning, info
  duration: { type: Number, default: 3000 }
});

const emit = defineEmits(['close']);
const visible = ref(false);

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info
};

onMounted(() => {
  // Small delay to allow enter animation
  setTimeout(() => visible.value = true, 10);
  
  if (props.duration > 0) {
    setTimeout(() => {
      close();
    }, props.duration);
  }
});

const close = () => {
  visible.value = false;
  setTimeout(() => emit('close'), 300); // Wait for leave animation
};
</script>

<template>
  <div class="toast-wrapper" :class="[type, { show: visible }]">
    <component :is="icons[type]" class="icon" :size="20" />
    <span class="message">{{ message }}</span>
    <button class="close-btn" @click="close">
      <X :size="16" />
    </button>
  </div>
</template>

<style scoped>
.toast-wrapper {
  /* position: fixed; removed for stacking */
  /* top: 20px; right: 20px; removed */
  min-width: 300px;
  padding: 1rem 1.25rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  /* z-index: 9999; handled by container */
  transform: translateX(120%);
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  border-left: 5px solid transparent;
  backdrop-filter: blur(10px);
  pointer-events: auto;
}

.toast-wrapper.show {
  transform: translateX(0);
  opacity: 1;
}

/* Types */
.success { border-left-color: #10b981; }
.success .icon { color: #10b981; }

.error { border-left-color: #ef4444; }
.error .icon { color: #ef4444; }

.warning { border-left-color: #f59e0b; }
.warning .icon { color: #f59e0b; }

.info { border-left-color: #3b82f6; }
.info .icon { color: #3b82f6; }

.message {
  flex: 1;
  font-size: 0.9em;
  font-weight: 500;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}
</style>
