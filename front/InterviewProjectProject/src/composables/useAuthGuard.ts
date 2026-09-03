import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

export interface RequireLoginOptions {
  /** 弹窗中展示的提示文案 */
  message?: string
  /** 登录后要回跳的地址（一般传触发操作的页面路径） */
  redirect?: string
}

/** 未登录时的默认提示 */
const DEFAULT_MESSAGE = '该操作需要登录后才能进行，登录后即可继续编辑。'

/**
 * 登录提示弹窗的全局状态。
 *
 * 声明在模块作用域而非组件内，因此全站共用一个弹窗实例：
 * 任何页面调用 requireLogin 都会驱动同一个弹窗显示，无需各自维护显隐。
 */
const visible = ref(false)
const message = ref('')
const redirectTo = ref('')

/**
 * 未登录权限守卫。
 *
 * 典型用法：在需要登录的事件处理函数开头调用，返回 false 时直接 return，
 * 不再执行后续写操作（避免打后端接口被 401 拦截）。
 */
export function useAuthGuard() {
  const userStore = useUserStore()
  // useRouter 依赖组件实例上下文，只能在 setup 期间调用，故放在函数体内
  const router = useRouter()

  /**
   * 校验登录态：已登录返回 true（调用方继续执行）；
   * 未登录则弹出「需要登录」提示并返回 false（调用方应终止）。
   */
  function requireLogin(options: RequireLoginOptions = {}): boolean {
    if (userStore.isLogin) return true
    message.value = options.message ?? DEFAULT_MESSAGE
    redirectTo.value = options.redirect ?? ''
    visible.value = true
    return false
  }

  /** 关闭提示弹窗 */
  function closeLoginTip(): void {
    visible.value = false
  }

  /** 前往登录页，并带上来源地址以便登录后自动回跳 */
  function goLogin(): void {
    visible.value = false
    const redirect = redirectTo.value
    void router.push({ path: '/login', query: redirect ? { redirect } : {} })
  }

  return { visible, message, requireLogin, closeLoginTip, goLogin }
}
