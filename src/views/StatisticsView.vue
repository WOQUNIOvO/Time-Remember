<template>
  <section class="statistics-page">
    <div class="page-card stats-hero">
      <div>
        <span>Memory Analytics</span>
        <strong>看看最近被哪些小事填满</strong>
        <small>每一条记录都会变成一颗软乎乎的数据糖</small>
      </div>
      <div class="stats-mascots">
        <img :src="mascots.chiikawa[2]" alt="" />
        <img :src="mascots.sanrio[4]" alt="" />
      </div>
    </div>
    <div class="page-card filter-card">
      <div class="filter-title">
        <img :src="mascots.chiikawa[0]" alt="" />
        <div>
          <strong>筛选小雷达</strong>
          <span>选择时间，看看不同阶段的记录</span>
        </div>
      </div>
      <div class="filter-controls">
        <div class="filter-group period-group">
          <span class="filter-label">统计周期</span>
          <el-segmented v-model="period" :options="periodOptions" @change="loadStatistics" />
        </div>
        <div class="filter-group date-group">
          <span class="filter-label">时间范围</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadStatistics"
          />
        </div>
      </div>
      <img class="filter-sticker" :src="mascots.sanrio[2]" alt="" />
    </div>

    <div class="summary-row">
      <div class="page-card summary-item">
        <span>事件总数</span>
        <strong>{{ totalCount }}</strong>
        <small>颗记忆糖</small>
      </div>
      <div class="page-card summary-item">
        <span>分类数量</span>
        <strong>{{ categoryCounts.length }}</strong>
        <small>个小标签</small>
      </div>
      <div class="page-card summary-item">
        <span>最高频分类</span>
        <strong class="top-name">{{ topCategoryName }}</strong>
        <small>最常出现</small>
      </div>
    </div>

    <div class="chart-grid">
      <div class="page-card chart-card candy-card">
        <div class="chart-title">
          <span>
            <img :src="mascots.chiikawa[1]" alt="" />
            分类数量
          </span>
          <small>每个小标签出现了几次</small>
        </div>
        <div class="chart-shell">
          <div ref="categoryBarChartRef" class="chart"></div>
        </div>
      </div>
      <div class="page-card chart-card candy-card">
        <div class="chart-title">
          <span>
            <img :src="mascots.sanrio[1]" alt="" />
            分类占比
          </span>
          <small>生活被哪些事情填满</small>
        </div>
        <div class="chart-shell pie-shell">
          <div class="pie-center-note">
            <strong>{{ totalCount }}</strong>
            <span>total</span>
          </div>
          <div ref="categoryPieChartRef" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="page-card table-card">
      <el-table :data="categoryCounts" row-key="categoryId">
        <el-table-column label="分类" min-width="160">
          <template #default="{ row }: { row: CategoryCount }">
            <span class="category-cell stat-name-cell">
              <span class="color-dot" :style="{ backgroundColor: row.categoryColor }"></span>
              <strong>{{ row.categoryName }}</strong>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="{ row }: { row: CategoryCount }">
            <span class="stat-count-pill">{{ row.count }} 次</span>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="120">
          <template #default="{ row }: { row: CategoryCount }">
            <span class="ratio-pill">{{ ratio(row.count) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/modules'
import type { CategoryCount, StatisticsPeriod } from '@/types/api'
import { mascots } from '@/constants/mascots'

const period = ref<StatisticsPeriod>('MONTH')
const dateRange = ref<[string, string] | null>(null)
const categoryCounts = ref<CategoryCount[]>([])
const categoryBarChartRef = ref<HTMLDivElement>()
const categoryPieChartRef = ref<HTMLDivElement>()
let categoryBarChart: echarts.ECharts | null = null
let categoryPieChart: echarts.ECharts | null = null

const periodOptions = [
  { label: '周', value: 'WEEK' },
  { label: '月', value: 'MONTH' },
  { label: '年', value: 'YEAR' }
]

const totalCount = computed(() => categoryCounts.value.reduce((sum, item) => sum + item.count, 0))
const topCategoryName = computed(() => categoryCounts.value[0]?.categoryName || '-')
const softPalette = ['#ff8fc7', '#8cc8ff', '#ffd86b', '#8ee6c3', '#b9a7ff', '#ffb18a', '#f7a8ff']

async function loadStatistics(): Promise<void> {
  const params = {
    period: period.value,
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1]
  }
  categoryCounts.value = await statisticsApi.categories(params)
  await nextTick()
  renderCharts()
}

function renderCharts(): void {
  const names = categoryCounts.value.map(item => item.categoryName)
  const counts = categoryCounts.value.map(item => item.count)
  const colors = categoryCounts.value.map((item, index) => softPalette[index % softPalette.length] || item.categoryColor)

  if (categoryBarChartRef.value) {
    categoryBarChart = categoryBarChart || echarts.init(categoryBarChartRef.value)
    categoryBarChart.setOption({
      animationDuration: 900,
      animationEasing: 'elasticOut',
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          shadowStyle: { color: 'rgba(255, 143, 199, .09)' }
        },
        backgroundColor: 'rgba(255,255,255,.96)',
        borderColor: '#ffd7e9',
        borderWidth: 1,
        padding: [10, 12],
        textStyle: { color: '#573d59', fontWeight: 700 }
      },
      grid: { left: 44, right: 24, top: 32, bottom: 48 },
      xAxis: {
        type: 'category',
        data: names,
        axisLabel: { color: '#9a7596', fontWeight: 700, interval: 0, rotate: names.length > 6 ? 28 : 0 },
        axisLine: { lineStyle: { color: '#ffe3f0', width: 2 } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#b18dac', fontWeight: 700 },
        splitLine: { lineStyle: { color: '#fff0f7', type: 'dashed' } }
      },
      series: [
        {
          type: 'pictorialBar',
          symbol: 'circle',
          symbolSize: 18,
          symbolPosition: 'end',
          z: 3,
          data: counts.map((count, index) => ({
            value: count,
            itemStyle: {
              color: colors[index],
              shadowBlur: 14,
              shadowColor: `${colors[index]}66`
            }
          }))
        },
        {
          type: 'bar',
          data: counts.map((count, index) => ({
            value: count,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: colors[index] },
                { offset: 1, color: '#fff1f8' }
              ]),
              borderRadius: [999, 999, 18, 18],
              shadowBlur: 16,
              shadowColor: `${colors[index]}44`
            }
          })),
          barMaxWidth: 34,
          emphasis: {
            itemStyle: {
              shadowBlur: 22,
              shadowColor: 'rgba(255, 116, 184, .34)'
            }
          }
        }
      ]
    })
  }

  if (categoryPieChartRef.value) {
    categoryPieChart = categoryPieChart || echarts.init(categoryPieChartRef.value)
    categoryPieChart.setOption({
      animationDuration: 900,
      animationEasing: 'cubicOut',
      color: colors,
      tooltip: {
        trigger: 'item',
        formatter: '{b}<br />{c} 件 · {d}%',
        backgroundColor: 'rgba(255,255,255,.96)',
        borderColor: '#ffd7e9',
        padding: [10, 12],
        textStyle: { color: '#573d59', fontWeight: 700 }
      },
      legend: {
        bottom: 0,
        icon: 'circle',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: { color: '#9a7596', fontWeight: 700 }
      },
      series: [
        {
          type: 'pie',
          radius: ['72%', '73%'],
          center: ['50%', '43%'],
          silent: true,
          label: { show: false },
          data: [{ value: 1, itemStyle: { color: 'rgba(255, 214, 234, .78)' } }]
        },
        {
          type: 'pie',
          radius: ['46%', '66%'],
          center: ['50%', '43%'],
          roseType: 'radius',
          minAngle: 8,
          padAngle: 4,
          label: {
            color: '#7d5c79',
            fontWeight: 800,
            formatter: '{b}\n{d}%'
          },
          labelLine: {
            length: 10,
            length2: 8,
            lineStyle: { color: '#ffc8e0' }
          },
          itemStyle: {
            borderColor: '#fff',
            borderRadius: 14,
            borderWidth: 4,
            shadowBlur: 14,
            shadowColor: 'rgba(255, 143, 199, .18)'
          },
          data: categoryCounts.value.map((item, index) => ({
            name: item.categoryName,
            value: item.count,
            itemStyle: {
              color: colors[index],
              borderColor: '#fff',
              borderRadius: 14,
              borderWidth: 4
            }
          }))
        }
      ]
    })
  }
}

