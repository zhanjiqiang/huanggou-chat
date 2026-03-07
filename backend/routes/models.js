const express = require('express');
const { requireAuth } = require('./auth');
const db = require('../database');

const router = express.Router();

// 可用模型列表
const AVAILABLE_MODELS = [
  { id: 'zai/glm-4.7', name: 'GLM-4.7', description: '默认主力，性价比高', recommended: true },
  { id: 'zai/glm-5', name: 'GLM-5', description: '最强能力，复杂任务', recommended: false },
  { id: 'zai/glm-4.5-air', name: 'GLM-4.5 Air', description: '轻量快速', recommended: false }
];

// 获取所有可用模型
router.get('/available', (req, res) => {
  res.json({ models: AVAILABLE_MODELS });
});

// 获取用户收藏的模型
router.get('/my', requireAuth, (req, res) => {
  const userId = req.user.userId;

  const models = db.prepare(`
    SELECT * FROM user_models
    WHERE user_id = ?
    ORDER BY is_default DESC, created_at ASC
  `).all(userId);

  res.json({ models });
});

// 添加模型到用户列表
router.post('/add', requireAuth, (req, res) => {
  const userId = req.user.userId;
  const { modelName } = req.body;

  try {
    // 检查模型是否已存在
    const existing = db.prepare(
      'SELECT id FROM user_models WHERE user_id = ? AND model_name = ?'
    ).get(userId, modelName);

    if (existing) {
      return res.status(400).json({ error: '模型已存在' });
    }

    // 添加模型
    db.prepare(
      'INSERT INTO user_models (user_id, model_name, is_default) VALUES (?, ?, ?)'
    ).run(userId, modelName, 0);

    res.json({ success: true });
  } catch (error) {
    console.error('添加模型失败:', error);
    res.status(500).json({ error: '添加失败' });
  }
});

// 设置默认模型
router.post('/setDefault', requireAuth, (req, res) => {
  const userId = req.user.userId;
  const { modelName } = req.body;

  try {
    // 取消所有默认
    db.prepare(
      'UPDATE user_models SET is_default = 0 WHERE user_id = ?'
    ).run(userId);

    // 设置新的默认
    db.prepare(
      'UPDATE user_models SET is_default = 1 WHERE user_id = ? AND model_name = ?'
    ).run(userId, modelName);

    res.json({ success: true });
  } catch (error) {
    console.error('设置默认模型失败:', error);
    res.status(500).json({ error: '设置失败' });
  }
});

// 删除模型
router.delete('/:id', requireAuth, (req, res) => {
  const userId = req.user.userId;
  const modelId = req.params.id;

  try {
    db.prepare(
      'DELETE FROM user_models WHERE id = ? AND user_id = ?'
    ).run(modelId, userId);

    res.json({ success: true });
  } catch (error) {
    console.error('删除模型失败:', error);
    res.status(500).json({ error: '删除失败' });
  }
});

module.exports = router;
