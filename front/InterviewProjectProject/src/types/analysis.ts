export type AnalysisType = 'summary' | 'keywords' | 'category' | 'knowledge'

export interface AnalysisAggregate {
  summary?: string
  keywords?: string[]
  category?: string
  categoryReason?: string
  knowledge?: string[]
  analyzedAt?: string
}

export type TaskStatus = 'pending' | 'processing' | 'success' | 'failed'

export interface TaskStatusVO {
  taskId: string
  status: TaskStatus
  errorMsg?: string | null
}

export interface AnalysisTriggerResult {
  taskId: string
}
