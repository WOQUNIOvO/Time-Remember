<template>
  <el-container class="layout">
    <div class="floating-stickers">
      <span class="sticker sticker-a">♡</span>
      <span class="sticker sticker-b">✦</span>
      <span class="sticker sticker-c">✧</span>
      <img class="float-img float-img-a" :src="mascots.chiikawa[1]" alt="" />
      <img class="float-img float-img-b" :src="mascots.sanrio[1]" alt="" />
    </div>
    <el-aside class="aside" width="220px">
      <div class="brand">
        <span class="brand-mark">TR</span>
        <span>Time Remember</span>
      </div>
      <el-menu router :default-active="route.path" class="menu">
        <el-menu-item index="/calendar">
          <span class="nav-icon">
            <img :src="mascots.chiikawa[0]" alt="" />
          </span>
          <span class="nav-label">日历</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <span class="nav-icon">
            <img :src="mascots.sanrio[1]" alt="" />
          </span>
          <span class="nav-label">分类</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <span class="nav-icon">
            <img :src="mascots.chiikawa[3]" alt="" />
          </span>
          <span class="nav-label">统计</span>
        </el-menu-item>
      </el-menu>
      <div class="nav-footer-stickers">
        <img :src="mascots.sanrio[2]" alt="" />
        <img :src="mascots.chiikawa[4]" alt="" />
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">
          <span>{{ title }}</span>
          <small>{{ subtitle }}</small>
        </div>
        <div class="user-box">
          <img class="header-avatar" :src="mascots.chiikawa[0]" alt="" />
          <span>{{ user?.nickname || user?.username }}</span>
          <el-button :icon="SwitchButton" text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { clearSession, getCurrentUser } from '@/utils/auth'
import { mascots } from '@/constants/mascots'

const route = useRoute()
const router = useRouter()
const user = getCurrentUser()

const title = computed(() => {
  if (route.path.includes('categories')) return '分类管理'
  if (route.path.includes('statistics')) return '统计分析'
  return '事件日历'
})

const subtitle = computed(() => {
  if (route.path.includes('categories')) return '给生活贴上彩色小标签'
  if (route.path.includes('statistics')) return '看看最近的小小轨迹'
  return '把今天温柔地收进日历'
})

function logout(): void {
  clearSession()
  void router.replace('/login')
}
</script>

<style scoped>
.layout {
  position: relative;
  min-height: 100vh;
  background: transparent;
}

