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
