<template>
  <div class="cursor-dot" :style="dotStyle"></div>
  <div class="cursor-ring" :class="{ active: pressed }" :style="ringStyle"></div>
  <span
    v-for="sparkle in sparkles"
    :key="sparkle.id"
    class="sparkle"
    :style="{ left: `${sparkle.x}px`, top: `${sparkle.y}px`, '--hue': sparkle.hue }"
  >
    {{ sparkle.symbol }}
  </span>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

interface Sparkle {
  id: number
  x: number
  y: number
  hue: string
  symbol: string
}

const x = ref(-80)
const y = ref(-80)
const trailX = ref(-80)
const trailY = ref(-80)
const pressed = ref(false)
const sparkles = ref<Sparkle[]>([])
let sparkleId = 0
let lastTrailAt = 0
let frame = 0

const dotStyle = computed(() => ({
  transform: `translate3d(${x.value}px, ${y.value}px, 0)`
}))

const ringStyle = computed(() => ({
  transform: `translate3d(${trailX.value}px, ${trailY.value}px, 0)`
}))

function createSparkle(clientX: number, clientY: number, burst = false): void {
  const symbols = ['✦', '♡', '✧', '·']
  const count = burst ? 7 : 1
  for (let i = 0; i < count; i += 1) {
    const angle = Math.random() * Math.PI * 2
    const distance = burst ? 8 + Math.random() * 28 : Math.random() * 8
    const item: Sparkle = {
      id: sparkleId,
      x: clientX + Math.cos(angle) * distance,
      y: clientY + Math.sin(angle) * distance,
      hue: ['#ff8fc7', '#8cc8ff', '#ffd86b', '#b9a7ff'][sparkleId % 4],
      symbol: symbols[sparkleId % symbols.length]
    }
    sparkleId += 1
    sparkles.value.push(item)
    window.setTimeout(() => {
      sparkles.value = sparkles.value.filter(sparkle => sparkle.id !== item.id)
    }, 720)
  }
}

function onPointerMove(event: PointerEvent): void {
  x.value = event.clientX
  y.value = event.clientY
  const now = performance.now()
  if (pressed.value || now - lastTrailAt > 80) {
    createSparkle(event.clientX, event.clientY)
    lastTrailAt = now
  }
}

function onPointerDown(event: PointerEvent): void {
  pressed.value = true
  createSparkle(event.clientX, event.clientY, true)
}

function onPointerUp(): void {
  pressed.value = false
}

function tick(): void {
  trailX.value += (x.value - trailX.value) * 0.18
  trailY.value += (y.value - trailY.value) * 0.18
  frame = window.requestAnimationFrame(tick)
}

onMounted(() => {
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerdown', onPointerDown)
  window.addEventListener('pointerup', onPointerUp)
  frame = window.requestAnimationFrame(tick)
})

onUnmounted(() => {
  window.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerdown', onPointerDown)
  window.removeEventListener('pointerup', onPointerUp)
  window.cancelAnimationFrame(frame)
})
</script>

<style scoped>
.cursor-dot,
.cursor-ring,
.sparkle {
  position: fixed;
  z-index: 9999;
  left: 0;
  top: 0;
  pointer-events: none;
}

.cursor-dot {
  width: 10px;
  height: 10px;
  margin: -5px 0 0 -5px;
  border-radius: 999px;
  background: #ff74b8;
  box-shadow: 0 0 16px rgb(255 116 184 / 65%);
}

.cursor-ring {
  width: 34px;
  height: 34px;
  margin: -17px 0 0 -17px;
  border: 2px solid rgb(255 143 199 / 72%);
  border-radius: 999px;
  box-shadow: inset 0 0 0 6px rgb(255 255 255 / 60%);
  transition: width 120ms ease, height 120ms ease, margin 120ms ease, border-color 120ms ease;
}

.cursor-ring.active {
  width: 46px;
  height: 46px;
  margin: -23px 0 0 -23px;
  border-color: rgb(140 200 255 / 86%);
}

.sparkle {
  color: var(--hue);
  font-size: 16px;
  font-weight: 800;
  text-shadow: 0 2px 8px rgb(255 255 255 / 90%);
  animation: sparkle-pop 720ms ease-out forwards;
}

@keyframes sparkle-pop {
  0% {
    opacity: 0;
    transform: translate3d(-50%, -50%, 0) scale(0.25) rotate(0deg);
  }
  35% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate3d(-50%, -86px, 0) scale(1.25) rotate(22deg);
  }
}

@media (hover: none), (pointer: coarse) {
  .cursor-dot,
  .cursor-ring {
    display: none;
  }
}
</style>
