<template>
  <nav class="resume-nav" aria-label="简历切换">
    <router-link to="/" class="nav-back">
      <span aria-hidden="true">←</span>
      返回首页
    </router-link>

    <div class="nav-tabs">
      <router-link
        v-for="item in items"
        :key="item.id"
        :to="item.path"
        class="nav-tab"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
        {{ item.label }}
      </router-link>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import { useRoute } from 'vue-router'
import type { ResumeNavItem } from '@/types/resume'

defineProps<{
  items: ResumeNavItem[]
}>()

const route = useRoute()

function isActive(path: string) {
  return route.path === path
}
</script>

<style scoped>
.resume-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.nav-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #64748b;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}

.nav-back:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.nav-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  text-decoration: none;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.nav-tab:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #2563eb;
}

.nav-tab.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.nav-icon {
  font-size: 16px;
  line-height: 1;
}

@media (max-width: 640px) {
  .resume-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .nav-tabs {
    width: 100%;
  }

  .nav-tab {
    flex: 1;
    justify-content: center;
  }
}
</style>
