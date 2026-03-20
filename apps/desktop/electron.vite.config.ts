import { resolve } from 'node:path'
import react from '@vitejs/plugin-react'
import { defineConfig, externalizeDepsPlugin } from 'electron-vite'

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
