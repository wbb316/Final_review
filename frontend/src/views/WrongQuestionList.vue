<template>
  <div class="wrong-list">
    <header class="header">
      <h1>错题本</h1>
      <router-link to="/" class="back-link">&larr; 返回首页</router-link>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="groups.length === 0" class="empty">
      <p>暂无错题，继续加油！</p>
    </div>

    <div v-else>
      <!-- 全部练习按钮 -->
      <button class="practice-all" @click="startAllPractice">
        开始全部练习 ({{ totalCount }} 题)
      </button>

      <div v-for="group in groups" :key="group.subjectId" class="subject-group">
        <div class="subject-header">
          <h2 class="subject-name">{{ group.subjectName }}</h2>
          <button class="practice-btn" @click="startSubjectPractice(group.subjectId)">
            开始练习
          </button>
        </div>

        <div class="question-list">
          <div v-for="item in group.questions" :key="item.id" class="question-item">
            <span class="type-tag" :class="'type-' + item.type">{{ typeLabel(item.type) }}</span>
            <span class="question-content">{{ item.content }}</span>
            <span class="progress">已答对 {{ item.consecutiveCorrect }}/3</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWrongQuestions } from '../api/wrongQuestion.js'
import { getSubjects } from '../api/subject.js'

const router = useRouter()
const loading = ref(true)
const groups = ref([])
const totalCount = ref(0)

onMounted(async () => {
  try {
    const [wrongRes, subjectRes] = await Promise.all([
      getWrongQuestions(),
      getSubjects()
    ])
    const wrongQuestions = wrongRes.data
    const subjects = subjectRes.data
    const subjectMap = {}
    subjects.forEach(s => { subjectMap[s.id] = s.name })

    totalCount.value = wrongQuestions.length

    // Group by subject
    const groupMap = {}
    wrongQuestions.forEach(wq => {
      const sid = wq.subjectId
      if (!groupMap[sid]) {
        groupMap[sid] = {
          subjectId: sid,
          subjectName: wq.subjectName || subjectMap[sid] || '未知科目',
          questions: []
        }
      }
      groupMap[sid].questions.push(wq)
    })
    groups.value = Object.values(groupMap)
  } catch (e) {
    console.error('加载错题失败', e)
  } finally {
    loading.value = false
  }
})

function typeLabel(type) {
  if (type === 'SELECT') return '选择题'
  if (type === 'MULTI') return '多选题'
  if (type === 'BLANK') return '填空题'
  if (type === 'JUDGE') return '判断题'
  return type
}

function startSubjectPractice(subjectId) {
  router.push({ name: 'WrongExam', query: { subjectId } })
}

function startAllPractice() {
  router.push({ name: 'WrongExam' })
}
</script>

<style scoped>
.wrong-list {
  max-width: 600px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h1 {
  font-size: 24px;
  color: #1a1a1a;
}
.back-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}
.back-link:hover {
  text-decoration: underline;
}
.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
.empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
.practice-all {
  display: block;
  width: 100%;
  padding: 14px;
  margin-bottom: 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.practice-all:hover {
  background: #337ecc;
}
.subject-group {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.subject-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.subject-name {
  font-size: 18px;
  color: #333;
  margin: 0;
}
.practice-btn {
  padding: 6px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.practice-btn:hover {
  background: #337ecc;
}
.question-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.question-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.question-item:last-child {
  border-bottom: none;
}
.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
  flex-shrink: 0;
}
.type-tag.type-SELECT { background: #409eff; }
.type-tag.type-MULTI { background: #e6a23c; }
.type-tag.type-JUDGE { background: #67c23a; }
.type-tag.type-BLANK { background: #909399; }
.question-content {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}
.progress {
  flex-shrink: 0;
  font-size: 12px;
  color: #e6a23c;
  font-weight: 600;
}
</style>
