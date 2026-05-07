<template>
  <el-container class="layout">
    <div class="floating-stickers">
      <span class="sticker sticker-a">♡</span>
      <span class="sticker sticker-b">✦</span>
      <span class="sticker sticker-c">✧</span>
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
      <div class="side-note">
        <img src="/icons/calendar-heart.svg" alt="" />
        <span>{{ todayText }}</span>
        <strong>{{ sideTip }}</strong>
      </div>
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
          <el-button class="theme-toggle" :icon="isDark ? Sunny : Moon" circle text @click="toggleTheme" />
          <img class="header-avatar" :src="mascots.chiikawa[0]" alt="" />
          <span>{{ user?.nickname || user?.username }}</span>
          <el-button :icon="SwitchButton" text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
        <section class="page-tail">
          <div class="tail-copy">
            <img :src="tailIcon" alt="" />
            <div>
              <span>{{ tailEyebrow }}</span>
              <strong>{{ tailTitle }}</strong>
            </div>
          </div>
          <div class="tail-badges">
            <span><img src="/icons/stars.svg" alt="" /> softly saved</span>
            <span><img src="/icons/heart-fill.svg" alt="" /> tiny moments</span>
            <span><img src="/icons/magic.svg" alt="" /> cute data</span>
          </div>
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Moon, Sunny, SwitchButton } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { clearSession, getCurrentUser } from '@/utils/auth'
import { mascots } from '@/constants/mascots'

const route = useRoute()
const router = useRouter()
const user = getCurrentUser()
const isDark = ref(false)

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

const todayText = computed(() => dayjs().format('YYYY.MM.DD'))
const sideTip = computed(() => {
  if (route.path.includes('categories')) return '颜色标签正在整理中'
  if (route.path.includes('statistics')) return '数据糖果已经装盘'
  return '今天也要记一颗糖'
})

const tailEyebrow = computed(() => {
  if (route.path.includes('categories')) return 'Label Garden'
  if (route.path.includes('statistics')) return 'Sweet Analytics'
  return 'Memory Garden'
})

const tailTitle = computed(() => {
  if (route.path.includes('categories')) return '让分类像贴纸一样清楚又可爱'
  if (route.path.includes('statistics')) return '把记录变成一盒彩色数据糖'
  return '空白处也装下一点温柔的今日气泡'
})

const tailIcon = computed(() => {
  if (route.path.includes('categories')) return mascots.sanrio[5]
  if (route.path.includes('statistics')) return mascots.chiikawa[2]
  return mascots.chiikawaScene
})

onMounted(() => {
  isDark.value = document.documentElement.classList.contains('dark')
})

function toggleTheme(): void {
  isDark.value = !isDark.value
  document.documentElement.classList.add('theme-switching')
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('time-remember-theme', isDark.value ? 'dark' : 'light')
  window.setTimeout(() => {
    document.documentElement.classList.remove('theme-switching')
  }, 520)
}

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

