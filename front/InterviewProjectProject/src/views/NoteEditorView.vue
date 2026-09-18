<template>
  <div class="editor-page">
    <div class="page-inner">
      <!-- 顶部工具栏 -->
      <header class="editor-header">
        <div class="header-left">
          <router-link to="/notes" class="back-link"><span>←</span></router-link>
          <input
            v-model="form.title"
            class="title-input"
            type="text"
            placeholder="给笔记起个标题…"
          />
        </div>

        <div class="header-right">
          <button class="tool-btn" type="button" @click="saveDraft">保存草稿</button>
          <button class="primary-btn" type="button" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中…' : saveLabel }}
          </button>
        </div>
      </header>

      <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>

      <div class="editor-body">
        <!-- 编辑区 -->
        <section class="pane" :class="{ hidden: mobileTab === 'preview' }">
          <div class="pane-header">
            <span class="pane-title">Markdown 编辑</span>
            <div class="md-tools">
              <button type="button" title="粗体" @click="wrap('**', '**')"><b>B</b></button>
              <button type="button" title="斜体" @click="wrap('*', '*')"><i>I</i></button>
              <button type="button" title="标题" @click="prefixLine('## ')">H2</button>
              <button type="button" title="无序列表" @click="prefixLine('- ')">•</button>
              <button type="button" title="引用" @click="prefixLine('> ')">❝</button>
              <button type="button" title="行内代码" @click="wrap('`', '`')">&lt;/&gt;</button>
              <button type="button" title="代码块" @click="insertBlock">{}</button>
              <button type="button" title="链接" @click="wrap('[', '](https://)')">🔗</button>
              <button type="button" title="表格" @click="insertTable">▦</button>
            </div>
          </div>

          <textarea
            ref="textareaRef"
            v-model="form.content"
            class="md-textarea"
            placeholder="用 Markdown 记录你的想法…&#10;&#10;支持标题、列表、代码块、表格、引用等语法"
            @keydown="handleTab"
          ></textarea>
        </section>

        <!-- 预览区 -->
        <section class="pane" :class="{ hidden: mobileTab === 'edit' }">
          <div class="pane-header">
            <span class="pane-title">实时预览</span>
            <span class="pane-meta">{{ form.content.length }} 字 · 约 {{ readingTime(form.content) }} 分钟</span>
          </div>
          <div class="preview-body">
            <h1 v-if="form.title" class="preview-title">{{ form.title }}</h1>
            <MarkdownRendererComponent :source="form.content || '（暂无内容）'" />
          </div>
        </section>
      </div>

      <!-- 元信息 -->
      <aside class="meta-panel">
        <div class="meta-field">
          <label>分类</label>
          <select v-model="form.category">
            <option v-for="cat in noteStore.categories" :key="cat.id" :value="cat.id">
              {{ cat.icon }} {{ cat.label }}
            </option>
          </select>
        </div>

        <div class="meta-field">
          <label>标签</label>
          <div class="tag-input-wrap">
            <span v-for="tag in form.tags" :key="tag" class="tag-pill">
              {{ tag }}
              <button type="button" @click="removeTag(tag)">✕</button>
            </span>
            <input
              v-model="tagInput"
              type="text"
              placeholder="回车添加标签"
              @keydown.enter.prevent="addTag"
              @keydown.backspace="removeLastTag"
            />
          </div>
        </div>

        <div class="meta-field">
          <label>摘要 <em>（留空自动截取正文）</em></label>
          <textarea v-model="form.summary" rows="3" placeholder="一句话概括这篇笔记…"></textarea>
        </div>

        <div class="meta-field checkbox-field">
          <label class="checkbox-label">
            <input v-model="form.pinned" type="checkbox" />
            <span>置顶到列表顶部</span>
          </label>
        </div>

        <div class="meta-field checkbox-field">
          <label class="checkbox-label">
            <input v-model="form.toPlaza" type="checkbox" />
            <span>发布到讨论广场（所有人可见）</span>
          </label>
          <p class="field-hint">
            {{ form.toPlaza ? '保存后所有访客都能在广场看到这篇笔记。' : '默认私有：只出现在「我的笔记」中。' }}
          </p>
        </div>
      </aside>

      <!-- 移动端切换 -->
      <div class="mobile-tabs">
        <button
          type="button"
          :class="{ active: mobileTab === 'edit' }"
          @click="mobileTab = 'edit'"
        >
          编辑
        </button>
        <button
          type="button"
          :class="{ active: mobileTab === 'preview' }"
          @click="mobileTab = 'preview'"
        >
          预览
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownRendererComponent from '@/components/notes/MarkdownRendererComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'
import { useUserStore } from '@/stores/useUserStore'
import { excerpt, readingTime } from '@/utils/markdown'
import { writeStorage } from '@/utils/storage'
import { getErrorMessage } from '@/utils/errorMessage'
import type { NoteDraft } from '@/types/note'

