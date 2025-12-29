import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/wms-system'),
        configure: (proxy, options) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const cookies = proxyRes.headers['set-cookie'];
            if (cookies) {
              const newCookies = cookies.map(cookie => {
                return cookie.replace(/Path=\/wms-system/, 'Path=/');
              });
              proxyRes.headers['set-cookie'] = newCookies;
            }
          });
        }
      },
      // Proxy everything else that looks like an API call if needed, but explicit /api is better.
      // However, my previous request.js calls endpoints directly like /inbound/page, so I should probably proxy those.
      // Let's just proxy based on the root context if possible, or I need to update request.js to prepend /api.
      // Updating request.js is cleaner.
    }
  }
})
