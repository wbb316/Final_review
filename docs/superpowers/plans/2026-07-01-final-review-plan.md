# 期末复习工具 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个前后端分离的期末复习工具，支持科目管理、题目管理、随机抽题刷题

**Architecture:** 前后端分离架构，后端 Spring Boot 3.5 + MyBatis + MySQL 提供 REST API，前端 Vue 3 + Vite 构建 SPA 页面

**Tech Stack:** Spring Boot 3.5.16, MyBatis 3.0.5, MySQL, Vue 3, Vite, Axios, Vue Router 4

## Global Constraints

- Java 17, Spring Boot 3.5.16, MyBatis 3.0.5
- MySQL 数据库，默认端口 3306，数据库名 `final_review`
- Vue 3 + Vite，使用 Vue Router 4
- 前端开发服务器端口 5173，后端端口 8080
- 跨域使用 Vite proxy 代理解决
- 所有 API 路径以 `/api/` 开头

---

### Task 1: 后端基础配置与数据库初始化

**Files:**
- Modify: `src/main/resources/application.properties`
- Create: `src/main/resources/schema.sql`
- Create: `src/main/resources/data.sql`

**Interfaces:**
- Consumes: 无
- Produces: 数据库表结构 `subject` 和 `question`，基础配置

- [ ] **Step 1: 配置 application.properties**

写入以下内容到 `src/main/resources/application.properties`:

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/final_review?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true
```

- [ ] **Step 2: 创建 schema.sql**

写入 `src/main/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL COMMENT 'SELECT or JUDGE',
    content TEXT NOT NULL,
    options TEXT COMMENT 'JSON array for SELECT, null for JUDGE',
    answer VARCHAR(500) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE
);
```

- [ ] **Step 3: 创建 data.sql**

写入 `src/main/resources/data.sql`:

```sql
-- 先清空数据（按顺序避免外键冲突）
DELETE FROM question;
DELETE FROM subject;

-- 插入示例科目
INSERT INTO subject (id, name) VALUES (1, '数据库'), (2, '习近平新时代中国特色社会主义思想概论'), (3, '毛泽东思想和中国特色社会主义理论体系概论');

