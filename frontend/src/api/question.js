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
