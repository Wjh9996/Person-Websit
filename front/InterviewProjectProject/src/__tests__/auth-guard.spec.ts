import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { afterEach, beforeAll, describe, expect, it } from 'vitest'
import App from '@/App.vue'
import router from '@/router'
import { seedNotes } from '@/data/seedNotes'
import { resumeMap, resumeNavItems } from '@/data/resumeNav'

/**
 * 未登录场景下的权限收口测试。
 *
 * fetch 桩中 /api/auth/me 固定返回 401，页面因此始终处于未登录态，
 * 用于验证：未登录只能浏览，编辑类入口被收口为「一个提示登录的按钮」。
 */
beforeAll(() => {
  class MockIntersectionObserver {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  }
  ;(globalThis as unknown as { IntersectionObserver: unknown }).IntersectionObserver =
    MockIntersectionObserver

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
    // 关键：保持未登录态
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

/**
 * 登录提示弹窗使用全站单例状态，不会随组件卸载而复位，
 * 因此每个用例开头需先关闭可能残留的弹窗，避免用例之间相互污染。
 */
async function closeLoginTip(target: ReturnType<typeof mount>): Promise<void> {
  if (target.find('.login-tip-mask').exists()) {
    await target.find('.login-tip-btn.ghost').trigger('click')
  }
}

afterEach(() => {
  wrapper?.unmount()
  wrapper = null
  window.localStorage.clear()
})

describe('未登录只读权限收口', () => {
  it('首页只提供只读入口，不出现编辑类文案', async () => {
    const w = await renderAt('/')
    await closeLoginTip(w)

    const text = w.text()
    expect(text).toContain('查看简历')
    expect(text).toContain('写笔记')
    expect(text).not.toContain('新建笔记')
    expect(text).not.toContain('管理简历')
  })

  it('首页点击「写笔记」弹出需要登录提示', async () => {
    const w = await renderAt('/')
    await closeLoginTip(w)
    expect(w.find('.login-tip-mask').exists()).toBe(false)

    const writeBtn = w.findAll('.quick-btn').find((btn) => btn.text().includes('写笔记'))
    expect(writeBtn).toBeTruthy()

    await writeBtn?.trigger('click')
    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(w.find('.login-tip-mask').exists()).toBe(true)
    expect(w.text()).toContain('需要登录')
  })

  it('弹窗「去登录」跳转登录页并带上来源地址', async () => {
    const w = await renderAt('/')
    await closeLoginTip(w)

    const writeBtn = w.findAll('.quick-btn').find((btn) => btn.text().includes('写笔记'))
    await writeBtn?.trigger('click')
    await new Promise((resolve) => setTimeout(resolve, 50))

    await w.find('.login-tip-btn.primary').trigger('click')
    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/notes/create')
  })

  it('笔记详情页只保留一个编辑入口，隐藏置顶与删除', async () => {
    const w = await renderAt('/notes/note-router-lazy')
    await closeLoginTip(w)

    const actions = w.findAll('.article-actions .action-btn')
    expect(actions.length).toBe(1)
    expect(actions[0]?.text()).toContain('编辑')
    expect(w.find('.article-actions').text()).not.toContain('删除')
  })

  it('简历页只保留一个编辑入口，隐藏新增与删除', async () => {
    const w = await renderAt('/resume/testing')
    await closeLoginTip(w)

    const actions = w.findAll('.action-buttons .action-btn')
    expect(actions.length).toBe(1)
    expect(actions[0]?.text()).toContain('编辑简历')
    expect(w.find('.action-buttons').text()).not.toContain('删除')
  })

  it('未登录直接访问编辑页会被重定向到登录页', async () => {
    const w = await renderAt('/notes/create')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/notes/create')
    expect(w.exists()).toBe(true)
  })

  it('未登录直接访问个人中心会被重定向到登录页', async () => {
    await renderAt('/profile')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/profile')
  })
})
