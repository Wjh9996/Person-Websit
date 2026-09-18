<template>
  <div class="notes-page">
    <div class="page-inner">
      <!-- 页面头部 -->
      <header class="notes-hero">
        <div class="hero-text">
          <h1>{{ heading }}</h1>
          <p>{{ subtitle }}，共 {{ noteStore.notes.length }} 篇</p>
        </div>
        <button class="primary-btn" type="button" @click="goCreate">
          <span>✍️</span> 写笔记
        </button>
      </header>

      <!-- 搜索 -->
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input
          v-model="noteStore.keyword"
          type="search"
          placeholder="搜索笔记标题、摘要或正文…"
        />
        <button
          v-if="hasFilter"
          class="clear-btn"
          type="button"
          @click="noteStore.resetFilters()"
        >
          清空筛选
        </button>
      </div>

      <div class="notes-layout">
        <!-- 侧边栏：分类 + 标签 -->
        <aside class="notes-sidebar">
          <div class="sidebar-block">
            <h3 class="sidebar-title">分类</h3>
            <ul class="category-list">
              <li>
                <button
                  type="button"
                  class="category-item"
                  :class="{ active: noteStore.activeCategory === 'all' }"
                  @click="noteStore.activeCategory = 'all'"
                >
                  <span class="cat-icon">🗂️</span>
                  <span class="cat-label">全部</span>
                  <span class="cat-count">{{ noteStore.categoryCounts.all ?? 0 }}</span>
                </button>
              </li>
              <li v-for="cat in noteStore.categories" :key="cat.id">
                <button
                  type="button"
                  class="category-item"
                  :class="{ active: noteStore.activeCategory === cat.id }"
                  @click="noteStore.activeCategory = cat.id"
                >
                  <span class="cat-icon">{{ cat.icon }}</span>
                  <span class="cat-label">{{ cat.label }}</span>
                  <span class="cat-count">{{ noteStore.categoryCounts[cat.id] ?? 0 }}</span>
                </button>
              </li>
            </ul>
          </div>

          <div class="sidebar-block">
            <h3 class="sidebar-title">标签</h3>
            <div v-if="noteStore.tags.length" class="tag-cloud">
              <button
                v-for="item in noteStore.tags"
                :key="item.tag"
                type="button"
                class="tag-chip"
                :class="{ active: noteStore.activeTag === item.tag }"
                @click="toggleTag(item.tag)"
              >
                {{ item.tag }}
                <span class="tag-count">{{ item.count }}</span>
              </button>
            </div>
            <p v-else class="sidebar-empty">暂无标签</p>
          </div>
        </aside>

        <!-- 笔记列表 -->
        <section class="notes-main">
          <div class="result-bar">
            <span>
              共 <strong>{{ noteStore.filteredNotes.length }}</strong> 篇
              <em v-if="activeFilterText">（{{ activeFilterText }}）</em>
            </span>
          </div>

          <div v-if="noteStore.loading" class="skeleton-list">
            <div v-for="i in 6" :key="i" class="skeleton-card"></div>
          </div>

          <div v-else-if="noteStore.filteredNotes.length" class="note-grid">
            <NoteCardComponent
              v-for="note in noteStore.filteredNotes"
              :key="note.id"
              :note="note"
              :show-author="showAuthor"
            />
          </div>

          <div v-else class="empty-block">
            <p class="empty-icon">{{ hasFilter ? '🔍' : emptyIcon }}</p>
            <h3>{{ hasFilter ? '没有找到匹配的笔记' : emptyTitle }}</h3>
            <p>{{ hasFilter ? '试试更换关键词，或清空筛选条件' : emptyDesc }}</p>
            <button v-if="hasFilter" class="primary-btn" type="button" @click="noteStore.resetFilters()">
              清空筛选
            </button>
            <router-link v-else-if="writeLink" :to="writeLink" class="primary-btn">
              写第一篇笔记
            </router-link>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import NoteCardComponent from '@/components/notes/NoteCardComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'
import { useAuthGuard } from '@/composables/useAuthGuard'

/**
 * 笔记列表面板（「我的笔记」与「讨论广场」共用同一套布局与筛选逻辑）。
 * 展示哪份数据由 store 的 scope 决定，父级视图在进入时调用 setScope。
 */
withDefaults(
  defineProps<{
    /** 页面大标题，如「📝 我的笔记」 */
    heading: string
    /** 副标题，后面会自动拼上笔记总数 */
    subtitle: string
    /** 是否显示作者昵称（广场需要） */
    showAuthor?: boolean
    /** 空状态插画 emoji（无任何笔记时） */
    emptyIcon?: string
    emptyTitle?: string
    emptyDesc?: string
    /** 无任何笔记时的 CTA 链接（走 /notes/create，路由守卫负责拦截未登录） */
    writeLink?: string
  }>(),
  {
    showAuthor: false,
    emptyIcon: '📝',
    emptyTitle: '这里还没有笔记',
    emptyDesc: '写下第一篇笔记吧',
    writeLink: ''
  }
)

