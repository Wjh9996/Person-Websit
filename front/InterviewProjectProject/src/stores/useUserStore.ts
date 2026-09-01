import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginPayload, RegisterPayload, User } from '@/types/user'
import * as userService from '@/services/userService'

/**
 * 用户状态：登录态、个人资料
 */
export const useUserStore = defineStore('user', () => {
  const profile = ref<User | null>(userService.getCurrentUser())
  const loading = ref(false)
  const error = ref('')

  const isLogin = computed(() => profile.value !== null)
  const nickname = computed(() => profile.value?.nickname ?? '未登录')
  const avatar = computed(() => profile.value?.avatar ?? '游')

  async function login(payload: LoginPayload): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      profile.value = await userService.login(payload)
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '登录失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(payload: RegisterPayload): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      profile.value = await userService.register(payload)
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '注册失败'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout(): void {
    userService.logout()
    profile.value = null
  }

  async function updateProfile(patch: Partial<User>): Promise<void> {
    if (!profile.value) return
    profile.value = await userService.updateProfile(patch)
  }

  /** 应用启动时刷新登录态：本地已有令牌但 profile 为空时，向 /api/auth/me 换取 */
  async function refresh(): Promise<void> {
    if (profile.value) return
    try {
      profile.value = await userService.fetchCurrentUser()
    } catch {
      // 令牌失效则保持未登录
    }
  }

  return { profile, loading, error, isLogin, nickname, avatar, login, register, logout, updateProfile, refresh }
})