const route = useRoute()
const router = useRouter()
const noteStore = useNoteStore()
const userStore = useUserStore()

const noteId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => Boolean(noteId.value))
/** 按钮文案跟随「是否发布到广场」，避免用户误会默认就是公开的 */
const saveLabel = computed(() => {
  if (isEdit.value) return '保存修改'
  return form.toPlaza ? '🌐 发布到广场' : '🔒 保存为私密笔记'
})

const saving = ref(false)
const errorMsg = ref('')
const tagInput = ref('')
const mobileTab = ref<'edit' | 'preview'>('edit')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const form = reactive({
  title: '',
  content: '',
  summary: '',
  category: 'frontend',
  tags: [] as string[],
  pinned: false,
  /** 是否发布到讨论广场：新建默认私有 */
  toPlaza: false
})

onMounted(async () => {
  if (!userStore.isLogin) {
    void router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  // 编辑器永远操作自己的笔记，只加载 scope=mine
  await noteStore.loadMyNotes()

  if (isEdit.value && noteId.value) {
    // 列表不含正文，编辑时必须单独拉取完整笔记（含 content）
    const note = await noteStore.fetchNoteDetail(noteId.value)
    if (!note) {
      void router.replace('/notes')
      return
    }
    form.title = note.title
    form.content = note.content
    form.summary = note.summary
    form.category = note.category
    form.tags = [...note.tags]
    form.pinned = note.pinned
    form.toPlaza = note.visibility === 1
  }
})

/* ========== 标签 ========== */
function addTag(): void {
  const value = tagInput.value.trim().replace(/^#/, '')
  if (!value) return
  if (!form.tags.includes(value)) form.tags.push(value)
  tagInput.value = ''
}

function removeTag(tag: string): void {
  form.tags = form.tags.filter((item) => item !== tag)
}

function removeLastTag(): void {
  if (tagInput.value === '' && form.tags.length) form.tags.pop()
}

/* ========== Markdown 工具条 ========== */
function wrap(before: string, after: string): void {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = form.content.slice(start, end) || '文本'
  form.content = form.content.slice(0, start) + before + selected + after + form.content.slice(end)

  void nextTick(() => {
    el.focus()
    el.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

function prefixLine(prefix: string): void {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const lineStart = form.content.lastIndexOf('\n', start - 1) + 1
  form.content = form.content.slice(0, lineStart) + prefix + form.content.slice(lineStart)

  void nextTick(() => {
    el.focus()
    el.setSelectionRange(start + prefix.length, start + prefix.length)
  })
}

function insertBlock(): void {
  const el = textareaRef.value
  if (!el) return
  const snippet = '\n```ts\n\n```\n'
  const start = el.selectionStart
  form.content = form.content.slice(0, start) + snippet + form.content.slice(el.selectionEnd)

  void nextTick(() => {
    el.focus()
    el.setSelectionRange(start + 8, start + 8)
  })
}

function insertTable(): void {
  const el = textareaRef.value
  if (!el) return
  const snippet = '\n| 列 A | 列 B |\n| --- | --- |\n| 内容 | 内容 |\n'
  const start = el.selectionStart
  form.content = form.content.slice(0, start) + snippet + form.content.slice(el.selectionEnd)

  void nextTick(() => el.focus())
}

/** Tab 键插入两个空格，而不是切换焦点 */
function handleTab(event: KeyboardEvent): void {
  if (event.key !== 'Tab') return
  event.preventDefault()
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  form.content = `${form.content.slice(0, start)}  ${form.content.slice(el.selectionEnd)}`

  void nextTick(() => {
    el.focus()
    el.setSelectionRange(start + 2, start + 2)
  })
}

/* ========== 保存 ========== */
function buildDraft(): NoteDraft {
  return {
    title: form.title.trim(),
    content: form.content,
    summary: form.summary.trim() || excerpt(form.content),
    category: form.category,
    tags: [...form.tags],
    pinned: form.pinned,
    toPlaza: form.toPlaza
  }
}

function saveDraft(): void {
  // 本地草稿缓存（设计上就需要落本地，不进后端）；统一走 storage 封装以复用前缀与降级逻辑
  writeStorage('note-draft', { ...form, savedAt: Date.now() })
  window.alert('草稿已保存到本地')
}

async function handleSave(): Promise<void> {
  if (!form.title.trim()) {
    errorMsg.value = '请填写笔记标题'
    return
  }
  if (!form.content.trim()) {
    errorMsg.value = '笔记内容不能为空'
    return
  }

  errorMsg.value = ''
  saving.value = true
  try {
    const draft = buildDraft()
    if (isEdit.value && noteId.value) {
      await noteStore.updateNote(noteId.value, draft)
      void router.push(`/notes/${noteId.value}`)
    } else {
      const created = await noteStore.createNote(draft)
      void router.push(`/notes/${created.id}`)
    }
  } catch (e) {
    // 401 未登录 / 403 越权 / 409 乐观锁冲突等，后端文案已友好，直接展示
    errorMsg.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.editor-page {
  min-height: 100vh;
}

.page-inner {
  max-width: 1320px;
  margin: 0 auto;
  padding: 20px 24px 64px;
}

/* ========== 头部 ========== */
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 12px 16px;
  margin-bottom: 14px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #e8edf3;
  color: #64748b;
  text-decoration: none;
  flex-shrink: 0;
  transition: background 0.16s, color 0.16s;
}

.back-link:hover {
  background: #eff6ff;
  color: #2563eb;
}

.title-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
  padding: 6px 0;
}

.title-input::placeholder {
  color: #cbd5e1;
  font-weight: 400;
}

.header-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.tool-btn {
  padding: 8px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  background: #f8fafc;
  color: #334155;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.16s;
}

.tool-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #2563eb;
}

.primary-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 9px;
  background: #2563eb;
  color: #fff;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.18s;
}

.primary-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-tip {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: 9px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13.5px;
}

/* ========== 编辑主体 ========== */
.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.pane {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  overflow: hidden;
  min-height: 520px;
}

.pane-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafbfc;
}

.pane-title {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.pane-meta {
  font-size: 12px;
  color: #94a3b8;
}

.md-tools {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.md-tools button {
  min-width: 28px;
  height: 26px;
  padding: 0 7px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  color: #475569;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.16s;
}

.md-tools button:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #2563eb;
}

.md-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: vertical;
  padding: 16px 18px;
  font-family: 'JetBrains Mono', Consolas, Monaco, 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
  min-height: 480px;
}

.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 22px;
  max-height: 620px;
}

.preview-title {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
}

/* ========== 元信息面板 ========== */
.meta-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 16px;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 18px;
}

