import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useUserStore } from '@/stores/useUserStore'

// 扩展路由元信息类型，让 meta.requiresAuth 具备类型提示
declare module 'vue-router' {
  interface RouteMeta {
    /** 为 true 时，必须登录后才能进入该页面 */
    requiresAuth?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },

    // ===== 简历模块 =====
    {
      path: '/resume',
      redirect: '/resume/testing'
    },
    {
      path: '/resume/:type',
      name: 'resume',
      component: () => import('@/views/ResumeView.vue')
    },

    // ===== 笔记模块（静态路径需排在动态路径之前） =====
    {
      path: '/notes',
      name: 'notes',
      component: () => import('@/views/NotesView.vue')
    },
    {
      path: '/notes/create',
      name: 'note-create',
      component: () => import('@/views/NoteEditorView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/notes/:id/edit',
      name: 'note-edit',
      component: () => import('@/views/NoteEditorView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/notes/:id',
      name: 'note-detail',
      component: () => import('@/views/NoteDetailView.vue')
    },

    // ===== 用户模块 =====
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },

    // ===== 其他 =====
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ],
  // 切换路由后回到页面顶部
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition ?? { top: 0 }
  }
})

/**
 * 页面级权限守卫：编辑类页面（写笔记、编辑笔记、个人中心）必须登录才能进入。
 * 未登录时跳转登录页并带上来源地址，登录成功后会自动回跳到原页面。
 */
router.beforeEach((to) => {
  if (!to.meta.requiresAuth) return true

  const userStore = useUserStore()
  if (userStore.isLogin) return true

  return { path: '/login', query: { redirect: to.fullPath } }
})

export default router
