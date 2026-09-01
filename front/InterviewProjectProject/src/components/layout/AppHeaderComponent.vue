<template>
  <header class="site-header">
    <div class="header-inner">
      <!-- 品牌 -->
      <router-link to="/" class="brand" @click="mobileOpen = false">
        <span class="brand-logo">王</span>
        <span class="brand-text">
          <span class="brand-name">王建豪的个人站</span>
          <span class="brand-slogan">学习 · 记录 · 成长</span>
        </span>
      </router-link>

      <!-- 主导航 -->
      <nav class="main-nav" :class="{ open: mobileOpen }">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-link"
          :class="{ active: isActive(item.path) }"
          @click="mobileOpen = false"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          {{ item.label }}
        </router-link>
      </nav>

      <!-- 右侧操作 -->
      <div class="header-actions">
        <router-link to="/notes/create" class="header-write-btn">
          <span>✍️</span> 写笔记
        </router-link>
        <UserMenuComponent />
        <button
          class="menu-toggle"
          type="button"
          aria-label="切换导航菜单"
          @click="mobileOpen = !mobileOpen"
        >
          ☰
        </button>
      </div>
    </div>
  </header>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import UserMenuComponent from '@/components/user/UserMenuComponent.vue'

interface NavItem {
  label: string
  path: string
  icon: string
}

const route = useRoute()
const mobileOpen = ref(false)

const navItems: NavItem[] = [
  { label: '首页', path: '/', icon: '🏠' },
  { label: '简历', path: '/resume', icon: '📄' },
  { label: '笔记', path: '/notes', icon: '📝' },
  { label: '关于我', path: '/about', icon: '🙋' }
]

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

// 路由变化时收起移动端菜单
watch(() => route.fullPath, () => {
  mobileOpen.value = false
})
</script>

<style scoped>
@import '@/assets/styles/components/header.css';
</style>
