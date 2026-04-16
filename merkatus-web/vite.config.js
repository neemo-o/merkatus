import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Configuração para SPA - redireciona todas as rotas para index.html
    historyApiFallback: true,
  },
})
