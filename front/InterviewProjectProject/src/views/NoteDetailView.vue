<template>
  <div class="detail-page">
    <div class="page-inner">
      <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>

      <!-- 返回：广场笔记回广场，私有笔记回「我的笔记」 -->
      <router-link :to="backPath" class="back-link">
        <span>←</span> {{ backText }}
      </router-link>

      <!-- 加载中 -->
      <div v-if="!loadingDone" class="empty-block">
        <p class="empty-icon">⏳</p>
        <h3>正在加载笔记…</h3>
      </div>

      <!-- 加载失败 / 无权访问：不要展示任何正文 -->
      <div v-else-if="errorMsg" class="empty-block">
        <p class="empty-icon">🔒</p>
        <h3>无法查看这篇笔记</h3>
        <p>{{ errorMsg }}</p>
        <router-link to="/plaza" class="primary-btn">去讨论广场逛逛</router-link>
      </div>

      <div v-else-if="!note" class="empty-block">
        <p class="empty-icon">📄</p>
        <h3>笔记不存在或已被删除</h3>
        <router-link to="/plaza" class="primary-btn">回到笔记列表</router-link>
      </div>

      <template v-else>
        <!-- 文章头部 -->
        <header class="article-header">
          <div class="header-top">
            <span class="article-category">
              <span>{{ categoryIcon }}</span>
              {{ categoryLabel }}
            </span>
            <span v-if="note.pinned" class="article-pin">📌 置顶</span>
            <span v-if="isPublic" class="article-public">🌐 已发布广场</span>
            <span v-else class="article-private">🔒 私享笔记</span>
          </div>

          <h1 class="article-title">{{ note.title }}</h1>

          <div class="article-meta">
            <span v-if="note.authorName && !isOwner">✍️ {{ note.authorName }}</span>
            <span v-else-if="isOwner">✍️ 我</span>
            <span>🕒 更新于 {{ formatDateTime(note.updatedAt) }}</span>
            <span>📖 约 {{ readingTime(note.content) }} 分钟</span>
            <span>👁 {{ note.views }} 次阅读</span>
          </div>

          <div v-if="note.tags.length" class="article-tags">
            <span v-for="tag in note.tags" :key="tag" class="tag-item"># {{ tag }}</span>
          </div>

          <div class="article-actions">
            <!-- 智能分析入口：未登录点击弹登录提示，登录后触发大模型分析 -->
            <button class="action-btn ai-btn" type="button" @click="analyzeNote">✨ AI 分析</button>
            <!-- 仅作者本人可见管理操作：置顶 / 广场可见性 / 编辑 / 删除 -->
            <template v-if="isOwner">
              <button class="action-btn" type="button" @click="togglePin">
                {{ note.pinned ? '取消置顶' : '📌 置顶' }}
              </button>
              <button class="action-btn" type="button" @click="togglePlazaShare">
                {{ isPublic ? '🔒 从广场收回' : '🌐 发布到广场' }}
              </button>
              <button class="action-btn" type="button" @click="goEdit">✏️ 编辑</button>
              <button class="action-btn danger" type="button" @click="handleDelete">🗑 删除</button>
            </template>
          </div>
        </header>

        <!-- 正文 + 目录 -->
        <div class="article-layout">
          <article class="article-body">
            <MarkdownRendererComponent :source="note.content" />
          </article>

          <aside class="article-aside">
            <NoteTocComponent :items="toc" />
          </aside>
        </div>

        <!-- 智能分析面板 -->
        <section class="analysis-panel">
          <div class="panel-head">
            <h2>✨ 智能笔记分析</h2>
            <span class="panel-sub">由大模型自动提炼，仅供参考</span>
          </div>

          <div v-if="analyzing" class="panel-loading">
            <span class="spinner"></span> AI 正在分析中，请稍候…
          </div>

          <div v-else-if="analysisError" class="panel-error">
            <p>{{ analysisError }}</p>
            <button class="action-btn" type="button" @click="analyzeNote">重试</button>
          </div>

          <div v-else-if="analysis" class="panel-result">
            <div v-if="analysis.summary" class="result-block">
              <h3>📝 内容摘要</h3>
              <p>{{ analysis.summary }}</p>
            </div>

            <div v-if="analysis.keywords && analysis.keywords.length" class="result-block">
              <h3>🏷 关键词</h3>
              <div class="kw-list">
                <span v-for="kw in analysis.keywords" :key="kw" class="kw-item">{{ kw }}</span>
              </div>
            </div>

            <div v-if="analysis.category" class="result-block">
              <h3>🗂 分类建议</h3>
              <p>
                <span class="cat-tag">{{ analysis.category }}</span>
                <span v-if="analysis.categoryReason" class="cat-reason">{{ analysis.categoryReason }}</span>
              </p>
            </div>

            <div v-if="analysis.knowledge && analysis.knowledge.length" class="result-block">
              <h3>💡 核心知识点</h3>
              <ul class="knowledge-list">
                <li v-for="(p, i) in analysis.knowledge" :key="i">{{ p }}</li>
              </ul>
            </div>
          </div>

          <p v-else class="panel-hint">
            点击上方「✨ AI 分析」按钮，用 AI 为你提炼摘要、关键词与核心知识点。
          </p>
        </section>

        <!-- 上下篇 -->
        <nav v-if="prevNote || nextNote" class="article-nav">
          <router-link v-if="prevNote" :to="`/notes/${prevNote.id}`" class="nav-card prev">
            <span class="nav-label">← 上一篇</span>
            <span class="nav-title">{{ prevNote.title }}</span>
          </router-link>
          <span v-else class="nav-placeholder"></span>

          <router-link v-if="nextNote" :to="`/notes/${nextNote.id}`" class="nav-card next">
            <span class="nav-label">下一篇 →</span>
            <span class="nav-title">{{ nextNote.title }}</span>
          </router-link>
        </nav>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownRendererComponent from '@/components/notes/MarkdownRendererComponent.vue'
