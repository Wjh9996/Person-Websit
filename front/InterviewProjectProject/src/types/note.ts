/**
 * 笔记领域模型
 * 与后端对接时，字段名保持一致即可直接替换 service 层实现
 */
export interface Note {
  id: string
  title: string
  /** 列表页展示的摘要，不填时由正文自动截取 */
  summary: string
  /** Markdown 正文 */
  content: string
  /** 所属分类 id */
  category: string
  tags: string[]
  pinned: boolean
  views: number
  createdAt: string
  updatedAt: string
}

/** 新建笔记时的入参 */
export type NoteDraft = Omit<Note, 'id' | 'views' | 'createdAt' | 'updatedAt'>

export interface NoteCategory {
  id: string
  label: string
  icon: string
}

export interface NoteQuery {
  keyword?: string
  category?: string
  tag?: string
}
