<template>
  <div class="resume-page">
    <ResumeNav :items="resumeStore.navItems" />

    <section class="resume-actions">
      <div>
        <p class="action-label">当前简历</p>
        <h2>{{ currentItem?.label || '暂无简历' }}</h2>
      </div>
      <div class="action-buttons">
        <button type="button" class="action-btn primary" @click="createResume">新增简历</button>
        <button type="button" class="action-btn" :disabled="!currentResume" @click="openEditor">
          编辑简历
        </button>
        <button
          type="button"
          class="action-btn danger"
          :disabled="!currentResume"
          @click="deleteResume"
        >
          删除简历
        </button>
      </div>
    </section>

    <RenameComponent v-if="currentResume" :data="currentResume" />
    <div v-else class="empty-state">
      <h2>还没有简历</h2>
      <p>点击“新增简历”创建第一份简历。</p>
    </div>

    <ResumeEditorComponent
      v-if="draftResume"
      v-model:visible="editorVisible"
      :resume-data="draftResume"
      @save="saveResume"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import RenameComponent from '@/components/resume/RenameComponent.vue'
import ResumeEditorComponent from '@/components/resume/ResumeEditorComponent.vue'
import ResumeNav from '@/components/resume/ResumeNav.vue'
import { useResumeStore } from '@/stores/useResumeStore'
import type { ResumeData } from '@/types/resume'

const route = useRoute()
const router = useRouter()
const resumeStore = useResumeStore()

const editorVisible = ref(false)
const editingId = ref<string | null>(null)
const draftResume = ref<ResumeData | null>(null)

const currentType = computed(() => route.params.type as string)
const currentResume = computed(() => resumeStore.getResume(currentType.value))
const currentItem = computed(() => resumeStore.getNavItem(currentType.value))

onMounted(() => {
  void resumeStore.loadResumes()
})

// 数据加载完成后，若当前路径没有对应简历则回退到第一份
watch(
  [() => route.params.type, () => resumeStore.loaded],
  () => {
    if (!resumeStore.loaded) return
    const firstItem = resumeStore.navItems[0]
    if (firstItem && !currentResume.value) {
      void router.replace(firstItem.path)
    }
  },
  { immediate: true }
)

function cloneResume(resume: ResumeData): ResumeData {
  return JSON.parse(JSON.stringify(resume)) as ResumeData
}

function createResume(): void {
  const index = resumeStore.navItems.length + 1
  const resume = resumeStore.createEmptyResume(index)

  void resumeStore.createResume(resume, `新简历 ${index}`).then((item) => {
    editingId.value = item.id
    draftResume.value = cloneResume(resume)
    editorVisible.value = true
    void router.push(item.path)
  })
}

function openEditor(): void {
  if (!currentResume.value) return
  editingId.value = currentType.value
  draftResume.value = cloneResume(currentResume.value)
  editorVisible.value = true
}

async function saveResume(updatedResume: ResumeData): Promise<void> {
  if (!editingId.value) return
  await resumeStore.saveResume(editingId.value, updatedResume)
  editorVisible.value = false
  draftResume.value = null
  editingId.value = null
}

async function deleteResume(): Promise<void> {
  const id = currentType.value
  if (!resumeStore.getResume(id)) return

  if (!window.confirm('确定删除这份简历吗？此操作不可恢复。')) return

  const removedIndex = resumeStore.navItems.findIndex((item) => item.id === id)
  await resumeStore.removeResume(id)

  const nextItem = resumeStore.navItems[removedIndex] ?? resumeStore.navItems[removedIndex - 1]
  if (nextItem) {
    void router.replace(nextItem.path)
  }
}
</script>

<style scoped>
.resume-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 40px;
  min-height: 100vh;
  width: 100%;
}

.resume-actions {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.action-label {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 13px;
}

.resume-actions h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #334155;
  border-radius: 10px;
  padding: 9px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, color 0.2s;
}

.action-btn:hover:not(:disabled) {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #2563eb;
}

.action-btn.primary {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.action-btn.primary:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
  color: #ffffff;
}

.action-btn.danger {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fff5f5;
}

.action-btn.danger:hover:not(:disabled) {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #991b1b;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.empty-state {
  width: 100%;
  max-width: 1200px;
  padding: 56px 24px;
  text-align: center;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

.empty-state h2 {
  margin: 0 0 8px;
  color: #0f172a;
}

.empty-state p {
  margin: 0;
  color: #64748b;
}

@media (max-width: 640px) {
  .resume-actions {
    align-items: stretch;
  }

  .action-buttons,
  .action-btn {
    width: 100%;
  }
}
</style>