import NoteTocComponent from '@/components/notes/NoteTocComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'
import { useUserStore } from '@/stores/useUserStore'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { getCategoryIcon, getCategoryLabel } from '@/data/noteCategories'
import { renderMarkdown, readingTime } from '@/utils/markdown'
import { formatDateTime } from '@/utils/datetime'
import { getErrorMessage } from '@/utils/errorMessage'
import { triggerAnalysis, getAnalysis, getTaskStatus } from '@/services/analysisService'
import type { AnalysisAggregate } from '@/types/analysis'
import type { Note } from '@/types/note'

const route = useRoute()
const router = useRouter()
const noteStore = useNoteStore()
const userStore = useUserStore()
const { requireLogin } = useAuthGuard()

const ready = ref(false)
const errorMsg = ref('')
/** 当前笔记正文数据（始终以接口详情为准，避免依赖列表缓存导致串数据） */
const note = ref<Note | undefined>(undefined)

const noteId = computed(() => route.params.id as string)

/** 是否作者本人：决定能否编辑/删除/改可见性 */
const isOwner = computed(() => {
  const authorId = note.value?.authorId
  return Boolean(authorId) && authorId === userStore.profile?.id
})
const isPublic = computed(() => note.value?.visibility === 1)

const backPath = computed(() => (isPublic.value ? '/plaza' : '/notes'))
const backText = computed(() => (isPublic.value ? '返回讨论广场' : '返回我的笔记'))
/** 首次加载完成前显示 loading，避免闪烁「不存在」 */
const loadingDone = computed(() => ready.value)

const categoryLabel = computed(() => (note.value ? getCategoryLabel(note.value.category) : ''))
const categoryIcon = computed(() => (note.value ? getCategoryIcon(note.value.category) : ''))

const toc = computed(() => (note.value ? renderMarkdown(note.value.content).toc : []))

/** 同分类下按更新时间排序的相邻笔记（沿用当前 scope 列表） */
const siblings = computed(() =>
  noteStore.notes
    .filter((item) => item.category === note.value?.category)
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
)

const prevNote = computed(() => {
  const index = siblings.value.findIndex((item) => item.id === noteId.value)
  return index > 0 ? siblings.value[index - 1] : null
})

