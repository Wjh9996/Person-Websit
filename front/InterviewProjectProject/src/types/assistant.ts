/**
 * AI 助手（基于自己笔记的知识库问答）领域模型
 */
export interface ChatRef {
  noteId: string
  title: string
  /** 全文检索相关度得分，可能为 null */
  score: number | null
}

export interface ChatSession {
  id: string
  title: string
  updatedAt: string
}

export interface ChatMessage {
  id: string
  /** user = 我提的问题，assistant = 助手的回答 */
  role: 'user' | 'assistant'
  content: string
  /** 回答引用的笔记，user 消息为 null */
  refs: ChatRef[] | null
  createdAt: string
}

export interface AskResult {
  messageId: string
  answer: string
  refs: ChatRef[]
  /** 是否真的从知识库召回到了笔记；false 表示答案来自通用/引导文案 */
  fromKnowledgeBase: boolean
}