const noteStore = useNoteStore()
const router = useRouter()
const { requireLogin } = useAuthGuard()

const hasFilter = computed(
  () =>
    Boolean(noteStore.keyword.trim()) ||
    noteStore.activeCategory !== 'all' ||
    Boolean(noteStore.activeTag)
)

const activeFilterText = computed(() => {
  const parts: string[] = []
  if (noteStore.activeCategory !== 'all') {
    const cat = noteStore.categories.find((item) => item.id === noteStore.activeCategory)
    if (cat) parts.push(cat.label)
  }
  if (noteStore.activeTag) parts.push(`#${noteStore.activeTag}`)
  if (noteStore.keyword.trim()) parts.push(`“${noteStore.keyword.trim()}”`)
  return parts.join(' · ')
})

function toggleTag(tag: string): void {
  noteStore.activeTag = noteStore.activeTag === tag ? '' : tag
}

function goCreate(): void {
  const allowed = requireLogin({
    message: '写笔记需要先登录，登录后即可开始创作。',
    redirect: '/notes/create'
  })
  if (!allowed) return
  void router.push('/notes/create')
}
</script>

<style scoped>
.notes-page {
  min-height: 100vh;
}

.page-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}

/* ========== 头部 ========== */
.notes-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.hero-text h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.3px;
}

.hero-text p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-size: 14.5px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.18s, box-shadow 0.18s;
}

.primary-btn:hover {
  background: #1d4ed8;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.28);
}

/* ========== 搜索栏 ========== */
.search-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #e8edf3;
  border-radius: 12px;
  padding: 0 14px;
  margin-bottom: 24px;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.search-bar:focus-within {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}

.search-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  padding: 13px 0;
  font-size: 15px;
  color: #0f172a;
  background: transparent;
  min-width: 0;
}

.search-bar input::placeholder {
  color: #94a3b8;
}

.clear-btn {
  flex-shrink: 0;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #64748b;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.16s, color 0.16s;
}

.clear-btn:hover {
  background: #eff6ff;
  color: #2563eb;
  border-color: #bfdbfe;
}

/* ========== 主体布局 ========== */
.notes-layout {
  display: grid;
  grid-template-columns: 232px 1fr;
  gap: 28px;
  align-items: start;
}

/* ========== 侧边栏 ========== */
.notes-sidebar {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-block {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 16px;
}

.sidebar-title {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.sidebar-empty {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

.category-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 10px;
  border: none;
  border-radius: 9px;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
  text-align: left;
  transition: background 0.16s, color 0.16s;
}

.category-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.category-item.active {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 500;
}

.cat-icon {
  font-size: 14px;
}

.cat-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-count {
  font-size: 12.5px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 1px 7px;
  border-radius: 999px;
}

.category-item.active .cat-count {
  background: #dbeafe;
  color: #2563eb;
}

/* 标签云 */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border: 1px solid #e8edf3;
  border-radius: 999px;
  background: #fff;
  font-size: 12.5px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.16s;
}

.tag-chip:hover {
  border-color: #bfdbfe;
  color: #2563eb;
  background: #eff6ff;
}

.tag-chip.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

.tag-count {
  font-size: 11px;
  opacity: 0.75;
}

/* ========== 列表区 ========== */
.result-bar {
  margin-bottom: 14px;
  font-size: 14px;
  color: #64748b;
}

.result-bar strong {
  color: #0f172a;
}

.result-bar em {
  font-style: normal;
  color: #94a3b8;
}

.note-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 18px;
}

.empty-block {
  padding: 64px 24px;
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
  margin: 0 0 8px;
  font-size: 17px;
  color: #0f172a;
  font-weight: 600;
}

.empty-block p {
  margin: 0 0 18px;
  color: #64748b;
  font-size: 14px;
}

/* ========== 骨架屏 ========== */
.skeleton-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 18px;
}

.skeleton-card {
  height: 176px;
  border-radius: 14px;
  background: linear-gradient(100deg, #eef2f7 30%, #f7f9fc 50%, #eef2f7 70%);
  background-size: 220% 100%;
  animation: list-skeleton 1.3s ease-in-out infinite;
}

@keyframes list-skeleton {
  from {
    background-position: 120% 0;
  }
  to {
    background-position: -20% 0;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .notes-layout {
    grid-template-columns: 1fr;
  }

  .notes-sidebar {
    position: static;
    flex-direction: row;
    gap: 14px;
    overflow-x: auto;
  }

  .sidebar-block {
    min-width: 260px;
  }
}

@media (max-width: 560px) {
  .page-inner {
    padding: 20px 16px 48px;
  }

  .hero-text h1 {
    font-size: 24px;
  }

  .notes-hero {
    align-items: flex-start;
  }

  .note-grid {
    grid-template-columns: 1fr;
  }
}
</style>
