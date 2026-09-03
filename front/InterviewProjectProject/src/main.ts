import './assets/main.css'
import './assets/styles/global.css'
import 'highlight.js/styles/github-dark.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/useUserStore'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

/**
 * 挂载前先根据本地令牌恢复登录态。
 *
 * 路由守卫依赖 isLogin 判断权限，而登录态需要通过令牌异步换取用户信息。
 * 若先挂载路由再恢复登录态，刷新页面时访问 /profile 等页面会被误判为
 * 未登录而弹到登录页。这里用 3 秒超时兜底，避免后端不可用时长时间白屏。
 */
async function bootstrap(): Promise<void> {
  const userStore = useUserStore(pinia)

  await Promise.race([
    userStore.refresh().catch(() => undefined),
    new Promise((resolve) => setTimeout(resolve, 3000))
  ])

  app.use(router)
  app.mount('#app')
}

void bootstrap()
