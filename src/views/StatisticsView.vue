<template>
  <section class="statistics-page">
    <div class="page-card stats-hero">
      <div>
        <span>Memory Analytics</span>
        <strong>看看最近被哪些小事填满</strong>
      </div>
      <div class="stats-mascots">
        <img :src="mascots.chiikawa[2]" alt="" />
        <img :src="mascots.sanrio[4]" alt="" />
      </div>
    </div>
    <div class="page-card filter-card">
      <el-segmented v-model="period" :options="periodOptions" @change="loadStatistics" />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="loadStatistics"
      />
    </div>

    <div class="summary-row">
      <div class="page-card summary-item">
        <span>事件总数</span>
        <strong>{{ totalCount }}</strong>
      </div>
      <div class="page-card summary-item">
        <span>分类数量</span>
        <strong>{{ categoryCounts.length }}</strong>
      </div>
      <div class="page-card summary-item">
        <span>最高频分类</span>
        <strong>{{ topCategoryName }}</strong>
      </div>
    </div>

    <div class="chart-grid">
      <div class="page-card chart-card">
        <div class="chart-title">分类数量</div>
        <div ref="categoryBarChartRef" class="chart"></div>
      </div>
      <div class="page-card chart-card">
        <div class="chart-title">分类占比</div>
        <div ref="categoryPieChartRef" class="chart"></div>
      </div>
    </div>

    <div class="page-card table-card">
      <el-table :data="categoryCounts" row-key="categoryId">
        <el-table-column label="分类" min-width="160">
          <template #default="{ row }: { row: CategoryCount }">
            <span class="category-cell">
              <span class="color-dot" :style="{ backgroundColor: row.categoryColor }"></span>
              {{ row.categoryName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="120" />
        <el-table-column label="占比" width="120">
          <template #default="{ row }: { row: CategoryCount }">
            {{ ratio(row.count) }}
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
  const colors = categoryCounts.value.map(item => item.categoryColor)

  if (categoryBarChartRef.value) {
    categoryBarChart = categoryBarChart || echarts.init(categoryBarChartRef.value)
    categoryBarChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,.94)',
        borderColor: '#ffe0ef',
        textStyle: { color: '#573d59' }
      },
      grid: { left: 42, right: 20, top: 32, bottom: 52 },
      xAxis: {
        type: 'category',
        data: names,
        axisLabel: { color: '#8c728a', interval: 0, rotate: names.length > 6 ? 28 : 0 },
        axisLine: { lineStyle: { color: '#ffd7e9' } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#8c728a' },
        splitLine: { lineStyle: { color: '#fff0f7' } }
      },
      series: [
        {
          type: 'bar',
          data: counts.map((count, index) => ({
            value: count,
            itemStyle: {
              color: colors[index] || '#ff8fc7',
              borderRadius: [14, 14, 6, 6]
            }
          })),
          barMaxWidth: 42
        }
      ]
    })
  }

  if (categoryPieChartRef.value) {
    categoryPieChart = categoryPieChart || echarts.init(categoryPieChartRef.value)
    categoryPieChart.setOption({
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(255,255,255,.94)',
        borderColor: '#ffe0ef',
        textStyle: { color: '#573d59' }
      },
      legend: {
        bottom: 0,
        textStyle: { color: '#8c728a' }
      },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '44%'],
          padAngle: 3,
          itemStyle: {
            borderColor: '#fff',
            borderRadius: 10,
            borderWidth: 3
          },
          data: categoryCounts.value.map(item => ({
            name: item.categoryName,
            value: item.count,
            itemStyle: {
              color: item.categoryColor,
              borderColor: '#fff',
              borderRadius: 10,
              borderWidth: 3
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.summary-item {
  position: relative;
  display: grid;
  gap: 8px;
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
  color: #6b7280;
}

.summary-item strong {
  color: #573d59;
  font-size: 24px;
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
  margin-bottom: 12px;
  color: #573d59;
  font-size: 18px;
  font-weight: 700;
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

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgb(255 197 226 / 70%), 0 6px 12px rgb(255 143 199 / 18%);
}

@media (max-width: 960px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .summary-row {
    grid-template-columns: 1fr;
  }

  .filter-card {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
