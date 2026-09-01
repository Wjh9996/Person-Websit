/**
 * 用户领域模型
 */
export interface User {
  id: string
  username: string
  nickname: string
  email: string
  /** 头像占位字符（真实头像接入后改为 url） */
  avatar: string
  bio: string
  createdAt: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  nickname: string
  email: string
}

/** 后端返回结构统一包装，便于后续替换为真实接口 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
