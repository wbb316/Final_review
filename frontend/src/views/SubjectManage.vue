<template>
  <div class="manage">
    <header class="header">
      <h1>科目管理</h1>
      <router-link to="/" class="back-link">&larr; 返回首页</router-link>
    </header>

    <!-- 新增 -->
    <div class="add-row">
      <input
        v-model="newName"
        placeholder="输入科目名称"
        @keyup.enter="handleAddSubject"
      />
      <button @click="handleAddSubject">添加</button>
    </div>

    <!-- 列表 -->
    <table v-if="subjects.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>科目名称</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="subject in subjects" :key="subject.id">
          <td>{{ subject.id }}</td>
          <td>
            <template v-if="editingId === subject.id">
              <input v-model="editName" class="edit-input" />
            </template>
            <template v-else>{{ subject.name }}</template>
          </td>
          <td>{{ subject.createTime?.substring(0, 10) || '-' }}</td>
          <td class="actions">
            <template v-if="editingId === subject.id">
              <button class="save-btn" @click="saveEdit(subject.id)">保存</button>
              <button class="cancel-btn" @click="editingId = null">取消</button>
            </template>
            <template v-else>
              <button class="edit-btn" @click="startEdit(subject)">编辑</button>
              <button class="del-btn" @click="delSubject(subject.id)">删除</button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="empty">暂无科目</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSubjects, addSubject, updateSubject, deleteSubject } from '../api/subject.js'

const subjects = ref([])
const newName = ref('')
const editingId = ref(null)
const editName = ref('')

onMounted(loadSubjects)

async function loadSubjects() {
  const res = await getSubjects()
  subjects.value = res.data
}

async function handleAddSubject() {
  const name = newName.value.trim()
  if (!name) return
  await addSubject(name)
  newName.value = ''
  await loadSubjects()
}

function startEdit(subject) {
  editingId.value = subject.id
  editName.value = subject.name
}

async function saveEdit(id) {
  const name = editName.value.trim()
  if (!name) return
  await updateSubject(id, name)
  editingId.value = null
  await loadSubjects()
}

async function delSubject(id) {
  if (!confirm('确定删除该科目吗？（该科目下的题目也会被删除）')) return
  await deleteSubject(id)
  await loadSubjects()
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
.add-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.add-row input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}
.add-row input:focus { border-color: #409eff; }
.add-row button {
  padding: 10px 20px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.add-row button:hover { background: #337ecc; }
table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}
th { background: #f5f7fa; color: #606266; font-weight: 500; }
td { color: #333; }
.edit-input {
  padding: 6px 10px;
  border: 1px solid #409eff;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  width: 100%;
}
.actions { display: flex; gap: 8px; }
.actions button {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.edit-btn { background: #ecf5ff; color: #409eff; }
.save-btn { background: #67c23a; color: #fff; }
.cancel-btn { background: #f5f7fa; color: #606266; }
.del-btn { background: #fef0f0; color: #f56c6c; }
.empty { color: #999; text-align: center; padding: 40px; }
</style>
