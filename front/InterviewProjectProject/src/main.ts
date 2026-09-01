import './assets/main.css'
import './assets/styles/global.css'
import 'highlight.js/styles/github-dark.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
