<template>
  <aside class="assistant-sidebar">
    <div class="sidebar-head">
      <button class="new-chat-btn" type="button" :disabled="disabled" @click="emit('create')">
        <span>＋</span> 新对话
      </button>
    </div>

    <ul v-if="sessions.length" class="session-list">
      <li v-for="session in sessions" :key="session.id" :class="{ active: session.id === activeId }">
        <button type="button" class="session-item" @click="emit('select', session.id)">
          <span class="session-title">{{ session.title }}</span>
          <span class="session-time">{{ fromNow(session.updatedAt) }}</span>
        </button>
        <button class="session-del" type="button" title="删除会话" @click="emit('remove', session.id)">
          ✕
        </button>
      </li>
    </ul>

    <p v-else class="sidebar-empty">还没有会话，点上方「新对话」开始</p>

    <div class="sidebar-foot">
      <p>🤖 回答只参考<strong>你自己的笔记</strong>，并在下方标注引用来源</p>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import type { ChatSession } from '@/types/assistant'
import { fromNow } from '@/utils/datetime'

/** 会话列表：新建 / 切换 / 删除 */
defineProps<{
  sessions: ChatSession[]
  activeId: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'create'): void
  (e: 'remove', id: string): void
}>()
</script>

<style scoped>
@import '@/assets/styles/components/assistant.css';
</style>
