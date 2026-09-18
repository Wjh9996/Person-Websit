import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { ResumeData, ResumeNavItem } from '@/types/resume'
import * as resumeService from '@/services/resumeService'
import { useUserStore } from '@/stores/useUserStore'

/**
 * 简历状态：多份简历的导航项与内容，本地持久化
 */
export const useResumeStore = defineStore('resume', () => {
  const userStore = useUserStore()
  const resumes = ref<Record<string, ResumeData>>({})
  const navItems = ref<ResumeNavItem[]>([])
  const loading = ref(false)
  /** 已加载标记，避免 App 与页面组件重复请求 */
  const loaded = ref(false)
  /**
   * 记录本次数据是按哪个身份加载的：'guest'（匿名公开模板）或某个用户 id（登录用户本人）。
   * 关键是记**用户 id 而不是登录与否** —— 否则同一浏览器 A 退出、B 登录时
   * 两边都算 'user'，缓存不失效，A 的简历就会串到 B 的页面上。
   */
  const loadedFor = ref<string | null>(null)

  const total = computed(() => navItems.value.length)

  async function loadResumes(force = false): Promise<void> {
    const identity = userStore.profile?.id ?? 'guest'
    if (loaded.value && !force && loadedFor.value === identity) return
    loading.value = true
    try {
      const snapshot = await resumeService.fetchResumeSnapshot()
      resumes.value = snapshot.resumes
      navItems.value = snapshot.navItems
      loaded.value = true
      loadedFor.value = identity
    } finally {
      loading.value = false
    }
  }

  // 账号切换（含登录/登出）自动重载，保证模板与本人数据不串
  watch(
    () => userStore.profile?.id ?? 'guest',
    () => {
      void loadResumes(true)
    }
  )

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
