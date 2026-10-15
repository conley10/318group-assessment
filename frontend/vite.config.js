import { defineConfig } from 'vite';

export default defineConfig({
  server: { 
    strictPort: true, 
    watch: { usePolling: true }, 
    proxy: {
      '/catalogue-api': { 
        target: 'http://localhost:8081', 
        rewrite: path => path.replace('/catalogue-api', '/api') 
      },

      '/assistant-api': { 
        target: 'http://localhost:8085', 
        rewrite: path => path.replace('/assistant-api', '/api') 
      },

      '/booking-api': { 
        target: 'http://localhost:8082', 
        rewrite: path => path.replace('/booking-api', '/api') 
      },
      '/notification-api': {
  target: 'http://localhost:8084',
  rewrite: path => path.replace('/notification-api', '/api')
},

'/analytics-api': {
  target: 'http://localhost:8086',
  rewrite: path => path.replace('/analytics-api', '/api')
},
    } 
  },
});