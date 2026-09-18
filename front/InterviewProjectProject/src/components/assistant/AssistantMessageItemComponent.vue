<template>
  <div class="msg-row" :class="message.role === 'user' ? 'msg-user' : 'msg-assistant'">
    <!-- 助手头像在左，提问头像在右（DeepSeek 的左右分布） -->
    <span v-if="message.role === 'assistant'" class="msg-avatar">🤖</span>

    <div class="msg-body">
      <div v-if="message.role === 'user'" class="msg-bubble">{{ message.content }}</div>

      <template v-else>
        <div class="msg-answer">
          <MarkdownRendererComponent :source="message.content" />
        </div>

        <div class="msg-actions">
          <button class="msg-action" type="button" @click="copyAnswer">
            {{ copied ? '✓ 已复制' : '复制' }}
          </button>
        </div>

        <!-- 引用来源：可点回原文，带相关度 -->
        <div v-if="refs.length" class="msg-refs">
          <div class="refs-head">
            <span>📚 本回答引用了 {{ refs.length }} 篇你的笔记</span>
          </div>
          <div class="refs-list">
            <router-link
              v-for="ref in refs"
              :key="ref.noteId"
              :to="`/notes/${ref.noteId}`"
              class="ref-item"
            >
              <span>📄</span>
              <span class="ref-title">{{ ref.title }}</span>
              <span v-if="ref.score" class="ref-score">相关度 {{ ref.score.toFixed(1) }}</span>
            </router-link>
          </div>
        </div>
      </template>
    </div>

    <span v-if="message.role === 'user'" class="msg-avatar">我</span>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import MarkdownRendererComponent from '@/components/notes/MarkdownRendererComponent.vue'
import type { ChatMessage } from '@/types/assistant'

/** 单条消息：提问为右侧气泡，回答整段铺开（Markdown）+ 引用来源卡片 */
const props = defineProps<{
  message: ChatMessage
}>()

const refs = computed(() => props.message.refs ?? [])
const copied = ref(false)

async function copyAnswer(): Promise<void> {
  try {
    await navigator.clipboard.writeText(props.message.content)
    copied.value = true
    setTimeout(() => (copied.value = false), 1600)
  } catch {
    // 剪贴板不可用时忽略
  }
}
</script>

<style scoped>
@import '@/assets/styles/components/assistant.css';

/* MarkdownRendererComponent 是孙组件，其内部元素拿不到本组件的 scoped 标识，
   需要用 :deep 收敛字号，让回答更像"对话正文"而不是文档页 */
.msg-answer :deep(.markdown-body) {
  font-size: 15px;
}

.msg-answer :deep(.markdown-body h1) {
  font-size: 20px;
  margin: 6px 0 10px;
}

.msg-answer :deep(.markdown-body h2) {
  font-size: 17.5px;
  margin: 14px 0 8px;
}

.msg-answer :deep(.markdown-body h3) {
  font-size: 16px;
  margin: 12px 0 6px;
}

.msg-answer :deep(.markdown-body p),
.msg-answer :deep(.markdown-body ul),
.msg-answer :deep(.markdown-body ol) {
  margin: 0 0 10px;
}

.msg-answer :deep(.markdown-body pre) {
  margin: 10px 0;
}

.msg-answer :deep(.markdown-body) > :last-child {
  margin-bottom: 0;
}
</style>
