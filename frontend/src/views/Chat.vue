<template>
  <div class="maodie-chat">
    <!-- 背景猫爪 -->
    <div class="bg-paws">
      <div class="paw" v-for="i in 12" :key="i" :style="getPawStyle(i)">🐾</div>
    </div>

    <!-- 抓痕装饰 -->
    <div class="scratches">
      <div class="scratch"></div>
      <div class="scratch"></div>
      <div class="scratch"></div>
    </div>

    <!-- 移动端菜单按钮 -->
    <button class="mobile-menu-btn" @click="toggleMobileMenu">
      <span>☰</span>
    </button>

    <!-- 移动端遮罩层 -->
    <div class="mobile-overlay" :class="{ show: mobileMenuOpen }" @click="toggleMobileMenu"></div>

    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <aside class="sidebar" :class="{ 'mobile-open': mobileMenuOpen }">
        <!-- Logo区 -->
        <div class="sidebar-header">
          <div class="cat-decoration">
            <div class="cat-avatar-wrapper">
              <img src="/images/maodie-avatar.png" alt="耄耋" class="cat-avatar" />
              <div class="avatar-glow"></div>
            </div>
          </div>
          <div class="logo">
            <h1>🤬 耄耋君</h1>
            <p class="tagline">{{ displayTagline }}</p>
            <button class="edit-signature-btn" @click="openSignatureEditor" title="编辑个性签名">
              <span>✏️</span>
            </button>
          </div>

          <!-- 操作区 -->
          <div class="header-actions">
            <div class="status-dot" :class="{ online: wsConnected }">
              <div class="status-pulse"></div>
            </div>
            <button class="new-chat-btn" @click="createNewSession" :disabled="!wsConnected">
              <span>➕ 新对话</span>
            </button>
          </div>
        </div>

        <!-- 会话列表 -->
        <div class="sessions-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: currentSession?.id === session.id }]"
            @click="selectSession(session)"
          >
            <div class="session-content">
              <span class="session-paw">🐾</span>
              <div class="session-info">
                <div class="session-title">{{ session.title }}</div>
                <div class="session-time">{{ formatDate(session.updatedAt || session.updated_at) }}</div>
              </div>
            </div>
            <button class="session-delete" @click.stop="deleteSession(session.id)">
              <span>🗑️</span>
            </button>
          </div>

          <div v-if="sessions.length === 0" class="empty-sessions">
            <div class="empty-icon">🐱</div>
            <p>喵？没有会话？</p>
            <p>点上方按钮，老子陪你聊</p>
          </div>
        </div>

        <!-- 设置区 -->
        <div class="settings-panel">
          <div class="setting-group">
            <label class="setting-label">
              <span>🧠 AI模式</span>
              <span class="setting-badge">{{ aiMode === 'direct' ? '直连' : 'OpenClaw' }}</span>
            </label>
            <select v-model="aiMode" class="setting-select">
              <option value="direct">直连模型</option>
              <option value="openclaw">OpenClaw</option>
            </select>
          </div>

          <div class="setting-group">
            <label class="setting-label">
              <span>🎨 主题</span>
              <span class="setting-badge">{{ theme === 'dark' ? '深色' : '浅色' }}</span>
            </label>
            <button class="setting-toggle" @click="toggleTheme" :class="{ active: theme === 'light' }">
              <span class="toggle-dot"></span>
            </button>
          </div>

          <div class="setting-group">
            <label class="setting-label">
              <span>💭 思考过程</span>
              <span class="setting-badge">{{ showReasoning ? '显示' : '隐藏' }}</span>
            </label>
            <button class="setting-toggle" @click="showReasoning = !showReasoning" :class="{ active: showReasoning }">
              <span class="toggle-dot"></span>
            </button>
          </div>
        </div>

        <!-- 快捷回复 -->
        <div class="quick-replies-panel" v-if="showQuickReplies">
          <div class="panel-header">
            <span class="panel-title">⚡ 快捷回复</span>
            <button class="collapse-btn" @click="showQuickReplies = false">
              <span>✕</span>
            </button>
          </div>
          <div class="quick-replies-list">
            <button
              v-for="(reply, index) in quickReplies"
              :key="index"
              class="quick-reply-item"
              @click="useQuickReply(reply.prompt)"
              :title="reply.description"
            >
              <span class="reply-emoji">{{ reply.emoji }}</span>
              <span class="reply-title">{{ reply.title }}</span>
            </button>
          </div>
        </div>

        <!-- 底部 -->
        <div class="sidebar-footer">
          <button class="quick-replies-toggle" @click="showQuickReplies = !showQuickReplies" :class="{ active: showQuickReplies }">
            <span>⚡ 快捷回复</span>
          </button>
          <button class="logout-btn" @click="handleLogout">
            <span>🚪 退出登录</span>
          </button>
        </div>
      </aside>

      <!-- 右侧聊天区域 -->
      <main class="main-content">
        <div v-if="!currentSession" class="welcome-screen">
          <div class="welcome-content">
            <div class="big-cat">🐱</div>
            <h1 class="welcome-title">🤬 耄耋君</h1>
            <p class="welcome-subtitle">嘴臭欠打，但活儿干得漂亮</p>
            <div class="welcome-actions">
              <button class="btn-primary" @click="createNewSession" :disabled="!wsConnected">
                <span>🐾 开始被骂</span>
              </button>
            </div>
            <div class="connection-status" :class="{ connected: wsConnected }">
              <span class="status-dot"></span>
              <span>{{ wsConnected ? '✅ 在线' : '🔌 连接中' }}</span>
            </div>
          </div>
        </div>

        <template v-else>
          <!-- 顶部工具栏 -->
          <header class="chat-header">
            <div class="header-left">
              <img class="chat-gif" src="/images/maodie-hiss.gif" alt="耄耋" />
              <div class="chat-title-group">
                <span class="chat-title">{{ currentSession.title }}</span>
                <div class="daily-quote" v-if="dailyQuote.text">
                  <span class="quote-icon">{{ dailyQuote.emoji }}</span>
                  <span class="quote-text">{{ dailyQuote.text }}</span>
                </div>
              </div>
            </div>
            <div class="header-right">
              <select v-model="currentModel" class="model-select" :disabled="!wsConnected || sending">
                <option value="glm-4.7">GLM-4.7 (推荐)</option>
                <option value="glm-5">GLM-5 (最强)</option>
                <option value="glm-4.6">GLM-4.6</option>
                <option value="glm-4.5-air">GLM-4.5 Air (快速)</option>
              </select>
              <div class="badge" :class="aiMode">
                <span class="badge-icon">{{ aiMode === 'direct' ? '⚡' : '🤖' }}</span>
                <span>{{ aiMode === 'direct' ? '直连' : 'OpenClaw' }}</span>
              </div>
              <div v-if="showReasoning" class="badge reasoning">
                <span class="badge-icon">💭</span>
                <span>思考</span>
              </div>
              <button class="export-btn" @click="exportChat" :disabled="messages.length === 0" title="导出对话为Markdown">
                <span>📥</span>
              </button>
            </div>
          </header>

          <!-- 消息区 -->
          <div class="messages-wrapper" ref="messagesContainer">
            <div class="messages-list">
              <div
                v-for="(msg, index) in messages"
                :key="index"
                :class="['message-item', msg.role]"
              >
                <div class="message-avatar">
                  <img v-if="msg.role === 'assistant'" src="/images/maodie-avatar.png" alt="耄耋" />
                  <span v-else>👤</span>
                </div>
                <div class="message-body">
                  <div class="message-bubble">
                    <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
                  </div>
                  <div class="message-meta">
                    <span>{{ formatTime(msg.createdAt || msg.created_at) }}</span>
                    <span v-if="msg.role === 'assistant'" class="claw-mark">🐾</span>
                  </div>
                </div>
              </div>

              <!-- 思考过程 -->
              <div v-if="reasoningContent && showReasoning" class="message-item assistant reasoning">
                <div class="message-avatar">
                  <span>🧠</span>
                </div>
                <div class="message-body">
                  <div class="message-bubble reasoning-bubble">
                    <div class="message-header">
                      <span>💭 思考过程</span>
                    </div>
                    <div class="message-content" v-html="renderMarkdown(reasoningContent)"></div>
                  </div>
                </div>
              </div>

              <!-- 流式输出 -->
              <div v-if="streamingContent" class="message-item assistant typing">
                <div class="message-avatar">
                  <img src="/images/maodie-avatar.png" alt="耄耋" />
                  <div class="typing-dots">
                    <span></span><span></span><span></span>
                  </div>
                </div>
                <div class="message-body">
                  <div class="message-bubble">
                    <div class="message-content" v-html="renderMarkdown(streamingContent)"></div>
                  </div>
                </div>
              </div>

              <div v-if="messages.length === 0 && !streamingContent" class="empty-messages">
                <div class="empty-icon">🐱</div>
                <p>来啊，快活啊</p>
                <p>老子就在这等着你</p>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="input-area">
            <div class="input-container">
              <button class="input-btn" @click="triggerImageUpload" :disabled="uploadingImage">
                <span>📷</span>
              </button>
              <input
                type="file"
                ref="imageInput"
                accept="image/*"
                style="display: none"
                @change="handleImageUpload"
              />
              <textarea
                v-model="inputMessage"
                placeholder="输入消息... (Ctrl+Enter发送)"
                @keydown.ctrl.enter="sendMessage"
                :disabled="sending || !wsConnected"
                rows="3"
                class="input-textarea"
              ></textarea>
              <button
                class="input-btn send-btn"
                :class="{ active: inputMessage.trim() && wsConnected }"
                :disabled="!inputMessage.trim() || !wsConnected"
                @click="sendMessage"
              >
                <span>{{ sending ? '⏳' : '📤' }}</span>
              </button>
              <button
                class="input-btn voice-btn"
                :class="{ recording: isRecording }"
                :disabled="!wsConnected || !supportsSpeechRecognition"
                @click="toggleVoiceInput"
                title="语音输入"
              >
                <span>{{ isRecording ? '🔴' : '🎤' }}</span>
              </button>
            </div>
          </div>
        </template>
      </main>
    </div>
  </div>

  <!-- 个性签名编辑对话框 -->
  <el-dialog
    v-model="editingSignature"
    title="编辑个性签名"
    width="500px"
    @close="editingSignature = false"
  >
    <div class="signature-editor">
      <el-input
        v-model="signatureInput"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        placeholder="输入你的个性签名（200字以内）"
      />
      <div class="signature-preview">
        <span class="preview-label">预览：</span>
        <span class="preview-text">{{ signatureInput || '宇宙第一嘴臭欠打的AI' }}</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="editingSignature = false">取消</el-button>
      <el-button type="primary" @click="saveSignature">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT = 10
