<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>🤬 黄狗一号</h2>
        <el-button type="primary" size="small" @click="createNewSession">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="['session-item', { active: currentSession?.id === session.id }]"
          @click="selectSession(session)"
        >
          <div class="session-info">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-meta">
              <span>{{ formatDate(session.updated_at) }}</span>
              <span>{{ session.message_count }}条消息</span>
            </div>
          </div>
          <el-button
            type="danger"
            size="small"
            text
            @click.stop="deleteSession(session.id)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>

        <div v-if="sessions.length === 0" class="empty-sessions">
          <p>暂无会话</p>
          <p>点击上方按钮创建新对话</p>
        </div>
      </div>

      <div class="sidebar-footer">
        <el-button text @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="main-content">
      <div v-if="!currentSession" class="welcome">
        <h1>🤬 黄狗一号聊天机器人</h1>
        <p>选择一个会话或创建新对话开始聊天</p>
      </div>

      <template v-else>
        <!-- 顶部工具栏 -->
        <div class="toolbar">
          <div class="session-title">{{ currentSession.title }}</div>
          <div class="model-selector">
            <span>模型：</span>
            <el-select v-model="currentModel" size="small" style="width: 180px">
              <el-option
                v-for="model in availableModels"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              >
                <span>{{ model.name }}</span>
                <span v-if="model.recommended" style="color: #67c23a; margin-left: 8px;">推荐</span>
              </el-option>
            </el-select>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div class="messages">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message', msg.role]"
            >
              <div class="message-avatar">
                {{ msg.role === 'user' ? '👤' : '🤬' }}
              </div>
              <div class="message-content">
                <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
                <div class="message-time">{{ formatTime(msg.created_at) }}</div>
              </div>
            </div>

            <!-- 流式输出占位符 -->
            <div v-if="streamingContent" class="message assistant">
              <div class="message-avatar">🤬</div>
              <div class="message-content">
                <div class="message-text" v-html="renderMarkdown(streamingContent)"></div>
              </div>
            </div>

            <div v-if="messages.length === 0 && !streamingContent" class="empty-messages">
              <p>发送消息开始对话</p>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <el-button
              circle
              @click="triggerImageUpload"
              :disabled="uploadingImage"
            >
              <el-icon><Picture /></el-icon>
            </el-button>
            <input
              type="file"
              ref="imageInput"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />

            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息... (Ctrl+Enter发送)"
              @keydown.ctrl.enter="sendMessage"
              :disabled="sending"
            />

            <el-button
              type="primary"
              circle
              :loading="sending"
              :disabled="!inputMessage.trim()"
              @click="sendMessage"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { useUserStore } from '../store/user'
import { sessionApi } from '../api/session'
import { chatApi } from '../api/chat'
import { modelApi } from '../api/model'

const router = useRouter()
const userStore = useUserStore()

const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const uploadingImage = ref(false)
const streamingContent = ref('')
const messagesContainer = ref(null)
const imageInput = ref(null)

const availableModels = ref([])
const currentModel = ref('zai/glm-4.7')

// 加载会话列表
async function loadSessions() {
  try {
    const res = await sessionApi.getList()
    sessions.value = res.sessions
  } catch (error) {
    ElMessage.error('加载会话失败')
  }
}

// 加载模型列表
async function loadModels() {
  try {
    const res = await modelApi.getAvailable()
    availableModels.value = res.models
  } catch (error) {
    console.error('加载模型失败:', error)
  }
}

// 选择会话
async function selectSession(session) {
  currentSession.value = session
  await loadMessages(session.id)
}

