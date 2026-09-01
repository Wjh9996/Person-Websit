import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { afterEach, beforeAll, describe, expect, it } from 'vitest'
import App from '@/App.vue'
import router from '@/router'
import { seedNotes } from '@/data/seedNotes'
import { resumeMap, resumeNavItems } from '@/data/resumeNav'

// jsdom 不提供 IntersectionObserver 与 fetch，这里补桩：
// - IntersectionObserver：空实现，避免目录滚动高亮报错
// - fetch：按接口返回种子数据，使页面挂载冒烟测试无需真实后端
beforeAll(() => {
  class MockIntersectionObserver {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  }
  ;(globalThis as unknown as { IntersectionObserver: unknown }).IntersectionObserver = MockIntersectionObserver

  const ok = (data: unknown) => ({
    status: 200,
    text: async () => JSON.stringify({ code: 0, message: 'success', data })
  })
  const unauthorized = () => ({
    status: 401,
    text: async () => JSON.stringify({ code: 401, message: '未登录或缺少令牌', data: null })
  })

  const fetchMock = async (input: string | Request, init?: RequestInit) => {
    const url = typeof input === 'string' ? input : (input as Request).url
    const method = (init?.method ?? 'GET').toUpperCase()
    if (url.includes('/api/notes/tags')) return ok([])
    if (url === '/api/notes' && method === 'GET') return ok(seedNotes)
    if (url.startsWith('/api/notes/') && method === 'GET') {
      const id = url.split('/api/notes/')[1]
      return ok(seedNotes.find((n) => n.id === id) ?? null)
    }
    if (url === '/api/resumes' && method === 'GET') {
      return ok({ resumes: resumeMap, navItems: resumeNavItems })
    }
    if (url === '/api/auth/me') return unauthorized()
    return ok(null)
  }
  ;(globalThis as unknown as { fetch: unknown }).fetch = fetchMock
})

let wrapper: ReturnType<typeof mount> | null = null

async function renderAt(path: string) {
  const pinia = createPinia()
  wrapper = mount(App, { global: { plugins: [pinia, router] } })
  await router.isReady()
  await router.push(path)
  // 等待 service 层异步返回与后续渲染
  await new Promise((resolve) => setTimeout(resolve, 600))
  return wrapper
}

afterEach(() => {
  wrapper?.unmount()
  wrapper = null
  window.localStorage.clear()
})

describe('页面冒烟测试', () => {
  it('首页渲染', async () => {
    const w = await renderAt('/')
    expect(w.text()).toContain('欢迎回来')
    expect(w.text()).toContain('快速入口')
    expect(w.text()).toContain('最近更新')
  })

  it('首页展示笔记统计与卡片', async () => {
    const w = await renderAt('/')
    expect(w.text()).toContain('篇笔记')
    expect(w.text()).toContain('简历管理')
  })

  it('笔记列表页渲染', async () => {
    const w = await renderAt('/notes')
    expect(w.text()).toContain('学习笔记')
    expect(w.text()).toContain('Vue Router 路由懒加载的原理与配置')
  })

  it('笔记详情页渲染含目录与正文', async () => {
    const w = await renderAt('/notes/note-router-lazy')
    const text = w.text()
    expect(text).toContain('Vue Router 路由懒加载')
    expect(text).toContain('目录')
    expect(w.find('.markdown-body').exists()).toBe(true)
    expect(w.find('.markdown-body').html()).toContain('hljs')
  })

  it('登录页渲染', async () => {
    const w = await renderAt('/login')
    expect(w.text()).toContain('演示账号')
  })

  it('关于页渲染', async () => {
    const w = await renderAt('/about')
    expect(w.text()).toContain('王建豪')
    expect(w.text()).toContain('专业技能')
  })

  it('简历页渲染', async () => {
    const w = await renderAt('/resume/testing')
    expect(w.text()).toContain('软件测试')
    expect(w.text()).toContain('荆楚理工学院')
  })

  it('编辑器页未登录时跳转登录', async () => {
    const w = await renderAt('/notes/create')
    // 未登录会被重定向到登录页
    expect(router.currentRoute.value.path).toBe('/login')
    expect(w.exists()).toBe(true)
  })

  it('全局导航与页脚存在', async () => {
    const w = await renderAt('/')
    expect(w.find('.site-header').exists()).toBe(true)
    expect(w.find('.site-footer').exists()).toBe(true)
    expect(w.text()).toContain('首页')
    expect(w.text()).toContain('学习笔记')
  })
})
