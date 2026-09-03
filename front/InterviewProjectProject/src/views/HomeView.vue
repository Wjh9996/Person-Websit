<template>
  <div class="home-container">
    <!-- ========== 头部区域 ========== -->
    <header class="home-header">
      <div class="header-bg-decoration"></div>
      <div class="header-content">
        <div class="header-left">
          <div class="greeting">
            <span class="greeting-emoji">👋</span>
            <h1>欢迎回来</h1>
          </div>
          <p class="header-sub">这里是我的个人工作台，简历与学习笔记都在这里</p>
          <div class="header-quick-actions">
            <router-link to="/resume/testing" class="quick-btn primary">
              <span>📄</span> {{ userStore.isLogin ? '管理简历' : '查看简历' }}
            </router-link>
            <router-link
              to="/notes/create"
              class="quick-btn secondary"
              @click.prevent="handleCreate"
            >
              <span>✍️</span> {{ userStore.isLogin ? '新建笔记' : '写笔记' }}
            </router-link>
          </div>
        </div>
        <div class="header-right">
          <div class="stat-grid">
            <div class="stat-item">
              <span class="stat-number">{{ resumeStore.total }}</span>
              <span class="stat-label">份简历</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ noteStore.notes.length }}</span>
              <span class="stat-label">篇笔记</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ noteStore.tags.length }}</span>
              <span class="stat-label">个标签</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ noteStore.totalViews }}</span>
              <span class="stat-label">次阅读</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- ========== 功能卡片 ========== -->
    <section class="card-section">
      <div class="section-header">
        <h2>🚀 快速入口</h2>
        <span class="section-sub">点击卡片进入对应模块</span>
      </div>

      <div class="card-scroll">
        <div class="card-row">
          <!-- 1. 简历管理 -->
          <ModuleCardComponent
            title="简历管理"
            :description="userStore.isLogin ? '查看、编辑、维护你的个人简历' : '浏览我的个人简历'"
            icon-bg="#dbeafe"
            icon-color="#2563eb"
            status-text="● 已就绪"
            status-class="active"
            :badge-text="`${resumeStore.total} 份简历`"
            badge-class="blue"
            card-class="card-resume"
            :active="true"
            to="/resume/testing"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 4h16v16H4V4z" />
                <path d="M8 8h8v2H8zM8 12h6v2H8zM8 16h4v2H8z" />
              </svg>
            </template>
          </ModuleCardComponent>

          <!-- 2. 学习笔记 -->
          <ModuleCardComponent
            title="学习笔记"
            description="整理技术要点与踩坑经验"
            icon-bg="#d1fae5"
            icon-color="#059669"
            status-text="● 已就绪"
            status-class="active"
            :badge-text="`${noteStore.notes.length} 篇笔记`"
            badge-class="blue"
            card-class="card-note"
            :active="true"
            to="/notes"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </template>
          </ModuleCardComponent>

          <!-- 3. 个人中心 -->
          <ModuleCardComponent
            title="个人中心"
            description="管理账号资料与内容统计"
            icon-bg="#ede9fe"
            icon-color="#7c3aed"
            status-text="● 已就绪"
            status-class="active"
            :badge-text="userStore.isLogin ? '已登录' : '未登录'"
            badge-class="blue"
            card-class="card-project"
            :active="true"
            to="/profile"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="4" />
                <path d="M4 21c0-4 3.6-6 8-6s8 2 8 6" />
              </svg>
            </template>
          </ModuleCardComponent>

          <!-- 4. 关于我 -->
          <ModuleCardComponent
            title="关于我"
            description="了解我的经历与技术方向"
            icon-bg="#fef3c7"
            icon-color="#d97706"
            status-text="● 已就绪"
            status-class="active"
            badge-text="个人介绍"
            badge-class="blue"
            card-class="card-dashboard"
            :active="true"
            to="/about"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 16v-4M12 8h.01" />
              </svg>
            </template>
          </ModuleCardComponent>
        </div>
      </div>
    </section>

    <!-- ========== 最近笔记 ========== -->
    <section class="recent-section">
      <div class="section-header">
        <h2>📚 最近更新</h2>
        <router-link to="/notes" class="more-link">查看全部 →</router-link>
      </div>

      <div v-if="noteStore.loading" class="skeleton-list">
        <div v-for="i in 4" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else-if="noteStore.recentNotes.length" class="recent-grid">
        <NoteCardComponent v-for="note in noteStore.recentNotes" :key="note.id" :note="note" />
      </div>

      <div v-else class="empty-block">
        <p class="empty-icon">📝</p>
        <h3>还没有笔记</h3>
        <p>点击「写笔记」记录第一条学习内容</p>
        <router-link to="/notes/create" class="primary-btn" @click.prevent="handleCreate">
          {{ userStore.isLogin ? '新建笔记' : '写笔记' }}
        </router-link>
      </div>
    </section>

    <!-- ========== 底部提示 ========== -->
    <footer class="home-footer">
      <p>💡 内容由后端服务提供，未登录时可自由浏览，登录后即可编辑简历与笔记</p>
    </footer>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import ModuleCardComponent from '@/components/ModuleCardComponent.vue'
