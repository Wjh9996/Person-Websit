<template>
  <div class="resume-container">
    <!-- 左侧栏 -->
    <aside class="sidebar">
      <div class="avatar">
        <div class="avatar-placeholder">{{ data.basicInfo.name?.charAt(0) || '人' }}</div>
      </div>
      <h1 class="name">{{ data.basicInfo.name }}</h1>
      <div class="title">{{ data.basicInfo.title }}</div>
      <div class="contact">
        <p v-if="data.basicInfo.phone"><span>📞</span> {{ data.basicInfo.phone }}</p>
        <p v-if="data.basicInfo.email"><span>✉️</span> {{ data.basicInfo.email }}</p>
      </div>

      <div class="section">
        <h2>教育背景</h2>
        <div class="edu-item">
          <h3>{{ data.education.school }}</h3>
          <p>{{ data.education.major }}</p>
          <p class="date">{{ data.education.period }}</p>
        </div>
      </div>

      <div class="section">
        <h2>专业技能</h2>
        <ul class="skill-list">
          <li v-for="(skill, index) in data.skills" :key="index">{{ skill }}</li>
        </ul>
      </div>
    </aside>

    <!-- 右侧主内容 -->
    <main class="main-content">
      <div class="section">
        <h2>个人总结</h2>
        <ul class="summary-list">
          <li v-for="(item, index) in data.summary" :key="index">{{ item }}</li>
        </ul>
      </div>

      <div class="section">
        <h2>校园经历</h2>
        <ul class="experience-list">
          <li v-for="(item, index) in data.campus" :key="index">
            <span class="year">{{ item.period }}</span>
            {{ item.description }}
          </li>
        </ul>
      </div>

      <div class="section">
        <h2>实习经历</h2>
        <div v-for="(job, index) in data.internships" :key="index" class="exp-item">
          <h3>{{ job.company }} · {{ job.position }}</h3>
          <p class="date">{{ job.period }}</p>
          <ul>
            <li v-for="(duty, idx) in job.duties" :key="idx">{{ duty }}</li>
          </ul>
        </div>
      </div>

      <div class="section">
        <h2>项目经历</h2>
        <div v-for="(project, index) in data.projects" :key="index" class="project-item">
          <h3>{{ project.name }}</h3>
          <p class="tech-stack">技术架构：{{ project.techStack }}</p>
          <p class="project-desc">{{ project.description }}</p>
          <h4>{{ data.projectDutyTitle || '工作职责与成果' }}</h4>
          <ul>
            <li v-for="(duty, idx) in project.duties" :key="idx" v-html="duty"></li>
          </ul>
        </div>
      </div>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { ResumeData } from '@/types/resume'

export default defineComponent({
  name: 'RenameComponent',
  props: {
    data: {
      type: Object as PropType<ResumeData>,
      required: true
    }
  }
})
</script>

<style scoped>
@import '@/assets/styles/components/resume.css';
</style>