-- 重置自增 ID
ALTER TABLE subject AUTO_INCREMENT = 4;
```

- [ ] **Step 4: 验证数据库配置**

```bash
# 确认 MySQL 已运行并可连接
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS final_review DEFAULT CHARACTER SET utf8mb4;"
```

- [ ] **Step 5: 提交**

```bash
git add src/main/resources/application.properties src/main/resources/schema.sql src/main/resources/data.sql
git commit -m "feat: add database config and schema"
```

---

### Task 2: 实体类与 Mapper 层

**Files:**
- Create: `src/main/java/cn/itcast/final_review/entity/Subject.java`
- Create: `src/main/java/cn/itcast/final_review/entity/Question.java`
- Create: `src/main/java/cn/itcast/final_review/mapper/SubjectMapper.java`
- Create: `src/main/java/cn/itcast/final_review/mapper/QuestionMapper.java`
- Create: `src/main/resources/mapper/SubjectMapper.xml`
- Create: `src/main/resources/mapper/QuestionMapper.xml`

**Interfaces:**
- Consumes: Task 1 数据库配置
- Produces: `SubjectMapper` + `QuestionMapper` 提供完整 CRUD

- [ ] **Step 1: 创建 Subject 实体**

`src/main/java/cn/itcast/final_review/entity/Subject.java`:

```java
package cn.itcast.final_review.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Subject {
    private Long id;
    private String name;
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 创建 Question 实体**

`src/main/java/cn/itcast/final_review/entity/Question.java`:

```java
package cn.itcast.final_review.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long subjectId;
    private String type;       // SELECT / JUDGE
    private String content;
    private String options;    // JSON数组字符串，判断题null
    private String answer;
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 创建 SubjectMapper**

`src/main/java/cn/itcast/final_review/mapper/SubjectMapper.java`:

```java
package cn.itcast.final_review.mapper;

import cn.itcast.final_review.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SubjectMapper {
    List<Subject> findAll();
    Subject findById(Long id);
    void insert(Subject subject);
    void update(Subject subject);
    void deleteById(Long id);
}
```

- [ ] **Step 4: 创建 QuestionMapper**

`src/main/java/cn/itcast/final_review/mapper/QuestionMapper.java`:

```java
package cn.itcast.final_review.mapper;

import cn.itcast.final_review.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionMapper {
    List<Question> findBySubjectId(@Param("subjectId") Long subjectId);
    Question findById(Long id);
    void insert(Question question);
    void update(Question question);
    void deleteById(Long id);
    void deleteBySubjectId(Long subjectId);
    List<Question> findRandom(@Param("subjectId") Long subjectId, @Param("count") int count);
    Long countBySubjectId(@Param("subjectId") Long subjectId);
}
```

- [ ] **Step 5: 创建 SubjectMapper.xml**

`src/main/resources/mapper/SubjectMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.itcast.final_review.mapper.SubjectMapper">
    <resultMap id="BaseResultMap" type="cn.itcast.final_review.entity.Subject">
        <id property="id" column="id"/>
        <result property="name" column="name"/>
        <result property="createTime" column="create_time"/>
    </resultMap>

    <select id="findAll" resultMap="BaseResultMap">
        SELECT id, name, create_time FROM subject ORDER BY id
    </select>

    <select id="findById" resultMap="BaseResultMap">
        SELECT id, name, create_time FROM subject WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO subject (name) VALUES (#{name})
    </insert>

    <update id="update">
        UPDATE subject SET name = #{name} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM subject WHERE id = #{id}
    </delete>
</mapper>
```

- [ ] **Step 6: 创建 QuestionMapper.xml**

`src/main/resources/mapper/QuestionMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.itcast.final_review.mapper.QuestionMapper">
    <resultMap id="BaseResultMap" type="cn.itcast.final_review.entity.Question">
        <id property="id" column="id"/>
        <result property="subjectId" column="subject_id"/>
        <result property="type" column="type"/>
        <result property="content" column="content"/>
        <result property="options" column="options"/>
        <result property="answer" column="answer"/>
        <result property="createTime" column="create_time"/>
    </resultMap>

    <select id="findBySubjectId" resultMap="BaseResultMap">
        SELECT id, subject_id, type, content, options, answer, create_time
        FROM question WHERE subject_id = #{subjectId} ORDER BY id
    </select>

    <select id="findById" resultMap="BaseResultMap">
        SELECT id, subject_id, type, content, options, answer, create_time
        FROM question WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO question (subject_id, type, content, options, answer)
        VALUES (#{subjectId}, #{type}, #{content}, #{options}, #{answer})
    </insert>

    <update id="update">
        UPDATE question
        SET subject_id = #{subjectId}, type = #{type}, content = #{content},
            options = #{options}, answer = #{answer}
        WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM question WHERE id = #{id}
    </delete>

    <delete id="deleteBySubjectId">
        DELETE FROM question WHERE subject_id = #{subjectId}
    </delete>

    <select id="findRandom" resultMap="BaseResultMap">
        SELECT id, subject_id, type, content, options, answer, create_time
        FROM question
        WHERE subject_id = #{subjectId}
        ORDER BY RAND()
        LIMIT #{count}
    </select>

    <select id="countBySubjectId" resultType="long">
        SELECT COUNT(*) FROM question WHERE subject_id = #{subjectId}
    </select>
</mapper>
```

- [ ] **Step 7: 编译验证**

```bash
cd D:/WBB_JAVA/Final_review
./mvnw compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 8: 提交**

```bash
git add src/main/java/cn/itcast/final_review/entity/ src/main/java/cn/itcast/final_review/mapper/ src/main/resources/mapper/
git commit -m "feat: add entities and mapper layer"
```

---

### Task 3: Service 层

**Files:**
- Create: `src/main/java/cn/itcast/final_review/service/SubjectService.java`
- Create: `src/main/java/cn/itcast/final_review/service/QuestionService.java`

**Interfaces:**
- Consumes: `SubjectMapper`, `QuestionMapper` from Task 2
- Produces: `SubjectService` (findAll, findById, save, update, delete), `QuestionService` (findBySubjectId, findById, save, update, delete, findRandom)

- [ ] **Step 1: 创建 SubjectService**

`src/main/java/cn/itcast/final_review/service/SubjectService.java`:

```java
package cn.itcast.final_review.service;

import cn.itcast.final_review.entity.Subject;
import cn.itcast.final_review.mapper.SubjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubjectService {
    private final SubjectMapper subjectMapper;

    public SubjectService(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    public List<Subject> findAll() {
        return subjectMapper.findAll();
    }

    public Subject findById(Long id) {
        return subjectMapper.findById(id);
    }

    public Subject save(Subject subject) {
        subjectMapper.insert(subject);
        return subject;
    }

    public Subject update(Long id, Subject subject) {
        subject.setId(id);
        subjectMapper.update(subject);
        return subject;
    }

    public void delete(Long id) {
        subjectMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 创建 QuestionService**

`src/main/java/cn/itcast/final_review/service/QuestionService.java`:

```java
package cn.itcast.final_review.service;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<Question> findBySubjectId(Long subjectId) {
        return questionMapper.findBySubjectId(subjectId);
    }

    public Question findById(Long id) {
        return questionMapper.findById(id);
    }

    public Question save(Question question) {
        questionMapper.insert(question);
        return question;
    }

    public Question update(Long id, Question question) {
        question.setId(id);
        questionMapper.update(question);
        return question;
    }

    public void delete(Long id) {
        questionMapper.deleteById(id);
    }

    public List<Question> findRandom(Long subjectId, int count) {
        long total = questionMapper.countBySubjectId(subjectId);
        int actualCount = (int) Math.min(count, total);
        return questionMapper.findRandom(subjectId, actualCount);
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
./mvnw compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/cn/itcast/final_review/service/
git commit -m "feat: add service layer"
```

---

### Task 4: Controller 层

**Files:**
- Create: `src/main/java/cn/itcast/final_review/controller/SubjectController.java`
- Create: `src/main/java/cn/itcast/final_review/controller/QuestionController.java`
- Create: `src/main/java/cn/itcast/final_review/controller/ReviewController.java`

**Interfaces:**
- Consumes: `SubjectService`, `QuestionService` from Task 3
- Produces: REST API endpoints under `/api/`

- [ ] **Step 1: 创建 SubjectController**

`src/main/java/cn/itcast/final_review/controller/SubjectController.java`:

```java
package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Subject;
import cn.itcast.final_review.service.SubjectService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> findAll() {
        return subjectService.findAll();
    }

    @PostMapping
    public Subject save(@RequestBody Subject subject) {
        return subjectService.save(subject);
    }

    @PutMapping("/{id}")
    public Subject update(@PathVariable Long id, @RequestBody Subject subject) {
        return subjectService.update(id, subject);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }
}
```

- [ ] **Step 2: 创建 QuestionController**

`src/main/java/cn/itcast/final_review/controller/QuestionController.java`:

```java
package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> findBySubjectId(@RequestParam Long subjectId) {
        return questionService.findBySubjectId(subjectId);
    }

    @PostMapping
    public Question save(@RequestBody Question question) {
        return questionService.save(question);
    }

    @PutMapping("/{id}")
    public Question update(@PathVariable Long id, @RequestBody Question question) {
        return questionService.update(id, question);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }
}
```

- [ ] **Step 3: 创建 ReviewController**

`src/main/java/cn/itcast/final_review/controller/ReviewController.java`:

```java
package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final QuestionService questionService;

    public ReviewController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{subjectId}/random")
    public List<Question> getRandomQuestions(
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "10") int count) {
        return questionService.findRandom(subjectId, count);
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
./mvnw compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 5: 启动后端验证 API**

```bash
# 先确保数据库和表已创建
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS final_review DEFAULT CHARACTER SET utf8mb4;"

# 启动后端（后台运行，等5秒后测试）
./mvnw spring-boot:run -q &
sleep 5

# 测试科目接口
curl -s http://localhost:8080/api/subjects | head -200
```

Expected: 返回包含"数据库"等科目的 JSON 数组

```bash
# 停掉后端进程
kill %1 2>/dev/null || true
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/cn/itcast/final_review/controller/
git commit -m "feat: add REST controllers"
```

---

### Task 5: 前端项目脚手架

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.js`

**Interfaces:**
- Consumes: 无
- Produces: Vue 3 + Vite 项目骨架，包含路由配置

- [ ] **Step 1: 创建 package.json**

`frontend/package.json`:

```json
{
  "name": "final-review-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.4.0"
  }
}
```

- [ ] **Step 2: 创建 vite.config.js**

`frontend/vite.config.js`:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 创建 index.html**

`frontend/index.html`:

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>期末复习工具</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: 创建 main.js**

`frontend/src/main.js`:

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
```

- [ ] **Step 5: 创建 App.vue**

`frontend/src/App.vue`:

```vue
<template>
  <div id="app-container">
    <router-view />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  background-color: #f0f2f5;
  min-height: 100vh;
}
#app-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
</style>
```

- [ ] **Step 6: 创建路由配置**

`frontend/src/router/index.js`:

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/exam/:subjectId/:count',
    name: 'Exam',
    component: () => import('../views/ExamView.vue')
  },
  {
    path: '/result',
    name: 'Result',
    component: () => import('../views/ResultView.vue')
  },
  {
    path: '/subjects/manage',
    name: 'SubjectManage',
    component: () => import('../views/SubjectManage.vue')
  },
  {
    path: '/questions/manage',
    name: 'QuestionManage',
    component: () => import('../views/QuestionManage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

- [ ] **Step 7: 安装依赖**

```bash
cd D:/WBB_JAVA/Final_review/frontend
npm install
```

Expected: node_modules 安装成功

- [ ] **Step 8: 提交**

```bash
git add frontend/
git commit -m "feat: add frontend scaffold"
```

---

### Task 6: 前端 API 层

**Files:**
- Create: `frontend/src/api/subject.js`
- Create: `frontend/src/api/question.js`
- Create: `frontend/src/api/review.js`

**Interfaces:**
- Consumes: 无（直接调用后端 API）
- Produces: `subjectApi`, `questionApi`, `reviewApi` 供页面组件使用

- [ ] **Step 1: 创建 subject.js**

`frontend/src/api/subject.js`:

```javascript
import axios from 'axios'

const BASE = '/api/subjects'

export function getSubjects() {
  return axios.get(BASE)
}

export function addSubject(name) {
  return axios.post(BASE, { name })
}

export function updateSubject(id, name) {
  return axios.put(`${BASE}/${id}`, { name })
}

export function deleteSubject(id) {
  return axios.delete(`${BASE}/${id}`)
}
```

- [ ] **Step 2: 创建 question.js**

`frontend/src/api/question.js`:

```javascript
import axios from 'axios'

const BASE = '/api/questions'

export function getQuestions(subjectId) {
  return axios.get(BASE, { params: { subjectId } })
}

export function addQuestion(question) {
  return axios.post(BASE, question)
}

export function updateQuestion(id, question) {
  return axios.put(`${BASE}/${id}`, question)
}

export function deleteQuestion(id) {
  return axios.delete(`${BASE}/${id}`)
}
```

- [ ] **Step 3: 创建 review.js**

`frontend/src/api/review.js`:

```javascript
import axios from 'axios'

const BASE = '/api/review'

export function getRandomQuestions(subjectId, count) {
  return axios.get(`${BASE}/${subjectId}/random`, {
    params: { count }
  })
}
```

- [ ] **Step 4: 提交**

```bash
git add frontend/src/api/
git commit -m "feat: add frontend API layer"
```

---

### Task 7: 首页 + 答题页 + 结果页

**Files:**
- Create: `frontend/src/views/HomeView.vue`
- Create: `frontend/src/views/ExamView.vue`
- Create: `frontend/src/views/ResultView.vue`

**Interfaces:**
- Consumes: `getSubjects()` from Task 6, `getRandomQuestions()` from Task 6
- Produces: 完整复习流程的三个页面

- [ ] **Step 1: 创建 HomeView.vue**

`frontend/src/views/HomeView.vue`:

```vue
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
          {{ subject.name }}
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
}
.subject-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}
.subject-card {
  padding: 20px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  font-size: 16px;
  transition: all 0.2s;
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
```

- [ ] **Step 2: 创建 ExamView.vue**

`frontend/src/views/ExamView.vue`:

```vue
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
```

- [ ] **Step 3: 创建 ResultView.vue**

`frontend/src/views/ResultView.vue`:

```vue
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
```

- [ ] **Step 4: 提交**

```bash
git add frontend/src/views/HomeView.vue frontend/src/views/ExamView.vue frontend/src/views/ResultView.vue
git commit -m "feat: add review pages (home, exam, result)"
```

---

### Task 8: 科目管理 + 题目管理页面

**Files:**
- Create: `frontend/src/views/SubjectManage.vue`
- Create: `frontend/src/views/QuestionManage.vue`

**Interfaces:**
- Consumes: `subjectApi` + `questionApi` from Task 6
- Produces: 管理页面

- [ ] **Step 1: 创建 SubjectManage.vue**

`frontend/src/views/SubjectManage.vue`:

```vue
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
        @keyup.enter="addSubject"
      />
      <button @click="addSubject">添加</button>
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

async function addSubject() {
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
```

- [ ] **Step 2: 创建 QuestionManage.vue**

`frontend/src/views/QuestionManage.vue`:

```vue
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
            <option value="SELECT">选择题</option>
            <option value="JUDGE">判断题</option>
          </select>
        </div>
        <div class="form-group">
          <label>题目内容</label>
          <textarea v-model="form.content" rows="3"></textarea>
        </div>
        <div v-if="form.type === 'SELECT'" class="form-group">
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
          <td>{{ q.type === 'SELECT' ? '选择题' : '判断题' }}</td>
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

const form = ref({
  type: 'SELECT',
  content: '',
  answer: ''
})

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
}

function startEdit(q) {
  editingQuestion.value = q
  form.value = {
    type: q.type,
    content: q.content,
    answer: q.answer
  }
  if (q.type === 'SELECT' && q.options) {
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
  if (form.value.type === 'SELECT') {
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
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/views/SubjectManage.vue frontend/src/views/QuestionManage.vue
git commit -m "feat: add management pages"
```

---

### Task 9: 启动验证

- [ ] **Step 1: 启动后端**

```bash
cd D:/WBB_JAVA/Final_review
./mvnw spring-boot:run
```

在另一个终端中验证 API:

```bash
curl http://localhost:8080/api/subjects
```

期望返回包含"数据库"、"习近平新时代中国特色社会主义思想概论"、"毛泽东思想和中国特色社会主义理论体系概论"的 JSON 数组。

- [ ] **Step 2: 启动前端**

```bash
cd D:/WBB_JAVA/Final_review/frontend
npm run dev
```

浏览器访问 http://localhost:5173 ，验证：
1. 首页显示三个科目卡片
2. 选择一个科目，输入数量，点击开始答题
3. 跳转到答题页后，可以正常答题
4. 答完所有题后，看到结果页统计

- [ ] **Step 3: 提交剩余文件**

```bash
git add -A
git commit -m "chore: finalize project setup"
```