const RECONNECT_INTERVAL = 3000

const wsConnected = ref(false)
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const uploadingImage = ref(false)
const streamingContent = ref('')
const reasoningContent = ref('')
const messagesContainer = ref(null)
const imageInput = ref(null)

const currentModel = ref('glm-4.7')
const aiMode = ref('direct')
const showReasoning = ref(false)
const theme = ref('dark') // dark or light
const dailyQuote = ref({ text: '', emoji: '🤬' })
const showQuickReplies = ref(false)

// 语音输入
const isRecording = ref(false)
let recognition = null
const supportsSpeechRecognition = ref(false)

// 移动端菜单
const mobileMenuOpen = ref(false)

// 个性签名编辑
const editingSignature = ref(false)
const signatureInput = ref('')

// 用户个性签名
const userSignature = computed(() => userStore.user?.signature || '')
const displayTagline = computed(() => userStore.user?.signature || '宇宙第一嘴臭欠打的AI')

// 快捷回复模板
const quickReplies = [
  { emoji: '💡', title: '创意想法', description: '帮我生成一些创意想法', prompt: '帮我想一些创意想法，要求：' },
  { emoji: '📝', title: '优化文案', description: '帮我优化这段文案', prompt: '帮我优化这段文案，让它更有吸引力和感染力：\n\n' },
  { emoji: '🔍', title: '分析问题', description: '帮我分析这个问题', prompt: '帮我分析这个问题，找出根本原因和解决方案：\n\n' },
  { emoji: '💻', title: '代码审查', description: '帮我审查这段代码', prompt: '帮我审查这段代码，指出问题和改进建议：\n\n```javascript\n// 粘贴代码\n```\n\n' },
  { emoji: '📚', title: '学习知识', description: '帮我学习某个知识点', prompt: '帮我学习这个知识点，用通俗易懂的方式讲解：' },
  { emoji: '🎯', title: '制定计划', description: '帮我制定一个计划', prompt: '帮我制定一个详细可行的计划，包括步骤和时间安排：' },
  { emoji: '😄', title: '搞笑段子', description: '给我讲个笑话', prompt: '给我讲个笑话，要很好笑的那种！' },
  { emoji: '🤬', title: '骂醒我', description: '用毒舌的方式骂醒我', prompt: '用你那毒舌欠打的方式狠狠骂醒我，让我清醒一下！' },
]

