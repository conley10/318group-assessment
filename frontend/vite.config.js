import { defineConfig } from 'vite';

export default defineConfig({
  server: { strictPort: true, watch: { usePolling: true }, proxy: {
    '/catalogue-api': { target: 'http://localhost:8081', rewrite: path => path.replace('/catalogue-api', '/api') },
    '/assistant-api': { target: 'http://localhost:8085', rewrite: path => path.replace('/assistant-api', '/api') },
  } },
});
