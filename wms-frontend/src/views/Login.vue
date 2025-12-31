<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';

const router = useRouter();
const loginForm = reactive({
  username: '',
  password: ''
});
const loading = ref(false);
const errorMsg = ref('');

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    errorMsg.value = '请输入用户名和密码';
    return;
  }
  
  loading.value = true;
  errorMsg.value = '';
  
  try {
    // 登录接口：POST /login，参数 { username, password }
    const res = await request.post('/login', loginForm);
    // 这里要注意：LoginController 如果成功，返回 "登录成功: session_id"，
    // 由于后端用的是 HttpSession，浏览器会自动处理 Cookie (JSESSIONID)。
    // 如果是 JWT，通常会返回 token。如果是 Session，只要此时请求成功，Cookie Set-Cookie 会被浏览器保存。
    // 我们只需要跳转。
    
    if (res.code === 200) {
       // Save user info to LocalStorage
       localStorage.setItem('isLoggedIn', 'true');
       
       const data = res.data;
       if (data) {
           localStorage.setItem('username', data.username || data.account || '用户');
           localStorage.setItem('role', data.role || '普通用户');
           localStorage.setItem('account', data.account || '');
           if (data.roles) {
               localStorage.setItem('roles', JSON.stringify(data.roles));
           }
           if (data.permissions) {
               localStorage.setItem('permissions', JSON.stringify(data.permissions));
           }
       }
       
       router.push('/');
    } else {
       errorMsg.value = res.message || '登录失败';
    }
  } catch (error) {
    console.error(error);
    errorMsg.value = '登录请求异常，请检查网络或后端状态';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <!-- Dynamic Background Image -->
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    
    <div class="login-card">
      <div class="card-header">
        <div class="logo-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-box"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
        </div>
        <h2 class="title">WMS 智能仓储管理</h2>
        <p class="subtitle">让管理更简单 · 让运营更高效</p>
      </div>
      
      <div class="form-container">
        <div class="form-group">
          <label>用户账号</label>
          <div class="input-wrapper">
             <input 
              v-model="loginForm.username" 
              type="text" 
              placeholder="请输入用户名" 
              @keyup.enter="handleLogin"
            />
          </div>
        </div>
        
        <div class="form-group">
          <label>登录密码</label>
          <div class="input-wrapper">
            <input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              @keyup.enter="handleLogin"
            />
          </div>
        </div>
        
        <div v-if="errorMsg" class="error-msg">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          {{ errorMsg }}
        </div>
        
        <button class="login-btn" :disabled="loading" @click="handleLogin">
          <span>{{ loading ? '正在验证...' : '登 录 系 统' }}</span>
          <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
        </button>
      </div>
      
      <div class="footer">
        <p>快速体验: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  color: #fff;
  background-color: #1e293b;
}

/* Background Image with Animation */
.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('/assets/login-bg.png');
  background-size: cover;
  background-position: center;
  z-index: 1;
  animation: bgZoom 30s linear infinite alternate;
}

@keyframes bgZoom {
  0% { transform: scale(1); }
  100% { transform: scale(1.05); } /* Reduced scale for sharpness */
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* Lightened the overlay significantly */
  background: radial-gradient(circle at center, rgba(30, 41, 59, 0.1) 0%, rgba(15, 23, 42, 0.45) 100%);
  z-index: 2;
}

.login-card {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.08); /* More transparent */
  backdrop-filter: blur(12px); /* Reduced blur for clarity */
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  padding: 3.5rem;
  border-radius: 2rem;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 40px 80px -20px rgba(0, 0, 0, 0.5);
  animation: cardFadeIn 1s ease-out;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.card-header {
  text-align: center;
  margin-bottom: 3rem;
}

.logo-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #38bdf8, #2563eb);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
}

.title {
  font-size: 1.85rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  color: #ffffff;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group {
  margin-bottom: 1.75rem;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.6rem;
  margin-left: 0.2rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group input {
  width: 100%;
  padding: 1rem 1.25rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 0.85rem;
  color: #fff;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-group input:focus {
  background: rgba(255, 255, 255, 0.2);
  border-color: #38bdf8;
  /* Cleaner focus without the heavy green shadow */
  box-shadow: 0 0 0 1px rgba(56, 189, 248, 0.2);
}

.error-msg {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fee2e2;
  padding: 0.8rem;
  border-radius: 0.75rem;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}

.login-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #38bdf8 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 0.85rem;
  font-weight: 700;
  font-size: 1.05rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.3);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(37, 99, 235, 0.4);
  background: linear-gradient(135deg, #7dd3fc 0%, #38bdf8 100%);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  filter: grayscale(0.5);
  cursor: not-allowed;
}

.footer {
  margin-top: 2.5rem;
  text-align: center;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 1.5rem;
}
</style>
