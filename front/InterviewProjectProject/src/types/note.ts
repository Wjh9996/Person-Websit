/**
 * 笔记领域模型
 * 字段与后端 NoteDTO 保持一致
 */

/** 笔记可见性：0 私有（仅作者本人） 1 讨论广场公开 */
export type NoteVisibility = 0 | 1

/** 列表数据来源：mine = 我的笔记，plaza = 讨论广场 */
export type NoteScope = 'mine' | 'plaza'

export interface Note {
  id: string
  title: string
  /** 列表页展示的摘要，不填时由正文自动截取 */
  summary: string
  /** Markdown 正文（列表接口不返回，需单独拉详情） */
  content: string
  /** 所属分类 id */
  category: string
  tags: string[]
  pinned: boolean
  views: number
  /** 可见性：0 私有 1 广场公开 */
  visibility: NoteVisibility
  /** 作者 id（后端一定会回填，便于前端判断「是不是我的笔记」） */
  authorId?: string
  /** 作者昵称（广场列表返回） */
  authorName?: string
  createdAt: string
  updatedAt: string
}

/** 新建 / 更新笔记的入参（不含服务端生成的字段） */
export type NoteDraft = Omit<
  Note,
  'id' | 'views' | 'createdAt' | 'updatedAt' | 'visibility' | 'authorId' | 'authorName'
> & {
  /** 是否发布到讨论广场：true 公开给所有人；false 仅自己可见。不传表示不改动可见性 */
  toPlaza?: boolean
}

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