function resizeCharts(): void {
  categoryBarChart?.resize()
  categoryPieChart?.resize()
}

function ratio(count: number): string {
  if (totalCount.value === 0) {
    return '0%'
  }
  return `${((count / totalCount.value) * 100).toFixed(1)}%`
}

onMounted(async () => {
  await loadStatistics()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  categoryBarChart?.dispose()
  categoryPieChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
  display: grid;
  gap: 20px;
}

.stats-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
}

.stats-hero div:first-child {
  display: grid;
  gap: 5px;
}

.stats-hero span {
  color: #ff70b5;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.stats-hero strong {
  color: #573d59;
  font-size: 20px;
  line-height: 1.25;
}

.stats-hero small {
  color: #a584a0;
  font-size: 13px;
  font-weight: 800;
}

.stats-mascots {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-mascots img {
  width: 70px;
  height: 70px;
  padding: 4px;
  border-radius: 24px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 12px 22px rgb(255 143 199 / 16%);
  transition: transform 180ms ease;
}

.stats-mascots img:first-child {
  object-fit: contain;
}

.stats-mascots img:hover {
  transform: translateY(-5px) rotate(4deg);
}

.filter-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(220px, .8fr) minmax(420px, 1.4fr);
  align-items: center;
  gap: 18px;
  padding: 18px 88px 18px 18px;
  overflow: visible;
  background:
    radial-gradient(circle at 18% 18%, rgb(255 198 229 / 54%), transparent 140px),
    radial-gradient(circle at 75% 30%, rgb(188 225 255 / 42%), transparent 160px),
    linear-gradient(135deg, rgb(255 255 255 / 90%), rgb(255 248 252 / 82%));
}

