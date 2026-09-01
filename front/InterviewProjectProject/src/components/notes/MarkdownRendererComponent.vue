<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  source: string
}>()

const html = computed(() => renderMarkdown(props.source).html)
</script>

<style>
@import '@/assets/styles/components/markdown.css';
</style>
