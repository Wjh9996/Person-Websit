import type { ResumeData, ResumeNavItem } from '@/types/resume'
import { http } from '@/utils/http'

/**
 * 简历数据服务（已对接后端）
 *
 * 调用 Spring Boot 的 /api/resumes 接口，页面与 store 无需改动。
 */

interface ResumeSnapshot {
  resumes: Record<string, ResumeData>
  navItems: ResumeNavItem[]
}

export async function fetchResumeSnapshot(): Promise<ResumeSnapshot> {
  return http.get<ResumeSnapshot>('/api/resumes')
}

export async function saveResume(id: string, data: ResumeData): Promise<void> {
  await http.put<void>(`/api/resumes/${encodeURIComponent(id)}`, data)
}

export async function createResume(data: ResumeData, label: string): Promise<ResumeNavItem> {
  return http.post<ResumeNavItem>('/api/resumes', { data, label })
}

export async function deleteResume(id: string): Promise<boolean> {
  return http.del<boolean>(`/api/resumes/${encodeURIComponent(id)}`)
}

/**
 * 重置为初始数据（仅本地阶段使用）。后端以数据库为准。
 */
export async function resetResumes(): Promise<void> {
  // no-op
}
