import api from './index'

export const authApi = {
  login(username, password) {
    return api.post('/auth/login', { username, password })
      .then(res => res.data)
  },

  register(username, password) {
    return api.post('/auth/register', { username, password })
      .then(res => res.data)
  },

  getMe() {
    return api.get('/auth/me')
      .then(res => res.data)
  }
}
