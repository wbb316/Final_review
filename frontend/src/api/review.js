import axios from 'axios'

const BASE = '/api/review'

export function getRandomQuestions(subjectId, count) {
  return axios.get(`${BASE}/${subjectId}/random`, {
    params: { count }
  })
}
