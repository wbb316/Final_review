<template>
  <div class="manage">
    <header class="header">
      <h1>题目管理</h1>
      <router-link to="/" class="back-link">&larr; 返回首页</router-link>
    </header>

    <!-- 选择科目 -->
    <div class="filter-row">
      <label>选择科目：</label>
      <select v-model="selectedSubjectId" @change="loadQuestions">
        <option value="">-- 请选择 --</option>
        <option v-for="s in subjects" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <button class="add-btn" :disabled="!selectedSubjectId" @click="showAdd = true">新增题目</button>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showAdd || editingQuestion" class="modal-overlay" @click.self="closeForm">
      <div class="modal">
        <h3>{{ editingQuestion ? '编辑题目' : '新增题目' }}</h3>
        <div class="form-group">
          <label>题型</label>
          <select v-model="form.type">
            <option value="SELECT">单选题</option>
            <option value="MULTI">多选题</option>
            <option value="JUDGE">判断题</option>
          </select>
        </div>
        <div class="form-group">
          <label>题目内容</label>
          <textarea v-model="form.content" rows="3"></textarea>
        </div>
        <div v-if="form.type === 'SELECT' || form.type === 'MULTI'" class="form-group">
          <label>选项（一行一个）</label>
          <textarea v-model="optionsText" rows="4" placeholder="选项A&#10;选项B&#10;选项C&#10;选项D"></textarea>
        </div>
        <div class="form-group">
          <label>正确答案</label>
          <div v-if="form.type === 'SELECT'">
            <select v-model="form.answer">
              <option value="">-- 请选择 --</option>
              <option value="A">A</option>
              <option value="B">B</option>
              <option value="C">C</option>
              <option value="D">D</option>
            </select>
          </div>
          <div v-else-if="form.type === 'MULTI'">
            <div class="multi-answer-checkboxes">
              <label v-for="letter in ['A','B','C','D','E']" :key="letter" class="multi-checkbox">
                <input type="checkbox" :value="letter" :checked="multiAnswer.includes(letter)" @change="toggleMultiAnswer(letter)" />
                {{ letter }}
              </label>
            </div>
          </div>
          <div v-else>
            <select v-model="form.answer">
              <option value="">-- 请选择 --</option>
              <option value="正确">正确</option>
              <option value="错误">错误</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button class="save-btn" @click="saveQuestion">保存</button>
          <button class="cancel-btn" @click="closeForm">取消</button>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <table v-if="questions.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>题型</th>
          <th>题目内容</th>
          <th>答案</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="q in questions" :key="q.id">
          <td>{{ q.id }}</td>
          <td>{{ q.type === 'SELECT' ? '单选题' : q.type === 'MULTI' ? '多选题' : '判断题' }}</td>
          <td class="content-cell">{{ q.content }}</td>
          <td>{{ q.answer }}</td>
          <td class="actions">
            <button class="edit-btn" @click="startEdit(q)">编辑</button>
            <button class="del-btn" @click="delQuestion(q.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else-if="selectedSubjectId" class="empty">该科目暂无题目</p>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getSubjects } from '../api/subject.js'
import { getQuestions, addQuestion, updateQuestion, deleteQuestion } from '../api/question.js'

const subjects = ref([])
const selectedSubjectId = ref('')
const questions = ref([])
const showAdd = ref(false)
const editingQuestion = ref(null)
const optionsText = ref('')

const multiAnswer = ref([])

const form = ref({
  type: 'SELECT',
  content: '',
  answer: ''
})

function toggleMultiAnswer(letter) {
  const idx = multiAnswer.value.indexOf(letter)
  if (idx > -1) {
    multiAnswer.value.splice(idx, 1)
  } else {
    multiAnswer.value.push(letter)
  }
  form.value.answer = [...multiAnswer.value].sort().join(',')
}

onMounted(async () => {
  const res = await getSubjects()
  subjects.value = res.data
})

watch(selectedSubjectId, () => {
  if (!selectedSubjectId.value) questions.value = []
})

watch(() => form.value.type, () => {
  form.value.answer = ''
  optionsText.value = ''
  multiAnswer.value = []
})

async function loadQuestions() {
  if (!selectedSubjectId.value) return
  const res = await getQuestions(selectedSubjectId.value)
  questions.value = res.data
}

function closeForm() {
  showAdd.value = false
  editingQuestion.value = null
  form.value = { type: 'SELECT', content: '', answer: '' }
  optionsText.value = ''
  multiAnswer.value = []
}

function startEdit(q) {
  editingQuestion.value = q
  form.value = {
    type: q.type,
    content: q.content,
    answer: q.answer
  }
  if (q.type === 'MULTI') {
    multiAnswer.value = q.answer ? q.answer.split(',') : []
  } else {
    multiAnswer.value = []
  }
  if ((q.type === 'SELECT' || q.type === 'MULTI') && q.options) {
    try {
      optionsText.value = JSON.parse(q.options).join('\n')
    } catch {
      optionsText.value = ''
    }
  }
}

async function saveQuestion() {
  if (!form.value.content.trim() || !form.value.answer) return
  const question = {
    subjectId: Number(selectedSubjectId.value),
    type: form.value.type,
    content: form.value.content,
    answer: form.value.answer
  }
  if (form.value.type === 'SELECT' || form.value.type === 'MULTI') {
    question.options = JSON.stringify(
      optionsText.value.split('\n').filter(s => s.trim())
    )
  }
  if (editingQuestion.value) {
    await updateQuestion(editingQuestion.value.id, question)
  } else {
    await addQuestion(question)
  }
  closeForm()
  await loadQuestions()
}

async function delQuestion(id) {
  if (!confirm('确定删除该题目吗？')) return
  await deleteQuestion(id)
  await loadQuestions()
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header h1 { font-size: 22px; color: #1a1a1a; }
.back-link { color: #409eff; text-decoration: none; font-size: 14px; }
.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-row label { font-size: 14px; color: #606266; }
.filter-row select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  flex: 1;
}
.filter-row select:focus { border-color: #409eff; }
.add-btn {
  padding: 8px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
}
.add-btn:disabled { background: #a0cfff; cursor: not-allowed; }
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
}
.modal h3 { margin-bottom: 20px; font-size: 18px; color: #1a1a1a; }
.form-group { margin-bottom: 15px; }
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}
.form-group select:focus,
.form-group textarea:focus { border-color: #409eff; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
.modal-actions button {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.save-btn { background: #409eff; color: #fff; }
.cancel-btn { background: #f5f7fa; color: #606266; }
.multi-answer-checkboxes { display: flex; gap: 15px; }
.multi-checkbox { display: flex; align-items: center; gap: 4px; cursor: pointer; font-size: 14px; }
table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #ebeef5; }
th { background: #f5f7fa; color: #606266; font-weight: 500; }
td { color: #333; }
.content-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.actions { display: flex; gap: 8px; }
.actions button { padding: 4px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.edit-btn { background: #ecf5ff; color: #409eff; }
.del-btn { background: #fef0f0; color: #f56c6c; }
.empty { color: #999; text-align: center; padding: 40px; }
</style>
