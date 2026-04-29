<template>
  <section class="page-card category-page">
    <div class="category-hero">
      <div>
        <span>Color Labels</span>
        <strong>给每种生活小事一个颜色</strong>
      </div>
      <img :src="mascots.sanrio[3]" alt="" />
    </div>
    <div class="toolbar">
      <h2><span>♡</span> 分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>
    <el-table v-loading="loading" :data="categories" row-key="id">
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="颜色" width="140">
        <template #default="{ row }: { row: Category }">
          <span class="color-cell">
            <span class="color-dot" :style="{ backgroundColor: row.color }"></span>
            {{ row.color }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
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
import { onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { categoryApi } from '@/api/modules'
import type { Category, CategoryPayload } from '@/types/api'
import { mascots } from '@/constants/mascots'

const categories = ref<Category[]>([])
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

async function loadCategories(): Promise<void> {
  loading.value = true
  try {
    categories.value = await categoryApi.list()
  } finally {
    loading.value = false
  }
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

.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgb(255 197 226 / 70%), 0 6px 12px rgb(255 143 199 / 18%);
}
</style>