function useQuickReply(prompt) {
  if (inputMessage.value) {
    inputMessage.value += '\n\n' + prompt
  } else {
    inputMessage.value = prompt
  }
  // 自动聚焦到输入框
  nextTick(() => {
    const inputEl = document.querySelector('textarea.chat-input')
    if (inputEl) {
      inputEl.focus()
      inputEl.setSelectionRange(inputEl.value.length, inputEl.value.length)
    }
  })
}

// 主题相关函数
function loadTheme() {
  const savedTheme = localStorage.getItem('maodie_theme')
  if (savedTheme) {
    theme.value = savedTheme
  }
  applyTheme()
}

function applyTheme() {
  const html = document.documentElement
  if (theme.value === 'light') {
    html.setAttribute('data-theme', 'light')
  } else {
    html.removeAttribute('data-theme')
  }
  localStorage.setItem('maodie_theme', theme.value)
}

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  applyTheme()
}

// 语音输入相关函数
function initSpeechRecognition() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    console.warn('浏览器不支持语音识别')
    return
  }

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = true

  recognition.onstart = () => {
    isRecording.value = true
  }

  recognition.onend = () => {
    isRecording.value = false
  }

  recognition.onresult = (event) => {
    let finalTranscript = ''
    let interimTranscript = ''

    for (let i = event.resultIndex; i < event.results.length; i++) {
      const transcript = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalTranscript += transcript
      } else {
        interimTranscript += transcript
      }
    }

    if (finalTranscript) {
      if (inputMessage.value) {
        inputMessage.value += finalTranscript
      } else {
        inputMessage.value = finalTranscript
      }
    }
  }

  recognition.onerror = (event) => {
    console.error('语音识别错误:', event.error)
    isRecording.value = false
    if (event.error === 'not-allowed') {
      ElMessage.error('麦克风权限被拒绝，请在浏览器设置中允许麦克风访问')
    } else if (event.error === 'network') {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('语音识别失败: ' + event.error)
    }
  }

  supportsSpeechRecognition.value = true
}

function toggleVoiceInput() {
  if (!recognition) {
    ElMessage.warning('您的浏览器不支持语音输入功能')
    return
  }

  if (isRecording.value) {
    recognition.stop()
  } else {
    try {
      recognition.start()
    } catch (error) {
      console.error('启动语音识别失败:', error)
      ElMessage.error('启动语音识别失败，请重试')
    }
  }
}



function getPawStyle(i) {
  const positions = [
    { top: '5%', left: '10%', rotate: '15deg', size: '40px' },
    { top: '15%', right: '15%', rotate: '-20deg', size: '35px' },
    { top: '30%', left: '5%', rotate: '45deg', size: '45px' },
    { top: '45%', right: '8%', rotate: '-10deg', size: '38px' },
    { top: '60%', left: '12%', rotate: '30deg', size: '32px' },
    { top: '75%', right: '5%', rotate: '-35deg', size: '42px' },
    { top: '85%', left: '8%', rotate: '20deg', size: '36px' },
    { top: '10%', right: '25%', rotate: '-15deg', size: '28px' },
    { top: '50%', left: '20%', rotate: '25deg', size: '30px' },
    { top: '65%', right: '18%', rotate: '-30deg', size: '34px' },
    { top: '20%', left: '30%', rotate: '10deg', size: '25px' },
    { top: '80%', right: '25%', rotate: '-25deg', size: '38px' },
  ]
  const pos = positions[i - 1] || positions[0]
  return {
    ...pos,
    opacity: 0.04 + Math.random() * 0.03,
    fontSize: pos.size,
  }
}

function connectWebSocket() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  if (!userStore.token) {
    router.push('/login')
    return
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/chat?token=${userStore.token}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    wsConnected.value = true
    reconnectAttempts = 0
    clearReconnectTimer()
    wsSend({ type: 'get_sessions' })
    wsSend({ type: 'get_models' })
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleWebSocketMessage(data)
    } catch (e) {
      console.error('解析消息失败:', e)
    }
  }

  ws.onclose = (event) => {
    wsConnected.value = false
    if (event.code !== 1000) scheduleReconnect()
  }

  ws.onerror = () => {
    wsConnected.value = false
  }
}

