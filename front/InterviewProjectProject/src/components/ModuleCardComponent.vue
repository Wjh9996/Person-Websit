<template>
  <component :is="tag" :to="to" class="card-link" v-if="to">
    <div class="module-card" :class="cardClass">
      <div class="card-top">
        <div class="card-icon-wrap" :style="{ background: iconBg, color: iconColor }">
          <slot name="icon">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
            </svg>
          </slot>
        </div>
        <span class="card-status" :class="statusClass">{{ statusText }}</span>
      </div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <div class="card-bottom">
        <span class="badge" :class="badgeClass">{{ badgeText }}</span>
        <span class="card-arrow" :class="{ disabled: !active }">→</span>
      </div>
    </div>
  </component>
  <div v-else class="module-card" :class="[cardClass, { placeholder: !active }]">
    <!-- 同样的内容，但没有链接 -->
    <div class="card-top">
      <div class="card-icon-wrap" :style="{ background: iconBg, color: iconColor }">
        <slot name="icon"></slot>
      </div>
      <span class="card-status" :class="statusClass">{{ statusText }}</span>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <div class="card-bottom">
      <span class="badge" :class="badgeClass">{{ badgeText }}</span>
      <span class="card-arrow" :class="{ disabled: !active }">→</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'ModuleCardComponent',
  props: {
    title: { type: String, required: true },
    description: { type: String, required: true },
    iconBg: { type: String, default: '#dbeafe' },
    iconColor: { type: String, default: '#2563eb' },
    statusText: { type: String, default: '已就绪' },
    statusClass: { type: String, default: 'active' }, // 'active' | 'coming'
    badgeText: { type: String, default: '1 份简历' },
    badgeClass: { type: String, default: 'blue' }, // 'blue' | 'gray'
    cardClass: { type: String, default: '' }, // 如 'card-resume'
    active: { type: Boolean, default: true },
    to: { type: String, default: '' } // 路由链接，为空则不可点击
  },
  computed: {
    tag() {
      return this.to ? 'router-link' : 'div'
    }
  }
})
</script>

<style scoped>
/* 导入通用卡片样式（无需重复定义） */
@import '@/assets/styles/common/cards.css';
</style>
