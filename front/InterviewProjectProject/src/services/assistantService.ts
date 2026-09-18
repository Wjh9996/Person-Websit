import type { AskResult, ChatMessage, ChatSession } from '@/types/assistant'
import { http } from '@/utils/http'

/**
 * AI 助手数据服务（已对接后端 /api/assistant）
 *
 * 所有接口都必须登录：会话与消息都按 user_id 隔离，
 * 前端不传 userId，由后端从 JWT 里取，避免拼 id 越权。
 */

export async function fetchSessions(): Promise<ChatSession[]> {
  return http.get<ChatSession[]>('/api/assistant/sessions')
}

export async function createSession(title?: string): Promise<ChatSession> {
  return http.post<ChatSession>('/api/assistant/sessions', { title: title ?? '' })
}

export async function deleteSession(sessionId: string): Promise<boolean> {
  return http.del<boolean>(`/api/assistant/sessions/${encodeURIComponent(sessionId)}`)
}

export async function fetchMessages(sessionId: string): Promise<ChatMessage[]> {
  return http.get<ChatMessage[]>(
    `/api/assistant/sessions/${encodeURIComponent(sessionId)}/messages`
  )
}

/** 提问：同步返回答案 + 引用的笔记 */
export async function askQuestion(sessionId: string, question: string): Promise<AskResult> {
  return http.post<AskResult>(
    `/api/assistant/sessions/${encodeURIComponent(sessionId)}/ask`,
    { question }
  )
}
