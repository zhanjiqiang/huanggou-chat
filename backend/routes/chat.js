const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { requireAuth } = require('./auth');
const db = require('../database');
const openclaw = require('../openclaw');

const router = express.Router();

// 配置文件上传
const upload = multer({
  dest: path.join(__dirname, 'uploads'),
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB
});

// 发送文本消息
router.post('/send', requireAuth, async (req, res) => {
  const { sessionId, message, model } = req.body;
  const userId = req.user.userId;

  try {
    // 获取会话
    const session = db.prepare(
      'SELECT * FROM sessions WHERE id = ? AND user_id = ?'
    ).get(sessionId, userId);

    if (!session) {
      return res.status(404).json({ error: '会话不存在' });
    }

    // 保存用户消息
    db.prepare(
      'INSERT INTO messages (session_id, role, content, model) VALUES (?, ?, ?, ?)'
    ).run(sessionId, 'user', message, model || null);

    // 更新会话时间
    db.prepare(
      'UPDATE sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = ?'
    ).run(sessionId);

    // 发送到OpenClaw
    const response = await openclaw.sendMessage(session.session_key, message);

    // 保存AI回复
    db.prepare(
      'INSERT INTO messages (session_id, role, content, model) VALUES (?, ?, ?, ?)'
    ).run(sessionId, 'assistant', response.reply || '', model || null);

    res.json({ reply: response.reply });
  } catch (error) {
    console.error('发送消息失败:', error);
    res.status(500).json({ error: '发送失败' });
  }
});

// 流式发送消息（SSE）
router.post('/stream', requireAuth, async (req, res) => {
  const { sessionId, message, model } = req.body;
  const userId = req.user.userId;

  try {
    // 获取会话
    const session = db.prepare(
      'SELECT * FROM sessions WHERE id = ? AND user_id = ?'
    ).get(sessionId, userId);

    if (!session) {
      return res.status(404).json({ error: '会话不存在' });
    }

    // 保存用户消息
    db.prepare(
      'INSERT INTO messages (session_id, role, content, model) VALUES (?, ?, ?, ?)'
    ).run(sessionId, 'user', message, model || null);

    // 更新会话时间
    db.prepare(
      'UPDATE sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = ?'
    ).run(sessionId);

    // 流式发送到OpenClaw
    await openclaw.sendMessageStream(session.session_key, message, res);
  } catch (error) {
    console.error('流式发送失败:', error);
    if (!res.headersSent) {
      res.status(500).json({ error: '发送失败' });
    }
  }
});

// 上传图片并识别
router.post('/upload', requireAuth, upload.single('image'), async (req, res) => {
  const userId = req.user.userId;

  try {
    if (!req.file) {
      return res.status(400).json({ error: '请上传图片' });
    }

    const imagePath = req.file.path;
    const imageBase64 = fs.readFileSync(imagePath, 'base64');
    const dataUrl = `data:image/${req.file.mimetype.split('/')[1]};base64,${imageBase64}`;

    // 清理临时文件
    fs.unlinkSync(imagePath);

    // 识别图片（使用OpenClaw的image工具）
    // 这里需要调用OpenClaw的API，暂时返回占位符
    res.json({
      success: true,
      description: '图片识别功能待实现',
      imageUrl: dataUrl
    });
  } catch (error) {
    console.error('图片识别失败:', error);
    res.status(500).json({ error: '识别失败' });
  }
});

module.exports = router;