.filter-title {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.filter-title img {
  width: 56px;
  height: 56px;
  padding: 4px;
  border: 3px solid rgb(255 255 255 / 90%);
  border-radius: 22px;
  background: linear-gradient(135deg, #fff, #fff0f7);
  object-fit: cover;
  box-shadow: 0 12px 22px rgb(255 143 199 / 18%);
  animation: filter-bounce 4.2s ease-in-out infinite;
}

.filter-title div {
  display: grid;
  gap: 3px;
}

.filter-title strong {
  color: #573d59;
  font-size: 18px;
}

.filter-title span,
.filter-label {
  color: #a584a0;
  font-size: 12px;
  font-weight: 800;
}

.filter-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  min-width: 0;
}

.filter-group {
  display: grid;
  gap: 7px;
}

.period-group {
  min-width: 230px;
}

.date-group {
  min-width: 330px;
}

.filter-label {
  padding-left: 8px;
}

.filter-card :deep(.el-segmented) {
  --el-segmented-item-selected-bg-color: transparent;
  --el-segmented-item-selected-color: #fff;
  padding: 5px;
  border: 1px solid rgb(255 218 236 / 86%);
  border-radius: 999px;
  background: rgb(255 255 255 / 68%);
  box-shadow: inset 0 0 0 4px rgb(255 255 255 / 32%), 0 10px 18px rgb(255 143 199 / 10%);
}

.filter-card :deep(.el-segmented__item) {
  min-width: 62px;
  border-radius: 999px;
  color: #8f6f8a;
  font-weight: 900;
}

.filter-card :deep(.el-segmented__item-selected) {
  background: linear-gradient(135deg, #ff8fc7, #b9a7ff);
  box-shadow: 0 8px 16px rgb(255 116 184 / 22%);
}

.filter-card :deep(.el-date-editor) {
  width: 100%;
  height: 42px;
  border-radius: 999px;
  background: rgb(255 255 255 / 76%);
  box-shadow: 0 10px 18px rgb(140 200 255 / 10%);
}

.filter-card :deep(.el-range-input) {
  color: #6f526d;
  font-weight: 800;
}

.filter-card :deep(.el-range-separator) {
  color: #ff8fc7;
  font-weight: 900;
}

.filter-sticker {
  position: absolute;
  right: 18px;
  top: 50%;
  width: 58px;
  height: 58px;
  padding: 4px;
  border-radius: 20px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 14px 24px rgb(255 143 199 / 20%);
  transform: translateY(-50%) rotate(8deg);
  transition: transform 180ms ease;
}

.filter-card:hover .filter-sticker {
  transform: translateY(-55%) rotate(-5deg) scale(1.05);
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.summary-item {
  position: relative;
  display: grid;
  gap: 6px;
  padding: 18px 20px;
}

.summary-item::after {
  position: absolute;
  right: 18px;
  top: 16px;
  color: rgb(255 143 199 / 48%);
  content: "✦";
  font-size: 24px;
}

.summary-item span {
  display: inline-flex;
  width: fit-content;
  padding: 4px 10px;
  color: #ff70b5;
  border-radius: 999px;
  background: rgb(255 246 251 / 84%);
  font-size: 12px;
  font-weight: 900;
}

.summary-item strong {
  color: #573d59;
  font-size: 30px;
  line-height: 1.1;
  letter-spacing: 0;
}

.summary-item .top-name {
  max-width: 100%;
  overflow: hidden;
  font-size: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-item small {
  color: #a584a0;
  font-size: 12px;
  font-weight: 800;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.chart-card,
.table-card {
  padding: 20px;
}

.chart-title {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: #573d59;
  font-weight: 700;
}

.chart-title span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #573d59;
  letter-spacing: 0;
}

.chart-title img {
  width: 34px;
  height: 34px;
  padding: 3px;
  border-radius: 14px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 8px 16px rgb(255 143 199 / 16%);
}

.chart-title small {
  color: #aa87a5;
  font-size: 12px;
  font-weight: 800;
}

.candy-card {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 20%, rgb(255 198 229 / 48%), transparent 150px),
    radial-gradient(circle at 82% 16%, rgb(188 225 255 / 42%), transparent 160px),
    linear-gradient(180deg, rgb(255 255 255 / 92%), rgb(255 248 252 / 82%));
}

.candy-card::after {
  position: absolute;
  right: 18px;
  bottom: 14px;
  z-index: 0;
  color: rgb(255 143 199 / 22%);
  content: "♡";
  font-size: 86px;
  font-weight: 900;
  line-height: 1;
  transform: rotate(-12deg);
}

.chart-shell {
  position: relative;
  z-index: 1;
  border: 1px solid rgb(255 224 239 / 76%);
  border-radius: 24px;
  background:
    linear-gradient(90deg, rgb(255 255 255 / 52%) 1px, transparent 1px),
    linear-gradient(rgb(255 255 255 / 52%) 1px, transparent 1px),
    linear-gradient(135deg, rgb(255 253 255 / 76%), rgb(246 251 255 / 68%));
  background-size: 22px 22px, 22px 22px, auto;
  box-shadow: inset 0 0 0 5px rgb(255 255 255 / 42%);
}

.pie-shell {
  overflow: hidden;
}

.pie-center-note {
  position: absolute;
  left: 50%;
  top: 45%;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 94px;
  height: 94px;
  border: 1px solid rgb(255 224 239 / 86%);
  border-radius: 999px;
  background: rgb(255 255 255 / 86%);
  box-shadow: 0 14px 30px rgb(255 143 199 / 16%);
  transform: translate(-50%, -50%);
}

.pie-center-note strong {
  color: #ff70b5;
  font-size: 26px;
  line-height: 1;
}

.pie-center-note span {
  color: #aa87a5;
  font-size: 11px;
  font-weight: 900;
  text-transform: uppercase;
}

.chart {
  width: 100%;
  height: 340px;
}

.category-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.stat-name-cell {
  max-width: 100%;
  padding: 6px 12px 6px 8px;
  border: 1px solid rgb(255 224 239 / 76%);
  border-radius: 999px;
  background: linear-gradient(135deg, rgb(255 246 251 / 88%), rgb(239 248 255 / 72%));
  box-shadow: 0 8px 16px rgb(255 143 199 / 10%);
}

.stat-name-cell strong {
  min-width: 0;
  overflow: hidden;
  color: #573d59;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgb(255 197 226 / 70%), 0 6px 12px rgb(255 143 199 / 18%);
}

.stat-count-pill,
.ratio-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 58px;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
}

.stat-count-pill {
  color: #ff70b5;
  border: 1px solid rgb(255 203 229 / 78%);
  background: rgb(255 246 251 / 86%);
  box-shadow: 0 8px 16px rgb(255 143 199 / 10%);
}

.ratio-pill {
  color: #6e78c8;
  border: 1px solid rgb(208 215 255 / 82%);
  background: rgb(242 245 255 / 86%);
  box-shadow: 0 8px 16px rgb(140 200 255 / 10%);
}

@media (max-width: 960px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .summary-row {
    grid-template-columns: 1fr;
  }

  .filter-card {
    grid-template-columns: 1fr;
    padding-right: 18px;
  }

  .filter-controls {
    align-items: stretch;
    flex-direction: column;
  }

  .period-group,
  .date-group {
    min-width: 0;
  }

  .filter-sticker {
    display: none;
  }
}

@keyframes filter-bounce {
  0%,
  100% {
    transform: translateY(0) rotate(-4deg);
  }
  50% {
    transform: translateY(-6px) rotate(4deg);
  }
}
</style>
