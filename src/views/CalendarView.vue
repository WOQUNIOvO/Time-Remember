<template>
  <section class="calendar-grid">
    <div class="page-card calendar-card">
      <img class="calendar-corner corner-left" :src="mascots.chiikawa[2]" alt="" />
      <img class="calendar-corner corner-right" :src="mascots.sanrio[0]" alt="" />
      <div class="toolbar">
        <h2>
          <img class="title-icon" :src="mascots.chiikawa[0]" alt="" />
          {{ currentMonth.format('YYYY 年 MM 月') }}
          <span class="time-widget">
            <img :src="mascots.sanrio[1]" alt="" />
            <span class="time-content">
              <span>{{ nowDateText }}</span>
              <strong>{{ nowTimeText }}</strong>
            </span>
          </span>
        </h2>
        <div class="toolbar-actions">
          <el-button :icon="ArrowLeft" @click="moveMonth(-1)" />
          <el-button @click="goToday">今天</el-button>
          <el-button :icon="ArrowRight" @click="moveMonth(1)" />
          <el-button type="primary" :icon="Plus" @click="openCreate(selectedDate)">新增</el-button>
        </div>
      </div>
      <div class="week-row">
        <span v-for="(day, index) in weekDays" :key="day">
          <img :src="weekIcons[index]" alt="" />
          {{ day }}
        </span>
      </div>
      <div v-loading="loading" class="date-grid">
        <button
          v-for="cell in cells"
          :key="cell.date"
          class="date-cell"
          :class="{ muted: !cell.currentMonth, active: cell.date === selectedDate }"
          @click="selectDate(cell.date)"
          @dblclick="openCreate(cell.date)"
        >
          <img v-if="cell.date === selectedDate" class="selected-day-sticker" :src="mascots.chiikawa[4]" alt="" />
          <span class="date-number">{{ dayjs(cell.date).date() }}</span>
          <span v-for="event in groupedEvents[cell.date]?.slice(0, 3)" :key="event.id" class="event-chip">
            <span class="chip-dot" :style="{ backgroundColor: event.categoryColor }"></span>
            {{ event.title }}
            <span v-if="attachmentMap[event.id]?.length" class="media-count">
              <el-icon><PictureFilled /></el-icon>
              {{ attachmentMap[event.id].length }}
            </span>
          </span>
          <span v-if="(groupedEvents[cell.date]?.length || 0) > 3" class="more-chip">
            +{{ (groupedEvents[cell.date]?.length || 0) - 3 }}
          </span>
        </button>
      </div>
    </div>

    <aside class="page-card side-panel">
      <div class="mini-hero">
        <img :src="mascots.chiikawaBanner" alt="" />
        <div>
          <strong>今天也有好好记录</strong>
          <span>选一天看看小事件</span>
        </div>
      </div>
      <div class="toolbar">
        <h3>{{ selectedDate }} <small>{{ selectedEvents.length }} 件事</small></h3>
        <el-button type="primary" :icon="Plus" @click="openCreate(selectedDate)">新增</el-button>
      </div>
      <div v-if="selectedEvents.length === 0" class="cute-empty">
        <img :src="mascots.chiikawa[4]" alt="" />
        <span>这一天还空空的，放一颗小记忆吧</span>
      </div>
      <div v-else class="event-list">
        <article v-for="event in selectedEvents" :key="event.id" class="event-item">
          <div class="event-head">
            <span class="event-color" :style="{ backgroundColor: event.categoryColor }"></span>
            <strong>{{ event.title }}</strong>
            <el-tag size="small" :type="levelTag(event.level)">{{ levelText(event.level) }}</el-tag>
          </div>
          <p v-if="event.note">{{ event.note }}</p>
          <div v-if="attachmentMap[event.id]?.length" class="media-strip">
            <button
              v-for="attachment in attachmentMap[event.id]"
              :key="attachment.id"
              class="media-thumb"
              type="button"
              @click="previewAttachment(attachment)"
            >
              <img v-if="attachment.type !== 'VIDEO'" :src="attachmentPreviewMap[attachment.id]" :alt="attachment.originalName" />
              <video v-else :src="attachmentPreviewMap[attachment.id]" muted playsinline></video>
              <span v-if="attachment.type === 'VIDEO'" class="media-play">
                <el-icon><VideoCamera /></el-icon>
              </span>
            </button>
          </div>
          <div class="event-actions">
            <el-tag size="small" effect="plain">{{ event.categoryName }}</el-tag>
            <span>
              <el-button :icon="Edit" text @click="openEdit(event)" />
              <el-button :icon="Delete" text type="danger" @click="removeEvent(event)" />
            </span>
          </div>
        </article>
      </div>
    </aside>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑事件' : '新增事件'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="日期" prop="eventDate">
          <el-date-picker v-model="form.eventDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id">
              <span class="option-row">
                <span class="color-dot" :style="{ backgroundColor: category.color }"></span>
                {{ category.name }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="重要程度" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio-button label="LOW">低</el-radio-button>
            <el-radio-button label="MEDIUM">中</el-radio-button>
            <el-radio-button label="HIGH">高</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="form.note" type="textarea" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="图片 / 动图 / 视频">
          <div class="attachment-uploader">
            <el-upload
              drag
              multiple
              accept="image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm,video/quicktime"
              :show-file-list="false"
              :http-request="uploadAttachmentRequest"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">拖入图片、GIF 或视频，或点击选择</div>
              <template #tip>
                <div class="upload-tip">支持 JPG / PNG / GIF / WEBP / MP4 / WEBM / MOV，单个文件不超过 50MB</div>
              </template>
            </el-upload>
            <div v-if="pendingFiles.length || editingAttachments.length" class="attachment-list">
              <article v-for="(file, index) in pendingFiles" :key="`${file.name}-${index}`" class="attachment-pill pending">
                <el-icon><PictureFilled /></el-icon>
                <span>{{ file.name }}</span>
                <el-button :icon="Delete" text type="danger" @click="removePendingFile(index)" />
              </article>
              <article v-for="attachment in editingAttachments" :key="attachment.id" class="attachment-pill">
                <el-icon>
                  <VideoCamera v-if="attachment.type === 'VIDEO'" />
                  <PictureFilled v-else />
                </el-icon>
                <button type="button" @click="previewAttachment(attachment)">{{ attachment.originalName }}</button>
                <el-button :icon="Delete" text type="danger" @click="removeAttachment(attachment)" />
              </article>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEvent">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="previewVisible" title="媒体预览" width="720px" class="media-preview-dialog">
      <div v-if="previewingAttachment" class="preview-stage">
        <img
          v-if="previewingAttachment.type !== 'VIDEO'"
          :src="attachmentPreviewMap[previewingAttachment.id]"
          :alt="previewingAttachment.originalName"
        />
        <video v-else :src="attachmentPreviewMap[previewingAttachment.id]" controls autoplay playsinline></video>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { ArrowLeft, ArrowRight, Delete, Edit, PictureFilled, Plus, UploadFilled, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type TagProps, type UploadRequestOptions } from 'element-plus'
import { categoryApi, eventApi, eventAttachmentApi } from '@/api/modules'
import type { Category, EventAttachment, EventLevel, EventPayload, EventRecord } from '@/types/api'
import { mascots } from '@/constants/mascots'

interface CalendarCell {
  date: string
  currentMonth: boolean
}

const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const weekIcons = [
  mascots.chiikawa[0],
  mascots.sanrio[1],
  mascots.chiikawa[1],
  mascots.sanrio[2],
  mascots.chiikawa[3],
  mascots.sanrio[3],
  mascots.chiikawa[4]
]
const currentMonth = ref(dayjs().startOf('month'))
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const now = ref(dayjs())
const events = ref<EventRecord[]>([])
const categories = ref<Category[]>([])
const attachmentMap = ref<Record<string, EventAttachment[]>>({})
const attachmentPreviewMap = ref<Record<string, string>>({})
const pendingFiles = ref<File[]>([])
const previewVisible = ref(false)
const previewingAttachment = ref<EventAttachment | null>(null)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editing = ref<EventRecord | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<EventPayload>({
  eventDate: selectedDate.value,
  title: '',
  categoryId: '',
  note: '',
  level: 'MEDIUM'
})

const rules: FormRules<EventPayload> = {
  eventDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择重要程度', trigger: 'change' }]
}

const cells = computed<CalendarCell[]>(() => {
  const start = currentMonth.value.startOf('month')
  const firstWeekDay = start.day() === 0 ? 7 : start.day()
  const gridStart = start.subtract(firstWeekDay - 1, 'day')
  return Array.from({ length: 42 }, (_, index) => {
    const date = gridStart.add(index, 'day')
    return { date: date.format('YYYY-MM-DD'), currentMonth: date.month() === currentMonth.value.month() }
  })
})

const groupedEvents = computed<Record<string, EventRecord[]>>(() => {
  return events.value.reduce<Record<string, EventRecord[]>>((map, event) => {
    map[event.eventDate] = map[event.eventDate] || []
    map[event.eventDate].push(event)
    return map
  }, {})
})

const selectedEvents = computed(() => groupedEvents.value[selectedDate.value] || [])
const editingAttachments = computed(() => editing.value ? attachmentMap.value[editing.value.id] || [] : [])
const nowDateText = computed(() => `${now.value.format('YYYY-MM-DD')} ${weekDayText(now.value.day())}`)
const nowTimeText = computed(() => now.value.format('HH:mm:ss'))
let timer: number | undefined

async function loadData(): Promise<void> {
  const startDate = currentMonth.value.startOf('month').subtract(7, 'day').format('YYYY-MM-DD')
  const endDate = currentMonth.value.endOf('month').add(7, 'day').format('YYYY-MM-DD')
  loading.value = true
  try {
    const [categoryList, eventList] = await Promise.all([
      categoryApi.list(),
      eventApi.list({ startDate, endDate })
    ])
    categories.value = categoryList
    events.value = eventList
    await loadAttachments(eventList)
  } finally {
    loading.value = false
  }
}

async function loadAttachments(eventList: EventRecord[]): Promise<void> {
  const entries = await Promise.all(eventList.map(async event => {
    const attachments = await eventAttachmentApi.list(event.id)
    return [event.id, attachments] as const
  }))
  attachmentMap.value = Object.fromEntries(entries)
  selectedEvents.value.flatMap(event => attachmentMap.value[event.id] || []).slice(0, 12).forEach(attachment => {
    void ensureAttachmentPreview(attachment)
  })
}

function selectDate(date: string): void {
  selectedDate.value = date
  const selectedMonth = dayjs(date).startOf('month')
  if (!selectedMonth.isSame(currentMonth.value, 'month')) {
    currentMonth.value = selectedMonth
    void loadData()
  }
}

function moveMonth(offset: number): void {
  currentMonth.value = currentMonth.value.add(offset, 'month')
  void loadData()
}

function goToday(): void {
  const today = dayjs()
  currentMonth.value = today.startOf('month')
  selectedDate.value = today.format('YYYY-MM-DD')
  void loadData()
}

function resetForm(date: string): void {
  form.eventDate = date
  form.title = ''
  form.categoryId = categories.value[0]?.id || ''
  form.note = ''
  form.level = 'MEDIUM'
  pendingFiles.value = []
}

function openCreate(date: string): void {
  editing.value = null
  resetForm(date)
  dialogVisible.value = true
}

function openEdit(event: EventRecord): void {
  editing.value = event
  pendingFiles.value = []
  form.eventDate = event.eventDate
  form.title = event.title
  form.categoryId = event.categoryId
  form.note = event.note || ''
  form.level = event.level
  dialogVisible.value = true
}

async function saveEvent(): Promise<void> {
  await formRef.value?.validate()
  if (!form.categoryId) {
    ElMessage.warning('请先创建分类')
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      const updated = await eventApi.update(editing.value.id, form)
      await uploadPendingFiles(updated.id)
    } else {
      const created = await eventApi.create(form)
      await uploadPendingFiles(created.id)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    currentMonth.value = dayjs(form.eventDate).startOf('month')
    selectedDate.value = form.eventDate
    await loadData()
  } finally {
    saving.value = false
  }
}

async function uploadPendingFiles(eventId: string): Promise<void> {
  if (pendingFiles.value.length === 0) {
    return
  }
  for (const file of pendingFiles.value) {
    await eventAttachmentApi.upload(eventId, file)
  }
  pendingFiles.value = []
}

async function uploadAttachmentRequest(options: UploadRequestOptions): Promise<void> {
  const file = options.file as File
  if (!validateAttachmentFile(file)) {
    options.onError(new Error('invalid file') as never)
    return
  }
  try {
    if (editing.value) {
      const attachment = await eventAttachmentApi.upload(editing.value.id, file)
      attachmentMap.value = {
        ...attachmentMap.value,
        [editing.value.id]: [...(attachmentMap.value[editing.value.id] || []), attachment]
      }
      await ensureAttachmentPreview(attachment)
    } else {
      pendingFiles.value = [...pendingFiles.value, file]
      ElMessage.success('已加入待上传列表，保存事件后自动绑定')
    }
    options.onSuccess({})
  } catch (error) {
    options.onError(error as never)
  }
}

function validateAttachmentFile(file: File): boolean {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'video/mp4', 'video/webm', 'video/quicktime']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WEBP、MP4、WEBM、MOV 文件')
    return false
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('单个文件不能超过 50MB')
    return false
  }
  return true
}

