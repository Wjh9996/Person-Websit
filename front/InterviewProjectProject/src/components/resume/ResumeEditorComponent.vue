<template>
  <!-- 遮罩层 -->
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal-container">
      <header class="modal-header">
        <h2>✏️ 编辑简历</h2>
        <button class="close-btn" @click="close">✕</button>
      </header>

      <div class="modal-body">
        <!-- 基本信息 -->
        <section class="edit-section">
          <h3>基本信息</h3>
          <div class="form-row">
            <label>姓名</label>
            <input v-model="localData.basicInfo.name" type="text" />
          </div>
          <div class="form-row">
            <label>求职意向</label>
            <input v-model="localData.basicInfo.title" type="text" />
          </div>
          <div class="form-row">
            <label>电话</label>
            <input v-model="localData.basicInfo.phone" type="text" />
          </div>
          <div class="form-row">
            <label>邮箱</label>
            <input v-model="localData.basicInfo.email" type="text" />
          </div>
        </section>

        <!-- 教育背景 -->
        <section class="edit-section">
          <h3>教育背景</h3>
          <div class="form-row">
            <label>学校</label>
            <input v-model="localData.education.school" type="text" />
          </div>
          <div class="form-row">
            <label>专业</label>
            <input v-model="localData.education.major" type="text" />
          </div>
          <div class="form-row">
            <label>时间</label>
            <input v-model="localData.education.period" type="text" />
          </div>
        </section>

        <!-- 个人总结 -->
        <section class="edit-section">
          <h3>个人总结</h3>
          <div
            v-for="(item, index) in localData.summary"
            :key="index"
            class="array-item"
          >
            <input v-model="localData.summary[index]" type="text" placeholder="总结条目" />
            <button class="remove-btn" @click="removeSummary(index)">✕</button>
          </div>
          <button class="add-btn" @click="addSummary">+ 添加总结</button>
        </section>

        <!-- 校园经历 -->
        <section class="edit-section">
          <h3>校园经历</h3>
          <div
            v-for="(item, index) in localData.campus"
            :key="index"
            class="array-item compound"
          >
            <input v-model="item.period" type="text" placeholder="时间" />
            <input v-model="item.description" type="text" placeholder="描述" />
            <button class="remove-btn" @click="removeCampus(index)">✕</button>
          </div>
          <button class="add-btn" @click="addCampus">+ 添加经历</button>
        </section>

        <!-- 专业技能 -->
        <section class="edit-section">
          <h3>专业技能</h3>
          <div
            v-for="(skill, index) in localData.skills"
            :key="index"
            class="array-item"
          >
            <textarea v-model="localData.skills[index]" rows="2" placeholder="技能描述"></textarea>
            <button class="remove-btn" @click="removeSkill(index)">✕</button>
          </div>
          <button class="add-btn" @click="addSkill">+ 添加技能</button>
        </section>

        <!-- 实习经历 -->
        <section class="edit-section">
          <h3>实习经历</h3>
          <div
            v-for="(job, idx) in localData.internships"
            :key="idx"
            class="array-item compound"
          >
            <div class="form-row">
              <label>公司</label>
              <input v-model="job.company" type="text" />
            </div>
            <div class="form-row">
              <label>职位</label>
              <input v-model="job.position" type="text" />
            </div>
            <div class="form-row">
              <label>时间</label>
              <input v-model="job.period" type="text" />
            </div>
            <div class="form-row">
              <label>职责列表</label>
              <div
                v-for="(duty, dIdx) in job.duties"
                :key="dIdx"
                class="array-item inline"
              >
                <input v-model="job.duties[dIdx]" type="text" placeholder="职责描述" />
                <button class="remove-btn" @click="removeDuty(idx, dIdx)">✕</button>
              </div>
              <button class="add-btn small" @click="addDuty(idx)">+ 添加职责</button>
            </div>
            <button class="remove-btn" @click="removeInternship(idx)">删除此实习</button>
          </div>
          <button class="add-btn" @click="addInternship">+ 添加实习经历</button>
        </section>

        <!-- 项目经历 -->
        <section class="edit-section">
          <h3>项目经历</h3>
          <div
            v-for="(proj, pIdx) in localData.projects"
            :key="pIdx"
            class="array-item compound"
          >
            <div class="form-row">
              <label>项目名称</label>
              <input v-model="proj.name" type="text" />
            </div>
            <div class="form-row">
              <label>技术栈</label>
              <input v-model="proj.techStack" type="text" />
            </div>
            <div class="form-row">
              <label>项目描述</label>
              <textarea v-model="proj.description" rows="3"></textarea>
            </div>
            <div class="form-row">
              <label>职责列表</label>
              <div
                v-for="(duty, dIdx) in proj.duties"
                :key="dIdx"
                class="array-item inline"
              >
                <textarea v-model="proj.duties[dIdx]" rows="2" placeholder="职责描述"></textarea>
                <button class="remove-btn" @click="removeProjectDuty(pIdx, dIdx)">✕</button>
              </div>
              <button class="add-btn small" @click="addProjectDuty(pIdx)">+ 添加职责</button>
            </div>
            <button class="remove-btn" @click="removeProject(pIdx)">删除此项目</button>
          </div>
          <button class="add-btn" @click="addProject">+ 添加项目经历</button>
        </section>
      </div>

      <footer class="modal-footer">
        <button class="btn-cancel" @click="close">取消</button>
        <button class="btn-save" @click="save">保存修改</button>
      </footer>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch, type PropType } from 'vue'
