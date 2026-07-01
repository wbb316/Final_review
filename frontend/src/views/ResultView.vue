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
      <div class="actions">
        <router-link to="/" class="btn btn-primary">再来一次</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const total = Number(route.query.total) || 0
const correct = Number(route.query.correct) || 0
const wrong = Number(route.query.wrong) || 0
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
  max-width: 500px;
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
