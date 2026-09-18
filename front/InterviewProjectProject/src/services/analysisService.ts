import { http } from '@/utils/http'
import type { AnalysisAggregate, AnalysisTriggerResult, TaskStatusVO } from '@/types/analysis'

/** 触发分析（异步），返回任务 id；types 缺省则后端全量分析 */
export function triggerAnalysis(noteId: string, types?: string[]): Promise<AnalysisTriggerResult> {
  const qs = types && types.length ? `?types=${encodeURIComponent(types.join(','))}` : ''
  return http.post<AnalysisTriggerResult>(`/api/notes/${noteId}/analyze${qs}`)
}

/** 查询某笔记全部有效分析结果（按类型聚合） */
export function getAnalysis(noteId: string): Promise<AnalysisAggregate> {
  return http.get<AnalysisAggregate>(`/api/notes/${noteId}/analysis`)
}

/** 轮询异步任务状态 */
export function getTaskStatus(taskId: string): Promise<TaskStatusVO> {
  return http.get<TaskStatusVO>(`/api/analysis/tasks/${taskId}`)
}