.floating-stickers {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.sticker {
  position: absolute;
  color: rgb(255 116 184 / 42%);
  font-size: 38px;
  font-weight: 900;
  animation: floaty 6s ease-in-out infinite;
}

.sticker-a {
  left: 265px;
  top: 92px;
}

.sticker-b {
  right: 52px;
  top: 120px;
  color: rgb(140 200 255 / 52%);
  animation-delay: -1.4s;
}

.sticker-c {
  right: 18%;
  bottom: 58px;
  color: rgb(255 216 107 / 62%);
  animation-delay: -2.2s;
}

.float-img {
  position: absolute;
  width: 86px;
  height: 86px;
  object-fit: contain;
  filter: drop-shadow(0 12px 18px rgb(255 143 199 / 20%));
  animation: image-bob 5.6s ease-in-out infinite;
}

.float-img-a {
  left: 232px;
  bottom: 42px;
}

.float-img-b {
  right: 28px;
  bottom: 128px;
  width: 110px;
  height: 86px;
  border: 4px solid rgb(255 255 255 / 78%);
  border-radius: 26px;
  object-fit: cover;
  transform: rotate(7deg);
  animation-delay: -1.8s;
}

.aside {
  display: flex;
  flex-direction: column;
  z-index: 1;
  margin: 16px 0 16px 16px;
  overflow: hidden;
  background: linear-gradient(180deg, rgb(255 255 255 / 86%), rgb(255 246 251 / 76%));
  border: 1px solid rgb(255 224 239 / 90%);
  border-radius: 26px;
  box-shadow: 0 18px 42px rgb(255 143 199 / 18%);
  backdrop-filter: blur(18px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 78px;
  padding: 0 20px;
  font-size: 18px;
  font-weight: 700;
  color: #573d59;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  color: #fff;
  border-radius: 16px 16px 18px 18px;
  background: linear-gradient(135deg, #ff8fc7, #8cc8ff);
  box-shadow: 0 10px 22px rgb(255 116 184 / 26%);
}

.side-mascot {
  margin: 0 14px 12px;
  padding: 8px;
  border: 1px solid rgb(255 224 239 / 88%);
  border-radius: 22px;
  background: linear-gradient(135deg, rgb(255 246 251 / 90%), rgb(238 247 255 / 90%));
  box-shadow: inset 0 0 0 4px rgb(255 255 255 / 44%);
}

.side-mascot img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 16px;
  object-fit: cover;
  transition: transform 220ms ease;
}

.side-mascot:hover img {
  transform: translateY(-3px) rotate(-2deg) scale(1.03);
}

.menu {
  border-right: 0;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  height: 54px;
  margin: 8px 12px;
  gap: 10px;
  color: #7a637b;
  border-radius: 18px;
}

.menu :deep(.el-menu-item.is-active) {
  color: #ff5da7;
  background: linear-gradient(135deg, rgb(255 230 243 / 95%), rgb(237 246 255 / 95%));
  box-shadow: inset 0 0 0 1px rgb(255 197 226 / 80%);
}

.nav-icon {
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 2px solid rgb(255 255 255 / 88%);
  border-radius: 13px;
  background: rgb(255 255 255 / 78%);
  box-shadow: 0 8px 14px rgb(255 143 199 / 14%);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.nav-icon img {
  width: 26px;
  height: 26px;
  object-fit: cover;
  border-radius: 10px;
}

.menu :deep(.el-menu-item:hover) .nav-icon,
.menu :deep(.el-menu-item.is-active) .nav-icon {
  box-shadow: 0 10px 18px rgb(255 116 184 / 22%);
  transform: translateY(-2px) rotate(-4deg) scale(1.05);
}

.nav-footer-stickers {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: auto 12px 18px;
  padding: 10px;
  border: 1px solid rgb(255 224 239 / 70%);
  border-radius: 20px;
  background: linear-gradient(135deg, rgb(255 246 251 / 72%), rgb(238 247 255 / 66%));
}

.nav-footer-stickers img {
  width: 42px;
  height: 42px;
  padding: 3px;
  border-radius: 16px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 10px 16px rgb(255 143 199 / 14%);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 1;
  height: 76px;
  margin: 16px 16px 0;
  background: rgb(255 255 255 / 70%);
  border: 1px solid rgb(255 224 239 / 82%);
  border-radius: 26px;
  box-shadow: 0 12px 36px rgb(140 200 255 / 14%);
  backdrop-filter: blur(16px);
}

.header-title {
  display: grid;
  gap: 2px;
  font-size: 18px;
  font-weight: 700;
}

.header-title small {
  color: #9c7f9b;
  font-size: 12px;
  font-weight: 600;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #7a637b;
}

.header-avatar {
  width: 34px;
  height: 34px;
  padding: 3px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 8px 16px rgb(255 143 199 / 18%);
  object-fit: cover;
}

.main {
  z-index: 1;
  padding: 20px 16px 24px;
}

@keyframes floaty {
  0%,
  100% {
    transform: translate3d(0, 0, 0) rotate(-8deg);
  }
  50% {
    transform: translate3d(0, -16px, 0) rotate(8deg);
  }
}

@keyframes image-bob {
  0%,
  100% {
    transform: translate3d(0, 0, 0) rotate(-5deg);
  }
  50% {
    transform: translate3d(0, -14px, 0) rotate(5deg);
  }
}

@media (max-width: 720px) {
  .aside {
    width: 64px !important;
    margin: 10px 0 10px 10px;
  }

  .brand span,
  .nav-label,
  .nav-footer-stickers {
    display: none;
  }

  .nav-icon {
    width: 34px;
    height: 34px;
  }
}
</style>