function removePendingFile(index: number): void {
  pendingFiles.value = pendingFiles.value.filter((_, fileIndex) => fileIndex !== index)
}

async function removeAttachment(attachment: EventAttachment): Promise<void> {
  await ElMessageBox.confirm(`确认删除附件「${attachment.originalName}」？`, '删除确认', { type: 'warning' })
  await eventAttachmentApi.remove(attachment.eventId, attachment.id)
  revokePreviewUrl(attachment.id)
  attachmentMap.value = {
    ...attachmentMap.value,
    [attachment.eventId]: (attachmentMap.value[attachment.eventId] || []).filter(item => item.id !== attachment.id)
  }
  ElMessage.success('附件已删除')
}

async function previewAttachment(attachment: EventAttachment): Promise<void> {
  await ensureAttachmentPreview(attachment)
  previewingAttachment.value = attachment
  previewVisible.value = true
}

async function ensureAttachmentPreview(attachment: EventAttachment): Promise<void> {
  if (attachmentPreviewMap.value[attachment.id]) {
    return
  }
  const blob = await eventAttachmentApi.content(attachment.id)
  attachmentPreviewMap.value = {
    ...attachmentPreviewMap.value,
    [attachment.id]: URL.createObjectURL(blob)
  }
}

function revokePreviewUrl(attachmentId: string): void {
  const url = attachmentPreviewMap.value[attachmentId]
  if (url) {
    URL.revokeObjectURL(url)
    const nextMap = { ...attachmentPreviewMap.value }
    delete nextMap[attachmentId]
    attachmentPreviewMap.value = nextMap
  }
}

