import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { useNoteStore } from '@/stores/useNoteStore'
import { useUserStore } from '@/stores/useUserStore'
import { setToken } from '@/utils/http'
import type { Note } from '@/types/note'

/**
 * 「我的笔记」的数据归属测试。
 *
 * 重点覆盖曾经的线上问题：同一浏览器先登录 A、再登录 B，
 * store 因 myLoaded=true 直接复用缓存，导致 A 的笔记出现在 B 的「我的笔记」里
 * （点开才被后端 403 拦住）。现在要求：账号 id 一变就强制重新拉取。
 */

const NOTE_A: Note = {
  id: 'note-a',
  title: 'A 的私有笔记',
  summary: 'A 写的',
  content: '# A',
  category: 'backend',
  tags: ['A'],
  pinned: false,
  views: 1,
  visibility: 0,
  authorId: 'user-a',
  createdAt: '2026-09-17T10:00:00.000Z',
  updatedAt: '2026-09-17T10:00:00.000Z'
}

const NOTE_B: Note = {
  id: 'note-b',
  title: 'B 的私有笔记',
  summary: 'B 写的',
  content: '# B',
  category: 'backend',
  tags: ['B'],
  pinned: false,
  views: 1,
  visibility: 0,
  authorId: 'user-b',
  createdAt: '2026-09-17T11:00:00.000Z',
  updatedAt: '2026-09-17T11:00:00.000Z'
}

const userOf = (token: string | null) => (token === 'token-a' ? 'user-a' : token === 'token-b' ? 'user-b' : null)

beforeEach(() => {
  setActivePinia(createPinia())

  const ok = (data: unknown) => ({
    status: 200,
    text: async () => JSON.stringify({ code: 0, message: 'success', data })
  })

  const fetchMock = async (input: string | Request, init?: RequestInit) => {
    const url = typeof input === 'string' ? input : (input as Request).url
    const token = (init?.headers as Record<string, string>)?.Authorization?.replace('Bearer ', '') ?? null
    const me = userOf(token)

    if (url.startsWith('/api/notes?') && url.includes('scope=mine')) {
      return ok(me === 'user-a' ? [NOTE_A] : me === 'user-b' ? [NOTE_B] : [])
    }
    if (url.startsWith('/api/notes?') && url.includes('scope=plaza')) {
      return ok([NOTE_A, NOTE_B])
    }
    if (url.startsWith('/api/notes/tags')) return ok([])
    return ok(null)
  }
  ;(globalThis as unknown as { fetch: unknown }).fetch = fetchMock
})

afterEach(() => {
  window.localStorage.clear()
})

async function loginAs(id: 'user-a' | 'user-b') {
  const userStore = useUserStore()
  setToken(id === 'user-a' ? 'token-a' : 'token-b')
  userStore.profile = {
    id,
    username: id,
    nickname: id,
    email: `${id}@demo.com`,
    avatar: '用',
    bio: '',
    createdAt: '2026-09-17T10:00:00.000Z'
  }
  // 等待 watch 触发的自动重载
  await new Promise((resolve) => setTimeout(resolve, 60))
}

describe('我的笔记只属于当前登录用户', () => {
  it('A 登录后只拿到 A 的笔记', async () => {
    await loginAs('user-a')
    const noteStore = useNoteStore()
    await noteStore.loadMyNotes()
    expect(noteStore.myNotes.map((n) => n.id)).toEqual(['note-a'])
  })

  it('同一浏览器切换到 B 后，不再残留 A 的笔记', async () => {
    const noteStore = useNoteStore()
    await loginAs('user-a')
    await noteStore.loadMyNotes()
    expect(noteStore.myNotes.map((n) => n.id)).toEqual(['note-a'])

    // 关键：不刷新页面直接换账号
    await loginAs('user-b')
    await noteStore.loadMyNotes()
    expect(noteStore.myNotes.map((n) => n.id)).toEqual(['note-b'])
    expect(noteStore.myNotes.some((n) => n.authorId === 'user-a')).toBe(false)
  })

  it('退出登录后清空上一位用户的笔记', async () => {
    const noteStore = useNoteStore()
    await loginAs('user-a')
    await noteStore.loadMyNotes()
    expect(noteStore.myNotes.length).toBe(1)

    const userStore = useUserStore()
    userStore.logout()
    await new Promise((resolve) => setTimeout(resolve, 60))
    expect(noteStore.myNotes.length).toBe(0)
  })

  it('讨论广场不受登录切换影响，始终返回公开笔记', async () => {
    const noteStore = useNoteStore()
    await noteStore.loadPlazaNotes()
    expect(noteStore.plazaNotes.map((n) => n.id).sort()).toEqual(['note-a', 'note-b'])
  })
})