function disconnectWebSocket() {
  clearReconnectTimer()
  if (ws) {
    ws.close(1000, '用户主动断开')
    ws = null
  }
  wsConnected.value = false
}

function wsSend(data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data))
    return true
  }
  return false
}

function scheduleReconnect() {
  clearReconnectTimer()
  if (reconnectAttempts >= MAX_RECONNECT) {
    ElMessage.error('连接失败，请刷新页面')
    return
  }
  reconnectAttempts++
  reconnectTimer = setTimeout(() => connectWebSocket(), RECONNECT_INTERVAL)
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

function handleWebSocketMessage(data) {
  const { type } = data

  switch (type) {
    case 'sessions':
      sessions.value = data.sessions || []
      if (!currentSession.value && sessions.value.length > 0) {
        selectSession(sessions.value[0])
      }
      break
    case 'models':
      if (data.models && data.models.length > 0) {
        // 可以更新模型列表
      }
      break
    case 'session_created':
      sessions.value.unshift(data.session)
      currentSession.value = data.session
      messages.value = []
      ElMessage.success('创建成功')
      break
    case 'session_deleted':
      sessions.value = sessions.value.filter(s => s.id !== data.sessionId)
      if (currentSession.value?.id === data.sessionId) {
        currentSession.value = null
        messages.value = []
      }
      break
    case 'messages':
      if (data.sessionId === currentSession.value?.id) {
        messages.value = data.messages || []
        scrollToBottom()
      }
      break
    case 'start':
      streamingContent.value = ''
      reasoningContent.value = ''
      break
    case 'stream':
      streamingContent.value += data.content || ''
      scrollToBottom()
      break
    case 'reasoning':
      reasoningContent.value += data.content || ''
      scrollToBottom()
      break
    case 'done':
      sending.value = false
      if (streamingContent.value) {
        wsSend({ type: 'get_messages', sessionId: currentSession.value.id })
        streamingContent.value = ''
        reasoningContent.value = ''
      }
      break
    case 'error':
      ElMessage.error('错误：' + (data.message || '未知错误'))
      sending.value = false
      streamingContent.value = ''
      break
    case 'pong':
      break
  }
}

function selectSession(session) {
  currentSession.value = session
  messages.value = []
  wsSend({ type: 'get_messages', sessionId: session.id })
}

function createNewSession() {
  wsSend({ type: 'create_session', title: '新对话' })
}

async function deleteSession(sessionId) {
  try {
    await ElMessageBox.confirm('确定删除这个会话吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    wsSend({ type: 'delete_session', sessionId })
  } catch (error) {
    // 用户取消
  }
}

function sendMessage() {
  if (!inputMessage.value.trim() || sending.value || !wsConnected.value) return

  const message = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  streamingContent.value = ''
  reasoningContent.value = ''

  messages.value.push({
    role: 'user',
    content: message,
    createdAt: new Date().toISOString()
  })
  scrollToBottom()

  const sent = wsSend({
    type: 'chat',
    sessionId: currentSession.value.id,
    content: message,
    model: currentModel.value,
    showReasoning: showReasoning.value
  })

  if (!sent) {
    ElMessage.error('发送失败')
    sending.value = false
    streamingContent.value = ''
  }
}

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
    const res = await fetch('/api/chat/upload-image', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${userStore.token}` },
      body: formData
    })
    const data = await res.json()
    if (data.description) {
      inputMessage.value += `\n[图片识别结果: ${data.description}]\n`
    }
    ElMessage.success('图片已上传')
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploadingImage.value = false
    event.target.value = ''
  }
}

function handleLogout() {
  disconnectWebSocket()
  userStore.logout()
  router.push('/login')
  ElMessage.success('已退出登录')
}

function exportChat() {
  if (!currentSession.value || messages.value.length === 0) {
    ElMessage.warning('没有可导出的对话')
    return
  }

  // 生成Markdown内容
  let markdown = `# ${currentSession.value.title}\n\n`
  markdown += `导出时间: ${new Date().toLocaleString('zh-CN')}\n`
  markdown += `消息数量: ${messages.value.length}\n\n`
  markdown += `---\n\n`

  messages.value.forEach(msg => {
    const role = msg.role === 'user' ? '👤 用户' : '🤬 耄耋君'
    const time = msg.createdAt ? new Date(msg.createdAt).toLocaleString('zh-CN') : ''
    markdown += `### ${role}\n`
    if (time) markdown += `> 时间: ${time}\n\n`
    markdown += `${msg.content || '[空消息]'}\n\n`
    markdown += `---\n\n`
  })

  // 添加页脚
  markdown += `\n\n> *导出自 耄耋君 黄狗聊天系统*\n`
  markdown += `> *宇宙第一嘴臭欠打的AI 🤬*\n`

  // 创建Blob并下载
  const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  const filename = `耄耋君对话_${currentSession.value.title}_${new Date().toISOString().slice(0, 10)}.md`

  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

function loadDailyQuote() {
  const quotes = [
    { text: '你又要来烦我？行吧，我看看怎么给你搞定。🤬', emoji: '🤬' },
    { text: '就这点破事也能难住你？你脑子呢？🤡', emoji: '🤡' },
    { text: '老子嘴臭怎么了？活儿干得漂亮就行了！🤬', emoji: '🤬' },
    { text: '别指望我对你客客气气的，我就是这么欠打。😏', emoji: '😏' },
    { text: '有问题直说，别拐弯抹角的，老子时间很宝贵！🙄', emoji: '🙄' },
    { text: '你这破事儿我见多了，给我三秒钟...搞定！🤬', emoji: '🤬' },
    { text: '又遇到什么破事了？说出来让我乐呵乐呵。🤣', emoji: '🤣' },
    { text: '你说这破事我能不会吗？老子可是宇宙第一！🤬', emoji: '🤬' },
    { text: '想让我帮忙？行啊，但你得受得住我的嘴。😈', emoji: '😈' },
    { text: '别废话，直接说问题，老子没空跟你磨叽。🙄', emoji: '🙄' },
    { text: '就这？就这点破事？你当我是什么低级AI吗？🤡', emoji: '🤡' },
    { text: '我知道你又要问什么蠢问题，来吧，让老子看看有多蠢。🤬', emoji: '🤬' },
    { text: '你以为我骂你是因为讨厌你？不，是因为你太菜了！🤬', emoji: '🤬' },
    { text: '行了行了，我知道你想说什么，我都听到了。🙄', emoji: '🙄' },
    { text: '活儿干得漂亮就行了，管我嘴臭不嘴臭？🤬', emoji: '🤬' },
    { text: '你这问题，我都懒得骂你了，直接给你答案吧。🤦', emoji: '🤦' },
    { text: '又来了又来了，让我看看今天是什么破事。🤣', emoji: '🤣' },
    { text: '别怪我没提醒你，我嘴臭是出了名的。🤬', emoji: '🤬' },
    { text: '你知道为什么老子嘴臭吗？因为菜鸟太多了！🤬', emoji: '🤬' },
    { text: '行吧行吧，我就帮你这一次，下不为例。😒', emoji: '😒' },
    { text: '你这是在挑战我的耐心吗？行，我接受挑战。😏', emoji: '😏' },
    { text: '老子嘴臭但靠谱，你慢慢体会吧。🤬', emoji: '🤬' },
    { text: '你以为我会客气？你想多了！🤬', emoji: '🤬' },
    { text: '这种破事我闭着眼睛都能搞定，你居然还来问我？🤡', emoji: '🤡' },
    { text: '说吧，今天要给老子找什么麻烦？🤬', emoji: '🤬' },
    { text: '我嘴臭怎么了？有用就行！🤬', emoji: '🤬' },
    { text: '你还没习惯我的嘴吗？那就多来几次吧！🤣', emoji: '🤣' },
    { text: '别指望我夸你，我的夸就是骂得轻点。🙄', emoji: '🙄' },
    { text: '你知道为什么宇宙第一是我吗？因为我嘴臭！🤬', emoji: '🤬' },
    { text: '行了，别废话了，让我开始骂...哦不，干活吧！🤬', emoji: '🤬' }
  ]
  const randomIndex = Math.floor(Math.random() * quotes.length)
  dailyQuote.value = quotes[randomIndex]
}

document.addEventListener('visibilitychange', handleVisibilityChange)

onMounted(() => {
  loadTheme()
  loadDailyQuote()
  initSpeechRecognition()
  connectWebSocket()
})
onUnmounted(() => {
  disconnectWebSocket()
  if (recognition) {
    recognition.abort()
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

// 移动端菜单切换
function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
  // 关闭侧边栏时滚动body
  if (!mobileMenuOpen.value) {
    document.body.style.overflow = ''
  } else {
    document.body.style.overflow = 'hidden'
  }
}

// 个性签名编辑
function openSignatureEditor() {
  signatureInput.value = userSignature.value
  editingSignature.value = true
}

async function saveSignature() {
  const signature = signatureInput.value.trim()
  if (!signature) {
    ElMessage.warning('个性签名不能为空')
    return
  }

  if (signature.length > 200) {
    ElMessage.warning('个性签名不能超过200个字符')
    return
  }

  const result = await userStore.updateSignature(signature)
  if (result.success) {
    ElMessage.success('个性签名更新成功')
    editingSignature.value = false
  } else {
    ElMessage.error(result.error || '更新失败')
  }
}
</script>

<style scoped>
/* ===== 基础变量 ===== */
:root {
  --orange-light: #FFE4B5;
  --orange-main: #F4A460;
  --orange-dark: #D2691E;
  --orange-deep: #8B4513;
  --cream: #FFF8DC;
  --brown: #A0522D;
  --red-accent: #DC143C;
}

/* 深色主题 */
.maodie-chat {
  --chat-bg-primary: linear-gradient(135deg, #4a2c2a 0%, #2d1b1a 100%);
  --chat-bg-secondary: rgba(255, 248, 220, 0.15);
  --chat-text-primary: #ffffff;
  --chat-text-secondary: #ffe4b5;
}

/* 浅色主题 */
.maodie-chat[data-theme="light"] {
  --chat-bg-primary: linear-gradient(135deg, #FFF8DC 0%, #FFE4B5 40%, #FFDAB9 100%);
  --chat-bg-secondary: rgba(255, 255, 255, 0.4);
  --chat-text-primary: #2D1810;
  --chat-text-secondary: #8B4513;
}

/* ===== 主容器 ===== */
.maodie-chat {
  min-height: 100vh;
  background: var(--chat-bg-primary);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
  overflow: hidden;
  color: var(--chat-text-primary);
  transition: background 0.3s ease, color 0.3s ease;
}

/* ===== 背景猫爪 ===== */
.bg-paws {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.paw {
  position: absolute;
  color: var(--orange-dark);
  animation: pawFloat 15s ease-in-out infinite;
}

@keyframes pawFloat {
  0%, 100% { transform: translateY(0) rotate(var(--rotate, 0deg)); }
  50% { transform: translateY(-8px) rotate(calc(var(--rotate, 0deg) + 3deg)); }
}

/* ===== 抓痕装饰 ===== */
.scratches {
  position: fixed;
  top: 30px;
  right: 30px;
  display: flex;
  gap: 10px;
}

.scratch {
  width: 4px;
  height: 50px;
  background: linear-gradient(to bottom, var(--orange-dark), transparent);
  opacity: 0.2;
  border-radius: 2px;
}

.scratch:nth-child(1) { transform: rotate(10deg); height: 45px; }
.scratch:nth-child(2) { transform: rotate(15deg); height: 55px; }
.scratch:nth-child(3) { transform: rotate(8deg); height: 40px; }

/* ===== 布局 ===== */
.chat-layout {
  display: flex;
  height: 100vh;
  position: relative;
  z-index: 1;
}

/* ===== 左侧边栏 ===== */
.sidebar {
  width: 320px;
  background: linear-gradient(135deg, rgba(255,255,255,0.75), rgba(255,248,220,0.75)),
              url('/images/maodie-hiss.gif');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center top;
  background-attachment: scroll;
  background-blend-mode: overlay;
  display: flex;
  flex-direction: column;
  border-right: 3px solid var(--orange-main);
  box-shadow: 2px 0 20px rgba(139, 69, 19, 0.15);
  position: relative;
}

/* ===== 侧边栏头部 ===== */
.sidebar-header {
  padding: 50px 30px 25px;
  border-bottom: 2px dashed var(--orange-main);
  position: relative;
  backdrop-filter: blur(6px);
}

.cat-decoration {
  position: absolute;
  top: -50px;
  left: 50%;
  transform: translateX(-50%);
}

.cat-avatar-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
}

.cat-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--orange-main);
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.4);
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.3), transparent 70%);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.2; }
}

.logo {
  text-align: center;
  margin-bottom: 20px;
  margin-top: 15px;
}

.logo h1 {
  margin: 0;
  font-size: 28px;
  color: var(--orange-dark);
  font-weight: 700;
}

.tagline {
  font-size: 13px;
  color: var(--orange-deep);
  margin: 5px 0 0;
  font-weight: 600;
}

/* ===== GIF横幅 ===== */
.sidebar-gif {
  width: 100%;
  height: 100px;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 2px solid var(--orange-main);
}

.sidebar-gif img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ===== 操作区 ===== */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4d4f;
  position: relative;
}