async function removeEvent(event: EventRecord): Promise<void> {
  await ElMessageBox.confirm(`确认删除事件「${event.title}」？`, '删除确认', { type: 'warning' })
  await eventApi.remove(event.id)
  ElMessage.success('删除成功')
  await loadData()
}

function levelText(level: EventLevel): string {
  return { LOW: '低', MEDIUM: '中', HIGH: '高' }[level]
}

function levelTag(level: EventLevel): TagProps['type'] {
  return { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger' }[level] as TagProps['type']
}

function weekDayText(day: number): string {
  return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][day]
}

onMounted(() => {
  timer = window.setInterval(() => {
    now.value = dayjs()
  }, 1000)
  void loadData()
})

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer)
  }
  Object.values(attachmentPreviewMap.value).forEach(URL.revokeObjectURL)
})

watch(selectedEvents, eventsInDay => {
  eventsInDay.flatMap(event => attachmentMap.value[event.id] || []).slice(0, 12).forEach(attachment => {
    void ensureAttachmentPreview(attachment)
  })
})
</script>

<style scoped>
.calendar-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
}

.calendar-card,
.side-panel {
  padding: 20px;
}

.calendar-card {
  position: relative;
}

.time-widget {
  position: relative;
  z-index: 1;
  display: inline-grid;
  grid-template-columns: 34px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 176px;
  margin-left: 8px;
  padding: 6px 11px 6px 6px;
  border: 1px solid rgb(255 224 239 / 82%);
  border-radius: 999px;
  background: linear-gradient(135deg, rgb(255 255 255 / 88%), rgb(255 246 251 / 78%));
  box-shadow: 0 14px 28px rgb(255 143 199 / 16%);
  backdrop-filter: blur(12px);
}

