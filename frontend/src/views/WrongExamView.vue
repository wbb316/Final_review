<template>
  <div class="exam">
    <header class="exam-header">
      <router-link to="/wrong-questions" class="back-link">&larr; 返回错题本</router-link>
      <span class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="finished" class="result-card">
      <h2>练习完成！</h2>
      <p class="result-summary">共练习 {{ questions.length }} 题，本次掌握 {{ masteredCount }} 题</p>
      <router-link to="/wrong-questions" class="result-link">返回错题本</router-link>
    </div>

    <div v-else-if="questions.length === 0" class="empty">
      <p>暂无错题，继续加油！</p>
      <router-link to="/" class="link">返回首页</router-link>
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
        确认答案（已选 {{ [...multiSelected].sort().join(', ') }}）
      </button>

      <!-- 反馈 -->
      <div v-if="answered" class="feedback" :class="finalCorrect ? 'correct' : 'wrong'">
        <template v-if="finalCorrect">
          <template v-if="mastered">已掌握！&#10003;</template>
          <template v-else>回答正确！已答对 {{ getCurrentLocalCount() }}/3</template>
        </template>
        <template v-else>
          答错了，正确答案是：{{ questions[currentIndex].answer }}，重新开始计数
        </template>
      </div>

      <!-- 下一题 / 完成 -->
      <button
        v-if="answered"
        class="next-btn"
        @click="goNext"
      >
        {{ currentIndex < questions.length - 1 ? '下一题' : '完成练习' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWrongQuestions, getWrongQuestionsBySubject, recordWrong, recordCorrect } from '../api/wrongQuestion.js'

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
const loading = ref(true)
const finished = ref(false)
const masteredCount = ref(0)
const mastered = ref(false)

// 本地跟踪每题连续答对次数（以 questionId 为 key）
const localProgressMap = ref({})

function getCurrentLocalCount() {
  const q = questions.value[currentIndex.value]
  if (!q) return 0
  return localProgressMap.value[q.questionId] || 0
}

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
  try {
    const subjectId = route.query.subjectId
    let res
    if (subjectId) {
      res = await getWrongQuestionsBySubject(subjectId)
    } else {
      res = await getWrongQuestions()
    }
    questions.value = res.data
    // 初始化本地进度计数器
    const map = {}
    res.data.forEach(q => {
      map[q.questionId] = q.consecutiveCorrect || 0
    })
    localProgressMap.value = map
  } catch (e) {
    console.error('加载错题失败', e)
  } finally {
    loading.value = false
  }
})

function optionClass(idx) {
  const letter = typeof idx === 'number' ? String.fromCharCode(65 + idx) : idx
  const q = questions.value[currentIndex.value]
  if (!q) return ''
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
  multiSelected.value = [...multiSelected.value]
}

async function confirmMultiAnswer() {
  if (answered.value || multiSelected.value.length === 0) return
  answered.value = true
  const q = questions.value[currentIndex.value]
  const answerStr = [...multiSelected.value].sort().join(',')
  const correct = q.answer.split(',').sort().join(',') === answerStr
  if (correct) {
    const res = await recordCorrect(q.questionId)
    mastered.value = res.data.mastered
    localProgressMap.value[q.questionId]++
    if (mastered.value) masteredCount.value++
  } else {
    localProgressMap.value[q.questionId] = 0
    await recordWrong(q.questionId, q.subjectId)
  }
  answers.value.push({
    questionId: q.questionId,
    content: q.content,
    type: q.type,
    selected: answerStr,
    correctAnswer: q.answer,
    correct
  })
}

async function confirmBlankAnswer() {
  if (answered.value || !blankAnswer.value.trim()) return
  answered.value = true
  const q = questions.value[currentIndex.value]
  const userAnswer = blankAnswer.value.trim()
  blankCorrect.value = userAnswer === q.answer
  if (blankCorrect.value) {
    const res = await recordCorrect(q.questionId)
    mastered.value = res.data.mastered
    localProgressMap.value[q.questionId]++
    if (mastered.value) masteredCount.value++
  } else {
    localProgressMap.value[q.questionId] = 0
    await recordWrong(q.questionId, q.subjectId)
  }
  answers.value.push({
    questionId: q.questionId,
    content: q.content,
    type: q.type,
    selected: userAnswer,
    correctAnswer: q.answer,
    correct: blankCorrect.value
  })
}

async function selectAnswer(answer) {
  if (answered.value) return
  selectedAnswer.value = answer
  answered.value = true
  const q = questions.value[currentIndex.value]
  const correct = answer === q.answer
  if (correct) {
    const res = await recordCorrect(q.questionId)
    mastered.value = res.data.mastered
    localProgressMap.value[q.questionId]++
    if (mastered.value) masteredCount.value++
  } else {
    localProgressMap.value[q.questionId] = 0
    await recordWrong(q.questionId, q.subjectId)
  }
  answers.value.push({
    questionId: q.questionId,
    content: q.content,
    type: q.type,
    selected: answer,
    correctAnswer: q.answer,
    correct
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
    mastered.value = false
  } else {
    finished.value = true
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
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.result-card h2 {
  font-size: 24px;
  color: #1a1a1a;
  margin-bottom: 16px;
}
.result-summary {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}
.result-link {
  display: inline-block;
  padding: 10px 24px;
  background: #409eff;
  color: #fff;
  text-decoration: none;
  border-radius: 8px;
  font-size: 15px;
}
.result-link:hover {
  background: #337ecc;
}
</style>