.meta-field label {
  display: block;
  margin-bottom: 7px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.meta-field label em {
  font-style: normal;
  color: #94a3b8;
  font-weight: 400;
}

.meta-field select,
.meta-field textarea {
  width: 100%;
  padding: 9px 11px;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  font-size: 13.5px;
  color: #334155;
  font-family: inherit;
  outline: none;
  transition: border-color 0.16s;
}

.meta-field select:focus,
.meta-field textarea:focus {
  border-color: #93c5fd;
}

.meta-field textarea {
  resize: vertical;
}

.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  min-height: 40px;
}

.tag-input-wrap input {
  flex: 1;
  min-width: 100px;
  border: none;
  outline: none;
  font-size: 13.5px;
  font-family: inherit;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border-radius: 6px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12.5px;
}

.tag-pill button {
  border: none;
  background: transparent;
  color: #93c5fd;
  cursor: pointer;
  font-size: 11px;
  padding: 0;
}

.tag-pill button:hover {
  color: #b91c1c;
}

.checkbox-field {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-end;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 8px;
  margin: 0 !important;
  cursor: pointer;
  font-size: 13.5px !important;
  color: #475569 !important;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: #2563eb;
  cursor: pointer;
}

.field-hint {
  margin: 6px 0 0 24px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

/* ========== 移动端切换 ========== */
.mobile-tabs {
  display: none;
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 4px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.14);
  z-index: 50;
}

.mobile-tabs button {
  padding: 8px 24px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
}

.mobile-tabs button.active {
  background: #2563eb;
  color: #fff;
}

@media (max-width: 980px) {
  .editor-body {
    grid-template-columns: 1fr;
  }

  .pane.hidden {
    display: none;
  }

  .pane {
    min-height: 60vh;
  }

  .mobile-tabs {
    display: flex;
  }

  .page-inner {
    padding: 16px 16px 80px;
  }
}

@media (max-width: 560px) {
  .editor-header {
    flex-wrap: wrap;
  }

  .header-right {
    width: 100%;
  }

  .tool-btn,
  .primary-btn {
    flex: 1;
  }
}
</style>