.time-widget img {
  width: 34px;
  height: 34px;
  padding: 3px;
  border-radius: 999px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 8px 14px rgb(140 200 255 / 16%);
  animation: clock-bob 3.8s ease-in-out infinite;
}

.time-content {
  display: grid;
  gap: 2px;
}

.time-widget span {
  color: #a584a0;
  font-size: 12px;
  font-weight: 900;
}

.time-widget strong {
  color: #ff70b5;
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 18px;
  letter-spacing: 0;
  line-height: 1;
  text-shadow: 0 6px 18px rgb(255 143 199 / 18%);
}

.calendar-corner {
  position: absolute;
  z-index: 1;
  width: 62px;
  height: 62px;
  padding: 4px;
  border-radius: 20px;
  background: rgb(255 255 255 / 76%);
  box-shadow: 0 12px 24px rgb(255 143 199 / 16%);
  object-fit: cover;
  pointer-events: none;
  animation: sticker-float 4.6s ease-in-out infinite;
}

.corner-left {
  left: 22px;
  bottom: 20px;
  transform: rotate(-8deg);
}

.corner-right {
  right: 18px;
  top: 72px;
  transform: rotate(8deg);
  animation-delay: -1.8s;
}

.mini-hero {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 10px;
  border: 1px solid rgb(255 224 239 / 78%);
  border-radius: 20px;
  background: linear-gradient(135deg, rgb(255 247 251 / 88%), rgb(238 247 255 / 78%));
}

