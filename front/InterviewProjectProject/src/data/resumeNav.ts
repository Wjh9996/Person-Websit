import type { ResumeData, ResumeNavItem } from '@/types/resume'
import { testResume } from '@/data/TestResume'
import { devResume } from '@/data/DevResume'
import { techSupportResume } from '@/data/TechSupportResume'

export const resumeNavItems: ResumeNavItem[] = [
  {
    id: 'testing',
    label: '软件测试',
    path: '/resume/testing',
    icon: '🧪'
  },
  {
    id: 'development',
    label: '软件开发',
    path: '/resume/development',
    icon: '💻'
  },
  {
    id: 'support',
    label: '技术支持',
    path: '/resume/support',
    icon: '🛠️'
  }
]

export const resumeMap: Record<string, ResumeData> = {
  testing: testResume,
  development: devResume,
  support: techSupportResume
}
