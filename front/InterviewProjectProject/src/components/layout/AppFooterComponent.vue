<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <p class="footer-brand-name">王建豪的个人站</p>
        <p class="footer-desc">
          记录学习过程，沉淀项目经验。内容涵盖前端开发、软件测试、后端基础与面试准备。
        </p>
        <div class="footer-contact">
          <span>📞 18727990870</span>
          <span>✉️ 18727990870@163.com</span>
        </div>
      </div>

      <div>
        <p class="footer-col-title">站点导航</p>
        <ul class="footer-links">
          <li><router-link to="/">首页</router-link></li>
          <li><router-link to="/resume">我的简历</router-link></li>
          <li><router-link to="/notes">学习笔记</router-link></li>
          <li><router-link to="/about">关于我</router-link></li>
        </ul>
      </div>

      <div>
        <p class="footer-col-title">技术栈</p>
        <ul class="footer-links">
          <li>Vue 3 + TypeScript</li>
          <li>Vite + Pinia</li>
          <li>Spring Boot + MyBatis</li>
          <li>MySQL + Redis</li>
        </ul>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p>© {{ currentYear }} 王建豪 · 个人网站</p>
        <p>Vue 3 + Vite 构建 · 数据暂存于浏览器本地</p>
      </div>
    </div>
  </footer>
</template>

<script lang="ts" setup>
const currentYear = new Date().getFullYear()
</script>

<style scoped>
@import '@/assets/styles/components/footer.css';
</style>
