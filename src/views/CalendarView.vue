<template>
  <section class="calendar-page">
    <div class="calendar-intro">
      <img class="calendar-corner corner-left" :src="mascots.chiikawa[2]" alt="" />
      <img class="calendar-corner corner-right" :src="mascots.sanrio[0]" alt="" />
      <section class="today-status-card">
        <div class="status-date">
          <span class="status-eyebrow">Today</span>
          <strong>{{ todayDateText }}</strong>
          <small>{{ todayWeekText }}</small>
          <p>{{ atmosphereText }}</p>
        </div>
        <div class="status-events">
          <div class="status-section-title">
            <span>今日记录</span>
            <strong>{{ todayEvents.length }}</strong>
          </div>
          <div v-if="todayEvents.length" class="today-tags">
            <el-tag
              v-for="event in todayEvents.slice(0, 8)"
              :key="event.id"
              effect="plain"
              round
              :style="{ '--tag-color': event.categoryColor }"
              class="today-tag"
            >
              <span class="event-emoji">{{ eventEmoji(event) }}</span>
              {{ event.title }}
            </el-tag>
          </div>
          <div v-else class="today-empty-line">今天还很安静，适合放进第一条小记录。</div>
        </div>
        <div class="status-metrics">
          <div class="day-progress">
            <div class="progress-head">
              <span>今天已经过去</span>
              <strong>{{ dayProgressText }}%</strong>
            </div>
            <el-progress :percentage="dayProgressPercentage" :show-text="false" />
            <div class="salary-meter">
              <div class="salary-copy">
                <span>今日打工收入</span>
                <strong>¥{{ earnedTodayText }}</strong>
              </div>
              <div class="money-walk" :style="{ '--salary-progress': `${workProgress * 100}%` }">
                <div class="money-road">
                  <span class="road-dot"></span>
                  <span class="road-dot"></span>
                  <span class="road-dot"></span>
                </div>
                <div class="money-worker">
                  <span class="worker-face">ᵔ ᵕ ᵔ</span>
                  <span class="money-cart">💴</span>
                </div>
              </div>
              <el-popover placement="bottom-end" width="260" trigger="click">
                <template #reference>
                  <el-button text class="salary-config">配置</el-button>
                </template>
                <div class="salary-config-panel">
                  <span>工资配置</span>
                  <el-form label-position="top">
                    <el-form-item label="日薪">
                      <el-input-number v-model="salaryConfig.dailyWage" :min="0" :step="10" controls-position="right" />
                    </el-form-item>
                    <el-form-item label="上班时间">
                      <el-time-picker v-model="salaryConfig.startTime" format="HH:mm" value-format="HH:mm" />
                    </el-form-item>
                    <el-form-item label="下班时间">
                      <el-time-picker v-model="salaryConfig.endTime" format="HH:mm" value-format="HH:mm" />
                    </el-form-item>
                  </el-form>
                </div>
              </el-popover>
            </div>
          </div>
          <div class="streak-card">
            <span>连续记录</span>
            <strong>{{ streakDays }}</strong>
            <small>天</small>
          </div>
        </div>
      </section>
      <section class="quick-record" :class="{ sending: quickSaving, done: quickJustSaved }">
        <div class="quick-copy">
          <span>Quick Note</span>
          <strong>像发一条动态一样记录今天</strong>
        </div>
        <div class="quick-input-wrap">
          <span class="quick-spark">{{ quickPreviewEmoji }}</span>
          <el-input
            v-model="quickText"
            class="quick-input"
            clearable
            :disabled="quickSaving"
            placeholder="今天做饭了 / 今天心情很差 / 今天来例假了"
            @keyup.enter="submitQuickRecord"
          />
          <el-button type="primary" :loading="quickSaving" @click="submitQuickRecord">记录</el-button>
        </div>
      </section>
    </div>

    <section class="page-card timeline-summary">
      <div class="timeline-head">
        <div>
          <span>Timeline</span>
          <strong>{{ selectedDate }}</strong>
          <small>{{ selectedEvents.length }} 件事被放进这一天</small>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate(selectedDate)">新增</el-button>
      </div>
      <div v-if="selectedEvents.length === 0" class="timeline-empty">
        <img :src="mascots.chiikawa[4]" alt="" />
        <span>这一天还很轻，双击日历格或用快速记录放进第一件小事。</span>
      </div>
      <div v-else class="event-list">
        <article v-for="event in selectedEvents" :key="event.id" class="event-pill-wrap">
          <button
            class="event-pill-card"
            type="button"
            :style="{ '--event-color': event.categoryColor }"
            @click="openEdit(event)"
          >
            <span class="event-pill-icon">{{ eventEmoji(event) }}</span>
            <span class="event-pill-main">
              <strong>{{ event.title }}</strong>
              <small>{{ event.categoryName }} · {{ levelText(event.level) }}</small>
            </span>
            <span v-if="attachmentMap[event.id]?.length" class="event-pill-media">
              <el-icon><PictureFilled /></el-icon>
              {{ attachmentMap[event.id].length }}
            </span>
            <el-button :icon="Delete" circle text type="danger" @click.stop="removeEvent(event)" />
          </button>
          <p v-if="event.note" class="event-note">{{ event.note }}</p>
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
        </article>
      </div>
    </section>

    <div class="page-card calendar-card calendar-board">
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
          <span
            v-for="event in groupedEvents[cell.date]?.slice(0, 3)"
            :key="event.id"
            class="event-chip"
            role="button"
            tabindex="0"
            :style="{ '--event-color': event.categoryColor }"
            @click.stop="openEdit(event)"
          >
            <span class="event-emoji">{{ eventEmoji(event) }}</span>
            <span class="event-chip-title">{{ event.title }}</span>
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
        <article v-for="event in selectedEvents" :key="event.id" class="event-pill-wrap">
          <button
            class="event-pill-card"
            type="button"
            :style="{ '--event-color': event.categoryColor }"
            @click="openEdit(event)"
          >
            <span class="event-pill-icon">{{ eventEmoji(event) }}</span>
            <span class="event-pill-main">
              <strong>{{ event.title }}</strong>
              <small>{{ event.categoryName }} · {{ levelText(event.level) }}</small>
            </span>
            <span v-if="attachmentMap[event.id]?.length" class="event-pill-media">
              <el-icon><PictureFilled /></el-icon>
              {{ attachmentMap[event.id].length }}
            </span>
            <el-button :icon="Delete" circle text type="danger" @click.stop="removeEvent(event)" />
          </button>
          <p v-if="event.note" class="event-note">{{ event.note }}</p>
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
        </article>
      </div>
    </aside>

    <section class="stats-overview">
      <article class="page-card stat-tile">
        <span>本月记录</span>
        <strong>{{ monthEvents.length }}</strong>
        <small>条生活片段</small>
      </article>
      <article class="page-card stat-tile">
        <span>活跃日期</span>
        <strong>{{ activeDays }}</strong>
        <small>天有记录</small>
      </article>
      <article class="page-card stat-tile">
        <span>常用分类</span>
        <strong>{{ monthTopCategory }}</strong>
        <small>最近最常出现</small>
      </article>
    </section>

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
import { ArrowLeft, ArrowRight, Delete, PictureFilled, Plus, UploadFilled, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { categoryApi, eventApi, eventAttachmentApi } from '@/api/modules'
import type { Category, EventAttachment, EventLevel, EventPayload, EventRecord } from '@/types/api'
import { mascots } from '@/constants/mascots'

interface CalendarCell {
  date: string
  currentMonth: boolean
}

interface SalaryConfig {
  dailyWage: number
  startTime: string
  endTime: string
}

const SALARY_CONFIG_KEY = 'time-remember-salary-config'

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
const quickText = ref('')
const quickSaving = ref(false)
const quickJustSaved = ref(false)
const salaryConfig = reactive<SalaryConfig>(loadSalaryConfig())
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
const monthEvents = computed(() => events.value.filter(event => dayjs(event.eventDate).isSame(currentMonth.value, 'month')))
const activeDays = computed(() => new Set(monthEvents.value.map(event => event.eventDate)).size)
const monthTopCategory = computed(() => {
  const counts = monthEvents.value.reduce<Record<string, number>>((map, event) => {
    map[event.categoryName] = (map[event.categoryName] || 0) + 1
    return map
  }, {})
  const top = Object.entries(counts).sort((a, b) => b[1] - a[1])[0]
  return top?.[0] || '暂无'
})
const nowDateText = computed(() => `${now.value.format('YYYY-MM-DD')} ${weekDayText(now.value.day())}`)
const nowTimeText = computed(() => now.value.format('HH:mm:ss'))
const todayKey = computed(() => now.value.format('YYYY-MM-DD'))
const todayDateText = computed(() => now.value.format('YYYY 年 MM 月 DD 日'))
const todayWeekText = computed(() => weekDayText(now.value.day()))
const todayEvents = computed(() => groupedEvents.value[todayKey.value] || [])
const dayProgressPercentage = computed(() => {
  const start = now.value.startOf('day')
  const elapsed = now.value.diff(start)
  return Math.min(100, Math.max(0, Number(((elapsed / 86400000) * 100).toFixed(1))))
})
const dayProgressText = computed(() => dayProgressPercentage.value.toFixed(1))
const workProgress = computed(() => {
  const start = parseTodayTime(salaryConfig.startTime)
  const end = parseTodayTime(salaryConfig.endTime)
  if (!start || !end || !end.isAfter(start)) {
    return 0
  }
  const total = end.diff(start)
  const elapsed = now.value.diff(start)
  return Math.min(1, Math.max(0, elapsed / total))
})
const earnedToday = computed(() => salaryConfig.dailyWage * workProgress.value)
const earnedTodayText = computed(() => earnedToday.value.toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
}))
const streakDays = computed(() => {
  const eventDates = new Set(events.value.map(event => event.eventDate))
  let cursor = now.value.startOf('day')
  let count = 0
  while (eventDates.has(cursor.format('YYYY-MM-DD'))) {
    count += 1
    cursor = cursor.subtract(1, 'day')
  }
  return count
})
const atmosphereText = computed(() => {
  if (todayEvents.value.length >= 3) return '今天的生活已经有了清晰的纹理，继续轻轻收集。'
  if (todayEvents.value.length > 0) return '有些时刻已经被你留下，时间正在慢慢变得可见。'
  if (dayProgressPercentage.value < 45) return '上午还很新，给今天留一个温柔的开头。'
  if (dayProgressPercentage.value < 75) return '白昼走到中段，适合把发生过的小事放回这里。'
  return '夜色靠近时，记录会替今天留下一盏小灯。'
})
const quickPreviewEmoji = computed(() => eventEmoji({
  id: 'preview',
  eventDate: todayKey.value,
  title: quickText.value || '快速记录',
  categoryId: '',
  categoryName: inferQuickMeta(quickText.value).categoryName,
  categoryColor: inferQuickMeta(quickText.value).color,
  level: inferQuickMeta(quickText.value).level,
  createdAt: '',
  updatedAt: ''
}))
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

