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
