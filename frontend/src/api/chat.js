import api from './index'

export const chatApi = {
  // 发送文本消息
  send(sessionId, message, model) {
    return api.post('/chat/send', { sessionId, message, model })
      .then(res => res.data)
  },

  // 流式发送消息（返回EventSource URL）
  getStreamUrl(sessionId, model) {
    return `/api/chat/stream`
  },

  // 上传图片
  uploadImage(formData) {
    return api.post('/chat/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(res => res.data)
  }
}
