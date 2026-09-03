<template>
  <div class="detail-page">
    <div class="page-inner">
      <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>

      <!-- 返回 -->
      <router-link to="/notes" class="back-link">
        <span>←</span> 返回笔记列表
      </router-link>

      <!-- 加载中 -->
      <div v-if="!note" class="empty-block">
        <p class="empty-icon">📄</p>
        <h3>笔记不存在或已被删除</h3>
        <router-link to="/notes" class="primary-btn">回到笔记列表</router-link>
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
          </div>

          <h1 class="article-title">{{ note.title }}</h1>

          <div class="article-meta">
            <span>🕒 更新于 {{ formatDateTime(note.updatedAt) }}</span>
            <span>📖 约 {{ readingTime(note.content) }} 分钟</span>
            <span>👁 {{ note.views }} 次阅读</span>
          </div>

          <div v-if="note.tags.length" class="article-tags">
            <span v-for="tag in note.tags" :key="tag" class="tag-item"># {{ tag }}</span>
          </div>

          <div class="article-actions">
            <!-- 已登录：完整管理操作 -->
            <template v-if="userStore.isLogin">
              <button class="action-btn" type="button" @click="togglePin">
                {{ note.pinned ? '取消置顶' : '📌 置顶' }}
              </button>
              <button class="action-btn" type="button" @click="goEdit">✏️ 编辑</button>
              <button class="action-btn danger" type="button" @click="handleDelete">🗑 删除</button>
            </template>

            <!-- 未登录：只保留一个编辑入口，点击后提示需要登录 -->
            <button v-else class="action-btn" type="button" @click="goEdit">🔒 编辑</button>
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

const route = useRoute()
const router = useRouter()
const noteStore = useNoteStore()
const userStore = useUserStore()
const { requireLogin } = useAuthGuard()

const ready = ref(false)
const errorMsg = ref('')

const noteId = computed(() => route.params.id as string)
const note = computed(() => noteStore.getNoteById(noteId.value))

const categoryLabel = computed(() => (note.value ? getCategoryLabel(note.value.category) : ''))
const categoryIcon = computed(() => (note.value ? getCategoryIcon(note.value.category) : ''))

const toc = computed(() => (note.value ? renderMarkdown(note.value.content).toc : []))

/** 同分类下按更新时间排序的相邻笔记 */
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
  try {
    if (!noteStore.loaded) await noteStore.loadNotes()
    // 列表不含正文，必须先拉取完整笔记（含 content）再渲染
    const full = await noteStore.fetchNoteDetail(noteId.value)
    if (full) {
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
