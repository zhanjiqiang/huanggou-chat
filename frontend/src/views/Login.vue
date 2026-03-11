<template>
  <div class="maodie-login">
    <!-- 背景装饰 -->
    <div class="bg-paws">
      <div class="paw" v-for="i in 12" :key="i" :style="getPawStyle(i)">🐾</div>
    </div>

    <div class="login-card">
      <!-- 猫头装饰 -->
      <div class="cat-decoration">
        <div class="cat-avatar-wrapper">
          <img src="/images/maodie-avatar.png" alt="耄耋" class="cat-avatar-img" />
          <div class="avatar-glow"></div>
        </div>
      </div>

      <div class="logo">
        <h1>耄耋君</h1>
        <p class="tagline">宇宙第一嘴臭欠打的AI</p>
      </div>

      <!-- 切换按钮 -->
      <div class="tab-switcher">
        <button
          :class="['tab-btn', { active: activeTab === 'login' }]"
          @click="activeTab = 'login'"
        >
          <span>😼</span> 登录
        </button>
        <button
          class="tab-btn disabled"
          disabled
          title="暂不开放注册"
        >
          <span>😺</span> 注册
        </button>
      </div>

      <!-- 登录表单 -->
      <div v-if="activeTab === 'login'" class="form-container">
        <div class="input-group">
          <span class="input-icon">👤</span>
          <input
            v-model="loginForm.username"
            type="text"
            placeholder="用户名"
            @keyup.enter="handleLogin"
          />
        </div>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            @keyup.enter="handleLogin"
          />
        </div>
        <button class="submit-btn" @click="handleLogin" :disabled="loading">
          <span v-if="loading" class="loading-dots">
            <span>.</span><span>.</span><span>.</span>
          </span>
          <span v-else>
            <span class="claw">🐾</span> 进来挨骂
          </span>
        </button>
      </div>

      <!-- 注册表单 -->
      <div v-else class="form-container">
        <div class="input-group">
          <span class="input-icon">👤</span>
          <input
            v-model="registerForm.username"
            type="text"
            placeholder="用户名（3-20字符）"
          />
        </div>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（至少6位）"
          />
        </div>
        <div class="input-group">
          <span class="input-icon">🔑</span>
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            @keyup.enter="handleRegister"
          />
        </div>
        <button class="submit-btn" @click="handleRegister" :disabled="loading">
          <span v-if="loading" class="loading-dots">
            <span>.</span><span>.</span><span>.</span>
          </span>
          <span v-else>
            <span class="claw">🐾</span> 加入被骂
          </span>
        </button>
      </div>

      <div class="footer">
        <p>Powered by OpenClaw · GLM-4.7/5</p>
        <p class="warning">⚠️ 脾气差，但活儿干得漂亮</p>
      </div>
    </div>

    <!-- 抓痕装饰 -->
    <div class="scratches">
      <div class="scratch"></div>
      <div class="scratch"></div>
      <div class="scratch"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

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

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    return
  }

  loading.value = true
  const result = await userStore.login(loginForm.value.username, loginForm.value.password)
  loading.value = false

  if (result.success) {
    router.push('/chat')
  } else {
    alert(result.error || '登录失败')
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    alert('两次密码不一致')
    return
  }

  loading.value = true
  const result = await userStore.register(registerForm.value.username, registerForm.value.password)
  loading.value = false

  if (result.success) {
    router.push('/chat')
  } else {
    alert(result.error || '注册失败')
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

/* ===== 主容器 ===== */
.maodie-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF8DC 0%, #FFE4B5 40%, #FFDAB9 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
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

/* ===== 登录卡片 ===== */
.login-card {
  width: 100%;
  max-width: 400px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 248, 220, 0.95));
  border-radius: 24px;
  padding: 50px 40px 40px;
  box-shadow: 0 20px 60px rgba(139, 69, 19, 0.2);
  position: relative;
  z-index: 10;
  border: 3px solid #F4A460;
}

/* ===== 猫头装饰 ===== */
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

.cat-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #FF6B35;
  box-shadow: 0 8px 30px rgba(255, 107, 53, 0.4);
  position: relative;
  z-index: 2;
  animation: catBounce 2s ease-in-out infinite;
}

@keyframes catBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
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

/* ===== Logo ===== */
.logo {
  text-align: center;
  margin-bottom: 30px;
  margin-top: 20px;
}

.logo h1 {
  font-size: 32px;
  color: white;
  margin: 0 0 8px 0;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(139, 69, 19, 0.5);
}

.tagline {
  font-size: 14px;
  color: white;
  margin: 0;
  font-weight: 600;
  text-shadow: 1px 1px 2px rgba(139, 69, 19, 0.5);
}

/* ===== 标签切换 ===== */
.tab-switcher {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: 3px solid #F4A460;
  background: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #8B4513;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s;
}

.tab-btn:hover {
  border-color: #D2691E;
  background: #FFE4B5;
}

.tab-btn.active {
  background: linear-gradient(135deg, #F4A460, #D2691E);
  color: white;
  border-color: transparent;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.tab-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #F5F5F5;
  color: #999;
}

/* ===== 表单 ===== */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
}

.input-group input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 3px solid #F4A460;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.9);
  color: #8B4513;
  font-weight: 600;
}

.input-group input:focus {
  border-color: #D2691E;
  background: white;
  box-shadow: 0 0 0 3px rgba(244, 164, 96, 0.4);
}

.input-group input::placeholder {
  color: rgba(139, 69, 19, 0.5);
  font-weight: 500;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #FF6B35, #D2691E);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  margin-top: 8px;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.5);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.claw {
  font-size: 18px;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  animation: dotBounce 0.6s infinite ease-in-out;
}

.loading-dots span:nth-child(1) { animation-delay: 0s; }
.loading-dots span:nth-child(2) { animation-delay: 0.1s; }
.loading-dots span:nth-child(3) { animation-delay: 0.2s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
}

/* ===== 底部 ===== */
.footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #FFE8D0;
}

.footer p {
  font-size: 12px;
  color: white;
  margin: 4px 0;
  font-weight: 600;
  text-shadow: 1px 1px 2px rgba(139, 69, 19, 0.5);
}

.warning {
  color: #DC143C !important;
  font-weight: 700 !important;
}
</style>
