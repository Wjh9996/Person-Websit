export interface ResumeData {
  basicInfo: {
    name: string
    title: string
    phone?: string
    email?: string
  }
  education: {
    school: string
    major: string
    period: string
  }
  summary: string[]
  campus: Array<{ period: string; description: string }>
  skills: string[]
  internships: Array<{
    company: string
    position: string
    period: string
    duties: string[]
  }>
  projects: Array<{
    name: string
    techStack: string
    description: string
    duties: string[]
  }>
  projectDutyTitle?: string
}

export interface ResumeNavItem {
  id: string
  label: string
  path: string
  icon: string
}