import NoteCardComponent from '@/components/notes/NoteCardComponent.vue'
import { useNoteStore } from '@/stores/useNoteStore'
import { useResumeStore } from '@/stores/useResumeStore'
import { useUserStore } from '@/stores/useUserStore'
import { useAuthGuard } from '@/composables/useAuthGuard'

const router = useRouter()
const noteStore = useNoteStore()
const resumeStore = useResumeStore()
const userStore = useUserStore()
const { requireLogin } = useAuthGuard()

/** 写笔记需要登录：未登录时弹出提示（按钮保留作为入口），已登录才进入编辑器 */
function handleCreate(): void {
  const allowed = requireLogin({
    message: '写笔记需要先登录，登录后即可开始创作。',
    redirect: '/notes/create'
  })
  if (!allowed) return
  void router.push('/notes/create')
}
</script>

<style scoped>
@import '@/assets/styles/common/buttons.css';
@import '@/assets/styles/common/cards.css';
@import '@/assets/styles/common/badges.css';
@import '@/assets/styles/common/stats.css';
@import '@/assets/styles/components/notes.css';

.home-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ========== 头部 ========== */
.home-header {
  position: relative;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 24px;
  padding: 40px 48px;
  margin-bottom: 48px;
  overflow: hidden;
  isolation: isolate;
}

.header-bg-decoration {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(37, 99, 235, 0.15) 0%, transparent 60%),
    radial-gradient(circle at 80% 20%, rgba(124, 58, 237, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 60% 80%, rgba(6, 182, 212, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.header-left {
  flex: 1 1 300px;
}

.greeting {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.greeting-emoji {
  font-size: 32px;
  line-height: 1;
}

.home-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  letter-spacing: -0.5px;
}

.header-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 0 20px;
  font-weight: 400;
}

.header-quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 14.5px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.18s;
}

.quick-btn.primary {
  background: #2563eb;
  color: #fff;
  border: 1px solid #2563eb;
}

.quick-btn.primary:hover {
  background: #1d4ed8;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.3);
}

.quick-btn.secondary {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.quick-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.16);
}

/* ========== 区块 ========== */
.card-section,
.recent-section {
  margin-bottom: 36px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 8px;
}

.section-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}

.section-sub {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 400;
}

.more-link {
  font-size: 14px;
  color: #2563eb;
  text-decoration: none;
  transition: color 0.16s;
}

.more-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* ========== 卡片横向滚动 ========== */
.card-scroll {
  overflow-x: auto;
  overflow-y: visible;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
  padding: 4px 0 12px;
}

.card-scroll::-webkit-scrollbar {
  height: 6px;
}

.card-scroll::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 8px;
}

.card-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 8px;
}

.card-row {
  display: flex;
  gap: 24px;
  flex-wrap: nowrap;
  width: max-content;
  min-width: 100%;
}

.card-row .card-link,
.card-row .module-card {
  width: 260px;
  flex: 0 0 auto;
}

/* ========== 最近笔记 ========== */
.recent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

/* ========== 空状态 ========== */
.empty-block {
  padding: 56px 24px;
  text-align: center;
  background: #fff;
  border-radius: 14px;
  border: 1px dashed #dbe3ec;
}

.empty-icon {
  font-size: 40px;
  margin: 0 0 12px;
}

.empty-block h3 {
  margin: 0 0 8px;
  font-size: 17px;
  color: #0f172a;
  font-weight: 600;
}

.empty-block p {
  margin: 0 0 18px;
  color: #64748b;
  font-size: 14px;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.18s;
}

.primary-btn:hover {
  background: #1d4ed8;
}

/* ========== 底部 ========== */
.home-footer {
  margin-top: 16px;
  padding: 16px 24px;
  background: #f8fafc;
  border-radius: 16px;
  text-align: center;
  border: 1px solid #f1f5f9;
}

.home-footer p {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .home-container {
    padding: 20px 16px 32px;
  }

  .home-header {
    padding: 28px 24px;
    border-radius: 18px;
  }

  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .header-right {
    width: 100%;
  }

  .stat-grid {
    justify-content: space-around;
    padding: 14px 20px;
    gap: 16px;
  }

  .stat-number {
    font-size: 22px;
  }

  .home-header h1 {
    font-size: 26px;
  }

  .section-header h2 {
    font-size: 20px;
  }

  .card-row .card-link,
  .card-row .module-card {
    width: 220px;
  }

  .card-row {
    gap: 16px;
  }

  .recent-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .home-header {
    padding: 20px 16px;
  }

  .stat-grid {
    gap: 12px;
    padding: 10px 14px;
  }

  .stat-number {
    font-size: 18px;
  }

  .stat-label {
    font-size: 11px;
  }

  .greeting h1 {
    font-size: 22px;
  }

  .header-sub {
    font-size: 14px;
  }

  .quick-btn {
    padding: 6px 14px;
    font-size: 13px;
  }

  .card-row .card-link,
  .card-row .module-card {
    width: 190px;
  }

  .module-card h3 {
    font-size: 16px;
  }
}
</style>
