<template>
  <div class="about-page">
    <div class="page-inner">
      <!-- Hero -->
      <header class="about-hero">
        <div class="hero-avatar">{{ resume.basicInfo.name.charAt(0) }}</div>
        <div class="hero-info">
          <h1>{{ resume.basicInfo.name }}</h1>
          <p class="hero-title">{{ resume.basicInfo.title }}</p>
          <p class="hero-bio">
            计算机科学与技术专业在读，方向为软件测试与全栈开发。喜欢把学到的东西整理成文档，
            也喜欢在项目中把知识点真正用起来。这个站点就是我的学习档案。
          </p>
          <ul class="hero-contact">
            <li><span>📞</span> {{ resume.basicInfo.phone }}</li>
            <li><span>✉️</span> {{ resume.basicInfo.email }}</li>
            <li><span>🎓</span> {{ resume.education.school }}</li>
          </ul>
          <div class="hero-actions">
            <router-link to="/resume" class="primary-btn">📄 查看简历</router-link>
            <router-link to="/notes" class="ghost-btn">📝 浏览笔记</router-link>
          </div>
        </div>
      </header>

      <div class="about-grid">
        <!-- 技能 -->
        <section class="panel">
          <h2 class="panel-title">🛠 专业技能</h2>
          <ul class="skill-list">
            <li v-for="(skill, index) in resume.skills" :key="index">{{ skill }}</li>
          </ul>
        </section>

        <!-- 时间线 -->
        <section class="panel">
          <h2 class="panel-title">🗓 经历时间线</h2>
          <ol class="timeline">
            <li v-for="item in timeline" :key="item.key">
              <span class="time-dot"></span>
              <div class="time-body">
                <span class="time-period">{{ item.period }}</span>
                <h3>{{ item.title }}</h3>
                <p v-if="item.desc">{{ item.desc }}</p>
              </div>
            </li>
          </ol>
        </section>
      </div>

      <!-- 关于站点 -->
      <section class="panel site-panel">
        <h2 class="panel-title">💡 关于这个站点</h2>
        <div class="site-grid">
          <div class="site-item">
            <span class="site-icon">📄</span>
            <h3>简历模块</h3>
            <p>多份简历在线维护，登录后支持新增、编辑、删除，内容实时预览。</p>
          </div>
          <div class="site-item">
            <span class="site-icon">📝</span>
            <h3>笔记模块</h3>
            <p>Markdown 编写与渲染，支持分类、标签、搜索与目录跳转。</p>
          </div>
          <div class="site-item">
            <span class="site-icon">👤</span>
            <h3>用户模块</h3>
            <p>登录注册与个人资料管理，为后续多端同步预留接口。</p>
          </div>
        </div>
        <p class="site-note">
          前端基于 Vue 3 + TypeScript + Vite + Pinia 构建，数据由 Spring Boot 后端服务提供，
          笔记、简历与用户模块均已通过接口实时读写。
        </p>
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import { useResumeStore } from '@/stores/useResumeStore'
import { testResume } from '@/data/TestResume'
import type { ResumeData } from '@/types/resume'

const resumeStore = useResumeStore()

// 优先使用后端返回的第一份简历；未加载完成或后端不可用时用本地兜底，避免页面空白
const resume = computed<ResumeData>(() => {
  const list = Object.values(resumeStore.resumes)
  return list[0] ?? testResume
})

onMounted(() => {
  // 幂等加载；App 启动时已预加载，这里确保进入页面时数据一定就绪
  void resumeStore.loadResumes()
})

/** 由简历数据拼出时间线，避免内容重复维护 */
const timeline = computed(() => {
  const r = resume.value
  const items: Array<{ key: string; period: string; title: string; desc?: string }> = [
    {
      key: 'edu',
      period: r.education.period,
      title: `${r.education.school} · ${r.education.major}`,
      desc: '本科在读'
    }
  ]

  r.campus.forEach((item, index) => {
    items.push({
      key: `campus-${index}`,
      period: item.period,
      title: item.description,
      desc: '竞赛获奖'
    })
  })

  r.internships.forEach((job, index) => {
    items.push({
      key: `job-${index}`,
      period: job.period,
      title: `${job.company} · ${job.position}`,
      desc: job.duties[0]
    })
  })

  return items
})
</script>

<style scoped>
.about-page {
  min-height: 100vh;
}

.page-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}

/* ========== Hero ========== */
.about-hero {
  display: flex;
  gap: 28px;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 20px;
  padding: 36px 40px;
  margin-bottom: 22px;
  position: relative;
  overflow: hidden;
}

.about-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 15% 40%, rgba(37, 99, 235, 0.2) 0%, transparent 58%),
    radial-gradient(circle at 85% 20%, rgba(124, 58, 237, 0.16) 0%, transparent 52%);
  pointer-events: none;
}

.about-hero > * {
  position: relative;
  z-index: 1;
}

.hero-avatar {
  width: 104px;
  height: 104px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 10px 28px rgba(37, 99, 235, 0.32);
}

.hero-info h1 {
  margin: 0 0 6px;
  font-size: 30px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.4px;
}

.hero-title {
  margin: 0 0 14px;
  font-size: 16px;
  color: #93c5fd;
  font-weight: 500;
}

.hero-bio {
  margin: 0 0 16px;
  font-size: 14.5px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.74);
  max-width: 640px;
}

.hero-contact {
  list-style: none;
  margin: 0 0 20px;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
}

.hero-contact li {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.82);
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary-btn,
.ghost-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14.5px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.18s;
}

.primary-btn {
  background: #2563eb;
  border: 1px solid #2563eb;
  color: #fff;
}

.primary-btn:hover {
  background: #1d4ed8;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.3);
}

.ghost-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #fff;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.16);
}

/* ========== 面板 ========== */
.about-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.panel {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 24px 26px;
}

.panel-title {
  margin: 0 0 18px;
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
}

.skill-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.skill-list li {
  position: relative;
  padding-left: 18px;
  font-size: 14.5px;
  line-height: 1.7;
  color: #475569;
}

.skill-list li::before {
  content: '▸';
  position: absolute;
  left: 0;
  color: #2563eb;
  font-weight: 700;
}

/* ========== 时间线 ========== */
.timeline {
  list-style: none;
  margin: 0;
  padding: 0 0 0 22px;
  position: relative;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  background: #e8edf3;
}

.timeline li {
  position: relative;
  margin-bottom: 20px;
}

.timeline li:last-child {
  margin-bottom: 0;
}

.time-dot {
  position: absolute;
  left: -22px;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2563eb;
  box-shadow: 0 0 0 3px #eff6ff;
}

.time-period {
  display: inline-block;
  font-size: 12.5px;
  color: #2563eb;
  font-weight: 500;
  margin-bottom: 3px;
}

.time-body h3 {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.time-body p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.65;
  color: #64748b;
}

/* ========== 站点说明 ========== */
.site-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.site-item {
  padding: 18px;
  border-radius: 12px;
  background: #f8fafc;
}

.site-icon {
  font-size: 24px;
  display: block;
  margin-bottom: 10px;
}

.site-item h3 {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.site-item p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.65;
  color: #64748b;
}

.site-note {
  margin: 0;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  font-size: 13.5px;
  line-height: 1.7;
  color: #94a3b8;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .about-hero {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 32px 24px;
  }

  .hero-contact {
    justify-content: center;
  }

  .hero-actions {
    justify-content: center;
  }

  .about-grid,
  .site-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .page-inner {
    padding: 20px 16px 48px;
  }

  .hero-info h1 {
    font-size: 25px;
  }
}
</style>