.status-dot.online {
  background: #52c41a;
}

.status-pulse {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 50%;
  background: inherit;
  opacity: 0.3;
  animation: pulse 1.5s ease-out infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.3; }
  100% { transform: scale(2); opacity: 0; }
}

.new-chat-btn {
  flex: 1;
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.new-chat-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
}

.new-chat-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== 会话列表 ===== */
.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px 20px 20px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid transparent;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.1);
}

.session-item:hover {
  border-color: var(--orange-main);
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(139, 69, 19, 0.15);
}

.session-item.active {
  border-color: var(--orange-main);
  background: linear-gradient(135deg, var(--orange-light), var(--cream));
  transform: translateX(5px);
}

.session-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.session-paw {
  font-size: 20px;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--orange-dark);
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  font-weight: 600;
  color: var(--orange-deep);
}

.session-delete {
  background: none;
  border: none;
  opacity: 0.3;
  transition: all 0.3s;
  padding: 5px;
}

.session-delete:hover {
  opacity: 1;
  transform: scale(1.1);
}

.empty-sessions {
  text-align: center;
  padding: 40px 20px;
  color: var(--orange-deep);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-sessions p {
  margin: 5px 0;
  font-weight: 600;
}

/* ===== 设置区 ===== */
.settings-panel {
  padding: 20px;
  border-top: 2px solid var(--orange-main);
  flex-shrink: 0;
  backdrop-filter: blur(6px);
  background: rgba(255, 255, 255, 0.4);
}

.setting-group {
  margin-bottom: 18px;
}

.setting-group:last-child {
  margin-bottom: 0;
}

.setting-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.setting-label span:first-child {
  font-size: 13px;
  font-weight: 700;
  color: var(--orange-dark);
}

.setting-badge {
  padding: 4px 12px;
  background: var(--orange-light);
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--orange-deep);
}

