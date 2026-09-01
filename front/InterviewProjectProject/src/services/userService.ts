import type { LoginPayload, RegisterPayload, User } from '@/types/user'
import { http, setToken, setStoredProfile, getStoredProfile, clearAuth } from '@/utils/http'

/**
 * 用户服务（已对接后端）
 *
 * login / register 调用 /api/auth/* 并持久化 JWT 与用户信息；
 * 写接口（/api/users/me）由 http 客户端自动携带 Authorization: Bearer 令牌。
 */

interface AuthResultDTO {
  token: string
  user: User
}

export async function login(payload: LoginPayload): Promise<User> {
  const result = await http.post<AuthResultDTO>('/api/auth/login', payload)
  setToken(result.token)
  setStoredProfile(result.user)
  return result.user
}

export async function register(payload: RegisterPayload): Promise<User> {
  const result = await http.post<AuthResultDTO>('/api/auth/register', payload)
  setToken(result.token)
  setStoredProfile(result.user)
  return result.user
}

export function logout(): void {
  // 后端为无状态 JWT，通知一下即可，失败不影响本地退出
  void http.post('/api/auth/logout').catch(() => {})
  clearAuth()
}

/** 同步读取本地已登录用户（store 初始化时使用） */
export function getCurrentUser(): User | null {
  return getStoredProfile<User>()
}

/** 携带令牌向 /api/auth/me 换取最新用户信息，用于刷新登录态 */
export async function fetchCurrentUser(): Promise<User | null> {
  try {
    const user = await http.get<User>('/api/auth/me')
    setStoredProfile(user)
    return user
  } catch {
    return null
  }
}

export async function updateProfile(patch: Partial<User>): Promise<User> {
  const body: Record<string, unknown> = {}
  if (patch.nickname !== undefined) body.nickname = patch.nickname
  if (patch.email !== undefined) body.email = patch.email
  if (patch.avatar !== undefined) body.avatar = patch.avatar
  if (patch.bio !== undefined) body.bio = patch.bio

  const user = await http.patch<User>('/api/users/me', body)
  setStoredProfile(user)
  return user
}
