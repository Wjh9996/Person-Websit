/**
 * 统一 HTTP 客户端
 *
 * - 所有请求走 /api 前缀，开发环境由 vite 代理转发到后端 8080（见 vite.config.ts）
 * - 自动从 localStorage 读取 JWT 并注入 Authorization 头
 * - 后端统一返回 ApiResult<T>{code,message,data}，这里负责解包；code !== 0 抛 ApiError
 * - 401（未登录/令牌失效）自动清空本地登录态
 */
import { readStorage, removeStorage, writeStorage } from '@/utils/storage'

const TOKEN_KEY = 'user-token'
const PROFILE_KEY = 'user-profile'

// 生产环境若前后端不同源，可设置 VITE_API_BASE_URL 指向后端地址（如 http://api.example.com）
const env = (import.meta as { env?: Record<string, string | undefined> }).env
const BASE_URL = env?.VITE_API_BASE_URL ?? ''

export function getToken(): string | null {
  return readStorage<string | null>(TOKEN_KEY, null)
}

export function setToken(token: string | null): void {
  if (token) writeStorage(TOKEN_KEY, token)
  else removeStorage(TOKEN_KEY)
}

export function getStoredProfile<T>(): T | null {
  return readStorage<T | null>(PROFILE_KEY, null)
}

export function setStoredProfile<T>(profile: T | null): void {
  if (profile) writeStorage(PROFILE_KEY, profile)
  else removeStorage(PROFILE_KEY)
}

export function clearAuth(): void {
  setToken(null)
  setStoredProfile(null)
}

export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  code: number
  constructor(message: string, code: number) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

interface RequestOptions {
  method: string
  headers: Record<string, string>
  body?: string
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const options: RequestOptions = { method, headers }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }

  let response: Response
  try {
    response = await fetch(BASE_URL + path, options as RequestInit)
  } catch {
    throw new ApiError('网络异常，无法连接服务器', 0)
  }

  const text = await response.text()
  let envelope: ApiEnvelope<T>
  try {
    envelope = text ? (JSON.parse(text) as ApiEnvelope<T>) : ({ code: 0, message: 'ok', data: undefined as T })
  } catch {
    throw new ApiError(`服务器返回异常 (HTTP ${response.status})`, response.status)
  }

  // 令牌失效：清空本地登录态并抛出，由页面引导重新登录
  if (response.status === 401 || envelope.code === 401) {
    clearAuth()
    throw new ApiError(envelope.message || '登录已过期，请重新登录', 401)
  }

  if (envelope.code !== 0) {
    throw new ApiError(envelope.message || '请求失败', envelope.code)
  }

  return envelope.data
}

export const http = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, body),
  del: <T>(path: string) => request<T>('DELETE', path)
}
