import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { ResumeData, ResumeNavItem } from '@/types/resume'
import * as resumeService from '@/services/resumeService'

/**
 * 简历状态：多份简历的导航项与内容，本地持久化
 */
export const useResumeStore = defineStore('resume', () => {
  const resumes = ref<Record<string, ResumeData>>({})
  const navItems = ref<ResumeNavItem[]>([])
  const loading = ref(false)
  /** 已加载标记，避免 App 与页面组件重复请求 */
  const loaded = ref(false)

  const total = computed(() => navItems.value.length)

  async function loadResumes(force = false): Promise<void> {
    if (loaded.value && !force) return
    loading.value = true
    try {
      const snapshot = await resumeService.fetchResumeSnapshot()
      resumes.value = snapshot.resumes
      navItems.value = snapshot.navItems
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  function getResume(id: string): ResumeData | undefined {
    return resumes.value[id]
  }

  function getNavItem(id: string): ResumeNavItem | undefined {
    return navItems.value.find((item) => item.id === id)
  }

  async function saveResume(id: string, data: ResumeData): Promise<void> {
    resumes.value[id] = JSON.parse(JSON.stringify(data)) as ResumeData
    const item = navItems.value.find((nav) => nav.id === id)
    if (item) {
      item.label = data.basicInfo.title || data.basicInfo.name || '未命名简历'
    }
    await resumeService.saveResume(id, data)
  }

  async function createResume(data: ResumeData, label: string): Promise<ResumeNavItem> {
    const item = await resumeService.createResume(data, label)
    resumes.value[item.id] = JSON.parse(JSON.stringify(data)) as ResumeData
    navItems.value.push(item)
    return item
  }

  async function removeResume(id: string): Promise<void> {
    const ok = await resumeService.deleteResume(id)
    if (!ok) return
    delete resumes.value[id]
    navItems.value = navItems.value.filter((item) => item.id !== id)
  }

  function createEmptyResume(index: number): ResumeData {
    return {
      basicInfo: {
        name: '新简历',
        title: `求职意向 ${index}`,
        phone: '',
        email: ''
      },
      education: { school: '', major: '', period: '' },
      summary: [''],
      campus: [{ period: '', description: '' }],
      skills: [''],
      internships: [{ company: '', position: '', period: '', duties: [''] }],
      projects: [{ name: '', techStack: '', description: '', duties: [''] }],
      projectDutyTitle: '工作职责与成果'
    }
  }

  return {
    resumes,
    navItems,
    loading,
    loaded,
    total,
    loadResumes,
    getResume,
    getNavItem,
    saveResume,
    createResume,
    removeResume,
    createEmptyResume
  }
})
