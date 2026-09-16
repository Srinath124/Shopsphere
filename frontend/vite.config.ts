import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api/auth': {
        target: process.env.AUTH_SERVICE_URL || 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/products': {
        target: process.env.PRODUCT_SERVICE_URL || 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/categories': {
        target: process.env.PRODUCT_SERVICE_URL || 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/cart': {
        target: process.env.CART_SERVICE_URL || 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/orders': {
        target: process.env.ORDER_SERVICE_URL || 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/inventory': {
        target: process.env.INVENTORY_SERVICE_URL || 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/notifications': {
        target: process.env.NOTIFICATION_SERVICE_URL || 'http://localhost:8086',
        changeOrigin: true
      }
    }
  }
});
