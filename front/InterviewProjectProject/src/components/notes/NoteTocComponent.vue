<template>
  <nav class="toc-wrap" aria-label="笔记目录">
    <p class="toc-title">目录</p>
    <p v-if="!items.length" class="toc-empty">暂无目录</p>
    <ul v-else class="toc-list">
      <li v-for="item in items" :key="item.id" class="toc-item">
        <a
          class="toc-link"
          :class="[`level-${item.level}`, { active: activeId === item.id }]"
          :href="`#${item.id}`"
          @click.prevent="scrollTo(item.id)"
        >
          {{ item.text }}
        </a>
      </li>
    </ul>
  </nav>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { TocItem } from '@/utils/markdown'

const props = defineProps<{
  items: TocItem[]
}>()

const activeId = ref('')
let observer: IntersectionObserver | null = null

function scrollTo(id: string): void {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  activeId.value = id
}

/** 滚动时高亮当前所在章节 */
function setupObserver(): void {
  observer?.disconnect()
  if (!props.items.length) return

  observer = new IntersectionObserver(
    (entries) => {
      const visible = entries
        .filter((entry) => entry.isIntersecting)
        .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
      const first = visible[0]
      if (first) activeId.value = first.target.id
    },
    { rootMargin: '-80px 0px -70% 0px', threshold: 0 }
  )

  props.items.forEach((item) => {
    const el = document.getElementById(item.id)
    if (el) observer?.observe(el)
  })
}

onMounted(setupObserver)
watch(() => props.items, setupObserver, { deep: true })
onBeforeUnmount(() => observer?.disconnect())
</script>

<style scoped>
@import '@/assets/styles/components/toc.css';
</style>
