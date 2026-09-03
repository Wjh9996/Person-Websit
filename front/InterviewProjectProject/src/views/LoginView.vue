<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌区 -->
      <aside class="auth-side">
        <div class="side-logo">
          <span class="side-logo-icon">王</span>
          <span>王建豪的个人站</span>
        </div>

        <div>
          <h2 class="side-title">记录学习<br />沉淀经验</h2>
          <p class="side-desc">
            一个用于展示简历、整理学习笔记的个人站点。登录后可以管理简历内容、撰写并发布笔记。
          </p>
          <ul class="side-features">
            <li><span>📄</span> 多份简历在线编辑与切换</li>
            <li><span>📝</span> Markdown 笔记编写与归档</li>
            <li><span>📊</span> 学习成果可视化展示</li>
          </ul>
        </div>

        <p class="side-desc" style="margin: 0; font-size: 13px">
          内容由后端服务提供，未登录时可浏览，登录后即可编辑简历与笔记。
        </p>
      </aside>

      <!-- 右侧表单 -->
      <div class="auth-form-wrap">
        <div class="auth-tabs">
          <button
            type="button"
            class="auth-tab"
            :class="{ active: mode === 'login' }"
            @click="switchMode('login')"
          >
            登录
          </button>
          <button
            type="button"
            class="auth-tab"
            :class="{ active: mode === 'register' }"
            @click="switchMode('register')"
          >
            注册
          </button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-field">
            <label for="username">账号</label>
            <div class="input-wrap">
              <span class="input-icon">👤</span>
              <input
                id="username"
                v-model="form.username"
                type="text"
                placeholder="请输入账号"
                autocomplete="username"
              />
            </div>
          </div>

          <div v-if="mode === 'register'" class="form-field">
            <label for="nickname">昵称</label>
            <div class="input-wrap">
              <span class="input-icon">🏷️</span>
              <input id="nickname" v-model="form.nickname" type="text" placeholder="展示用的名字" />
            </div>
          </div>

          <div v-if="mode === 'register'" class="form-field">
            <label for="email">邮箱</label>
            <div class="input-wrap">
              <span class="input-icon">✉️</span>
              <input id="email" v-model="form.email" type="email" placeholder="you@example.com" />
            </div>
          </div>

          <div class="form-field">
            <label for="password">密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input
                id="password"
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
            </div>
          </div>

          <div v-if="mode === 'register'" class="form-field">
            <label for="confirm">确认密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input
                id="confirm"
                v-model="form.confirmPassword"
                type="password"
                placeholder="再次输入密码"
              />
            </div>
          </div>

          <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>

          <button class="submit-btn" type="submit" :disabled="userStore.loading">
            {{ userStore.loading ? '处理中…' : mode === 'login' ? '登录' : '注册并登录' }}
          </button>
        </form>

        <p v-if="mode === 'login'" class="demo-tip">
          演示账号：<code>admin</code> / <code>123456</code>
          <button class="fill-demo" type="button" @click="fillDemo">一键填充</button>
        </p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref<'login' | 'register'>('login')
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

/** 支持 /login?mode=register 直接进入注册态 */
onMounted(() => {
  if (route.query.mode === 'register') mode.value = 'register'
  if (userStore.isLogin) void router.replace('/profile')
})

watch(mode, () => {
  errorMsg.value = ''
})

function switchMode(next: 'login' | 'register'): void {
  mode.value = next
}

function fillDemo(): void {
  form.username = 'admin'
  form.password = '123456'
}

function validate(): boolean {
  if (!form.username.trim()) return fail('请输入账号')
  if (!form.password) return fail('请输入密码')
  if (mode.value === 'register') {
    if (form.password.length < 6) return fail('密码至少 6 位')
    if (form.password !== form.confirmPassword) return fail('两次输入的密码不一致')
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return fail('邮箱格式不正确')
  }
  return true
}

function fail(message: string): boolean {
  errorMsg.value = message
  return false
}

async function handleSubmit(): Promise<void> {
  if (!validate()) return

  const redirect = (route.query.redirect as string) || '/profile'

  if (mode.value === 'login') {
    const ok = await userStore.login({ username: form.username.trim(), password: form.password })
    if (!ok) {
      errorMsg.value = userStore.error || '登录失败'
      return
    }
  } else {
    const ok = await userStore.register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || form.username.trim(),
      email: form.email.trim()
    })
    if (!ok) {
      errorMsg.value = userStore.error || '注册失败'
      return
    }
  }

  void router.replace(redirect)
}
</script>

<style scoped>
@import '@/assets/styles/views/auth.css';
</style>
