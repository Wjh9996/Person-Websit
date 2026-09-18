<template>
  <router-link :to="`/notes/${note.id}`" class="note-card" :class="{ pinned: note.pinned }">
    <div class="note-card-top">
      <span class="note-category">
        <span>{{ categoryIcon }}</span>
        {{ categoryLabel }}
      </span>
      <span v-if="note.pinned" class="note-pin">📌 置顶</span>
      <span v-if="note.visibility === 0" class="note-private">🔒 仅自己可见</span>
    </div>

    <h3 class="note-title">{{ note.title }}</h3>
    <p class="note-summary">{{ note.summary || excerpt(note.content ?? '') }}</p>

    <div v-if="note.tags.length" class="note-tags">
      <span v-for="tag in visibleTags" :key="tag" class="note-tag"># {{ tag }}</span>
      <span v-if="note.tags.length > 3" class="note-tag-more">+{{ note.tags.length - 3 }}</span>
    </div>

    <div class="note-meta">
      <span v-if="showAuthor" class="note-author">👤 {{ authorLabel }}</span>
      <span>🕒 {{ fromNow(note.updatedAt) }}</span>
      <span>📖 {{ readingTime(note.content ?? note.summary) }} 分钟</span>
      <span class="meta-views">👁 {{ note.views }}</span>
    </div>
  </router-link>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import type { Note } from '@/types/note'
import { getCategoryIcon, getCategoryLabel } from '@/data/noteCategories'
import { excerpt, readingTime } from '@/utils/markdown'
import { fromNow } from '@/utils/datetime'

const props = withDefaults(
  defineProps<{
    note: Note
    /** 讨论广场需要显示作者昵称，「我的笔记」不需要 */
    showAuthor?: boolean
  }>(),
  { showAuthor: false }
)

const categoryLabel = computed(() => getCategoryLabel(props.note.category))
const categoryIcon = computed(() => getCategoryIcon(props.note.category))
const visibleTags = computed(() => props.note.tags.slice(0, 3))
const authorLabel = computed(() => props.note.authorName || '匿名作者')
</script>

<style scoped>
@import '@/assets/styles/components/notes.css';
</style>
