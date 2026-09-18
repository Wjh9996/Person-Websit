<template>
  <div class="profile-page">
    <div class="page-inner">
      <div v-if="!userStore.isLogin" class="empty-block">
        <p class="empty-icon">🔒</p>
        <h3>还没有登录</h3>
        <p>登录后即可管理你的简历与笔记</p>
        <router-link class="primary-btn" to="/login">前往登录</router-link>
      </div>

      <template v-else>
        <div class="profile-layout">
          <!-- 资料卡 -->
          <aside class="profile-card">
            <div class="avatar-lg">{{ userStore.avatar }}</div>
            <h2 class="profile-name">{{ userStore.profile?.nickname }}</h2>
            <p class="profile-username">@{{ userStore.profile?.username }}</p>

            <p class="profile-bio">
              {{ userStore.profile?.bio || '这个人很懒，还没有填写简介…' }}
            </p>

            <ul class="profile-info">
              <li><span>✉️</span> {{ userStore.profile?.email || '未绑定邮箱' }}</li>
              <li>
                <span>📅</span> 加入于 {{ formatDate(userStore.profile?.createdAt ?? '') }}
              </li>
            </ul>

            <div class="card-actions">
              <button v-if="!editing" class="primary-btn" type="button" @click="startEdit">
                编辑资料
              </button>
              <button class="logout-btn" type="button" @click="handleLogout">退出登录</button>
            </div>
          </aside>

          <!-- 右侧内容 -->
          <div class="profile-main">
            <!-- 编辑表单 -->
            <section v-if="editing" class="panel">
              <h3 class="panel-title">编辑资料</h3>
              <div class="edit-grid">
                <div class="form-field">
                  <label>昵称</label>
                  <input v-model="editForm.nickname" type="text" />
                </div>
                <div class="form-field">
                  <label>头像字符</label>
                  <input v-model="editForm.avatar" maxlength="2" type="text" />
                </div>
                <div class="form-field">
                  <label>邮箱</label>
                  <input v-model="editForm.email" type="email" />
                </div>
              </div>
              <div class="form-field">
                <label>个人简介</label>
                <textarea v-model="editForm.bio" rows="3"></textarea>
              </div>
              <div class="form-actions">
                <button class="tool-btn" type="button" @click="editing = false">取消</button>
                <button class="primary-btn" type="button" @click="saveProfile">保存</button>
              </div>
            </section>

            <!-- 数据统计 -->
            <section class="panel">
              <h3 class="panel-title">我的数据</h3>
              <div class="stat-grid">
                <div class="stat-box">
                  <span class="stat-value">{{ noteStore.notes.length }}</span>
                  <span class="stat-label">篇笔记</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ resumeStore.total }}</span>
                  <span class="stat-label">份简历</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ noteStore.totalViews }}</span>
                  <span class="stat-label">总阅读</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ noteStore.tags.length }}</span>
                  <span class="stat-label">个标签</span>
                </div>
              </div>
            </section>

            <!-- 快捷入口 -->
            <section class="panel">
              <h3 class="panel-title">快捷操作</h3>
              <div class="quick-grid">
                <router-link to="/notes/create" class="quick-item">
                  <span class="quick-icon blue">✍️</span>
                  <span class="quick-text">
                    <strong>写笔记</strong>
                    <em>记录新的学习内容</em>
                  </span>
                </router-link>
                <router-link to="/resume" class="quick-item">
                  <span class="quick-icon purple">📄</span>
                  <span class="quick-text">
                    <strong>管理简历</strong>
                    <em>编辑并维护简历内容</em>
                  </span>
                </router-link>
                <router-link to="/notes" class="quick-item">
                  <span class="quick-icon green">📚</span>
                  <span class="quick-text">
                    <strong>笔记列表</strong>
                    <em>查看全部笔记</em>
                  </span>
                </router-link>
                <router-link to="/" class="quick-item">
                  <span class="quick-icon orange">🏠</span>
                  <span class="quick-text">
                    <strong>返回首页</strong>
                    <em>回到工作台</em>
                  </span>
                </router-link>
              </div>
            </section>

            <!-- 最近笔记 -->
            <section v-if="noteStore.recentNotes.length" class="panel">
              <div class="panel-head">
                <h3 class="panel-title">最近更新</h3>
                <router-link to="/notes" class="more-link">查看全部 →</router-link>
              </div>
              <ul class="recent-list">
                <li v-for="note in noteStore.recentNotes" :key="note.id">
                  <router-link :to="`/notes/${note.id}`" class="recent-item">
                    <span class="recent-title">{{ note.title }}</span>
                    <span class="recent-time">{{ fromNow(note.updatedAt) }}</span>
                  </router-link>
                </li>
              </ul>
            </section>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'
import { useNoteStore } from '@/stores/useNoteStore'
import { useResumeStore } from '@/stores/useResumeStore'
import { formatDate, fromNow } from '@/utils/datetime'

