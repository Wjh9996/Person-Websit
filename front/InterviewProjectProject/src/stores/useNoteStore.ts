import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { Note, NoteDraft } from '@/types/note'
import { noteCategories } from '@/data/noteCategories'
import * as noteService from '@/services/noteService'

/**
 * 笔记状态：列表、筛选条件、增删改
 * 筛选在前端 computed 中完成，接入后端分页后可改为请求式筛选
 */
export const useNoteStore = defineStore('note', () => {
  const notes = ref<Note[]>([])
  const tags = ref<Array<{ tag: string; count: number }>>([])
  const loading = ref(false)
  /** 已加载标记，避免 App 与页面组件重复请求 */
  const loaded = ref(false)

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
        return (
          note.title.toLowerCase().includes(kw) ||
          note.summary.toLowerCase().includes(kw) ||
          note.content.toLowerCase().includes(kw)
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

  async function loadNotes(force = false): Promise<void> {
    if (loaded.value && !force) return
    loading.value = true
    try {
      notes.value = await noteService.fetchNotes()
      tags.value = await noteService.fetchAllTags()
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  function getNoteById(id: string): Note | undefined {
    return notes.value.find((note) => note.id === id)
  }

  async function createNote(draft: NoteDraft): Promise<Note> {
    const note = await noteService.createNote(draft)
    notes.value.unshift(note)
    tags.value = await noteService.fetchAllTags()
    return note
  }

  async function updateNote(id: string, draft: NoteDraft): Promise<void> {
    const updated = await noteService.updateNote(id, draft)
    if (updated) {
      const index = notes.value.findIndex((note) => note.id === id)
      if (index !== -1) notes.value[index] = updated
    }
    tags.value = await noteService.fetchAllTags()
  }

  async function removeNote(id: string): Promise<void> {
    const ok = await noteService.deleteNote(id)
    if (ok) notes.value = notes.value.filter((note) => note.id !== id)
    tags.value = await noteService.fetchAllTags()
  }

  async function togglePinned(id: string): Promise<void> {
    const updated = await noteService.togglePinned(id)
    if (updated) {
      const target = notes.value.find((note) => note.id === id)
      if (target) target.pinned = updated.pinned
    }
  }

  async function increaseViews(id: string): Promise<void> {
    await noteService.increaseViews(id)
    const target = notes.value.find((note) => note.id === id)
    if (target) target.views += 1
  }

  function resetFilters(): void {
    keyword.value = ''
    activeCategory.value = 'all'
    activeTag.value = ''
  }

  return {
    notes,
    tags,
    loading,
    loaded,
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
    getNoteById,
    createNote,
    updateNote,
    removeNote,
    togglePinned,
    increaseViews,
    resetFilters
  }
})
