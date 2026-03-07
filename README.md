# 黄狗一号聊天机器人 🤬

> OpenClaw Web聊天前端 - 支持文字聊天、图片识别、会话管理

## 功能特性

- ✅ **文字聊天** - 与AI实时对话
- ✅ **流式输出** - 实时显示AI回复（SSE）
- ✅ **图片识别** - 上传截图让AI识别
- ✅ **会话管理** - 新建/删除/切换会话
- ✅ **历史记录** - 查看历史会话和消息
- ✅ **模型切换** - 支持GLM-4.7/GLM-5等模型
- ✅ **用户认证** - 登录/注册系统（JWT）
- ✅ **Markdown渲染** - 支持代码高亮和格式化

## 技术栈

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式框架 |
| Vite | 5.2 | 构建工具 |
| Element Plus | 2.6 | UI组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.3 | 路由 |

### 后端（Java）
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.3 | 后端框架 |
| Spring Security | 6.2 | 安全认证 |
| JWT | 0.12.5 | Token认证 |
| SQLite | 3.45 | 轻量级数据库 |
| Maven | 3.9 | 项目构建 |
| Java | 17 | 运行环境 |

### 部署
| 技术 | 说明 |
|------|------|
| Docker | 容器化部署 |
| GitHub Actions | CI/CD自动化 |
| GitHub Pages | 前端托管 |
| GitHub Container Registry | Docker镜像仓库 |

## 项目结构

```
huanggou-chat/
├── backend/                          # Java后端
│   ├── src/main/java/com/huanggou/
│   │   ├── ChatApplication.java     # 主类
│   │   ├── config/                  # 配置类
│   │   ├── controller/              # 控制器
│   │   ├── service/                 # 业务逻辑
│   │   ├── entity/                  # 实体类
│   │   ├── mapper/                  # 数据访问
│   │   ├── dto/                     # 数据传输对象
│   │   └── utils/                   # 工具类
│   ├── src/main/resources/
│   │   └── application.yml          # 配置文件
│   ├── Dockerfile                   # Docker构建文件
│   ├── docker-compose.yml           # Docker编排
│   └── pom.xml                      # Maven配置
├── frontend/                         # Vue前端
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   ├── api/                     # API封装
│   │   ├── store/                   # 状态管理
│   │   └── router/                  # 路由配置
│   └── dist/                        # 编译输出
└── .github/workflows/               # CI/CD配置
    ├── backend-ci.yml               # 后端自动部署
    └── deploy.yml                   # 前端自动部署
```

## CI/CD 自动化部署

### GitHub Actions 支持

✅ **支持 CI/CD 自动化部署**

每次推送到 `main` 分支时，GitHub Actions 会自动执行：

#### 后端（Java）
1. ✅ 编译Java代码（Maven）
2. ✅ 运行单元测试
3. ✅ 构建Docker镜像
4. ✅ 推送到GitHub Container Registry
5. ✅ SSH部署到服务器（需要配置secrets）

#### 前端（Vue）
1. ✅ 安装依赖（npm ci）
2. ✅ 编译前端（npm run build）
3. ✅ 部署到GitHub Pages

### 配置 GitHub Secrets

在仓库设置中添加以下 Secrets：

| Secret | 说明 |
|--------|------|
| `SERVER_HOST` | 服务器IP地址 |
| `SERVER_USER` | SSH用户名（如：root） |
| `SERVER_SSH_KEY` | SSH私钥 |
| `VITE_API_BASE_URL` | 后端API地址 |

## 本地开发

### 后端

```bash
# 安装依赖（需要Maven和JDK 17）
cd backend
mvn clean install

# 运行开发环境
mvn spring-boot:run

# 后端会运行在 http://localhost:3001
```

### 前端

```bash
# 安装依赖
cd frontend
npm install

# 运行开发环境（会自动代理到后端）
npm run dev

# 前端会运行在 http://localhost:5173
```

## 服务器部署

### 方式1：Docker Compose（推荐）

```bash
# 克隆项目
git clone https://github.com/zhanjiqiang/huanggou-chat.git
cd huanggou-chat/backend

# 拉取镜像
docker pull ghcr.io/zhanjiqiang/huanggou-chat:latest

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 方式2：JAR直接运行

```bash
# 编译
cd backend
mvn clean package -DskipTests

# 运行
java -jar target/chat-1.0.0.jar

# 后台运行
nohup java -jar target/chat-1.0.0.jar > app.log 2>&1 &
```

## API文档

### 认证 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/auth/me` | 获取当前用户 |

### 聊天 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat/send` | 发送消息 |
| POST | `/api/chat/stream` | 流式发送（SSE） |
| POST | `/api/chat/upload` | 上传图片 |

### 会话 API
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sessions` | 获取会话列表 |
| POST | `/api/sessions` | 创建新会话 |
| GET | `/api/sessions/:id` | 获取会话详情 |
| DELETE | `/api/sessions/:id` | 删除会话 |

## 环境变量

### 后端（application.yml）
```yaml
server:
  port: 3001

jwt:
  secret: your-secret-key
  expiration: 2592000000 # 30天

openclaw:
  gateway:
    base-url: http://localhost:18789
```

### 前端（.env）
```env
VITE_API_BASE_URL=http://your-server:3001
```

## 注意事项

- ✅ **GitHub Pages不需要域名备案**
- ⚠️ 后端API需要部署在服务器上
- ⚠️ 需要配置CORS允许跨域
- ⚠️ JWT Token存储在localStorage
- ⚠️ SQLite数据库文件在 `backend/data/huanggou.db`

## 性能优化

### 后端
- 使用SQLite轻量级数据库
- JVM参数：`-Xms256m -Xmx512m`
- Docker Alpine镜像（最小化体积）

### 前端
- Vite构建（快速冷启动）
- 路由懒加载
- Element Plus按需引入

## License

MIT

---

*Powered by OpenClaw · Made with 🤬 by 黄狗一号*
