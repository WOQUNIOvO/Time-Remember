<template>
  <section class="page-card category-page">
    <div class="category-hero">
      <div>
        <span>Color Labels</span>
        <strong>给每种生活小事一个颜色</strong>
      </div>
      <img :src="mascots.sanrio[3]" alt="" />
    </div>
    <div class="hot-panel">
      <div class="hot-title">
        <img src="/icons/fire-fill.svg" alt="" />
        <div>
          <strong>分类热点榜</strong>
          <span>按日历中已记录的事件数量排序</span>
        </div>
      </div>
      <div class="hot-list">
        <article v-for="(item, index) in hotCategories" :key="item.id" class="hot-item">
          <span class="hot-rank" :class="{ champion: index === 0 }">{{ index + 1 }}</span>
          <span class="hot-color" :style="{ backgroundColor: item.color }"></span>
          <div class="hot-info">
            <strong>{{ item.name }}</strong>
            <el-progress
              :percentage="hotPercentage(item.count)"
              :color="item.color"
              :show-text="false"
              :stroke-width="10"
            />
          </div>
          <span class="hot-count">{{ item.count }} 次</span>
        </article>
      </div>
    </div>
    <div class="toolbar">
      <h2><span>♡</span> 分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>
    <el-table v-loading="loading" :data="categories" row-key="id">
      <el-table-column label="名称" min-width="180">
        <template #default="{ row }: { row: Category }">
          <span class="name-cell">
            <img src="/icons/tags-fill.svg" alt="" />
            <strong>{{ row.name }}</strong>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="颜色" width="110">
        <template #default="{ row }: { row: Category }">
          <span class="color-cell">
            <span class="color-dot" :style="{ backgroundColor: row.color }"></span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="记录数" width="120">
        <template #default="{ row }: { row: Category }">
          <span class="count-pill">{{ countMap[row.id] || 0 }} 次</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="210">
        <template #default="{ row }: { row: Category }">
          <span class="time-cell">{{ formatTime(row.updatedAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }: { row: Category }">
          <el-button :icon="Edit" text @click="openEdit(row)" />
          <el-button :icon="Delete" text type="danger" @click="remove(row)" />
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { categoryApi, eventApi } from '@/api/modules'
import type { Category, CategoryPayload } from '@/types/api'
import { mascots } from '@/constants/mascots'

const categories = ref<Category[]>([])
const countMap = ref<Record<string, number>>({})
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editing = ref<Category | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CategoryPayload>({ name: '', color: '#409EFF' })

const rules: FormRules<CategoryPayload> = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  color: [{ required: true, message: '请选择颜色', trigger: 'change' }]
}

const hotCategories = computed(() => {
  return categories.value
    .map(category => ({ ...category, count: countMap.value[category.id] || 0 }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 5)
})

const maxCount = computed(() => Math.max(...hotCategories.value.map(item => item.count), 1))

async function loadCategories(): Promise<void> {
  loading.value = true
  try {
    const [categoryList, eventList] = await Promise.all([categoryApi.list(), eventApi.list()])
    categories.value = categoryList
    countMap.value = eventList.reduce<Record<string, number>>((map, event) => {
      map[event.categoryId] = (map[event.categoryId] || 0) + 1
      return map
    }, {})
  } finally {
    loading.value = false
  }
}

function hotPercentage(count: number): number {
  return Math.round((count / maxCount.value) * 100)
}

function formatTime(value: string): string {
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

function openCreate(): void {
  editing.value = null
  form.name = ''
  form.color = '#409EFF'
  dialogVisible.value = true
}

function openEdit(row: Category): void {
  editing.value = row
  form.name = row.name
  form.color = row.color
  dialogVisible.value = true
}

async function save(): Promise<void> {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editing.value) {
      await categoryApi.update(editing.value.id, form)
    } else {
      await categoryApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadCategories()
  } finally {
    saving.value = false
  }
}

async function remove(row: Category): Promise<void> {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '删除确认', { type: 'warning' })
  await categoryApi.remove(row.id)
  ElMessage.success('删除成功')
  await loadCategories()
}

onMounted(loadCategories)
</script>

<style scoped>
.category-page {
  padding: 20px;
}

.category-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border: 1px solid rgb(255 224 239 / 78%);
  border-radius: 22px;
  background:
    radial-gradient(circle at 12% 20%, rgb(255 205 230 / 58%), transparent 120px),
    linear-gradient(135deg, rgb(255 248 252 / 88%), rgb(240 248 255 / 78%));
}

