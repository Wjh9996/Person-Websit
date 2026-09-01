import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

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
      component: () => import('@/views/NoteEditorView.vue')
    },
    {
      path: '/notes/:id/edit',
      name: 'note-edit',
      component: () => import('@/views/NoteEditorView.vue')
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
      component: () => import('@/views/ProfileView.vue')
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

export default router
