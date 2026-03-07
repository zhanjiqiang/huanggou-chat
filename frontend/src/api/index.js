import axios from 'axios'
import { useUserStore } from '../store/user'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL 
    ? `${import.meta.env.VITE_API_BASE_URL}/api` 
    : '/api'
})

// 请求拦截器 - 添加token
api.interceptors.request.use(config => {
  // 先尝试从localStorage读取token
  const token = localStorage.getItem('huanggou_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('huanggou_token')
      localStorage.removeItem('huanggou_user')
      window.location.href = '/chat/'
    }
    return Promise.reject(error)
  }
)

export default api
