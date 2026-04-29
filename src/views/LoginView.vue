<template>
  <div class="login-page">
    <div class="login-orbit orbit-one">✦</div>
    <div class="login-orbit orbit-two">♡</div>
    <div class="login-orbit orbit-three">✧</div>
    <section class="login-panel page-card">
      <div class="intro">
        <div class="hero-collage">
          <img class="hero-main" :src="mascots.chanMain" alt="" />
          <img class="hero-badge badge-left" :src="mascots.chiikawa[3]" alt="" />
          <img class="hero-badge badge-right" :src="mascots.sanrio[1]" alt="" />
        </div>
        <h1>Time Remember</h1>
        <p>把每天发生过的事，按日期、分类和重要程度安静地记下来。</p>
      </div>
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" autocomplete="username" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
            </el-form-item>
            <el-button type="primary" class="full-button" :loading="loading" @click="submitLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" autocomplete="username" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" autocomplete="new-password" show-password />
            </el-form-item>
            <el-button type="primary" class="full-button" :loading="loading" @click="submitRegister">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/modules'
import { setSession } from '@/utils/auth'
import type { LoginRequest, RegisterRequest } from '@/types/api'
import { mascots } from '@/constants/mascots'

const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive<LoginRequest>({ username: '', password: '' })
const registerForm = reactive<RegisterRequest>({ username: '', nickname: '', password: '' })

const loginRules: FormRules<LoginRequest> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules: FormRules<RegisterRequest> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3-32 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度为 6-64 位', trigger: 'blur' }
  ]
}

async function submitLogin(): Promise<void> {
  await loginFormRef.value?.validate()
  loading.value = true
  try {
    const session = await authApi.login(loginForm)
    setSession(session)
    void router.replace('/calendar')
  } finally {
    loading.value = false
  }
}

async function submitRegister(): Promise<void> {
  await registerFormRef.value?.validate()
  loading.value = true
  try {
    await authApi.register(registerForm)
    const session = await authApi.login({ username: registerForm.username, password: registerForm.password })
    setSession(session)
    ElMessage.success('注册成功')
    void router.replace('/calendar')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 16%, rgb(255 143 199 / 42%) 0 160px, transparent 161px),
    radial-gradient(circle at 82% 22%, rgb(140 200 255 / 46%) 0 180px, transparent 181px),
    radial-gradient(circle at 72% 84%, rgb(255 216 107 / 42%) 0 170px, transparent 171px),
    linear-gradient(135deg, #fff7fb 0%, #edf7ff 52%, #fff8df 100%);
}

.login-panel {
  z-index: 1;
  width: min(420px, 100%);
  padding: 32px;
  animation: panel-in 520ms cubic-bezier(.2,.8,.2,1);
}

.intro {
  text-align: center;
}

.hero-collage {
  position: relative;
  width: 188px;
  height: 138px;
  margin: 0 auto 12px;
}

.hero-main {
  width: 100%;
  height: 100%;
  border: 5px solid rgb(255 255 255 / 82%);
  border-radius: 36px;
  box-shadow: 0 18px 34px rgb(255 143 199 / 24%);
  object-fit: cover;
  animation: mascot-breathe 3.6s ease-in-out infinite;
}

.hero-badge {
  position: absolute;
  width: 58px;
  height: 58px;
  padding: 4px;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 12px 24px rgb(140 200 255 / 22%);
  object-fit: cover;
  animation: badge-wiggle 4s ease-in-out infinite;
}

.badge-left {
  left: -24px;
  bottom: 4px;
  transform: rotate(-12deg);
}

.badge-right {
  right: -22px;
  top: 6px;
  transform: rotate(11deg);
  animation-delay: -1.2s;
}

.intro h1 {
  margin: 0 0 8px;
  color: #573d59;
  font-size: 32px;
  letter-spacing: 0;
}

.intro p {
  margin: 0 0 22px;
  color: #8c728a;
  line-height: 1.7;
}

.full-button {
  width: 100%;
}

.login-orbit {
  position: absolute;
  color: rgb(255 116 184 / 50%);
  font-size: 52px;
  font-weight: 900;
  animation: login-float 7s ease-in-out infinite;
}

.orbit-one {
  left: 15%;
  top: 20%;
}

.orbit-two {
  right: 16%;
  top: 18%;
  color: rgb(140 200 255 / 62%);
  animation-delay: -1.6s;
}

.orbit-three {
  right: 22%;
  bottom: 18%;
  color: rgb(255 216 107 / 72%);
  animation-delay: -2.8s;
}

@keyframes login-float {
  0%,
  100% {
    transform: translateY(0) rotate(-10deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

@keyframes panel-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes mascot-breathe {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-4px) scale(1.015);
  }
}

@keyframes badge-wiggle {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 -8px;
  }
}
</style>
