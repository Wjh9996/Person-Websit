import { ApiError } from '@/utils/http'

/**
 * 把任意异常转成可展示给用户的中文文案。
 *
 * - ApiError：直接复用后端返回的友好文案（已含 401/403/409/404 等场景说明）
 * - 其它 Error：取其 message
 * - 未知类型：兜底文案
 *
 * 写操作（新建/编辑/删除/置顶）的 catch 里统一调用它，
 * 保证 401 未登录、403 越权、409 乐观锁冲突等都能被用户看到原因。
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return '操作失败，请稍后重试'
}