.category-hero div {
  display: grid;
  gap: 4px;
}

.category-hero span {
  color: #ff70b5;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.category-hero strong {
  color: #573d59;
  font-size: 18px;
}

.category-hero img {
  width: 116px;
  height: 82px;
  border: 4px solid rgb(255 255 255 / 82%);
  border-radius: 22px;
  object-fit: cover;
  box-shadow: 0 14px 24px rgb(255 143 199 / 16%);
  transform: rotate(4deg);
  transition: transform 180ms ease;
}

.category-hero:hover img {
  transform: rotate(-3deg) translateY(-3px);
}

.hot-panel {
  display: grid;
  gap: 14px;
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid rgb(255 224 239 / 78%);
  border-radius: 24px;
  background:
    radial-gradient(circle at 12% 10%, rgb(255 216 107 / 38%), transparent 140px),
    radial-gradient(circle at 88% 18%, rgb(255 143 199 / 32%), transparent 150px),
    linear-gradient(135deg, rgb(255 255 255 / 82%), rgb(255 248 252 / 76%));
  box-shadow: inset 0 0 0 5px rgb(255 255 255 / 34%);
}

.hot-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hot-title img {
  width: 42px;
  height: 42px;
  padding: 10px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fff, #fff1f8);
  box-shadow: 0 10px 18px rgb(255 143 199 / 14%);
}

.hot-title div {
  display: grid;
  gap: 3px;
}

.hot-title strong {
  color: #573d59;
  font-size: 18px;
}

.hot-title span {
  color: #a584a0;
  font-size: 12px;
  font-weight: 800;
}

.hot-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.hot-item {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  min-height: 86px;
  padding: 12px;
  border: 1px solid rgb(255 224 239 / 76%);
  border-radius: 20px;
  background: rgb(255 255 255 / 76%);
  box-shadow: 0 10px 20px rgb(255 143 199 / 10%);
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.hot-item:hover {
  box-shadow: 0 16px 28px rgb(255 116 184 / 18%);
  transform: translateY(-3px);
}

.hot-rank {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  color: #a56d97;
  border-radius: 999px;
  background: #fff0f7;
  font-weight: 900;
}

.hot-rank.champion {
  color: #fff;
  background: linear-gradient(135deg, #ff8fc7, #ffd86b);
}

.hot-color {
  width: 18px;
  height: 18px;
  border: 3px solid #fff;
  border-radius: 999px;
  box-shadow: 0 0 0 1px rgb(255 197 226 / 70%);
}

.hot-info {
  display: grid;
  min-width: 0;
  gap: 8px;
}

.hot-info strong {
  overflow: hidden;
  color: #573d59;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-count {
  grid-column: 1 / -1;
  color: #ff70b5;
  font-size: 13px;
  font-weight: 900;
  text-align: right;
}

.category-page h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #573d59;
  font-size: 20px;
}

.category-page h2 span {
  color: #ff8fc7;
}

.color-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.name-cell {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  gap: 9px;
  padding: 6px 12px 6px 8px;
  border: 1px solid rgb(255 224 239 / 76%);
  border-radius: 999px;
  background: linear-gradient(135deg, rgb(255 246 251 / 88%), rgb(239 248 255 / 72%));
  box-shadow: 0 8px 16px rgb(255 143 199 / 10%);
}

.name-cell img {
  width: 24px;
  height: 24px;
  padding: 5px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 6px 12px rgb(255 143 199 / 12%);
}

.name-cell strong {
  min-width: 0;
  overflow: hidden;
  color: #573d59;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.color-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgb(255 197 226 / 70%), 0 6px 12px rgb(255 143 199 / 18%);
}

.count-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 58px;
  height: 30px;
  padding: 0 12px;
  color: #ff70b5;
  border: 1px solid rgb(255 203 229 / 78%);
  border-radius: 999px;
  background: rgb(255 246 251 / 86%);
  font-size: 13px;
  font-weight: 900;
  box-shadow: 0 8px 16px rgb(255 143 199 / 10%);
}

.time-cell {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  color: #7d6379;
  border-radius: 999px;
  background: rgb(255 255 255 / 68%);
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 12px;
  font-weight: 800;
  box-shadow: inset 0 0 0 1px rgb(255 224 239 / 72%);
}

@media (max-width: 1180px) {
  .hot-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .hot-list {
    grid-template-columns: 1fr;
  }

  .category-hero {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
