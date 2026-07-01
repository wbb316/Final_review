<template>
  <div class="result">
    <div class="result-card">
      <h1>答题完成！</h1>
      <div class="stats">
        <div class="stat">
          <span class="stat-value">{{ total }}</span>
          <span class="stat-label">总题数</span>
        </div>
        <div class="stat correct-stat">
          <span class="stat-value">{{ correct }}</span>
          <span class="stat-label">答对</span>
        </div>
        <div class="stat wrong-stat">
          <span class="stat-value">{{ wrong }}</span>
          <span class="stat-label">答错</span>
        </div>
        <div class="stat">
          <span class="stat-value">{{ rate }}%</span>
          <span class="stat-label">正确率</span>
        </div>
      </div>

      <div v-if="answers.length > 0" class="review">
        <h2>题目回顾</h2>
        <div
          v-for="(item, idx) in answers"
          :key="idx"
          class="review-item"
          :class="item.correct ? 'review-correct' : 'review-wrong'"
        >
          <div class="review-index">第 {{ idx + 1 }} 题</div>
          <div class="review-content">{{ item.content }}</div>
          <div class="review-detail">
            <span>你的答案：<strong :class="item.correct ? 'text-correct' : 'text-wrong'">{{ item.selected }}</strong></span>
            <span v-if="!item.correct">正确答案：<strong class="text-correct">{{ item.correctAnswer }}</strong></span>
          </div>
        </div>
      </div>

      <div class="actions">
        <router-link to="/" class="btn btn-primary">再来一次</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const total = Number(route.query.total) || 0
const correct = Number(route.query.correct) || 0
const wrong = Number(route.query.wrong) || 0
const answers = ref([])

onMounted(() => {
  const stored = sessionStorage.getItem('examAnswers')
  if (stored) {
    answers.value = JSON.parse(stored)
  }
})

const rate = computed(() => {
  if (total === 0) return 0
  return Math.round((correct / total) * 100)
})
</script>

<style scoped>
.result {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  width: 100%;
  max-width: 600px;
}
.result-card h1 {
  font-size: 28px;
  color: #1a1a1a;
  margin-bottom: 30px;
}
.stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #1a1a1a;
}
.correct-stat .stat-value {
  color: #67c23a;
}
.wrong-stat .stat-value {
  color: #f56c6c;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}
.review {
  text-align: left;
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}
.review h2 {
  font-size: 18px;
  color: #1a1a1a;
  margin-bottom: 16px;
}
.review-item {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  border: 1px solid #dcdfe6;
}
.review-item.review-correct {
  border-color: #67c23a;
  background: #f0f9eb;
}
.review-item.review-wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}
.review-index {
  font-size: 13px;
  color: #999;
  margin-bottom: 6px;
}
.review-content {
  font-size: 15px;
  color: #1a1a1a;
  margin-bottom: 8px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.review-detail {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}
.text-correct {
  color: #67c23a;
}
.text-wrong {
  color: #f56c6c;
}
.actions {
  margin-top: 20px;
}
.btn {
  display: inline-block;
  padding: 12px 40px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 16px;
}
.btn-primary {
  background: #409eff;
  color: #fff;
}
.btn-primary:hover {
  background: #337ecc;
}
</style>