const userStore = useUserStore()
const noteStore = useNoteStore()
const resumeStore = useResumeStore()
const router = useRouter()

const editing = ref(false)
const editForm = reactive({ nickname: '', avatar: '', email: '', bio: '' })

// 个人中心统计的是「我的笔记」
noteStore.setScope('mine')

onMounted(() => {
  void noteStore.loadMyNotes()
  void resumeStore.loadResumes()
})

function startEdit(): void {
  editForm.nickname = userStore.profile?.nickname ?? ''
  editForm.avatar = userStore.profile?.avatar ?? ''
  editForm.email = userStore.profile?.email ?? ''
  editForm.bio = userStore.profile?.bio ?? ''
  editing.value = true
}

async function saveProfile(): Promise<void> {
  await userStore.updateProfile({
    nickname: editForm.nickname.trim() || userStore.profile?.username || '用户',
    avatar: editForm.avatar.trim().charAt(0) || '用',
    email: editForm.email.trim(),
    bio: editForm.bio.trim()
  })
  editing.value = false
}

function handleLogout(): void {
  userStore.logout()
  void router.push('/')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

.page-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 296px 1fr;
  gap: 24px;
  align-items: start;
}

/* ========== 资料卡 ========== */
.profile-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 28px 24px;
  text-align: center;
  position: sticky;
  top: 88px;
}

.avatar-lg {
  width: 84px;
  height: 84px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 600;
  box-shadow: 0 8px 22px rgba(37, 99, 235, 0.24);
}

.profile-name {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.profile-username {
  margin: 0 0 14px;
  font-size: 13.5px;
  color: #94a3b8;
}

.profile-bio {
  margin: 0 0 18px;
  font-size: 13.5px;
  line-height: 1.7;
  color: #64748b;
  text-align: left;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 10px;
}

.profile-info {
  list-style: none;
  margin: 0 0 20px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
  text-align: left;
}

.profile-info li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: #64748b;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.18s;
}

.primary-btn:hover {
  background: #1d4ed8;
}

.logout-btn {
  padding: 10px;
  border: 1px solid #fecaca;
  border-radius: 10px;
  background: #fff5f5;
  color: #b91c1c;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.16s;
}

.logout-btn:hover {
  background: #fee2e2;
}

/* ========== 右侧面板 ========== */
.profile-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 22px 24px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.panel-head .panel-title {
  margin: 0;
}

.more-link {
  font-size: 13.5px;
  color: #2563eb;
  text-decoration: none;
}

.more-link:hover {
  text-decoration: underline;
}

/* 统计 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 10px;
  border-radius: 12px;
  background: #f8fafc;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2563eb;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  text-decoration: none;
  transition: border-color 0.18s, box-shadow 0.18s, transform 0.18s;
}

.quick-item:hover {
  border-color: #bfdbfe;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.07);
  transform: translateY(-2px);
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.quick-icon.blue {
  background: #dbeafe;
}

.quick-icon.purple {
  background: #ede9fe;
}

.quick-icon.green {
  background: #d1fae5;
}

.quick-icon.orange {
  background: #fef3c7;
}

.quick-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.quick-text strong {
  font-size: 14.5px;
  color: #0f172a;
  font-weight: 600;
}

.quick-text em {
  font-style: normal;
  font-size: 12.5px;
  color: #94a3b8;
}

/* 最近笔记 */
.recent-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 11px 4px;
  border-bottom: 1px solid #f1f5f9;
  text-decoration: none;
  transition: background 0.16s;
}

.recent-item:hover {
  background: #f8fafc;
}

.recent-list li:last-child .recent-item {
  border-bottom: none;
}

.recent-title {
  font-size: 14.5px;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-item:hover .recent-title {
  color: #2563eb;
}

.recent-time {
  font-size: 12.5px;
  color: #94a3b8;
  flex-shrink: 0;
}

/* 编辑表单 */
.edit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.form-field {
  margin-bottom: 14px;
}

.form-field label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.form-field input,
.form-field textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  font-size: 14px;
  font-family: inherit;
  color: #334155;
  outline: none;
  transition: border-color 0.16s;
}

.form-field input:focus,
.form-field textarea:focus {
  border-color: #93c5fd;
}

.form-field textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.tool-btn {
  padding: 9px 18px;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  background: #f8fafc;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.16s;
}

.tool-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #2563eb;
}

/* 空状态 */
.empty-block {
  padding: 72px 24px;
  text-align: center;
  background: #fff;
  border-radius: 16px;
  border: 1px dashed #dbe3ec;
}

.empty-icon {
  font-size: 44px;
  margin: 0 0 12px;
}

.empty-block h3 {
  margin: 0 0 8px;
  font-size: 17px;
  color: #0f172a;
}

.empty-block p {
  margin: 0 0 18px;
  color: #64748b;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-card {
    position: static;
  }

  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .page-inner {
    padding: 20px 16px 48px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