async function submitQuickRecord(): Promise<void> {
  const content = quickText.value.trim()
  if (!content) {
    ElMessage.warning('先写下一句话吧')
    return
  }
  quickSaving.value = true
  quickJustSaved.value = false
  try {
    const meta = inferQuickMeta(content)
    const category = await ensureQuickCategory(meta.categoryName, meta.color)
    await eventApi.create({
      eventDate: todayKey.value,
      title: meta.title,
      categoryId: category.id,
      note: content,
      level: meta.level
    })
    quickText.value = ''
    quickJustSaved.value = true
    ElMessage.success('已轻轻记下这一刻')
    await loadData()
    window.setTimeout(() => {
      quickJustSaved.value = false
    }, 900)
  } finally {
    quickSaving.value = false
  }
}

async function ensureQuickCategory(name: string, color: string): Promise<Category> {
  const exists = categories.value.find(category => category.name === name)
  if (exists) {
    return exists
  }
  const created = await categoryApi.create({ name, color })
  categories.value = [created, ...categories.value]
  return created
}

function inferQuickMeta(content: string): { categoryName: string; color: string; title: string; level: EventLevel } {
  const text = content.toLowerCase()
  const rules = [
    { test: /(做饭|做菜|吃饭|早餐|午餐|晚餐|cook|dinner|breakfast|lunch|food)/i, categoryName: '做饭', color: '#F2A65A', title: '做饭记录', level: 'MEDIUM' as EventLevel },
    { test: /(心情|难过|开心|焦虑|崩溃|emo|mood|sad|happy)/i, categoryName: '心情', color: '#FF7AA2', title: '心情记录', level: 'MEDIUM' as EventLevel },
    { test: /(例假|姨妈|月经|period)/i, categoryName: '健康', color: '#E879B9', title: '身体记录', level: 'HIGH' as EventLevel },
    { test: /(运动|健身|跑步|训练|瑜伽|run|fitness|gym|workout)/i, categoryName: '运动', color: '#5DBB63', title: '运动记录', level: 'MEDIUM' as EventLevel },
    { test: /(熬夜|睡觉|失眠|早睡|晚睡|sleep|night|late)/i, categoryName: '睡眠', color: '#7C83FD', title: '睡眠记录', level: 'MEDIUM' as EventLevel },
    { test: /(学习|读书|工作|写代码|study|read|work|code)/i, categoryName: '成长', color: '#58A6FF', title: '成长记录', level: 'MEDIUM' as EventLevel }
  ]
  const matched = rules.find(rule => rule.test.test(text))
  if (matched) {
    return { ...matched, title: buildQuickTitle(content, matched.title) }
  }
  return { categoryName: '生活', color: '#B9A7FF', title: buildQuickTitle(content, '生活记录'), level: 'LOW' }
}

