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
        rewrite: (path) => path.replace(/^\/api/, '/wms-system/api'),
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
      }
    }
  }
})
