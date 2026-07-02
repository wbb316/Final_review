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
      <div class="question-type">{{ typeLabel }}</div>
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

      <!-- 多选题选项 -->
      <div v-else-if="questions[currentIndex].type === 'MULTI'" class="options">
        <div
          v-for="(opt, idx) in parsedOptions"
          :key="idx"
          class="option"
          :class="optionClass(idx)"
          @click="toggleMultiAnswer(String.fromCharCode(65 + idx))"
        >
          <span class="option-label">{{ String.fromCharCode(65 + idx) }}</span>
          <span class="option-text">{{ opt }}</span>
        </div>
      </div>

      <!-- 判断题选项 -->
      <div v-else-if="questions[currentIndex].type === 'JUDGE'" class="options judge-options">
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

      <!-- 填空题 -->
      <div v-else class="blank-section">
        <textarea
          v-model="blankAnswer"
          class="blank-input"
          rows="3"
          :disabled="answered"
          placeholder="请输入答案..."
        ></textarea>
        <button
          v-if="!answered"
          class="confirm-btn"
          :disabled="!blankAnswer.trim()"
          @click="confirmBlankAnswer"
        >
          确认答案
        </button>
      </div>

      <!-- 多选题确认按钮 -->
      <button
        v-if="questions[currentIndex].type === 'MULTI' && !answered && multiSelected.length > 0"
        class="confirm-btn"
        @click="confirmMultiAnswer"
      >
        确认答案（已选 {{ [...multiSelected].sort().join(", ") }}）
      </button>

      <!-- 反馈 -->
      <div v-if="answered" class="feedback" :class="finalCorrect ? 'correct' : 'wrong'">
        {{ finalCorrect ? '回答正确！' : `回答错误，正确答案是：${questions[currentIndex].answer}` }}
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
const multiSelected = ref([])
const blankAnswer = ref('')
const blankCorrect = ref(false)
const answers = ref([])
const answered = ref(false)

const typeLabel = computed(() => {
  const type = questions.value[currentIndex.value]?.type
  if (type === 'SELECT') return '选择题'
  if (type === 'MULTI') return '多选题'
  if (type === 'BLANK') return '填空题'
  return '判断题'
})

const isCorrect = computed(() => {
  const q = questions.value[currentIndex.value]
  if (!q) return false
  if (q.type === 'MULTI') {
    const correct = q.answer.split(',').sort().join(',')
    const selected = [...multiSelected.value].sort().join(',')
    return correct === selected
  }
  if (q.type === 'BLANK') return blankCorrect.value
  return selectedAnswer.value === q.answer
})

const finalCorrect = computed(() => {
  const q = questions.value[currentIndex.value]
  if (!q) return false
  if (q.type === 'BLANK') return blankCorrect.value
  return isCorrect.value
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
  const letter = typeof idx === 'number' ? String.fromCharCode(65 + idx) : idx
  const q = questions.value[currentIndex.value]
  if (!q) return ''
  // 多选题未确认时：显示选中状态
  if (q.type === 'MULTI' && !answered.value) {
    return multiSelected.value.includes(letter) ? 'selected' : ''
  }
  if (!answered.value) return ''
  const correctAnswers = q.answer.split(',')
  if (correctAnswers.includes(letter)) return 'correct'
  if (q.type === 'MULTI') {
    if (multiSelected.value.includes(letter) && !correctAnswers.includes(letter)) return 'wrong'
    return 'disabled'
  }
  if (letter === selectedAnswer.value && letter !== q.answer) return 'wrong'
  return 'disabled'
}

function toggleMultiAnswer(letter) {
  if (answered.value) return
  const idx = multiSelected.value.indexOf(letter)
  if (idx > -1) {
    multiSelected.value.splice(idx, 1)
  } else {
    multiSelected.value.push(letter)
  }
  multiSelected.value = [...multiSelected.value] // trigger reactivity
}

function confirmMultiAnswer() {
  if (answered.value || multiSelected.value.length === 0) return
  answered.value = true
  const q = questions.value[currentIndex.value]
  const answerStr = [...multiSelected.value].sort().join(',')
  const isCorrect = q.answer.split(',').sort().join(',') === answerStr
  answers.value.push({
    questionId: q.id,
    content: q.content,
    type: q.type,
    selected: answerStr,
    correctAnswer: q.answer,
    correct: isCorrect
  })
}

function confirmBlankAnswer() {
  if (answered.value || !blankAnswer.value.trim()) return
  answered.value = true
  const q = questions.value[currentIndex.value]
  const userAnswer = blankAnswer.value.trim()
  blankCorrect.value = userAnswer === q.answer
  answers.value.push({
    questionId: q.id,
    content: q.content,
    type: q.type,
    selected: userAnswer,
    correctAnswer: q.answer,
    correct: blankCorrect.value
  })
}

function selectAnswer(answer) {
  if (answered.value) return
  selectedAnswer.value = answer
  answered.value = true
  const q = questions.value[currentIndex.value]
  answers.value.push({
    questionId: q.id,
    content: q.content,
    type: q.type,
    selected: answer,
    correctAnswer: q.answer,
    correct: answer === q.answer
  })
}

function goNext() {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    answered.value = false
    selectedAnswer.value = null
    multiSelected.value = []
    blankAnswer.value = ''
    blankCorrect.value = false
  } else {
    const correctCount = answers.value.filter(a => a.correct).length
    sessionStorage.setItem('examAnswers', JSON.stringify(answers.value))
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
.option:hover:not(.correct):not(.wrong):not(.disabled):not(.selected) {
  border-color: #409eff;
  background: #f5faff;
}
.option.selected {
  border-color: #409eff;
  background: #ecf5ff;
}
.blank-section {
  margin-top: 20px;
}
.blank-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  resize: vertical;
  font-family: inherit;
  line-height: 1.5;
}
.blank-input:focus {
  border-color: #409eff;
}
.blank-input:disabled {
  background: #f5f7fa;
  color: #333;
}
.option.selected .option-label {
  background: #409eff;
  color: #fff;
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
.confirm-btn {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  background: #e6a23c;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}
.confirm-btn:hover {
  background: #cf9236;
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
