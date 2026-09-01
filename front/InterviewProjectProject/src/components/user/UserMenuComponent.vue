<template>
  <div ref="menuRef" class="user-menu">
    <button class="avatar-btn" type="button" @click="toggle">
      <span class="avatar-circle">{{ userStore.avatar }}</span>
      <span class="avatar-name">{{ userStore.nickname }}</span>
      <span class="caret" :class="{ open: opened }">▾</span>
    </button>

    <transition name="drop">
      <div v-if="opened" class="menu-dropdown">
        <template v-if="userStore.isLogin">
          <div class="menu-header">
            <span class="menu-avatar">{{ userStore.avatar }}</span>
            <div class="menu-info">
              <p class="menu-nickname">{{ userStore.nickname }}</p>
              <p class="menu-username">@{{ userStore.profile?.username }}</p>
            </div>
          </div>
          <ul class="menu-list">
            <li>
              <router-link to="/profile" @click="close">
                <span>👤</span> 个人中心
              </router-link>
            </li>
            <li>
              <router-link to="/notes/create" @click="close">
                <span>✍️</span> 写笔记
              </router-link>
            </li>
          </ul>
          <button class="menu-logout" type="button" @click="handleLogout">退出登录</button>
        </template>

        <template v-else>
          <p class="menu-tip">登录后可同步你的简历与笔记</p>
          <router-link class="menu-login-btn" to="/login" @click="close">立即登录</router-link>
          <router-link class="menu-register-link" to="/login?mode=register" @click="close">
            还没有账号？去注册
          </router-link>
        </template>
      </div>
    </transition>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()
const router = useRouter()
const opened = ref(false)
const menuRef = ref<HTMLElement | null>(null)

function toggle(): void {
  opened.value = !opened.value
}

function close(): void {
  opened.value = false
}

function handleLogout(): void {
  userStore.logout()
  close()
  router.push('/')
}

/** 点击空白处关闭下拉 */
function onDocumentClick(event: MouseEvent): void {
  if (!menuRef.value) return
  if (!menuRef.value.contains(event.target as Node)) close()
}

onMounted(() => document.addEventListener('click', onDocumentClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocumentClick))
</script>

<style scoped>
@import '@/assets/styles/components/usermenu.css';
</style>
