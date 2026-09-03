<template>
  <div class="app-shell">
    <AppHeaderComponent />

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <AppFooterComponent />

    <!-- 全局登录提示弹窗：任意页面调用 requireLogin 都会唤起它 -->
    <LoginRequiredModalComponent />
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import AppHeaderComponent from '@/components/layout/AppHeaderComponent.vue'
import AppFooterComponent from '@/components/layout/AppFooterComponent.vue'
import LoginRequiredModalComponent from '@/components/common/LoginRequiredModalComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'
import { useResumeStore } from '@/stores/useResumeStore'
import { useUserStore } from '@/stores/useUserStore'

const noteStore = useNoteStore()
const resumeStore = useResumeStore()
const userStore = useUserStore()

// 应用启动时预加载共享数据，首页统计与导航徽标依赖它
onMounted(() => {
  void noteStore.loadNotes()
  void resumeStore.loadResumes()
  void userStore.refresh()
})
</script>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f4f6f9;
}

.app-main {
  flex: 1;
}

/* 页面切换过渡 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