const nextNote = computed(() => {
  const index = siblings.value.findIndex((item) => item.id === noteId.value)
  return index >= 0 && index < siblings.value.length - 1 ? siblings.value[index + 1] : null
})

async function ensureNote(): Promise<void> {
  ready.value = false
  errorMsg.value = ''
  note.value = undefined
  analysis.value = null
  analysisError.value = ''
  try {
    // 详情接口自带可见性校验：私有笔记非作者会返回 403，这里转成页面提示
    const full = await noteStore.fetchNoteDetail(noteId.value)
    if (full) {
      note.value = full
      // 列表用于「上下篇」导航，缓存命中即返回，不阻塞正文渲染
      void noteStore.loadNotes()
      // 阅读量 +1 失败不影响阅读，单独吞掉异常
      try {
        await noteStore.increaseViews(noteId.value)
      } catch {
        /* noop */
      }
    }
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  } finally {
    ready.value = true
  }
}

onMounted(ensureNote)
watch(noteId, ensureNote)

function goEdit(): void {
  const allowed = requireLogin({
    message: '编辑笔记需要先登录，登录后即可修改这篇笔记。',
    redirect: `/notes/${noteId.value}/edit`
  })
  if (!allowed) return
  void router.push(`/notes/${noteId.value}/edit`)
}

/** 发布到讨论广场 / 从广场收回（作者本人操作） */
async function togglePlazaShare(): Promise<void> {
  if (!note.value || !isOwner.value) return
  const toPlaza = !isPublic.value
  errorMsg.value = ''
  try {
    await noteStore.togglePlaza(noteId.value, toPlaza)
    const refreshed = noteStore.getNoteById(noteId.value)
    if (refreshed) note.value = refreshed
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

async function togglePin(): Promise<void> {
  if (!note.value) return
  errorMsg.value = ''
  try {
    // store.togglePinned 已就地更新 notes 中该条目的 pinned（响应式），无需取返回值
    await noteStore.togglePinned(noteId.value)
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

async function handleDelete(): Promise<void> {
  if (!note.value) return
  if (!window.confirm(`确定删除笔记「${note.value.title}」吗？此操作不可恢复。`)) return
  errorMsg.value = ''
  try {
    await noteStore.removeNote(noteId.value)
    void router.push('/notes')
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

// ===================== 智能笔记分析 =====================
const analyzing = ref(false)
const analysis = ref<AnalysisAggregate | null>(null)
const analysisError = ref('')

/** 入口：未登录先弹登录提示，不发起请求 */
function analyzeNote(): void {
  const allowed = requireLogin({
    message: '分析笔记需要先登录，登录后即可用 AI 为你提炼要点。'
  })
  if (!allowed) return
  void runAnalysis()
}

async function runAnalysis(): Promise<void> {
  if (!note.value) return
  analyzing.value = true
  analysisError.value = ''
  try {
    const { taskId } = await triggerAnalysis(noteId.value)
    const ok = await pollTask(taskId)
    if (ok) {
      analysis.value = await getAnalysis(noteId.value)
    }
  } catch (e) {
    analysisError.value = getErrorMessage(e)
  } finally {
    analyzing.value = false
  }
}

/** 轮询任务状态，最多约 60s（1.5s × 40） */
async function pollTask(taskId: string): Promise<boolean> {
  for (let i = 0; i < 40; i++) {
    const status = await getTaskStatus(taskId)
    if (status.status === 'success') return true
    if (status.status === 'failed') {
      analysisError.value = status.errorMsg || '分析失败，请重试'
      return false
    }
    await new Promise((resolve) => setTimeout(resolve, 1500))
  }
  analysisError.value = '分析超时，请稍后重试'
  return false
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
}

.page-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 24px 64px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  margin-bottom: 18px;
  border-radius: 9px;
  background: #fff;
  border: 1px solid #e8edf3;
  color: #64748b;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.16s, color 0.16s;
}

.back-link:hover {
  background: #eff6ff;
  color: #2563eb;
  border-color: #bfdbfe;
}

/* ========== 文章头部 ========== */
.article-header {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 20px;
}

.header-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.article-category {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 11px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 13px;
  font-weight: 500;
}

.article-pin {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fef3c7;
  color: #d97706;
  font-size: 12.5px;
}

.article-public {
  padding: 4px 10px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #047857;
  font-size: 12.5px;
}

.article-private {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 12.5px;
}

.article-title {
  margin: 0 0 14px;
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
  letter-spacing: -0.4px;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13.5px;
  color: #94a3b8;
  margin-bottom: 14px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 4px 11px;
  border-radius: 7px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12.5px;
}

.article-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #f1f5f9;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  background: #f8fafc;
  color: #334155;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.16s;
}

.action-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #2563eb;
}

.action-btn.ai-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
  color: #fff;
}

.action-btn.ai-btn:hover {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
}

.action-btn.danger {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fff5f5;
}

.action-btn.danger:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #991b1b;
}

/* ========== 正文布局 ========== */
.article-layout {
  display: grid;
  grid-template-columns: 1fr 216px;
  gap: 24px;
  align-items: start;
}

.article-body {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 32px 36px;
  min-width: 0;
}

.article-aside {
  min-width: 0;
}

/* ========== 智能分析面板 ========== */
.analysis-panel {
  margin-top: 24px;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 24px 28px;
}

.panel-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.panel-sub {
  font-size: 12.5px;
  color: #94a3b8;
}

.panel-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6366f1;
  font-size: 14px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #c7d2fe;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.panel-error {
  color: #b91c1c;
  font-size: 14px;
}

