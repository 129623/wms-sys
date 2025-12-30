import axios from 'axios';

// Create an axios instance
const service = axios.create({
    baseURL: '/api', // Use proxy
    timeout: 5000 // Request timeout
});

// Request interceptor
service.interceptors.request.use(
    config => {
        // Add token if needed (for now assume no specific token handling or add it here)
        // const token = localStorage.getItem('token');
        // if (token) {
        //   config.headers['Authorization'] = token;
        // }
        return config;
    },
    error => {
        console.log(error); // for debug
        return Promise.reject(error);
    }
);

import toast from './toast';

// Response interceptor
// Response interceptor
service.interceptors.response.use(
    response => {
        const res = response.data;

        if (res.code !== 200) {
            // Check for both msg and message, backend uses 'msg' in Result.java
            const errorMessage = res.msg || res.message || '操作失败';

            // Handle specific backend error messages usually associated with permissions
            if (errorMessage === 'Access Denied') {
                toast.error('权限不足，无法执行该操作');
            } else {
                toast.error(errorMessage);
            }
            return Promise.reject(new Error(errorMessage));
        } else {
            return res;
        }
    },
    error => {
        if (error.response) {
            if (error.response.status === 401) {
                localStorage.removeItem('isLoggedIn');
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login';
                }
            } else if (error.response.status === 403) {
                toast.error('权限不足，无法执行该操作');
            } else {
                const errMsg = error.response.data?.msg || error.response.data?.message || '服务器错误';
                toast.error(errMsg);
            }
        } else {
            toast.error('网络连接异常');
        }
        console.log('err' + error); // for debug
        return Promise.reject(error);
    }
);

export default service;
