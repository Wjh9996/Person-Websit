import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { Note, NoteDraft, NoteScope } from '@/types/note'
import { noteCategories } from '@/data/noteCategories'
import * as noteService from '@/services/noteService'
import { ApiError } from '@/utils/http'
import { useUserStore } from '@/stores/useUserStore'

type TagCount = { tag: string; count: number }

/**
 * 笔记状态：我的笔记 / 讨论广场两份数据 + 前端筛选
 *
 * 之所以拆成两份：过去只有一份全站列表（后端 userId 为空时查全表），
 * 导致任何登录用户都能看到别人的笔记。现在：
 * - myNotes：  scope=mine，必须登录，只含本人笔记（私有 + 已发广场）
 * - plazaNotes：scope=plaza，公开，只含 visibility=1 的笔记
 * notes / tags / loading / loaded 都是「当前 scope」下的只读视图，
 * 页面通过 setScope 切换，用法与改造前完全一致。
 */
export const useNoteStore = defineStore('note', () => {
  const myNotes = ref<Note[]>([])
  const plazaNotes = ref<Note[]>([])
  const myTags = ref<TagCount[]>([])
  const plazaTags = ref<TagCount[]>([])
  const myLoading = ref(false)
  const plazaLoading = ref(false)
  /** 已加载标记，避免 App 与页面组件重复请求 */
  const myLoaded = ref(false)
  const plazaLoaded = ref(false)
  /**
   * myNotes 是按「哪个用户」的身份加载的。
   * 同一浏览器 A 退出、B 登录后，如果只靠 myLoaded 判断，会把 A 的笔记继续显示给 B
   * （详情页再被后端 403 拦下，表现得就像"B 能看到但没有权限打开"）。
   * 所以必须记住归属用户 id，切换账号时强制重新拉取。
   */
  const myNotesOwnerId = ref<string | null>(null)

  /** 当前浏览视图：mine = 我的笔记，plaza = 讨论广场 */
  const scope = ref<NoteScope>('plaza')

  const notes = computed<Note[]>(() => (scope.value === 'mine' ? myNotes.value : plazaNotes.value))
  const tags = computed<TagCount[]>(() => (scope.value === 'mine' ? myTags.value : plazaTags.value))
  const loading = computed(() => (scope.value === 'mine' ? myLoading.value : plazaLoading.value))
  const loaded = computed(() => (scope.value === 'mine' ? myLoaded.value : plazaLoaded.value))

  const keyword = ref('')
  const activeCategory = ref('all')
  const activeTag = ref('')

  const categories = computed(() => noteCategories)

  /** 按当前条件过滤后的列表 */
  const filteredNotes = computed(() => {
    const kw = keyword.value.trim().toLowerCase()
    return notes.value
      .filter((note) => activeCategory.value === 'all' || note.category === activeCategory.value)
      .filter((note) => !activeTag.value || note.tags.includes(activeTag.value))
      .filter((note) => {
        if (!kw) return true
        // 列表接口不返回正文（content 为空），搜索时仅对标题/摘要匹配
        return (
          note.title.toLowerCase().includes(kw) ||
          (note.summary ?? '').toLowerCase().includes(kw)
        )
      })
      .sort((a, b) => {
        if (a.pinned !== b.pinned) return a.pinned ? -1 : 1
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      })
  })

  /** 每个分类下的笔记数，用于侧边栏徽标 */
  const categoryCounts = computed(() => {
    const counts: Record<string, number> = { all: notes.value.length }
    noteCategories.forEach((cat) => {
      counts[cat.id] = notes.value.filter((note) => note.category === cat.id).length
    })
    return counts
  })

  const pinnedNotes = computed(() => notes.value.filter((note) => note.pinned))
  const recentNotes = computed(() =>
    [...notes.value]
      .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
      .slice(0, 4)
  )
  const totalViews = computed(() => notes.value.reduce((sum, note) => sum + note.views, 0))

  /* ========== 列表内部工具 ========== */

  /** 就地更新已有条目（保留列表对象引用），不存在则插入 */
  function upsert(list: Note[], note: Note): void {
    const index = list.findIndex((item) => item.id === note.id)
    if (index === -1) list.unshift(note)
    else list[index] = { ...list[index], ...note }
  }

  /** 已存在才更新，不存在则忽略 */
  function mergeIfExists(list: Note[], note: Note): void {
    const index = list.findIndex((item) => item.id === note.id)
    if (index !== -1) list[index] = { ...list[index], ...note }
  }

  function applyEverywhere(note: Note): void {
    mergeIfExists(myNotes.value, note)
    mergeIfExists(plazaNotes.value, note)
  }

  function currentUserId(): string | undefined {
    return useUserStore().profile?.id
  }

  async function refreshTags(): Promise<void> {
    plazaTags.value = await noteService.fetchAllTags('plaza')
    myTags.value = (await noteService.fetchAllTags('mine')) ?? []
  }

  /* ========== 加载 ========== */

  /** 丢弃上一位用户的私有数据（退出登录 / 切换账号时调用） */
  function clearMyNotes(): void {
    myNotes.value = []
    myTags.value = []
    myLoaded.value = false
    myNotesOwnerId.value = null
  }

  async function loadMyNotes(force = false): Promise<void> {
    const ownerId = currentUserId() ?? null
    // 换人了就必须重新拉，绝不能沿用上一个账号的缓存
    const ownerChanged = myNotesOwnerId.value !== null && myNotesOwnerId.value !== ownerId
    if (myLoaded.value && !force && !ownerChanged) return

    if (!ownerId) {
      // 未登录：清空残留数据，避免旧账号笔记留在页面上
      clearMyNotes()
      return
    }

    myLoading.value = true
    try {
      const [list, tagList] = await Promise.all([
        noteService.fetchNotes('mine'),
        noteService.fetchAllTags('mine')
      ])
      myNotes.value = list
      myTags.value = tagList
      myLoaded.value = true
      myNotesOwnerId.value = ownerId
    } catch (e) {
      // 令牌失效时静默降级为空列表，交由页面引导登录，避免全局报错
      if (e instanceof ApiError && e.code === 401) {
        clearMyNotes()
        return
      }
      throw e
    } finally {
      myLoading.value = false
    }
  }

  async function loadPlazaNotes(force = false): Promise<void> {
    if (plazaLoaded.value && !force) return
    plazaLoading.value = true
    try {
      plazaNotes.value = await noteService.fetchNotes('plaza')
      plazaTags.value = await noteService.fetchAllTags('plaza')
      plazaLoaded.value = true
    } finally {
      plazaLoading.value = false
    }
  }

  /** 按当前 scope 加载（保持旧调用兼容） */
  async function loadNotes(force = false): Promise<void> {
    if (scope.value === 'mine') await loadMyNotes(force)
    else await loadPlazaNotes(force)
  }

  function setScope(next: NoteScope): void {
    scope.value = next
  }

  function getNoteById(id: string): Note | undefined {
    return myNotes.value.find((note) => note.id === id) ?? plazaNotes.value.find((note) => note.id === id)
  }

  /**
   * 拉取单篇笔记的完整数据（含正文 content）。
   *
   * 列表接口为性能考虑不返回正文，详情/编辑页必须单独调用；
   * 私有笔记非作者会得到 403，这里原样抛出交给页面提示。
   * 取到后按可见性/归属补进对应列表，让返回后可正常浏览/编辑。
   */
  async function fetchNoteDetail(id: string): Promise<Note | undefined> {
    try {
      const detail = await noteService.fetchNoteById(id)
      if (!detail) return undefined
      applyEverywhere(detail)
      if (detail.authorId && detail.authorId === currentUserId()) upsert(myNotes.value, detail)
      if (detail.visibility === 1) upsert(plazaNotes.value, detail)
      return detail
    } catch (e) {
      if (e instanceof ApiError && e.code === 404) return undefined
      throw e
    }
  }

  async function createNote(draft: NoteDraft): Promise<Note> {
    const note = await noteService.createNote(draft)
    upsert(myNotes.value, note)
    if (note.visibility === 1) upsert(plazaNotes.value, note)
    await refreshTags()
    return note
  }

  async function updateNote(id: string, draft: NoteDraft): Promise<void> {
    const updated = await noteService.updateNote(id, draft)
    if (updated) {
      applyEverywhere(updated)
      upsert(myNotes.value, updated)
      if (updated.visibility === 1) upsert(plazaNotes.value, updated)
      else plazaNotes.value = plazaNotes.value.filter((note) => note.id !== id)
    }
    await refreshTags()
  }

  async function removeNote(id: string): Promise<void> {
    const ok = await noteService.deleteNote(id)
    if (ok) {
      myNotes.value = myNotes.value.filter((note) => note.id !== id)
      plazaNotes.value = plazaNotes.value.filter((note) => note.id !== id)
    }
    await refreshTags()
  }

  async function togglePinned(id: string): Promise<void> {
    const updated = await noteService.togglePinned(id)
    if (updated) applyEverywhere(updated)
  }

  /** 发布到广场 / 从广场收回 */
  async function togglePlaza(id: string, toPlaza: boolean): Promise<void> {
    const updated = await noteService.togglePlaza(id, toPlaza)
    if (!updated) return
    upsert(myNotes.value, updated)
    if (toPlaza) upsert(plazaNotes.value, updated)
    else plazaNotes.value = plazaNotes.value.filter((note) => note.id !== id)
    await refreshTags()
  }

  async function increaseViews(id: string): Promise<void> {
    await noteService.increaseViews(id)
    const target =
      myNotes.value.find((note) => note.id === id) ?? plazaNotes.value.find((note) => note.id === id)
    if (target) target.views += 1
  }

  function resetFilters(): void {
    keyword.value = ''
    activeCategory.value = 'all'
    activeTag.value = ''
  }

  const userStore = useUserStore()

  /**
   * 账号切换守卫：只要登录用户 id 变了（含退出登录），就立刻失效「我的笔记」缓存。
   * 否则停留在同一页面切换账号时，上一个账号的笔记会一直显示在新账号的页面上。
   */
  watch(
    () => userStore.profile?.id ?? null,
    (next) => {
      if (!next) clearMyNotes()
      else void loadMyNotes(true)
    }
  )

  return {
    myNotes,
    plazaNotes,
    scope,
    setScope,
    notes,
    tags,
    loading,
    loaded,
    myLoaded,
    plazaLoaded,
    keyword,
    activeCategory,
    activeTag,
    categories,
    filteredNotes,
    categoryCounts,
    pinnedNotes,
    recentNotes,
    totalViews,
    loadNotes,
    loadMyNotes,
    loadPlazaNotes,
    clearMyNotes,
    getNoteById,
    fetchNoteDetail,
    createNote,
    updateNote,
    removeNote,
    togglePinned,
    togglePlaza,
    increaseViews,
    resetFilters
  }
})
