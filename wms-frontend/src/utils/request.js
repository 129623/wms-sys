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

// Response interceptor
service.interceptors.response.use(
    response => {
        const res = response.data;

        // Adjust this check based on your actual backend response structure
        // If the expected behavior is that the backend returns { code: 200, data: ... }
        if (res.code !== 200) {
            console.error('Error:', res.message || 'Unknown Error');
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            localStorage.removeItem('isLoggedIn');
            // Ideally redirect to login, simple window location change or use router if available
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        console.log('err' + error); // for debug
        return Promise.reject(error);
    }
);

export default service;