function buildQuickTitle(content: string, fallback: string): string {
  const normalized = content.replace(/^今天/, '').trim()
  if (!normalized) {
    return fallback
  }
  return normalized.length > 18 ? `${normalized.slice(0, 18)}...` : normalized
}

function loadSalaryConfig(): SalaryConfig {
  try {
    const raw = localStorage.getItem(SALARY_CONFIG_KEY)
    if (raw) {
      return { dailyWage: 300, startTime: '09:00', endTime: '18:00', ...JSON.parse(raw) } as SalaryConfig
    }
  } catch {
    // ignore invalid local config
  }
  return { dailyWage: 300, startTime: '09:00', endTime: '18:00' }
}

function parseTodayTime(value: string): dayjs.Dayjs | null {
  const [hour, minute] = value.split(':').map(Number)
  if (!Number.isFinite(hour) || !Number.isFinite(minute)) {
    return null
  }
  return now.value.startOf('day').hour(hour).minute(minute).second(0).millisecond(0)
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

function eventEmoji(event: EventRecord): string {
  const text = `${event.title} ${event.categoryName}`.toLowerCase()
  if (/(做饭|做菜|吃|餐|饭|cook|food|dinner|breakfast|lunch|cooking)/i.test(text)) return '🍜'
  if (/(运动|健身|跑|训练|run|fitness|training|gym|workout)/i.test(text)) return '🏃'
  if (/(心情|情绪|难过|开心|焦虑|mood|heart|happy|sad)/i.test(text)) return '❤️'
  if (/(睡|熬夜|夜|sleep|night|late)/i.test(text)) return '🌙'
  if (/(健康|例假|身体|药|health|period)/i.test(text)) return '🌿'
  if (/(学习|读书|工作|写|study|read|work)/i.test(text)) return '✍️'
  if (/(旅行|散步|出门|travel|walk)/i.test(text)) return '✨'
  return '🫧'
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

watch(salaryConfig, value => {
  localStorage.setItem(SALARY_CONFIG_KEY, JSON.stringify(value))
}, { deep: true })
</script>

<style scoped>
.calendar-page {
  display: grid;
  max-width: 1440px;
  margin: 0 auto;
  gap: 24px;
}

.calendar-intro {
  position: relative;
  display: grid;
  gap: 18px;
}

.calendar-card,
.timeline-summary {
  position: relative;
  padding: 24px;
  border-radius: 16px;
}

.side-panel {
  display: none;
}

.timeline-summary {
  display: grid;
  gap: 20px;
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.timeline-head div {
  display: grid;
  gap: 4px;
}

.timeline-head span,
.stat-tile span {
  color: #ff70b5;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: .08em;
  text-transform: uppercase;
}

.timeline-head strong {
  color: #3d3340;
  font-size: 22px;
  line-height: 1.2;
}

.timeline-head small {
  color: #8c7288;
  font-size: 13px;
  font-weight: 700;
}

.timeline-empty {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 88px;
  padding: 16px;
  border-radius: 16px;
  background: rgb(255 255 255 / 58%);
  color: #806b7d;
  font-weight: 800;
}

.timeline-empty img {
  width: 58px;
  height: 58px;
  object-fit: contain;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.stat-tile {
  display: grid;
  gap: 7px;
  min-height: 126px;
  padding: 20px;
  border-radius: 16px;
}

.stat-tile strong {
  min-width: 0;
  overflow: hidden;
  color: #3d3340;
  font-size: 30px;
  line-height: 1.08;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-tile small {
  color: #8c7288;
  font-size: 13px;
  font-weight: 800;
}

.today-status-card {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(220px, .78fr) minmax(260px, 1fr) minmax(240px, .78fr);
  align-items: stretch;
  gap: 14px;
  margin-bottom: 18px;
  padding: 18px;
  overflow: hidden;
  border: 1px solid rgb(255 255 255 / 64%);
  border-radius: 16px;
  background:
    radial-gradient(circle at 8% 10%, rgb(255 214 234 / 72%), transparent 190px),
    radial-gradient(circle at 82% 18%, rgb(188 225 255 / 58%), transparent 210px),
    linear-gradient(135deg, rgb(255 255 255 / 86%), rgb(250 252 255 / 72%));
  box-shadow: 0 22px 54px rgb(74 61 79 / 10%), inset 0 0 0 1px rgb(255 255 255 / 42%);
  backdrop-filter: blur(20px) saturate(1.16);
}

.today-status-card::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: "";
  background:
    linear-gradient(120deg, transparent, rgb(255 255 255 / 38%), transparent),
    radial-gradient(circle at 52% 120%, rgb(255 255 255 / 52%), transparent 280px);
}

.status-date,
.status-events,
.status-metrics {
  position: relative;
  z-index: 1;
}

.status-date {
  display: grid;
  align-content: center;
  gap: 5px;
  min-width: 0;
}

.status-eyebrow,
.status-section-title span,
.status-metrics span {
  color: #987d93;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .08em;
  text-transform: uppercase;
}

.status-date strong {
  color: #2f2632;
  font-size: 25px;
  line-height: 1.16;
  letter-spacing: 0;
}

.status-date small {
  width: fit-content;
  padding: 5px 10px;
  color: #ff70b5;
  border: 1px solid rgb(255 201 229 / 74%);
  border-radius: 999px;
  background: rgb(255 255 255 / 58%);
  font-size: 12px;
  font-weight: 900;
}

.status-date p {
  max-width: 330px;
  margin: 6px 0 0;
  color: #6f6070;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.65;
}

.status-events {
  display: grid;
  align-content: center;
  gap: 12px;
  min-width: 0;
  padding: 0 6px;
}

.status-section-title {
  display: flex;
  align-items: center;
  gap: 9px;
}

.status-section-title strong {
  display: grid;
  place-items: center;
  min-width: 28px;
  height: 28px;
  color: #fff;
  border-radius: 999px;
  background: linear-gradient(135deg, #2f2632, #876b83);
  font-size: 13px;
  box-shadow: 0 12px 24px rgb(74 61 79 / 18%);
}

.today-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.today-tag {
  --el-tag-border-color: color-mix(in srgb, var(--tag-color) 42%, white);
  --el-tag-bg-color: color-mix(in srgb, var(--tag-color) 12%, white);
  --el-tag-text-color: #4a3d4f;
  height: 30px;
  padding: 0 11px;
  border-color: var(--el-tag-border-color);
  background: var(--el-tag-bg-color);
  box-shadow: 0 8px 18px rgb(74 61 79 / 7%);
  font-weight: 800;
  transition: transform 160ms ease, box-shadow 160ms ease, filter 160ms ease;
}

.today-tag:hover {
  box-shadow: 0 12px 24px rgb(74 61 79 / 12%);
  filter: saturate(1.08);
  transform: translateY(-2px) scale(1.03);
}

.event-emoji {
  display: inline-grid;
  place-items: center;
  flex: 0 0 auto;
  line-height: 1;
}

.today-empty-line {
  color: #8c7b89;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.6;
}

.status-metrics {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px;
  align-items: center;
  gap: 12px;
}

.day-progress,
.streak-card {
  border: 1px solid rgb(255 255 255 / 58%);
  background: rgb(255 255 255 / 48%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 30%);
}

.day-progress {
  display: grid;
  gap: 12px;
  padding: 14px;
  border-radius: 22px;
}

.progress-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.progress-head strong {
  color: #2f2632;
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 22px;
  line-height: 1;
}

.day-progress :deep(.el-progress-bar__outer) {
  height: 9px !important;
  border-radius: 999px;
  background: rgb(74 61 79 / 8%);
}

.day-progress :deep(.el-progress-bar__inner) {
  border-radius: 999px;
  background: linear-gradient(90deg, #2f2632, #ff8fc7 62%, #8cc8ff);
}

.salary-meter {
  display: grid;
  grid-template-columns: minmax(96px, .42fr) minmax(130px, 1fr) auto;
  align-items: center;
  gap: 10px;
  min-height: 68px;
  padding: 10px;
  border: 1px solid rgb(255 255 255 / 58%);
  border-radius: 14px;
  background:
    radial-gradient(circle at 12% 24%, rgb(255 216 107 / 26%), transparent 78px),
    linear-gradient(135deg, rgb(255 255 255 / 62%), rgb(255 248 226 / 52%));
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 28%);
}

.salary-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.salary-copy span {
  color: #987d93;
  font-size: 12px;
  font-weight: 900;
}

.salary-copy strong {
  color: #2f2632;
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 20px;
  line-height: 1;
  letter-spacing: 0;
  animation: salary-tick 1s ease-in-out infinite;
}

.money-walk {
  position: relative;
  min-width: 0;
  height: 44px;
  overflow: hidden;
  border: 1px solid rgb(255 255 255 / 66%);
  border-radius: 999px;
  background:
    linear-gradient(90deg, rgb(255 222 130 / 55%) var(--salary-progress), rgb(255 255 255 / 46%) 0),
    linear-gradient(135deg, rgb(255 255 255 / 58%), rgb(240 248 255 / 44%));
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 34%), 0 10px 20px rgb(244 183 70 / 12%);
}

.money-road {
  position: absolute;
  inset: auto 12px 8px;
  display: flex;
  justify-content: space-between;
  pointer-events: none;
}

.road-dot {
  width: 5px;
  height: 5px;
  border-radius: 999px;
  background: rgb(151 120 78 / 22%);
}

.money-worker {
  position: absolute;
  left: clamp(12px, var(--salary-progress), calc(100% - 62px));
  top: 50%;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transform: translateY(-50%);
  transition: left 700ms cubic-bezier(.2, .9, .2, 1);
  animation: worker-bob 1.2s ease-in-out infinite;
}

.worker-face {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  color: #6c4d59;
  border: 2px solid rgb(255 255 255 / 78%);
  border-radius: 999px 999px 12px 12px;
  background: linear-gradient(180deg, #fff5cd, #ffd9df);
  box-shadow: 0 8px 14px rgb(255 143 199 / 14%);
  font-family: "Cascadia Mono", "Consolas", monospace;
  font-size: 8px;
  font-weight: 900;
  white-space: nowrap;
}

.money-cart {
  display: grid;
  place-items: center;
  width: 28px;
  height: 24px;
  border: 2px solid rgb(255 255 255 / 74%);
  border-radius: 10px;
  background: linear-gradient(135deg, #fff6b8, #bff3d0);
  box-shadow: 0 8px 14px rgb(141 196 107 / 14%);
  font-size: 15px;
  transform: rotate(-4deg);
}

.salary-config {
  height: 26px;
  padding: 0 8px;
  font-size: 12px;
}

.salary-config-panel {
  display: grid;
  gap: 10px;
}

.salary-config-panel > span {
  color: #3d3340;
  font-weight: 900;
}

.salary-config-panel :deep(.el-input-number),
.salary-config-panel :deep(.el-date-editor) {
  width: 100%;
}

.streak-card {
  display: grid;
  place-items: center;
  min-height: 92px;
  padding: 12px 8px;
  border-radius: 24px;
}

.streak-card strong {
  color: #2f2632;
  font-size: 34px;
  line-height: 1;
}

.streak-card small {
  color: #8c7b89;
  font-size: 12px;
  font-weight: 900;
}

.quick-record {
  position: sticky;
  top: 10px;
  z-index: 12;
  display: grid;
  grid-template-columns: minmax(180px, .38fr) minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  padding: 12px 14px;
  overflow: hidden;
  border: 1px solid rgb(255 255 255 / 68%);
  border-radius: 16px;
  background:
    radial-gradient(circle at 8% 20%, rgb(255 216 234 / 62%), transparent 150px),
    linear-gradient(135deg, rgb(255 255 255 / 86%), rgb(248 251 255 / 72%));
  box-shadow: 0 18px 42px rgb(74 61 79 / 10%), inset 0 0 0 1px rgb(255 255 255 / 36%);
  backdrop-filter: blur(22px) saturate(1.16);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.quick-record::after {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: "";
  background: linear-gradient(110deg, transparent 0 28%, rgb(255 255 255 / 34%) 44%, transparent 62% 100%);
  opacity: 0;
  transform: translateX(-50%);
}

.quick-record.sending {
  border-color: rgb(255 143 199 / 48%);
  box-shadow: 0 22px 52px rgb(255 143 199 / 18%);
  transform: translateY(-1px);
}

.quick-record.done::after {
  animation: quick-shine 680ms ease;
}

.quick-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.quick-copy span {
  color: #ff70b5;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: .09em;
  text-transform: uppercase;
}

.quick-copy strong {
  color: #3d3340;
  font-size: 15px;
  line-height: 1.25;
}

.quick-input-wrap {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.quick-spark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: rgb(255 255 255 / 72%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 52%), 0 10px 20px rgb(74 61 79 / 8%);
  font-size: 18px;
  transition: transform 160ms ease;
}

.quick-record:focus-within .quick-spark {
  transform: rotate(-8deg) scale(1.08);
}

.quick-input :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 999px;
  background: rgb(255 255 255 / 64%);
  box-shadow: inset 0 0 0 1px rgb(255 218 236 / 70%);
}

.quick-input :deep(.el-input__inner) {
  color: #3d3340;
  font-weight: 800;
}

.quick-input :deep(.el-input__inner::placeholder) {
  color: #a58d9d;
  font-weight: 700;
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
  border-radius: 16px;
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
  border-radius: 16px;
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
  border-radius: 16px;
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
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 24px;
  margin-top: 4px;
  padding: 0 9px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--event-color, #ff8fc7) 36%, white);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--event-color, #ff8fc7) 15%, white), rgb(255 255 255 / 76%));
  box-shadow: 0 6px 14px color-mix(in srgb, var(--event-color, #ff8fc7) 18%, transparent);
  overflow: hidden;
  color: #453947;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 12px;
  font-weight: 800;
  transition: transform 150ms ease, box-shadow 150ms ease, filter 150ms ease;
}

.event-chip:hover {
  box-shadow: 0 10px 20px color-mix(in srgb, var(--event-color, #ff8fc7) 24%, transparent);
  filter: saturate(1.08);
  transform: translateX(2px) scale(1.02);
}

.event-chip-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
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
  display: flex;
  align-content: flex-start;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 2px;
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

.event-pill-wrap {
  display: grid;
  gap: 9px;
  width: 100%;
}

.event-pill-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 54px;
  padding: 8px 8px 8px 12px;
  border: 1px solid color-mix(in srgb, var(--event-color, #ff8fc7) 35%, white);
  border-radius: 999px;
  background:
    radial-gradient(circle at 12% 18%, color-mix(in srgb, var(--event-color, #ff8fc7) 20%, white), transparent 88px),
    linear-gradient(135deg, rgb(255 255 255 / 86%), color-mix(in srgb, var(--event-color, #ff8fc7) 10%, white));
  box-shadow: 0 12px 26px color-mix(in srgb, var(--event-color, #ff8fc7) 18%, transparent);
  color: #453947;
  text-align: left;
  transition: transform 170ms ease, box-shadow 170ms ease, filter 170ms ease;
}

.event-pill-card:hover {
  box-shadow: 0 18px 34px color-mix(in srgb, var(--event-color, #ff8fc7) 26%, transparent);
  filter: saturate(1.08);
  transform: translateY(-3px) scale(1.01);
}

.event-pill-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border: 1px solid rgb(255 255 255 / 78%);
  border-radius: 999px;
  background: rgb(255 255 255 / 72%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 40%);
  font-size: 19px;
}

.event-pill-main {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.event-pill-main strong,
.event-pill-main small {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-pill-main strong {
  color: #453947;
  font-size: 15px;
  font-weight: 900;
}

.event-pill-main small {
  color: #806b7d;
  font-size: 12px;
  font-weight: 800;
}

.event-pill-media {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 9px;
  color: color-mix(in srgb, var(--event-color, #ff8fc7) 70%, #453947);
  border-radius: 999px;
  background: rgb(255 255 255 / 64%);
  font-size: 12px;
  font-weight: 900;
}

.event-note {
  width: fit-content;
  max-width: 100%;
  margin: 0 0 0 50px;
  padding: 8px 12px;
  color: #735f72;
  border-radius: 16px;
  background: rgb(255 255 255 / 58%);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.6;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.media-strip {
  display: flex;
  gap: 8px;
  margin: 0 0 0 50px;
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

@media (max-width: 1080px) {
  .today-status-card {
    grid-template-columns: 1fr;
  }

  .status-metrics {
    grid-template-columns: minmax(0, 1fr) 110px;
  }

  .quick-record {
    grid-template-columns: 1fr;
  }

  .time-widget {
    margin-left: 0;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .today-status-card {
    padding: 14px;
    border-radius: 16px;
  }

  .status-date strong {
    font-size: 21px;
  }

  .status-metrics {
    grid-template-columns: 1fr;
  }

  .salary-meter {
    grid-template-columns: 1fr auto;
  }

  .money-walk {
    grid-column: 1 / -1;
  }

  .quick-record {
    top: 8px;
    padding: 12px;
    border-radius: 16px;
  }

  .quick-input-wrap {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .quick-input-wrap .el-button {
    grid-column: 1 / -1;
    width: 100%;
  }

  .date-cell {
    min-height: 88px;
  }
}

@media (prefers-color-scheme: dark) {
  .today-status-card {
    border-color: rgb(255 255 255 / 10%);
    background:
      radial-gradient(circle at 8% 10%, rgb(255 112 181 / 24%), transparent 190px),
      radial-gradient(circle at 82% 18%, rgb(140 200 255 / 18%), transparent 210px),
      linear-gradient(135deg, rgb(31 26 35 / 92%), rgb(25 29 38 / 78%));
    box-shadow: 0 24px 62px rgb(0 0 0 / 24%), inset 0 0 0 1px rgb(255 255 255 / 8%);
  }

  .today-status-card::before {
    background:
      linear-gradient(120deg, transparent, rgb(255 255 255 / 8%), transparent),
      radial-gradient(circle at 52% 120%, rgb(255 255 255 / 7%), transparent 280px);
  }

  .status-eyebrow,
  .status-section-title span,
  .status-metrics span,
  .status-date p,
  .today-empty-line,
  .streak-card small {
    color: rgb(231 220 231 / 72%);
  }

  .status-date strong,
  .progress-head strong,
  .streak-card strong {
    color: #fff8fd;
  }

  .status-date small,
  .day-progress,
  .streak-card,
  .salary-meter {
    border-color: rgb(255 255 255 / 10%);
    background:
      radial-gradient(circle at 12% 24%, rgb(255 216 107 / 13%), transparent 78px),
      linear-gradient(135deg, rgb(255 255 255 / 8%), rgb(255 255 255 / 5%));
  }

  .salary-copy strong,
  .salary-config-panel > span {
    color: #fff8fd;
  }

  .money-walk {
    border-color: rgb(255 255 255 / 10%);
    background:
      linear-gradient(90deg, rgb(244 209 122 / 28%) var(--salary-progress), rgb(255 255 255 / 6%) 0),
      linear-gradient(135deg, rgb(255 255 255 / 8%), rgb(255 255 255 / 4%));
    box-shadow: inset 0 0 0 1px rgb(255 255 255 / 8%), 0 12px 24px rgb(0 0 0 / 18%);
  }

  .worker-face {
    color: #3d3340;
    background: linear-gradient(180deg, #fff2b8, #ffbfd8);
  }

  .today-tag {
    --el-tag-bg-color: color-mix(in srgb, var(--tag-color) 22%, #1f1a23);
    --el-tag-border-color: color-mix(in srgb, var(--tag-color) 48%, #1f1a23);
    --el-tag-text-color: #fff8fd;
    box-shadow: 0 10px 20px rgb(0 0 0 / 16%);
  }

  .day-progress :deep(.el-progress-bar__outer) {
    background: rgb(255 255 255 / 10%);
  }

  .day-progress :deep(.el-progress-bar__inner) {
    background: linear-gradient(90deg, #fff8fd, #ff8fc7 58%, #8cc8ff);
  }

  .quick-record {
    border-color: rgb(255 255 255 / 10%);
    background:
      radial-gradient(circle at 8% 20%, rgb(255 112 181 / 18%), transparent 150px),
      linear-gradient(135deg, rgb(31 26 35 / 90%), rgb(25 29 38 / 76%));
    box-shadow: 0 20px 48px rgb(0 0 0 / 22%), inset 0 0 0 1px rgb(255 255 255 / 8%);
  }

  .quick-copy strong,
  .quick-input :deep(.el-input__inner) {
    color: #fff8fd;
  }

  .quick-input :deep(.el-input__wrapper),
  .quick-spark {
    background: rgb(255 255 255 / 8%);
    box-shadow: inset 0 0 0 1px rgb(255 255 255 / 10%);
  }

  .quick-input :deep(.el-input__inner::placeholder) {
    color: rgb(231 220 231 / 54%);
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

@keyframes salary-tick {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1px);
  }
}

@keyframes worker-bob {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 -2px;
  }
}

@keyframes quick-shine {
  0% {
    opacity: 0;
    transform: translateX(-60%);
  }
  30% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translateX(70%);
  }
}
</style>
