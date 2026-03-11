/**
 * WebSocket 连接管理器
 * 
 * 功能：
 * 1. 自动连接/重连
 * 2. 心跳保活
 * 3. 消息队列
 * 4. 事件订阅
 */

import { ref, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'

export function useWebSocket() {
  const userStore = useUserStore()
  
  let ws = null
  let reconnectTimer = null
  let heartbeatTimer = null
  let reconnectAttempts = 0
  
  const isConnected = ref(false)
  const connectionId = ref(null)
  
  // 消息处理器映射
  const messageHandlers = new Map()
  
  // 默认配置
  const config = {
    url: '',  // WebSocket URL
    heartbeatInterval: 30000,  // 心跳间隔 30s
    reconnectInterval: 3000,   // 重连间隔 3s
    maxReconnectAttempts: 10   // 最大重连次数
  }
  
  /**
   * 连接 WebSocket
   */
  function connect(sessionId = null) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket 已连接')
      return
    }
    
    // 构建 WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    let wsUrl = `${protocol}//${host}/ws/chat?token=${userStore.token}`
    
    if (sessionId) {
      wsUrl += `&sessionId=${sessionId}`
    }
    
    console.log('连接 WebSocket:', wsUrl)
    
    ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log('WebSocket 连接成功')
      isConnected.value = true
      reconnectAttempts = 0
      startHeartbeat()
      
      // 如果是重连，发送重连消息
      if (reconnectAttempts > 0) {
        send({
          type: 'reconnect',
          userId: userStore.userId,
          sessionId: sessionId
        })
      }
    }
    
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleMessage(data)
      } catch (e) {
        console.error('解析 WebSocket 消息失败:', e)
      }
    }
    
    ws.onclose = (event) => {
      console.log('WebSocket 连接关闭:', event.code, event.reason)
      isConnected.value = false
      stopHeartbeat()
      
      // 非正常关闭，尝试重连
      if (event.code !== 1000) {
        scheduleReconnect(sessionId)
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket 错误:', error)
      isConnected.value = false
    }
  }
  
  /**
   * 断开连接
   */
  function disconnect() {
    stopHeartbeat()
    clearReconnectTimer()
    
    if (ws) {
      ws.close(1000, '用户主动断开')
      ws = null
    }
    
    isConnected.value = false
  }
  
  /**
   * 发送消息
   */
  function send(data) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn('WebSocket 未连接，消息发送失败')
      return false
    }
    
    try {
      ws.send(JSON.stringify(data))
      return true
    } catch (e) {
      console.error('发送 WebSocket 消息失败:', e)
      return false
    }
  }
  
  /**
   * 发送聊天消息
   */
  function sendChatMessage(sessionId, content, model) {
    return send({
      type: 'chat',
      sessionId,
      content,
      model
    })
  }
  
  /**
   * 订阅消息类型
   */
  function on(type, handler) {
    if (!messageHandlers.has(type)) {
      messageHandlers.set(type, new Set())
    }
    messageHandlers.get(type).add(handler)
    
    // 返回取消订阅函数
    return () => {
      const handlers = messageHandlers.get(type)
      if (handlers) {
        handlers.delete(handler)
      }
    }
  }
  
  /**
   * 取消订阅
   */
  function off(type, handler) {
    const handlers = messageHandlers.get(type)
    if (handlers) {
      if (handler) {
        handlers.delete(handler)
      } else {
        messageHandlers.delete(type)
      }
    }
  }
  
  /**
   * 处理收到的消息
   */
  function handleMessage(data) {
    const { type, ...rest } = data
    
    // 触发对应类型的处理器
    const handlers = messageHandlers.get(type)
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(rest)
        } catch (e) {
          console.error('消息处理器执行失败:', e)
        }
      })
    }
    
    // 触发通配符处理器
    const wildcardHandlers = messageHandlers.get('*')
    if (wildcardHandlers) {
      wildcardHandlers.forEach(handler => {
        try {
          handler(data)
        } catch (e) {
          console.error('通配符处理器执行失败:', e)
        }
      })
    }
  }
  
  /**
   * 启动心跳
   */
  function startHeartbeat() {
    stopHeartbeat()
    
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        send({ type: 'ping' })
      }
    }, config.heartbeatInterval)
  }
  
  /**
   * 停止心跳
   */
  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }
  
  /**
   * 计划重连
   */
  function scheduleReconnect(sessionId) {
    clearReconnectTimer()
    
    if (reconnectAttempts >= config.maxReconnectAttempts) {
      console.warn('达到最大重连次数，停止重连')
      return
    }
    
    reconnectAttempts++
    console.log(`计划重连 (${reconnectAttempts}/${config.maxReconnectAttempts})...`)
    
    reconnectTimer = setTimeout(() => {
      connect(sessionId)
    }, config.reconnectInterval)
  }
  
  /**
   * 清除重连计时器
   */
  function clearReconnectTimer() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }
  
  // 组件卸载时自动断开
  onUnmounted(() => {
    disconnect()
  })
  
  // 页面可见性变化时重连
  if (typeof document !== 'undefined') {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && !isConnected.value) {
        console.log('页面恢复可见，尝试重连 WebSocket')
        // 触发重连事件
        const handlers = messageHandlers.get('_visibility_change')
        if (handlers) {
          handlers.forEach(h => h({ visible: true }))
        }
      }
    })
  }
  
  return {
    isConnected,
    connectionId,
    connect,
    disconnect,
    send,
    sendChatMessage,
    on,
    off
  }
}
