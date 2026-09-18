<template>
  <div class="assistant-page">
    <div class="assistant-layout" :class="{ 'sidebar-hidden': sidebarCollapsed }">
      <AssistantSessionListComponent
        v-show="!sidebarCollapsed"
        :sessions="sessions"
        :active-id="activeId"
        :disabled="loading"
        @select="selectSession"
        @create="createSession"
        @remove="removeSession"
      />

      <section class="assistant-main">
        <!-- 顶栏：折叠侧栏 / 模型标识 / 知识库说明 -->
        <header class="main-topbar">
          <button
            class="icon-btn"
            type="button"
            :title="sidebarCollapsed ? '展开会话列表' : '收起会话列表'"
            @click="sidebarCollapsed = !sidebarCollapsed"
          >
            ☰
          </button>
          <span class="model-tag"><span class="dot" />DeepSeek · 我的笔记知识库</span>
          <span class="topbar-tip">知识库共 {{ noteCount }} 篇笔记</span>
        </header>

        <!-- 对话区 -->
        <div ref="scrollRef" class="chat-scroll">
          <div class="chat-inner">
            <div v-if="!messages.length && !answering" class="chat-empty">
              <p class="greet-icon">🤖</p>
              <h3>有什么可以帮你的？</h3>
              <p>我会先在你的笔记里检索，再结合检索结果回答，并标注引用来源</p>

              <div class="suggest-list">
                <button
                  v-for="tip in suggestTips"
                  :key="tip"
                  type="button"
                  class="suggest-chip"
                  @click="send(tip)"
                >
                  {{ tip }}
                </button>
              </div>
            </div>

            <AssistantMessageItemComponent
              v-for="msg in messages"
              :key="msg.id"
              :message="msg"
            />

            <!-- 思考中：还原 DeepSeek 的"深度思考"提示，并显示已用时 -->
            <div v-if="answering" class="thinking">
              <span class="think-text">
                <span class="dot-typing"><span /><span /><span /></span>
                正在检索笔记并生成回答
              </span>
              <span class="think-timer">{{ elapsed }}s</span>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="composer-wrap">
          <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>
          <div class="composer">
            <textarea
              ref="inputRef"
              v-model="draft"
              rows="1"
              placeholder="给「我的笔记知识库」发送消息…"
              @input="autoResize"
              @keydown.enter.exact.prevent="send()"
            ></textarea>
            <div class="composer-bar">
              <span class="composer-hint">Enter 发送 · Shift + Enter 换行</span>
              <button
                class="send-btn"
                type="button"
                :disabled="answering || !draft.trim()"
                :title="answering ? '正在回答…' : '发送'"
                @click="send()"
              >
                ↑
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import AssistantMessageItemComponent from '@/components/assistant/AssistantMessageItemComponent.vue'
import AssistantSessionListComponent from '@/components/assistant/AssistantSessionListComponent.vue'
import * as assistantService from '@/services/assistantService'
import { useNoteStore } from '@/stores/useNoteStore'
import { getErrorMessage } from '@/utils/errorMessage'
import type { ChatMessage, ChatSession } from '@/types/assistant'

/**
 * AI 助手页：基于自己笔记的知识库问答（DeepSeek 风格布局）。
 * 会话与消息都归当前登录用户所有，后端按 JWT 隔离，前端不传 userId。
 */
const noteStore = useNoteStore()

const sessions = ref<ChatSession[]>([])
const messages = ref<ChatMessage[]>([])
const activeId = ref('')
const draft = ref('')
const loading = ref(false)
const answering = ref(false)
const errorMsg = ref('')
const elapsed = ref(0)
const sidebarCollapsed = ref(false)
const scrollRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLTextAreaElement | null>(null)
let timer: ReturnType<typeof setInterval> | null = null

const noteCount = computed(() => noteStore.myNotes.length)

const suggestTips = [
  '帮我总结笔记里关于 MySQL 索引的内容',
  '这些笔记里反复出现的技术点有哪些？',
  '面试前我应该重点复习什么？'
]

onMounted(async () => {
  noteStore.setScope('mine')
  await noteStore.loadMyNotes()
  await loadSessions()
})

onBeforeUnmount(() => stopTimer())

async function loadSessions(): Promise<void> {
  loading.value = true
  try {
    sessions.value = await assistantService.fetchSessions()
    if (!sessions.value.length) {
      await createSession()
      return
    }
    const first = sessions.value[0]
    if (first) await selectSession(first.id)
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  } finally {
    loading.value = false
  }
}

async function selectSession(id: string): Promise<void> {
  if (id === activeId.value) return
  activeId.value = id
  errorMsg.value = ''
  try {
    messages.value = await assistantService.fetchMessages(id)
    scrollToBottom()
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

async function createSession(): Promise<void> {
  try {
    const session = await assistantService.createSession()
    sessions.value = [session, ...sessions.value]
    activeId.value = ''
    messages.value = []
    await selectSession(session.id)
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

async function removeSession(id: string): Promise<void> {
  if (!window.confirm('确定删除这个会话吗？问答记录会一并删除。')) return
  try {
    await assistantService.deleteSession(id)
    sessions.value = sessions.value.filter((item) => item.id !== id)
    if (activeId.value === id) {
      activeId.value = ''
      messages.value = []
      const next = sessions.value[0]
      if (next) await selectSession(next.id)
      else await createSession()
    }
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  }
}

async function send(text?: string): Promise<void> {
  const question = (text ?? draft.value).trim()
  if (!question || answering.value) return
  if (!activeId.value) {
    await createSession()
    if (!activeId.value) return
  }

  errorMsg.value = ''
  draft.value = ''
  autoResize()
  // 乐观渲染：先把问题显示出来，避免等待时界面无反馈
  messages.value.push({
    id: `pending-${Date.now()}`,
    role: 'user',
    content: question,
    refs: null,
    createdAt: new Date().toISOString()
  })
  scrollToBottom()

  answering.value = true
  startTimer()
  try {
    const result = await assistantService.askQuestion(activeId.value, question)
    messages.value.push({
      id: result.messageId,
      role: 'assistant',
      content: result.answer,
      refs: result.fromKnowledgeBase ? result.refs : [],
      createdAt: new Date().toISOString()
    })
    // 会话标题随首条问题更新，顺手刷新侧栏
    sessions.value = await assistantService.fetchSessions()
  } catch (e) {
    errorMsg.value = getErrorMessage(e)
  } finally {
    stopTimer()
    answering.value = false
    scrollToBottom()
  }
}

/* ========== 细节交互 ========== */

function startTimer(): void {
  elapsed.value = 0
  timer = setInterval(() => {
    elapsed.value += 1
  }, 1000)
}

function stopTimer(): void {
  if (timer) clearInterval(timer)
  timer = null
}

function autoResize(): void {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 160)}px`
}

async function scrollToBottom(): Promise<void> {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<style scoped>
@import '@/assets/styles/components/assistant.css';

.assistant-page {
  padding: 16px 20px 20px;
}

/* 撑满首屏高度：内部滚动，避免整页抖动 */
.assistant-layout {
  display: flex;
  height: calc(100vh - 148px);
  min-height: 560px;
  max-width: 1400px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #eceff3;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.05);
}

.assistant-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.assistant-main {
  flex: 1;
  min-width: 0;
}

@media (max-width: 900px) {
  .assistant-page {
    padding: 10px 10px 14px;
  }

  .assistant-layout {
    height: calc(100vh - 130px);
  }

  .assistant-sidebar {
    width: 220px;
  }
}
</style>