.mini-hero img {
  width: 92px;
  height: 70px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 10px 18px rgb(255 143 199 / 16%);
}

.mini-hero div {
  display: grid;
  gap: 3px;
}

.mini-hero strong {
  color: #573d59;
  font-size: 14px;
}

.mini-hero span {
  color: #9c7f9b;
  font-size: 12px;
}

.toolbar h2,
.toolbar h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin: 0;
  color: #573d59;
  font-size: 20px;
}

.title-icon {
  width: 34px;
  height: 34px;
  padding: 3px;
  border-radius: 14px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 8px 16px rgb(255 143 199 / 16%);
}

.toolbar h3 {
  flex-wrap: wrap;
}

.toolbar h3 small {
  color: #aa87a5;
  font-size: 12px;
  font-weight: 700;
}

.week-row,
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.week-row {
  padding: 12px 0;
  color: #a17799;
  text-align: center;
  font-weight: 600;
}

.week-row span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.week-row img {
  width: 24px;
  height: 24px;
  padding: 2px;
  border-radius: 10px;
  background: rgb(255 255 255 / 78%);
  object-fit: cover;
  box-shadow: 0 6px 12px rgb(255 143 199 / 12%);
}

.date-grid {
  gap: 8px;
  border: 0;
}

.date-cell {
  position: relative;
  min-height: 118px;
  padding: 10px;
  border: 1px solid rgb(255 224 239 / 78%);
  border-radius: 18px;
  background: rgb(255 255 255 / 78%);
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 18px rgb(255 143 199 / 8%);
  transition: transform 160ms ease, background 160ms ease, box-shadow 160ms ease;
}

.selected-day-sticker {
  position: absolute;
  right: 8px;
  top: 8px;
  width: 28px;
  height: 28px;
  padding: 2px;
  border-radius: 12px;
  background: #fff;
  object-fit: cover;
  box-shadow: 0 8px 14px rgb(255 143 199 / 16%);
  animation: tiny-pop 2.8s ease-in-out infinite;
}

