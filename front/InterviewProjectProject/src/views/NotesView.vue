<template>
  <NoteListPanelComponent
    heading="📝 我的笔记"
    subtitle="这里是你自己的全部笔记，未发布到广场的只有你能看到"
    empty-title="还没有自己的笔记"
    empty-desc="写下第一篇学习笔记吧，写完后可以选择是否发布到讨论广场"
    write-link="/notes/create"
  />
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import NoteListPanelComponent from '@/components/notes/NoteListPanelComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'

/**
 * 我的笔记：需登录（路由 meta.requiresAuth）。
 * 数据源 scope=mine，后端按 JWT 取当前用户，绝不会混入别人的笔记。
 */
const noteStore = useNoteStore()
noteStore.setScope('mine')

onMounted(() => {
  void noteStore.loadMyNotes()
})
</script>
