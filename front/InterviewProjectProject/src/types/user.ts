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
  /** 邮箱收到的 6 位验证码，后端校验通过才会创建账号 */
  code: string
}

/** 验证码发送结果 */
export interface EmailCodeResult {
  sent: boolean
  /** 有效期（分钟） */
  expireMinutes: number
  /** 重发间隔（秒），前端据此做倒计时 */
  resendIntervalSeconds: number
}

/** 后端返回结构统一包装，便于后续替换为真实接口 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
