---
name: 期末复习工具设计文档
description: 通用期末复习工具，支持科目管理、题目管理和随机刷题
---

# 期末复习工具 — 设计文档

## 1. 概述

一个前后端分离的个人期末复习工具。后端 Spring Boot + MyBatis + MySQL，前端 Vue 3 + Vite。支持多科目管理、选择题/判断题管理、随机抽题刷题。

## 2. 项目结构

```
Final_review/
├── frontend/                    # Vue 3 + Vite
│   ├── src/
│   │   ├── views/
│   │   │   ├── HomeView.vue         # 首页：科目选择 + 抽题数量 + 开始答题
│   │   │   ├── ExamView.vue         # 答题页：逐题作答 + 即时反馈
│   │   │   ├── ResultView.vue       # 结果页：答题统计
│   │   │   ├── SubjectManage.vue    # 科目管理
│   │   │   └── QuestionManage.vue   # 题目管理（按科目管理）
│   │   ├── api/
│   │   │   ├── subject.js           # 科目 API
│   │   │   ├── question.js          # 题目 API
│   │   │   └── review.js            # 复习 API
│   │   ├── router/
│   │   │   └── index.js
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── src/
│   └── main/
│       ├── java/cn/itcast/final_review/
│       │   ├── controller/
│       │   │   ├── SubjectController.java
│       │   │   ├── QuestionController.java
│       │   │   └── ReviewController.java
│       │   ├── service/
│       │   │   ├── SubjectService.java
│       │   │   └── QuestionService.java
│       │   ├── mapper/
│       │   │   ├── SubjectMapper.java
│       │   │   └── QuestionMapper.java
│       │   └── entity/
│       │       ├── Subject.java
│       │       └── Question.java
│       └── resources/
│           ├── application.properties
│           └── mapper/
│               ├── SubjectMapper.xml
│               └── QuestionMapper.xml
├── pom.xml
└── docs/superpowers/specs/2026-07-01-final-review-design.md
```

## 3. 数据模型

### subject（科目表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| name | VARCHAR(100) NOT NULL UNIQUE | 科目名称 |
| create_time | DATETIME | 创建时间 |

### question（题目表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| subject_id | BIGINT (FK → subject.id) | 所属科目 |
| type | VARCHAR(10) NOT NULL | 题型：SELECT / JUDGE |
| content | TEXT NOT NULL | 题目内容 |
| options | TEXT | 选择题选项（JSON 数组，判断题 null） |
| answer | VARCHAR(500) NOT NULL | 正确答案 |
| create_time | DATETIME | 创建时间 |

## 4. API 设计

### 科目接口

| 方法 | 路径 | 请求体 | 响应 | 说明 |
|------|------|--------|------|------|
| GET | /api/subjects | - | Subject[] | 获取所有科目 |
| POST | /api/subjects | {name} | Subject | 新增科目 |
| PUT | /api/subjects/{id} | {name} | Subject | 修改科目 |
| DELETE | /api/subjects/{id} | - | - | 删除科目（级联删除题目） |

### 题目接口

| 方法 | 路径 | 请求体 | 响应 | 说明 |
|------|------|--------|------|------|
| GET | /api/questions?subjectId={id} | - | Question[] | 获取某科目所有题目 |
| POST | /api/questions | Question | Question | 新增题目 |
| PUT | /api/questions/{id} | Question | Question | 修改题目 |
| DELETE | /api/questions/{id} | - | - | 删除题目 |

### 复习接口

| 方法 | 路径 | 参数 | 响应 | 说明 |
|------|------|------|------|------|
| GET | /api/review/{subjectId}/random?count={n} | count | Question[] | 随机抽取 n 道题 |

## 5. 前端页面设计

### 5.1 首页 (HomeView.vue)

- **上半部分**：以卡片网格展示所有科目，点击选中高亮
- **中部**：输入抽题数量（数字输入框）
- **底部**："开始答题"按钮
- **右上角**：小字链接"管理科目/题目"

### 5.2 答题页 (ExamView.vue)

- 顶部显示进度：第 3/10 题
- 中间展示题目内容：
  - **选择题**：显示 ABCD 选项，单选按钮选择
  - **判断题**：显示"正确/错误"两个按钮
- 选择答案后**立即显示对错**（绿色/红色反馈），并显示正确答案
- "下一题"按钮继续，最后一题显示"查看结果"

### 5.3 结果页 (ResultView.vue)

- 显示统计：总题数、答对数、答错数、正确率
- 每题简要回顾（题目前半部分 + 你的答案 + 正确答案）
- "再来一次"按钮回到首页

### 5.4 科目管理 (SubjectManage.vue)

- 表格展示所有科目
- 新增：输入框 + 添加按钮
- 编辑：行内编辑或弹窗
- 删除：确认后删除

### 5.5 题目管理 (QuestionManage.vue)

- 顶部选择科目筛选
- 表格展示题目列表（内容预览 + 类型 + 答案）
- 新增/编辑弹窗：选择题型、输入题目内容、选项（选择题）、答案
- 删除：确认后删除

## 6. 跨域配置

开发环境下 Vite 配置代理到后端 localhost:8080，避免跨域问题。

## 7. 不包含的功能（明确排除）

- 用户登录/注册
- 困难模式（数据量不足）
- 题目导入/导出
- 复习历史记录
