import api from './index'

export const sessionApi = {
  // 获取会话列表
  getList() {
    return api.get('/sessions')
      .then(res => res.data)
  },

  // 获取单个会话
  getOne(id) {
    return api.get(`/sessions/${id}`)
      .then(res => res.data)
  },

  // 获取会话消息（支持分页）
  getMessages(sessionId, page = 0, size = 10) {
    return api.get(`/chat/${sessionId}/messages`, {
      params: { page, size }
    }).then(res => res.data)
  },

  // 创建新会话
  create(title, model) {
    return api.post('/sessions', { title, model })
      .then(res => res.data)
  },

  // 更新会话标题
  updateTitle(id, title) {
    return api.put(`/sessions/${id}`, { title })
      .then(res => res.data)
  },

  // 删除会话
  delete(id) {
    return api.delete(`/sessions/${id}`)
      .then(res => res.data)
  }
}