.panel-error .action-btn {
  margin-top: 10px;
}

.panel-result .result-block {
  margin-bottom: 18px;
}

.panel-result h3 {
  margin: 0 0 8px;
  font-size: 14.5px;
  color: #334155;
}

.panel-result p {
  margin: 0;
  color: #475569;
  line-height: 1.7;
  font-size: 14px;
}

.kw-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kw-item {
  padding: 4px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 13px;
}

.cat-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 7px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 13px;
  font-weight: 500;
}

.cat-reason {
  margin-left: 8px;
  color: #94a3b8;
  font-size: 13px;
}

.knowledge-list {
  margin: 0;
  padding-left: 20px;
  color: #475569;
  line-height: 1.8;
  font-size: 14px;
}

.panel-hint {
  margin: 0;
  color: #94a3b8;
  font-size: 13.5px;
}

@media (prefers-reduced-motion: reduce) {
  .spinner { animation: none; }
}

/* ========== 上下篇 ========== */
.article-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 24px;
}

.nav-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  text-decoration: none;
  transition: border-color 0.18s, box-shadow 0.18s, transform 0.18s;
}

.nav-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.07);
  transform: translateY(-2px);
}

.nav-card.next {
  text-align: right;
}

.nav-label {
  font-size: 12.5px;
  color: #94a3b8;
}

.nav-title {
  font-size: 14.5px;
  color: #0f172a;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-placeholder {
  display: block;
}

/* ========== 空状态 ========== */
.empty-block {
  padding: 72px 24px;
  text-align: center;
  background: #fff;
  border-radius: 14px;
  border: 1px dashed #dbe3ec;
}

.empty-icon {
  font-size: 44px;
  margin: 0 0 12px;
}

.empty-block h3 {
  margin: 0 0 18px;
  font-size: 17px;
  color: #0f172a;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-size: 14.5px;
  text-decoration: none;
  transition: background 0.18s;
}

.primary-btn:hover {
  background: #1d4ed8;
}

.error-tip {
  margin: 0 0 16px;
  padding: 10px 14px;
  border-radius: 9px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13.5px;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .article-layout {
    grid-template-columns: 1fr;
  }

  .article-aside {
    order: -1;
  }
}

@media (max-width: 560px) {
  .page-inner {
    padding: 16px 16px 48px;
  }

  .article-header {
    padding: 22px 20px;
  }

  .article-title {
    font-size: 23px;
  }

  .article-body {
    padding: 22px 18px;
  }

  .article-nav {
    grid-template-columns: 1fr;
  }

  .nav-card.next {
    text-align: left;
  }
}
</style>