.aside {
  display: flex;
  flex-direction: column;
  z-index: 1;
  margin: 16px 0 16px 16px;
  overflow: hidden;
  background: linear-gradient(180deg, rgb(255 255 255 / 86%), rgb(255 246 251 / 76%));
  border: 1px solid rgb(255 224 239 / 90%);
  border-radius: 16px;
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

.side-note {
  display: grid;
  justify-items: center;
  gap: 8px;
  margin: 12px 14px;
  padding: 18px 12px;
  min-height: 180px;
  border: 1px solid rgb(255 224 239 / 72%);
  border-radius: 16px;
  background:
    radial-gradient(circle at 50% 0, rgb(255 216 234 / 56%), transparent 100px),
    linear-gradient(180deg, rgb(255 255 255 / 64%), rgb(245 250 255 / 48%));
  box-shadow: inset 0 0 0 5px rgb(255 255 255 / 30%), 0 12px 26px rgb(255 143 199 / 10%);
  text-align: center;
}

.side-note img {
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 17px;
  background: #fff;
  box-shadow: 0 10px 18px rgb(255 143 199 / 14%);
}

.side-note span {
  color: #ff70b5;
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 12px;
  font-weight: 900;
}

.side-note strong {
  max-width: 120px;
  color: #6e526d;
  font-size: 13px;
  line-height: 1.55;
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
  border-radius: 16px;
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
  border-radius: 16px;
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

.theme-toggle {
  width: 36px;
  height: 36px;
  color: #7a637b;
  background: rgb(255 255 255 / 58%);
  box-shadow: inset 0 0 0 1px rgb(255 224 239 / 70%), 0 8px 16px rgb(255 143 199 / 10%);
}

.main {
  z-index: 1;
  padding: 20px 16px 24px;
}

.page-tail {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-top: 20px;
  padding: 18px 22px;
  min-height: 118px;
  border: 1px solid rgb(255 224 239 / 70%);
  border-radius: 16px;
  background:
    radial-gradient(circle at 12% 30%, rgb(255 216 234 / 46%), transparent 170px),
    radial-gradient(circle at 84% 45%, rgb(188 225 255 / 42%), transparent 180px),
    linear-gradient(135deg, rgb(255 255 255 / 66%), rgb(255 249 231 / 58%));
  box-shadow: 0 18px 44px rgb(255 143 199 / 10%), inset 0 0 0 6px rgb(255 255 255 / 26%);
  backdrop-filter: blur(12px);
}

.tail-copy {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.tail-copy img {
  width: 86px;
  height: 66px;
  border: 4px solid rgb(255 255 255 / 78%);
  border-radius: 24px;
  object-fit: cover;
  box-shadow: 0 14px 28px rgb(255 143 199 / 16%);
}

.tail-copy div {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.tail-copy span {
  color: #ff70b5;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.tail-copy strong {
  color: #573d59;
  font-size: 18px;
  line-height: 1.45;
}

.tail-badges {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.tail-badges span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 12px;
  color: #7d6379;
  border: 1px solid rgb(255 224 239 / 76%);
  border-radius: 999px;
  background: rgb(255 255 255 / 62%);
  font-size: 12px;
  font-weight: 900;
}

.tail-badges img {
  width: 16px;
  height: 16px;
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

@media (max-width: 720px) {
  .aside {
    width: 64px !important;
    margin: 10px 0 10px 10px;
  }

  .brand span,
  .nav-label,
  .nav-footer-stickers,
  .side-note {
    display: none;
  }

  .nav-icon {
    width: 34px;
    height: 34px;
  }
}

@media (max-width: 960px) {
  .page-tail {
    align-items: flex-start;
    flex-direction: column;
  }

  .tail-badges {
    justify-content: flex-start;
  }
}

:global(html.dark) .sticker {
  color: rgb(255 143 199 / 20%);
}

:global(html.dark) .aside,
:global(html.dark) .header,
:global(html.dark) .page-tail {
  border-color: rgb(255 255 255 / 10%);
  background:
    linear-gradient(180deg, rgb(34 36 43 / 68%), rgb(24 26 33 / 56%));
  box-shadow: 0 22px 52px rgb(0 0 0 / 28%), inset 0 0 0 1px rgb(255 255 255 / 6%);
}

:global(html.dark) .brand,
:global(html.dark) .header-title,
:global(html.dark) .tail-copy strong {
  color: #f6eef7;
}

:global(html.dark) .header-title small,
:global(html.dark) .user-box,
:global(html.dark) .side-note strong,
:global(html.dark) .tail-badges span {
  color: rgb(235 226 238 / 68%);
}

:global(html.dark) .menu :deep(.el-menu-item) {
  color: rgb(235 226 238 / 64%);
}

:global(html.dark) .menu :deep(.el-menu-item.is-active) {
  color: #ffd9ec;
  background: linear-gradient(135deg, rgb(255 143 199 / 18%), rgb(140 200 255 / 12%));
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 10%);
}

:global(html.dark) .nav-icon,
:global(html.dark) .header-avatar,
:global(html.dark) .theme-toggle,
:global(html.dark) .tail-badges span,
:global(html.dark) .side-note img,
:global(html.dark) .nav-footer-stickers img {
  background: rgb(255 255 255 / 8%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 8%), 0 10px 22px rgb(0 0 0 / 18%);
}

:global(html.dark) .side-note,
:global(html.dark) .nav-footer-stickers {
  border-color: rgb(255 255 255 / 9%);
  background:
    radial-gradient(circle at 50% 0, rgb(255 143 199 / 13%), transparent 110px),
    linear-gradient(180deg, rgb(255 255 255 / 7%), rgb(255 255 255 / 4%));
}

:global(html.dark) .page-tail {
  background:
    radial-gradient(circle at 12% 30%, rgb(255 143 199 / 13%), transparent 170px),
    radial-gradient(circle at 84% 45%, rgb(140 200 255 / 12%), transparent 180px),
    linear-gradient(135deg, rgb(34 36 43 / 64%), rgb(24 26 33 / 52%));
}
</style>