import type { ResumeData } from '@/types/resume'

export default defineComponent({
  name: 'ResumeEditorModal',
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    resumeData: {
      type: Object as PropType<ResumeData>,
      required: true
    }
  },
  emits: ['update:visible', 'update:resumeData', 'save'],
  setup(props, { emit }) {
    // 本地数据副本（深拷贝）
    const localData = ref<ResumeData>(JSON.parse(JSON.stringify(props.resumeData)))

    // 当外部传入数据变化时，同步本地副本（如打开弹窗时重新加载）
    watch(() => props.resumeData, (newVal) => {
      if (newVal) {
        localData.value = JSON.parse(JSON.stringify(newVal))
      }
    }, { deep: true })

    // 关闭弹窗
    const close = () => {
      emit('update:visible', false)
    }

    // 保存
    const save = () => {
      // 深拷贝一份用于保存，避免引用
      const saved = JSON.parse(JSON.stringify(localData.value))
      emit('update:resumeData', saved)
      emit('save', saved)
      // 不关闭，由父组件控制关闭
    }

    // ===== 数组操作函数 =====
    // 个人总结
    const addSummary = () => {
      localData.value.summary.push('')
    }
    const removeSummary = (index: number) => {
      localData.value.summary.splice(index, 1)
    }

    // 校园经历
    const addCampus = () => {
      localData.value.campus.push({ period: '', description: '' })
    }
    const removeCampus = (index: number) => {
      localData.value.campus.splice(index, 1)
    }

    // 专业技能
    const addSkill = () => {
      localData.value.skills.push('')
    }
    const removeSkill = (index: number) => {
      localData.value.skills.splice(index, 1)
    }

    // 实习经历
    const addInternship = () => {
      localData.value.internships.push({
        company: '',
        position: '',
        period: '',
        duties: ['']
      })
    }
    const removeInternship = (index: number) => {
      localData.value.internships.splice(index, 1)
    }
    const addDuty = (internIdx: number) => {
      localData.value.internships[internIdx]?.duties.push('')
    }
    const removeDuty = (internIdx: number, dutyIdx: number) => {
      localData.value.internships[internIdx]?.duties.splice(dutyIdx, 1)
    }

    // 项目经历
    const addProject = () => {
      localData.value.projects.push({
        name: '',
        techStack: '',
        description: '',
        duties: ['']
      })
    }
    const removeProject = (index: number) => {
      localData.value.projects.splice(index, 1)
    }
    const addProjectDuty = (projIdx: number) => {
      localData.value.projects[projIdx]?.duties.push('')
    }
    const removeProjectDuty = (projIdx: number, dutyIdx: number) => {
      localData.value.projects[projIdx]?.duties.splice(dutyIdx, 1)
    }

    return {
      localData,
      close,
      save,
      addSummary,
      removeSummary,
      addCampus,
      removeCampus,
      addSkill,
      removeSkill,
      addInternship,
      removeInternship,
      addDuty,
      removeDuty,
      addProject,
      removeProject,
      addProjectDuty,
      removeProjectDuty
    }
  }
})
</script>

<style scoped>
@import '@/assets/styles/components/editor.css';
</style>