// 加载消息
async function loadMessages(sessionId) {
  try {
    const res = await sessionApi.getMessages(sessionId)
    messages.value = res.messages
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

// 创建新会话
async function createNewSession() {
  try {
    const res = await sessionApi.create('新对话', currentModel.value)
    sessions.value.unshift(res)
    currentSession.value = res
    messages.value = []
    ElMessage.success('创建成功')
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

// 删除会话
async function deleteSession(sessionId) {
  try {
    await ElMessageBox.confirm('确定删除这个会话吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await sessionApi.delete(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)

    if (currentSession.value?.id === sessionId) {
      currentSession.value = null
      messages.value = []
    }

    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 发送消息（流式）
async function sendMessage() {
  if (!inputMessage.value.trim() || sending.value) return

  const message = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  streamingContent.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    created_at: new Date().toISOString()
  })
  scrollToBottom()

  try {
    // 使用SSE流式输出
    const eventSource = new EventSource(
      `${chatApi.getStreamUrl()}/${currentSession.value.id}?message=${encodeURIComponent(message)}&model=${currentModel.value}`
    )

    eventSource.onmessage = (event) => {
      const data = event.data
      if (data === '[DONE]') {
        eventSource.close()
        sending.value = false
        // 保存完整的AI回复到messages
        if (streamingContent.value) {
          messages.value.push({
            role: 'assistant',
            content: streamingContent.value,
            model: currentModel.value,
            created_at: new Date().toISOString()
          })
          streamingContent.value = ''
          loadMessages(currentSession.value.id) // 重新加载消息
        }
      } else if (data.startsWith('[ERROR]')) {
        eventSource.close()
        ElMessage.error(data.replace('[ERROR] ', ''))
        sending.value = false
        streamingContent.value = ''
      } else {
        streamingContent.value += data
        scrollToBottom()
      }
    }

    eventSource.onerror = (error) => {
      eventSource.close()
      ElMessage.error('连接失败')
      sending.value = false
      streamingContent.value = ''
    }
  } catch (error) {
    ElMessage.error('发送失败')
    sending.value = false
    streamingContent.value = ''
  }
}

// 上传图片
function triggerImageUpload() {
  imageInput.value?.click()
}

async function handleImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  uploadingImage.value = true
  const formData = new FormData()
  formData.append('image', file)

  try {
    const res = await chatApi.uploadImage(formData)
    if (res.description) {
      inputMessage.value += `\n[图片识别结果: ${res.description}]\n`
    }
    ElMessage.success('图片已上传')
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploadingImage.value = false
    event.target.value = '' // 清空input
  }
}

// 退出登录
function handleLogout() {
  userStore.logout()
  router.push('/login')
  ElMessage.success('已退出登录')
}

// 渲染Markdown
function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 格式化日期
function formatDate(dateStr) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

// 格式化时间
function formatTime(dateStr) {
  return new Date(dateStr).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadSessions()
  loadModels()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: rgba(0, 0, 0, 0.3);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.session-item {
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.session-item.active {
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  font-size: 12px;
  color: rgba(224, 224, 224, 0.5);
}

.session-meta span {
  margin-right: 10px;
}

.empty-sessions {
  text-align: center;
  padding: 40px 20px;
  color: rgba(224, 224, 224, 0.4);
}

.sidebar-footer {
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 右侧主区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.2);
}

.welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(224, 224, 224, 0.6);
}

.welcome h1 {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.toolbar {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-title {
  font-size: 16px;
  font-weight: 500;
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 消息列表 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.messages {
  max-width: 900px;
  margin: 0 auto;
}

.message {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.assistant .message-avatar {
  margin-right: 12px;
}

.message.user .message-avatar {
  margin-left: 12px;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  line-height: 1.6;
}

.message.user .message-text {
  background: rgba(102, 126, 234, 0.2);
}

.message-time {
  font-size: 12px;
  color: rgba(224, 224, 224, 0.4);
  margin-top: 4px;
  text-align: right;
}

.message.user .message-time {
  text-align: left;
}

.empty-messages {
  text-align: center;
  padding: 60px;
  color: rgba(224, 224, 224, 0.4);
}

/* 输入区域 */
.input-area {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

/* Markdown样式 */
.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-text :deep(code) {
  font-family: 'Fira Code', 'Monaco', monospace;
  font-size: 13px;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3) {
  margin: 16px 0 8px;
}

.message-text :deep(blockquote) {
  border-left: 3px solid #667eea;
  padding-left: 12px;
  margin: 10px 0;
  color: rgba(224, 224, 224, 0.7);
}
</style>