.date-cell:hover,
.date-cell.active {
  background: linear-gradient(180deg, #fff, #fff0f7);
  box-shadow: 0 14px 26px rgb(255 116 184 / 18%);
  transform: translateY(-3px);
}

.date-cell.muted {
  background: rgb(255 255 255 / 46%);
  color: #b9a2b7;
}

.date-number {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  margin-bottom: 6px;
  border-radius: 999px;
  background: #fff5fb;
  color: #724f70;
  font-weight: 700;
}

.date-cell.active .date-number {
  color: #fff;
  background: linear-gradient(135deg, #ff8fc7, #b9a7ff);
}

.event-chip,
.more-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  width: 100%;
  height: 22px;
  margin-top: 4px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgb(255 255 255 / 78%);
  overflow: hidden;
  color: #374151;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 12px;
}

.media-count {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
  color: #ff70b5;
  font-size: 11px;
  font-weight: 900;
}

.media-count .el-icon {
  font-size: 12px;
}

.chip-dot,
.color-dot,
.event-color {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.event-list {
  display: grid;
  gap: 12px;
}

.cute-empty {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 28px 12px;
  color: #9c7f9b;
  text-align: center;
  font-weight: 700;
}

.cute-empty img {
  width: 118px;
  height: 118px;
  object-fit: contain;
  filter: drop-shadow(0 14px 18px rgb(255 143 199 / 18%));
  animation: empty-bounce 3.8s ease-in-out infinite;
}

.event-item {
  padding: 14px;
  border: 1px solid rgb(255 224 239 / 86%);
  border-radius: 18px;
  background: linear-gradient(180deg, rgb(255 255 255 / 90%), rgb(255 246 251 / 72%));
  box-shadow: 0 10px 24px rgb(255 143 199 / 12%);
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.event-item:hover {
  box-shadow: 0 16px 30px rgb(255 116 184 / 18%);
  transform: translateY(-2px);
}

.event-head,
.event-actions,
.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.event-head strong {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-item p {
  margin: 10px 0;
  color: #735f72;
  line-height: 1.6;
}

.media-strip {
  display: flex;
  gap: 8px;
  margin: 10px 0;
  overflow-x: auto;
  padding-bottom: 2px;
}

.media-thumb {
  position: relative;
  flex: 0 0 auto;
  width: 76px;
  height: 58px;
  overflow: hidden;
  padding: 0;
  border: 1px solid rgb(255 224 239 / 86%);
  border-radius: 16px;
  background: rgb(255 255 255 / 82%);
  box-shadow: 0 10px 18px rgb(255 143 199 / 12%);
}

.media-thumb img,
.media-thumb video {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-play {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: #fff;
  background: rgb(74 61 79 / 28%);
  font-size: 20px;
}

.attachment-uploader {
  display: grid;
  width: 100%;
  gap: 12px;
}

.attachment-uploader :deep(.el-upload) {
  width: 100%;
}

.attachment-uploader :deep(.el-upload-dragger) {
  border-color: rgb(255 197 226 / 86%);
  border-radius: 20px;
  background:
    radial-gradient(circle at 15% 20%, rgb(255 216 234 / 52%), transparent 120px),
    linear-gradient(135deg, rgb(255 255 255 / 84%), rgb(241 248 255 / 72%));
}

.upload-icon {
  color: #ff8fc7;
  font-size: 34px;
}

.upload-text {
  color: #6e526d;
  font-weight: 900;
}

.upload-tip {
  color: #a584a0;
  font-size: 12px;
  line-height: 1.6;
}

.attachment-list {
  display: grid;
  gap: 8px;
}

.attachment-pill {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 6px 8px 6px 12px;
  border: 1px solid rgb(255 224 239 / 78%);
  border-radius: 999px;
  background: rgb(255 255 255 / 72%);
  color: #6e526d;
}

.attachment-pill.pending {
  background: linear-gradient(135deg, rgb(255 246 251 / 88%), rgb(239 248 255 / 72%));
}

.attachment-pill span,
.attachment-pill button:not(.el-button) {
  min-width: 0;
  overflow: hidden;
  border: 0;
  background: transparent;
  color: #6e526d;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 800;
}

.preview-stage {
  display: grid;
  place-items: center;
  min-height: 360px;
}

.preview-stage img,
.preview-stage video {
  max-width: 100%;
  max-height: 68vh;
  border-radius: 22px;
  box-shadow: 0 18px 44px rgb(255 143 199 / 16%);
}

.event-actions {
  justify-content: space-between;
}

@media (max-width: 1080px) {
  .calendar-grid {
    grid-template-columns: 1fr;
  }

  .time-widget {
    margin-left: 0;
  }
}

@media (max-width: 720px) {
  .date-cell {
    min-height: 88px;
  }
}

@keyframes empty-bounce {
  0%,
  100% {
    transform: translateY(0) rotate(-3deg);
  }
  50% {
    transform: translateY(-10px) rotate(3deg);
  }
}

@keyframes sticker-float {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 -8px;
  }
}

@keyframes tiny-pop {
  0%,
  100% {
    transform: scale(1) rotate(-4deg);
  }
  50% {
    transform: scale(1.08) rotate(5deg);
  }
}

@keyframes clock-bob {
  0%,
  100% {
    transform: translateY(0) rotate(-4deg);
  }
  50% {
    transform: translateY(-4px) rotate(4deg);
  }
}
</style>
