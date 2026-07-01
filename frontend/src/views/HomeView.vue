<template>
  <div class="home">
    <header class="header">
      <h1>期末复习工具</h1>
      <div class="nav-links">
        <router-link to="/subjects/manage">科目管理</router-link>
        <span class="sep">|</span>
        <router-link to="/questions/manage">题目管理</router-link>
      </div>
    </header>

    <!-- 选择科目 -->
    <section class="section">
      <h2>选择科目</h2>
      <div class="subject-grid">
        <div
          v-for="subject in subjects"
          :key="subject.id"
          class="subject-card"
          :class="{ selected: selectedSubject?.id === subject.id }"
          @click="selectedSubject = subject"
        >
          <div class="subject-name">{{ subject.name }}</div>
          <div class="subject-count">{{ subject.questionCount ?? 0 }} 道题</div>
        </div>
      </div>
    </section>

    <!-- 抽题数量 -->
    <section class="section" v-if="selectedSubject">
      <h2>抽取题目数量</h2>
      <div class="count-input">
        <button @click="count = Math.max(1, count - 1)">-</button>
        <input type="number" v-model.number="count" min="1" max="100" />
        <button @click="count = Math.min(100, count + 1)">+</button>
      </div>
    </section>

    <!-- 开始答题 -->
    <button
      v-if="selectedSubject"
      class="start-btn"
      @click="startExam"
    >
      开始答题
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSubjects } from '../api/subject.js'

const router = useRouter()
const subjects = ref([])
const selectedSubject = ref(null)
const count = ref(10)

onMounted(async () => {
  const res = await getSubjects()
  subjects.value = res.data
})

function startExam() {
  if (!selectedSubject.value) return
  router.push({
    name: 'Exam',
    params: {
      subjectId: selectedSubject.value.id,
      count: count.value
    }
  })
}
</script>

<style scoped>
.home {
  max-width: 500px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}
.header h1 {
  font-size: 24px;
  color: #1a1a1a;
}
.nav-links a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}
.nav-links a:hover {
  text-decoration: underline;
}
.sep {
  margin: 0 8px;
  color: #ccc;
}
.section {
  margin-bottom: 30px;
}
.section h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}
.subject-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.subject-card {
  padding: 20px 16px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
}
.subject-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}
.subject-count {
  font-size: 13px;
  color: #999;
}
.subject-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}
.subject-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
  font-weight: bold;
}
.count-input {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.count-input button {
  width: 36px;
  height: 36px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  font-size: 18px;
  cursor: pointer;
}
.count-input button:hover {
  border-color: #409eff;
  color: #409eff;
}
.count-input input {
  width: 100px;
  height: 36px;
  text-align: center;
  font-size: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  outline: none;
}
.count-input input:focus {
  border-color: #409eff;
}
.start-btn {
  display: block;
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
  padding: 14px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: background 0.2s;
}
.start-btn:hover {
  background: #337ecc;
}
</style>
