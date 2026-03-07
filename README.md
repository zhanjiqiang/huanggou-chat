# 黄狗一号聊天机器人 🤬

> OpenClaw Web聊天前端 - 支持文字聊天、图片识别、会话管理

## 功能特性

- ✅ **文字聊天** - 与AI实时对话
- ✅ **流式输出** - 实时显示AI回复
- ✅ **图片识别** - 上传截图让AI识别
- ✅ **会话管理** - 新建/删除/切换会话
- ✅ **历史记录** - 查看历史会话和消息
- ✅ **模型切换** - 支持GLM-4.7/GLM-5等模型
- ✅ **用户认证** - 登录/注册系统
- ✅ **Markdown渲染** - 支持代码高亮和格式化

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus |
| 后端 | Node.js + Express |
| 数据库 | SQLite (better-sqlite3) |
| 认证 | JWT Token |
| 托管 | GitHub Pages (前端) + 服务器 (后端) |

## 项目结构

```
huanggou-chat/
├── backend/                 # 后端服务
│   ├── server.js           # Express服务器
│   ├── database.js         # SQLite数据库
│   ├── openclaw.js         # OpenClaw API封装
│   ├── routes/             # API路由
│   │   ├── auth.js        # 认证
│   │   ├── chat.js        # 聊天
│   │   ├── sessions.js    # 会话管理
│   │   └── models.js      # 模型管理
│   └── data/              # 数据库文件（自动创建）
├── frontend/               # 前端应用
│   ├── src/
│   │   ├── views/         # 页面组件
│   │   ├── api/           # API封装
│   │   ├── store/         # Pinia状态管理
│   │   └── router/        # 路由配置
│   └── dist/              # 编译输出（自动创建）
└── README.md
```

## 部署说明

### 1. 后端部署（服务器）

```bash
# 克隆项目
git clone https://github.com/zhanjiqiang/huanggou-chat.git
cd huanggou-chat

# 安装依赖
npm install

# 启动服务
npm start
```

后端会运行在 `http://localhost:3001`

### 2. 前端部署（GitHub Pages）

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 修改 vite.config.js 中的 API 地址
# 将 target 改为你的服务器地址

# 编译
npm run build

# 部署到 GitHub Pages
# 在 GitHub 仓库设置中启用 Pages，选择 /frontend/dist 目录
```

### 3. 环境变量（可选）

创建 `.env` 文件：
```env
PORT=3001
JWT_SECRET=your-secret-key
```

## 本地开发

```bash
# 后端
npm run dev

# 前端（新终端）
cd frontend
npm run dev
```

访问 `http://localhost:5173`

## API文档

### 认证
- `POST /api/auth/register` - 注册
- `POST /api/auth/login` - 登录
- `GET /api/auth/me` - 获取当前用户

### 聊天
- `POST /api/chat/send` - 发送消息
- `POST /api/chat/stream` - 流式发送（SSE）
- `POST /api/chat/upload` - 上传图片

### 会话
- `GET /api/sessions` - 获取会话列表
- `POST /api/sessions` - 创建新会话
- `GET /api/sessions/:id/messages` - 获取会话消息
- `DELETE /api/sessions/:id` - 删除会话

### 模型
- `GET /api/models/available` - 获取可用模型
- `POST /api/models/add` - 添加模型
- `POST /api/models/setDefault` - 设置默认模型

## 注意事项

- GitHub Pages只能托管静态文件，后端API必须部署在服务器上
- 需要配置CORS允许跨域请求
- JWT Token存储在localStorage，有效期30天
- SQLite数据库文件在 `backend/data/huanggou.db`

## License

MIT

---

*Powered by OpenClaw · Made with 🤬 by 黄狗一号*
