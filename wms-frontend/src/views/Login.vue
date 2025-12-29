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
       // 模拟保存一个标志位，用于路由守卫（虽然 Session 是主要的，但前端还是标记一下已登录状态比较好）
       localStorage.setItem('isLoggedIn', 'true');
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
    <div class="login-card">
      <h2 class="title">WMS 仓储管理系统</h2>
      <p class="subtitle">请登录您的账户</p>
      
      <div class="form-group">
        <label>用户名</label>
        <input 
          v-model="loginForm.username" 
          type="text" 
          placeholder="请输入用户名" 
          @keyup.enter="handleLogin"
        />
      </div>
      
      <div class="form-group">
        <label>密码</label>
        <input 
          v-model="loginForm.password" 
          type="password" 
          placeholder="请输入密码" 
          @keyup.enter="handleLogin"
        />
      </div>
      
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      
      <button class="login-btn" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </button>
      
      <div class="footer">
        <p>默认账号: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  font-family: 'Inter', sans-serif;
}

.login-card {
  background: white;
  padding: 2.5rem;
  border-radius: 1rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.subtitle {
  text-align: center;
  color: #6b7280;
  margin-bottom: 2rem;
  font-size: 0.875rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.2s;
 box-sizing: border-box; 
}

.form-group input:focus {
  border-color: #84cc16; /* Primary color */
  box-shadow: 0 0 0 2px rgba(132, 204, 22, 0.2);
}

.error-msg {
  color: #ef4444;
  font-size: 0.875rem;
  text-align: center;
  margin-bottom: 1rem;
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #84cc16;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-btn:hover:not(:disabled) {
  background-color: #65a30d;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.footer {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.75rem;
  color: #9ca3af;
}
</style>