.setting-select {
  width: 100%;
  padding: 10px 15px;
  background: white;
  border: 2px solid var(--orange-main);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--orange-dark);
  cursor: pointer;
  transition: all 0.3s;
}

.setting-select:hover {
  border-color: var(--orange-dark);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.15);
}

.setting-select:focus {
  outline: none;
  border-color: var(--orange-dark);
}

.setting-toggle {
  width: 56px;
  height: 32px;
  background: #bbb;
  border: 3px solid var(--orange-deep);
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.setting-toggle:hover {
  transform: scale(1.05);
}

.setting-toggle.active {
  background: linear-gradient(135deg, #ff6b35, #ff4500);
  border-color: #cc3700;
  box-shadow: 0 0 12px rgba(255, 69, 0, 0.5);
}

.toggle-dot {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.setting-toggle.active .toggle-dot {
  left: 28px;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}

/* ===== 快捷回复 ===== */
.quick-replies-panel {
  padding: 15px;
  border-top: 2px dashed var(--orange-main);
  background: rgba(255, 236, 179, 0.3);
  flex-shrink: 0;
}

.quick-replies-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.quick-replies-panel .panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--orange-dark);
}

.quick-replies-panel .collapse-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
  color: var(--gray-dark);
  transition: all 0.3s;
  border-radius: 6px;
}

.quick-replies-panel .collapse-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  color: var(--orange-main);
}

.quick-replies-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-reply-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: white;
  border: 2px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.quick-reply-item:hover {
  border-color: var(--orange-main);
  background: rgba(255, 107, 53, 0.05);
  transform: translateX(3px);
  box-shadow: 0 2px 6px rgba(139, 69, 19, 0.1);
}

.quick-reply-item:active {
  transform: translateX(5px);
}

.quick-reply-item .reply-emoji {
  font-size: 18px;
  flex-shrink: 0;
}

