import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(null)
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    try {
      const res = await authApi.login(username, password)
      token.value = res.token
      user.value = res.user
      saveToLocal()
      return { success: true }
    } catch (error) {
      return { success: false, error: error.response?.data?.error || '登录失败' }
    }
  }

  async function register(username, password) {
    try {
      const res = await authApi.register(username, password)
      token.value = res.token
      user.value = res.user
      saveToLocal()
      return { success: true }
    } catch (error) {
      return { success: false, error: error.response?.data?.error || '注册失败' }
    }
  }

  function logout() {
    token.value = null
    user.value = null
    clearLocal()
  }

  function saveToLocal() {
    localStorage.setItem('huanggou_token', token.value)
    localStorage.setItem('huanggou_user', JSON.stringify(user.value))
  }

  function loadFromLocal() {
    const savedToken = localStorage.getItem('huanggou_token')
    const savedUser = localStorage.getItem('huanggou_user')
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  function clearLocal() {
    localStorage.removeItem('huanggou_token')
    localStorage.removeItem('huanggou_user')
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    logout,
    saveToLocal,
    loadFromLocal
  }
})
