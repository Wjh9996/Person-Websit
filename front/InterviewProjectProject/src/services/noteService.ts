import type { Note, NoteDraft, NoteScope } from '@/types/note'
import { http } from '@/utils/http'

/**
 * 笔记数据服务（已对接后端）
 *
 * 关键约定：
 * - `scope=mine` 走「我的笔记」（需登录，后端按 JWT 取当前用户，返回私有 + 已发广场的全部笔记）
 * - `scope=plaza` 走「讨论广场」（公开，任何人可见）
 * 前端不允许把 userId 当参数直接传，避免出现拼 id 偷看别人笔记的越权口子。
 */

interface TagCountDTO {
  tag: string
  count: number
}

function buildPath(scope: NoteScope, query: Record<string, string | undefined> = {}): string {
  const params = new URLSearchParams({ scope })
  Object.entries(query).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  return `/api/notes?${params.toString()}`
}

/** 列表查询：scope 决定拉「我的笔记」还是「讨论广场」 */
export async function fetchNotes(scope: NoteScope = 'plaza'): Promise<Note[]> {
  return http.get<Note[]>(buildPath(scope))
}

export async function fetchNoteById(id: string): Promise<Note | null> {
  return http.get<Note | null>(`/api/notes/${encodeURIComponent(id)}`)
}

/** 阅读数 +1 */
export async function increaseViews(id: string): Promise<void> {
  await http.patch<void>(`/api/notes/${encodeURIComponent(id)}/views`)
}

export async function createNote(draft: NoteDraft): Promise<Note> {
  return http.post<Note>('/api/notes', draft)
}

export async function updateNote(id: string, draft: NoteDraft): Promise<Note | null> {
  return http.put<Note | null>(`/api/notes/${encodeURIComponent(id)}`, draft)
}

export async function deleteNote(id: string): Promise<boolean> {
  return http.del<boolean>(`/api/notes/${encodeURIComponent(id)}`)
}

export async function togglePinned(id: string): Promise<Note | null> {
  return http.patch<Note | null>(`/api/notes/${encodeURIComponent(id)}/pin`)
}

/** 发布到讨论广场 / 从广场收回 */
export async function togglePlaza(id: string, toPlaza: boolean): Promise<Note | null> {
  return http.patch<Note | null>(
    `/api/notes/${encodeURIComponent(id)}/plaza?toPlaza=${toPlaza}`
  )
}

/** 标签聚合（同样区分 mine / plaza，游客只能看到广场标签） */
export async function fetchAllTags(scope: NoteScope = 'plaza'): Promise<Array<{ tag: string; count: number }>> {
  const list = await http.get<TagCountDTO[]>(`/api/notes/tags?scope=${scope}`)
  return list.map((item) => ({ tag: item.tag, count: item.count }))
}

/**
 * 重置为初始数据（仅本地阶段使用）。后端以数据库为准，
 * 如需重置请清空数据库 note 表后重启，或由后续管理功能提供。
 */
export async function resetNotes(): Promise<void> {
  // no-op
}
