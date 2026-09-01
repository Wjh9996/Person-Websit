import type { NoteCategory } from '@/types/note'

/** 笔记分类（后端接入后可改为接口下发） */
export const noteCategories: NoteCategory[] = [
  { id: 'frontend', label: '前端开发', icon: '🎨' },
  { id: 'backend', label: '后端开发', icon: '⚙️' },
  { id: 'testing', label: '软件测试', icon: '🧪' },
  { id: 'tools', label: '工具链', icon: '🛠️' },
  { id: 'interview', label: '面试准备', icon: '📚' }
]

export function getCategoryLabel(id: string): string {
  return noteCategories.find((item) => item.id === id)?.label ?? '未分类'
}

export function getCategoryIcon(id: string): string {
  return noteCategories.find((item) => item.id === id)?.icon ?? '📄'
}
