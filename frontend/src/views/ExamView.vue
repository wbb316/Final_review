<template>
  <div class="exam">
    <header class="exam-header">
      <router-link to="/" class="back-link">&larr; 返回首页</router-link>
      <span class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
    </header>

    <div v-if="questions.length === 0" class="empty">
      <p>该科目暂无题目，请先添加题目。</p>
      <router-link to="/questions/manage" class="link">去添加题目</router-link>
    </div>

    <div v-else class="question-card">
      <div class="question-type">{{ questions[currentIndex].type === 'SELECT' ? '选择题' : '判断题' }}</div>
      <div class="question-content">{{ questions[currentIndex].content }}</div>

      <!-- 选择题选项 -->
      <div v-if="questions[currentIndex].type === 'SELECT'" class="options">
        <div
          v-for="(opt, idx) in parsedOptions"
          :key="idx"
          class="option"
          :class="optionClass(idx)"
          @click="selectAnswer(String.fromCharCode(65 + idx))"
        >
          <span class="option-label">{{ String.fromCharCode(65 + idx) }}</span>
          <span class="option-text">{{ opt }}</span>
        </div>
      </div>

      <!-- 判断题选项 -->
      <div v-else class="options judge-options">
        <div
          class="option"
          :class="optionClass('正确')"
          @click="selectAnswer('正确')"
        >
          <span class="option-label">正确</span>
        </div>
        <div
          class="option"
          :class="optionClass('错误')"
          @click="selectAnswer('错误')"
        >
          <span class="option-label">错误</span>
        </div>
      </div>

      <!-- 反馈 -->
      <div v-if="answered" class="feedback" :class="isCorrect ? 'correct' : 'wrong'">
        {{ isCorrect ? '回答正确！' : `回答错误，正确答案是：${questions[currentIndex].answer}` }}
      </div>

      <!-- 下一题 / 查看结果 -->
      <button
        v-if="answered"
        class="next-btn"
        @click="goNext"
      >
        {{ currentIndex < questions.length - 1 ? '下一题' : '查看结果' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRandomQuestions } from '../api/review.js'

const route = useRoute()
const router = useRouter()
const questions = ref([])
const currentIndex = ref(0)
const selectedAnswer = ref(null)
const answers = ref([])
const answered = ref(false)

const isCorrect = computed(() => {
  return selectedAnswer.value === questions.value[currentIndex.value]?.answer
})

const parsedOptions = computed(() => {
  const q = questions.value[currentIndex.value]
  if (!q || !q.options) return []
  try {
    return JSON.parse(q.options)
  } catch {
    return []
  }
})

onMounted(async () => {
  const subjectId = Number(route.params.subjectId)
  const count = Number(route.params.count)
  const res = await getRandomQuestions(subjectId, count)
  questions.value = res.data
})

function optionClass(idx) {
  if (!answered.value) return ''
  const letter = typeof idx === 'number' ? String.fromCharCode(65 + idx) : idx
  const q = questions.value[currentIndex.value]
  if (letter === q.answer) return 'correct'
  if (letter === selectedAnswer.value && letter !== q.answer) return 'wrong'
  return 'disabled'
}

function selectAnswer(answer) {
  if (answered.value) return
  selectedAnswer.value = answer
  answered.value = true
  answers.value.push({
    questionId: questions.value[currentIndex.value].id,
    selected: answer,
    correct: answer === questions.value[currentIndex.value].answer
  })
}

function goNext() {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    answered.value = false
    selectedAnswer.value = null
  } else {
    const correctCount = answers.value.filter(a => a.correct).length
    router.push({
      name: 'Result',
      query: {
        total: questions.value.length,
        correct: correctCount,
        wrong: questions.value.length - correctCount
      }
    })
  }
}
</script>

<style scoped>
.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.back-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}
.back-link:hover {
  text-decoration: underline;
}
.progress {
  font-size: 16px;
  color: #666;
}
.empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
.link {
  color: #409eff;
  text-decoration: none;
}
.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.question-type {
  display: inline-block;
  padding: 4px 12px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 15px;
}
.question-content {
  font-size: 18px;
  color: #1a1a1a;
  line-height: 1.6;
  margin-bottom: 25px;
}
.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.judge-options {
  flex-direction: row;
  gap: 20px;
}
.judge-options .option {
  flex: 1;
  text-align: center;
  padding: 20px;
}
.option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.option:hover:not(.correct):not(.wrong):not(.disabled) {
  border-color: #409eff;
  background: #f5faff;
}
.option-label {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}
.option.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}
.option.correct .option-label {
  background: #67c23a;
  color: #fff;
}
.option.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}
.option.wrong .option-label {
  background: #f56c6c;
  color: #fff;
}
.option.disabled {
  opacity: 0.6;
  cursor: default;
}
.feedback {
  margin-top: 20px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 15px;
}
.feedback.correct {
  background: #f0f9eb;
  color: #67c23a;
}
.feedback.wrong {
  background: #fef0f0;
  color: #f56c6c;
}
.next-btn {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}
.next-btn:hover {
  background: #337ecc;
}
</style>
