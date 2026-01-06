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
    <div class="login-wrapper">
      <div class="login-card">
        <div class="card-header">
          <div class="logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-box"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
          </div>
          <h2 class="title">WMS 智能仓储</h2>
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
  color: var(--text-main);
  background-color: #f8fafc;
  background-image: 
    radial-gradient(at 0% 0%, rgba(132, 204, 22, 0.15) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(101, 163, 13, 0.1) 0px, transparent 50%);
}

.login-wrapper {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 1rem;
}

.login-card {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--white);
  padding: 3rem 2.5rem;
  border-radius: 1.5rem;
  width: 100%;
  box-shadow: 
    0 20px 25px -5px rgba(0, 0, 0, 0.05),
    0 8px 10px -6px rgba(0, 0, 0, 0.01);
  animation: cardFadeIn 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: var(--primary-light);
  color: var(--primary-dark);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.25rem;
  box-shadow: 0 4px 6px -1px rgba(132, 204, 22, 0.2);
}

.title {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  color: var(--text-main);
  letter-spacing: -0.025em;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 0.5rem;
}

.form-group input {
  width: 100%;
  padding: 0.875rem 1rem;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 0.75rem;
  color: var(--text-main);
  font-size: 0.95rem;
  outline: none;
  transition: all 0.2s ease;
}

.form-group input::placeholder {
  color: #9ca3af;
}

.form-group input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(132, 204, 22, 0.15);
}

.error-msg {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}

.login-btn {
  width: 100%;
  padding: 0.875rem;
  background: var(--primary-color);
  color: #fff;
  border: none;
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(132, 204, 22, 0.3);
}

.login-btn:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: 0 6px 8px -1px rgba(132, 204, 22, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}


</style>
