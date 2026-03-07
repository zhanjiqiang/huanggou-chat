import api from './index'

export const modelApi = {
  // 获取所有可用模型
  getAvailable() {
    return api.get('/models/available')
      .then(res => res.data)
  },

  // 获取用户模型列表
  getMyModels() {
    return api.get('/models/my')
      .then(res => res.data)
  },

  // 添加模型
  add(modelName) {
    return api.post('/models/add', { modelName })
      .then(res => res.data)
  },

  // 设置默认模型
  setDefault(modelName) {
    return api.post('/models/setDefault', { modelName })
      .then(res => res.data)
  },

  // 删除模型
  delete(id) {
    return api.delete(`/models/${id}`)
      .then(res => res.data)
  }
}
