<template>
  <transition name="tip-fade">
    <div
      v-if="visible"
      class="login-tip-mask"
      role="dialog"
      aria-modal="true"
      aria-labelledby="login-tip-title"
      @click.self="closeLoginTip"
    >
      <div class="login-tip-card">
        <div class="login-tip-icon">🔒</div>
        <h3 id="login-tip-title" class="login-tip-title">需要登录</h3>
        <p class="login-tip-desc">{{ message }}</p>

        <div class="login-tip-actions">
          <button type="button" class="login-tip-btn ghost" @click="closeLoginTip">
            稍后再说
          </button>
          <button type="button" class="login-tip-btn primary" @click="goLogin">去登录</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useAuthGuard } from '@/composables/useAuthGuard'

// 弹窗与守卫共享同一份全局状态，全站只需在 App.vue 挂载一次
const { visible, message, closeLoginTip, goLogin } = useAuthGuard()

/** 支持 Esc 关闭 */
function onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Escape' && visible.value) closeLoginTip()
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
@import '@/assets/styles/components/login-required.css';
</style>
