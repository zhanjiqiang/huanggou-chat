const express = require('express');
const { requireAuth } = require('./auth');
const { createSession } = require('../openclaw');
const db = require('../database');

const router = express.Router();

// 获取用户的会话列表
router.get('/', requireAuth, (req, res) => {
  const userId = req.user.userId;

  const sessions = db.prepare(`
    SELECT 
      s.*,
      (SELECT content FROM messages WHERE session_id = s.id ORDER BY created_at DESC LIMIT 1) as last_message,
      (SELECT COUNT(*) FROM messages WHERE session_id = s.id) as message_count
    FROM sessions s
    WHERE s.user_id = ?
    ORDER BY s.updated_at DESC
  `).all(userId);

  res.json({ sessions });
});

// 获取单个会话详情
router.get('/:id', requireAuth, (req, res) => {
  const userId = req.user.userId;
  const sessionId = req.params.id;

  const session = db.prepare(
    'SELECT * FROM sessions WHERE id = ? AND user_id = ?'
  ).get(sessionId, userId);

  if (!session) {
    return res.status(404).json({ error: '会话不存在' });
  }

  res.json({ session });
});

// 获取会话的消息历史
router.get('/:id/messages', requireAuth, (req, res) => {
  const userId = req.user.userId;
  const sessionId = req.params.id;

  // 检查会话是否属于当前用户
  const session = db.prepare(
    'SELECT id FROM sessions WHERE id = ? AND user_id = ?'
  ).get(sessionId, userId);

  if (!session) {
    return res.status(404).json({ error: '会话不存在' });
  }

  // 获取消息
  const messages = db.prepare(`
    SELECT * FROM messages
    WHERE session_id = ?
    ORDER BY created_at ASC
  `).all(sessionId);

  res.json({ messages });
});

// 创建新会话
router.post('/', requireAuth, async (req, res) => {
  const userId = req.user.userId;
  const { title, model } = req.body;

  try {
    // 在OpenClaw创建会话
    const openclawSession = await createSession({
      task: '聊天助手',
      model: model || 'zai/glm-4.7',
      runtime: 'subagent',
      mode: 'session',
      thinking: 'off'
    });

    // 在数据库中保存会话
    const result = db.prepare(`
      INSERT INTO sessions (user_id, title) VALUES (?, ?)
    `).run(userId, title || '新对话');

    const sessionId = result.lastInsertRowid;

    // 更新session_key
    db.prepare(`
      UPDATE sessions SET title = ? WHERE id = ?
    `).run(`${openclawSession.sessionKey}`, sessionId);

    res.json({
      id: sessionId,
      sessionKey: openclawSession.sessionKey,
      title: title || '新对话',
      created_at: new Date().toISOString()
    });
  } catch (error) {
    console.error('创建会话失败:', error);
    res.status(500).json({ error: '创建失败' });
  }
});

// 删除会话
router.delete('/:id', requireAuth, async (req, res) => {
  const userId = req.user.userId;
  const sessionId = req.params.id;

  try {
    const session = db.prepare(
      'SELECT * FROM sessions WHERE id = ? AND user_id = ?'
    ).get(sessionId, userId);

    if (!session) {
      return res.status(404).json({ error: '会话不存在' });
    }

    // 删除数据库记录（级联删除消息）
    db.prepare('DELETE FROM sessions WHERE id = ?').run(sessionId);

    // 删除OpenClaw会话
    // 注意：OpenClaw可能没有直接的删除API，可以忽略或通过subagents kill

    res.json({ success: true });
  } catch (error) {
    console.error('删除会话失败:', error);
    res.status(500).json({ error: '删除失败' });
  }
});

module.exports = router;
