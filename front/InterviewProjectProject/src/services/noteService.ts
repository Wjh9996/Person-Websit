import type { Note, NoteDraft, NoteQuery } from '@/types/note'
import { http } from '@/utils/http'

/**
 * 笔记数据服务（已对接后端）
 *
 * 所有方法调用 Spring Boot 的 /api/notes 接口，页面与 store 无需改动。
 * 字段命名与 NoteDTO 完全一致，可直接按 Note 类型使用。
 */

interface TagCountDTO {
  tag: string
  count: number
}

/** 列表查询：直接拉全量，前端 store 负责筛选/排序（与对接前行为一致） */
export async function fetchNotes(_query: NoteQuery = {}): Promise<Note[]> {
  return http.get<Note[]>('/api/notes')
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

/** 全站标签聚合，用于筛选栏 */
export async function fetchAllTags(): Promise<Array<{ tag: string; count: number }>> {
  const list = await http.get<TagCountDTO[]>('/api/notes/tags')
  return list.map((item) => ({ tag: item.tag, count: item.count }))
}

/**
 * 重置为初始数据（仅本地阶段使用）。后端以数据库为准，
 * 如需重置请清空数据库 note 表后重启，或由后续管理功能提供。
 */
export async function resetNotes(): Promise<void> {
  // no-op
}
