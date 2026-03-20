import { resolve } from 'path'
import { defineConfig, externalizeDepsPlugin } from 'electron-vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  main: {
    plugins: [externalizeDepsPlugin({ exclude: ['@uniso/shared'] })],
    resolve: {
      alias: {
        '@uniso/shared': resolve(__dirname, '../../packages/shared/src/index.ts'),
      },
    },
  },
  preload: {
    plugins: [externalizeDepsPlugin()],
  },
  renderer: {
    plugins: [react()],
    resolve: {
      alias: {
        '@uniso/shared': resolve(__dirname, '../../packages/shared/src/index.ts'),
      },
    },
  },
})
