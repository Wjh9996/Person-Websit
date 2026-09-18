<template>
  <NoteListPanelComponent
    heading="🌐 讨论广场"
    subtitle="大家主动分享的公开笔记，未登录也能浏览"
    show-author
    empty-title="广场还很安静"
    empty-desc="还没有人发布笔记，你可以成为第一个分享的人"
  />
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import NoteListPanelComponent from '@/components/notes/NoteListPanelComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'

/**
 * 讨论广场：公开页面，任何人可见。
 * 只加载 scope=plaza 的笔记（后端过滤 visibility=1），私有笔记不会出现在列表里。
 */
const noteStore = useNoteStore()
noteStore.setScope('plaza')

onMounted(() => {
  void noteStore.loadPlazaNotes()
})
</script>
