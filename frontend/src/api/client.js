import axios from 'axios'

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api' })
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function apiError(error) {
  return error?.response?.data?.error || error?.response?.data?.message || error?.message || 'Something went wrong. Please try again.'
}

export default api
