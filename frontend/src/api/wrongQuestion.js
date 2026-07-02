import axios from 'axios'

const BASE = '/api/wrong-questions'

export function getWrongQuestions() {
  return axios.get(BASE)
}

export function getWrongQuestionsBySubject(subjectId) {
  return axios.get(`${BASE}/subject/${subjectId}`)
}

export function recordWrong(questionId, subjectId) {
  return axios.post(`${BASE}/record-wrong`, { questionId, subjectId })
}

export function recordCorrect(questionId) {
  return axios.post(`${BASE}/record-correct`, { questionId })
}
