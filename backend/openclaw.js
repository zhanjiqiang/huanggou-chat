const axios = require('axios');

// OpenClaw Gateway 配置
const GATEWAY_BASE = 'http://localhost:18789';

// 创建新会话
async function createSession(data) {
  try {
    const response = await axios.post(`${GATEWAY_BASE}/sessions/spawn`, data, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('创建会话失败:', error.message);
    throw error;
  }
}

// 发送消息（非流式）
async function sendMessage(sessionKey, message) {
  try {
    const response = await axios.post(`${GATEWAY_BASE}/sessions/send`, {
      sessionKey,
      message
    });
    return response.data;
  } catch (error) {
    console.error('发送消息失败:', error.message);
    throw error;
  }
}

// 发送消息（流式输出，使用SSE）
async function sendMessageStream(sessionKey, message, res) {
  try {
    const response = await axios.post(`${GATEWAY_BASE}/sessions/send`, {
      sessionKey,
      message
    }, {
      responseType: 'stream'
    });

    // 设置SSE响应头
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    // 流式转发
    response.data.on('data', (chunk) => {
      res.write(`data: ${chunk.toString()}\n\n`);
    });

    response.data.on('end', () => {
      res.write('data: [DONE]\n\n');
      res.end();
    });

    response.data.on('error', (err) => {
      console.error('流式传输错误:', err);
      res.write(`data: [ERROR] ${err.message}\n\n`);
      res.end();
    });
  } catch (error) {
    console.error('流式发送失败:', error.message);
    res.write(`data: [ERROR] ${error.message}\n\n`);
    res.end();
  }
}

// 获取会话列表
async function listSessions() {
  try {
    const response = await axios.get(`${GATEWAY_BASE}/sessions/list`);
    return response.data;
  } catch (error) {
    console.error('获取会话列表失败:', error.message);
    return [];
  }
}

// 获取会话历史
async function getSessionHistory(sessionKey) {
  try {
    const response = await axios.get(`${GATEWAY_BASE}/sessions/history`, {
      params: { sessionKey }
    });
    return response.data;
  } catch (error) {
    console.error('获取会话历史失败:', error.message);
    return [];
  }
}

// 删除会话
async function deleteSession(sessionKey) {
  try {
    // OpenClaw 可能没有直接的删除API，这里用subagents
    const response = await axios.post(`${GATEWAY_BASE}/subagents`, {
      action: 'kill',
      target: sessionKey
    });
    return response.data;
  } catch (error) {
    console.error('删除会话失败:', error.message);
    throw error;
  }
}

// 切换模型
async function switchModel(sessionKey, model) {
  try {
    const response = await axios.post(`${GATEWAY_BASE}/session/status`, {
      sessionKey,
      model
    });
    return response.data;
  } catch (error) {
    console.error('切换模型失败:', error.message);
    throw error;
  }
}

module.exports = {
  createSession,
  sendMessage,
  sendMessageStream,
  listSessions,
  getSessionHistory,
  deleteSession,
  switchModel
};