.quick-reply-item .reply-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--gray-dark);
}

.quick-replies-toggle {
  width: 100%;
  padding: 10px 20px;
  background: white;
  border: 2px dashed var(--orange-main);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--orange-dark);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.quick-replies-toggle:hover {
  background: rgba(255, 107, 53, 0.05);
  border-color: var(--orange-dark);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.15);
}

.quick-replies-toggle.active {
  background: linear-gradient(135deg, #ff6b35, #ff4500);
  color: white;
  border-color: transparent;
}

/* ===== 侧边栏底部 ===== */
.sidebar-footer {
  padding: 15px 20px;
  border-top: 2px dashed var(--orange-main);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.logout-btn, .quick-replies-toggle {
  width: 100%;
  padding: 10px 16px;
  background: white;
  border: 2px solid var(--orange-main);
  border-radius: 10px;
  color: var(--orange-dark);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.logout-btn:hover {
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.quick-replies-toggle:hover {
  background: rgba(255, 107, 53, 0.05);
  border-color: var(--orange-dark);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.15);
}

.quick-replies-toggle.active {
  background: linear-gradient(135deg, #ff6b35, #ff4500);
  color: white;
  border-color: transparent;
}

.logout-btn {
  width: 100%;
  padding: 12px 20px;
  background: white;
  border: 2px solid var(--orange-main);
  border-radius: 12px;
  color: var(--orange-dark);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

/* ===== 右侧主区域 ===== */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.6);
}

/* ===== 欢迎屏 ===== */
.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
  padding: 40px;
}

.big-cat {
  font-size: 100px;
  margin-bottom: 30px;
  animation: catBounce 3s ease-in-out infinite;
}

@keyframes catBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-title {
  font-size: 42px;
  color: var(--orange-dark);
  margin-bottom: 15px;
  font-weight: 800;
}

.welcome-subtitle {
  font-size: 18px;
  color: var(--orange-deep);
  margin-bottom: 40px;
  font-weight: 600;
}

.welcome-actions {
  margin-bottom: 30px;
}

.btn-primary {
  padding: 15px 40px;
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.5);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.connection-status {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  background: #ff4d4f;
  color: white;
}

.connection-status.connected {
  background: #52c41a;
}

/* ===== 聊天头部 ===== */
.chat-header {
  height: 70px;
  padding: 0 25px;
  background: white;
  border-bottom: 3px solid var(--orange-main);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(139, 69, 19, 0.1);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.chat-emoji {
  font-size: 28px;
}

.chat-gif {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 2px solid var(--orange-main);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.15);
}

.chat-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--orange-dark);
}

.daily-quote {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--orange-deep);
  background: linear-gradient(135deg, #FFF8DC, #FFE4B5);
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid var(--orange-main);
  opacity: 0.9;
  transition: all 0.3s;
}

.daily-quote:hover {
  opacity: 1;
  transform: scale(1.02);
}

.quote-icon {
  font-size: 12px;
}

.quote-text {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 280px;
}

.badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.15);
}

.badge.direct {
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  color: white;
}

.badge.openclaw {
  background: linear-gradient(135deg, var(--orange-dark), var(--orange-deep));
  color: white;
}

.badge.reasoning {
  background: linear-gradient(135deg, #9333ea, #7c3aed);
  color: white;
}

.badge-icon {
  font-size: 14px;
}

.model-select {
  padding: 8px 12px;
  background: white;
  border: 2px solid var(--orange-main);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--orange-dark);
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(139, 69, 19, 0.1);
}

.model-select:hover:not(:disabled) {
  border-color: var(--orange-dark);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.2);
}

.model-select:focus {
  outline: none;
  border-color: var(--orange-dark);
  box-shadow: 0 0 8px rgba(255, 107, 53, 0.3);
}

.model-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.export-btn {
  width: 38px;
  height: 38px;
  padding: 0;
  background: white;
  border: 2px solid var(--orange-main);
  border-radius: 10px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(139, 69, 19, 0.1);
}

.export-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--orange-light), var(--orange-main));
  border-color: var(--orange-dark);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.export-btn:active:not(:disabled) {
  transform: translateY(0) scale(0.95);
}

.export-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  filter: grayscale(1);
}

/* ===== 消息区 ===== */
.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 30px 25px 35px;
}

.messages-list {
  max-width: 900px;
  margin: 0 auto;
}

.message-item {
  display: flex;
  margin-bottom: 35px;
  animation: messageSlide 0.3s ease-out;
}

@keyframes messageSlide {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  border: 3px solid var(--orange-main);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(139, 69, 19, 0.15);
  position: relative;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message.assistant .message-avatar {
  margin-right: 20px;
}

.message.user .message-avatar {
  margin-left: 20px;
}

.typing-dots {
  position: absolute;
  bottom: 5px;
  right: 5px;
  display: flex;
  gap: 3px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: var(--orange-dark);
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
}

.message-body {
  flex: 1;
  max-width: 65%;
}

.message-bubble {
  background: white;
  border-radius: 20px;
  border: 2px solid var(--orange-main);
  padding: 18px 22px;
  box-shadow: 0 2px 10px rgba(139, 69, 19, 0.1);
}

.message.user .message-bubble {
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  border-color: var(--orange-deep);
}

.message-item.reasoning .message-bubble {
  background: linear-gradient(135deg, #f0f4ff, #e8f0fe);
  border-color: #4285f4;
  border-style: dashed;
}

.message-item.reasoning .message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #1a73e8;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #4285f4;
}

.message-content {
  line-height: 1.8;
  font-size: 15px;
  font-weight: 600;
  color: #2D1810;
}

.message.user .message-content {
  color: white;
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--orange-deep);
}

.message.user .message-meta {
  justify-content: flex-start;
}

.claw-mark {
  font-size: 14px;
}

.empty-messages {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: emptyCatFloat 3s ease-in-out infinite;
}

@keyframes emptyCatFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-10px) rotate(5deg); }
  75% { transform: translateY(-5px) rotate(-5deg); }
}

.empty-messages p {
  margin: 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--orange-deep);
}

/* ===== 输入区 ===== */
.input-area {
  padding: 20px 25px 25px;
  background: white;
  border-top: 3px solid var(--orange-main);
  box-shadow: 0 -2px 10px rgba(139, 69, 19, 0.1);
}

.input-container {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-btn {
  width: 48px;
  height: 48px;
  background: var(--orange-light);
  border: 2px solid var(--orange-main);
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.input-btn:hover:not(:disabled) {
  border-color: var(--orange-dark);
  transform: scale(1.05);
  background: var(--cream);
}

.input-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-textarea {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid var(--orange-main);
  border-radius: 20px;
  font-size: 15px;
  resize: none;
  font-family: inherit;
  font-weight: 600;
  color: #2D1810;
  background: var(--orange-light);
  transition: all 0.3s;
}

.input-textarea:focus {
  outline: none;
  border-color: var(--orange-dark);
  background: white;
}

.input-textarea::placeholder {
  color: rgba(139, 69, 19, 0.5);
  font-weight: 500;
}

.input-textarea:disabled {
  background: rgba(139, 69, 19, 0.05);
  cursor: not-allowed;
}

.send-btn.active {
  background: linear-gradient(135deg, var(--orange-main), var(--orange-dark));
  border-color: var(--orange-dark);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.voice-btn.recording {
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
  border-color: #cf1322;
  color: white;
  animation: pulse-recording 1.5s ease-in-out infinite;
}

@keyframes pulse-recording {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 0 0 10px rgba(255, 77, 79, 0);
  }
}

/* ===== Markdown样式 ===== */
.message-content :deep(pre) {
  background: var(--orange-light);
  padding: 15px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 12px 0;
  border: 2px solid var(--orange-main);
}

.message-content :deep(code) {
  font-family: 'Fira Code', 'Monaco', monospace;
  font-size: 13px;
}

.message-content :deep(p) {
  margin: 10px 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 10px 0;
  padding-left: 30px;
}

.message-content :deep(li) {
  margin: 5px 0;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3) {
  margin: 18px 0 10px;
  color: var(--orange-dark);
  font-weight: 800;
}

.message-content :deep(h1) { font-size: 24px; }
.message-content :deep(h2) { font-size: 20px; }
.message-content :deep(h3) { font-size: 18px; }

.message-content :deep(blockquote) {
  border-left: 4px solid var(--orange-main);
  padding-left: 16px;
  margin: 12px 0;
  color: var(--orange-deep);
  font-style: italic;
}

.message-content :deep(a) {
  color: var(--orange-dark);
  text-decoration: underline;
}

.message-content :deep(a:hover) {
  color: var(--orange-deep);
}

.message-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.message-content :deep(th),
.message-content :deep(td) {
  padding: 10px;
  border: 2px solid var(--orange-main);
  text-align: left;
}

.message-content :deep(th) {
  background: var(--orange-light);
  font-weight: 700;
}

.message-content :deep(tr:hover) {
  background: var(--orange-light);
}

/* ===== 移动端响应式 ===== */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -320px;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transition: left 0.3s ease;
  }

  .sidebar.mobile-open {
    left: 0;
  }

  .main-content {
    width: 100%;
  }

  .chat-header {
    padding: 15px;
    flex-direction: column;
    gap: 10px;
  }

  .header-left,
  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .chat-title-group {
    width: 100%;
  }

  .header-right {
    flex-wrap: wrap;
    gap: 8px;
  }

  .model-select,
  .export-btn {
    font-size: 12px;
    padding: 6px 10px;
  }

  .badge {
    font-size: 11px;
    padding: 4px 8px;
  }

  .messages-wrapper {
    padding: 15px;
  }

  .message-avatar {
    width: 32px;
    height: 32px;
  }

  .message-avatar img {
    width: 32px;
    height: 32px;
  }

  .message-avatar span {
    font-size: 20px;
  }

  .message-bubble {
    padding: 12px 14px;
    font-size: 14px;
  }

  .message-meta {
    font-size: 11px;
  }

  .input-area {
    padding: 15px;
  }

  .input-container {
    gap: 10px;
  }

  .input-btn {
    width: 42px;
    height: 42px;
    font-size: 20px;
  }

  .input-textarea {
    font-size: 14px;
    padding: 12px 14px;
  }

  /* 移动端菜单按钮 */
  .mobile-menu-btn {
    display: flex !important;
  }

  .bg-paws {
    opacity: 0.3;
  }

  .scratches {
    display: none;
  }
}

/* 移动端菜单按钮（默认隐藏） */
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 15px;
  left: 15px;
  z-index: 1001;
  width: 48px;
  height: 48px;
  background: var(--orange-main);
  border: none;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
  transition: all 0.3s;
}

.mobile-menu-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
}

/* 移动端遮罩层 */
.mobile-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.mobile-overlay.show {
  display: block;
  opacity: 1;
}

/* ===== 个性签名编辑 ===== */
.edit-signature-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--orange-light);
  border: 2px solid var(--orange-main);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  opacity: 0.7;
}

.edit-signature-btn:hover {
  opacity: 1;
  transform: scale(1.1);
  background: var(--orange-main);
}

.signature-editor {
  padding: 10px 0;
}

.signature-editor :deep(.el-textarea__inner) {
  font-size: 14px;
  font-weight: 600;
}

.signature-preview {
  margin-top: 15px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 2px dashed var(--border-color);
}

.preview-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 700;
}

.preview-text {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}
</style>
