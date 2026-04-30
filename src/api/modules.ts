import http, { request, requestBlob } from '@/api/http'
import type {
  Category,
  CategoryCount,
  EventAttachment,
  CategoryPayload,
  EventPayload,
  EventRecord,
  LoginRequest,
  LoginResponse,
  PeriodCount,
  RegisterRequest,
  StatisticsPeriod
} from '@/types/api'

export const authApi = {
  login(payload: LoginRequest) {
    return request<LoginResponse>(http.post('/auth/login', payload))
  },
  register(payload: RegisterRequest) {
    return request<void>(http.post('/auth/register', payload))
  }
}

export const categoryApi = {
  list() {
    return request<Category[]>(http.get('/categories'))
  },
  create(payload: CategoryPayload) {
    return request<Category>(http.post('/categories', payload))
  },
  update(id: string, payload: CategoryPayload) {
    return request<Category>(http.put(`/categories/${id}`, payload))
  },
  remove(id: string) {
    return request<void>(http.delete(`/categories/${id}`))
  }
}

export const eventApi = {
  list(params?: { startDate?: string; endDate?: string }) {
    return request<EventRecord[]>(http.get('/events', { params }))
  },
  create(payload: EventPayload) {
    return request<EventRecord>(http.post('/events', payload))
  },
  update(id: string, payload: EventPayload) {
    return request<EventRecord>(http.put(`/events/${id}`, payload))
  },
  remove(id: string) {
    return request<void>(http.delete(`/events/${id}`))
  }
}

export const eventAttachmentApi = {
  list(eventId: string) {
    return request<EventAttachment[]>(http.get(`/events/${eventId}/attachments`))
  },
  upload(eventId: string, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<EventAttachment>(http.post(`/events/${eventId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    }))
  },
  remove(eventId: string, attachmentId: string) {
    return request<void>(http.delete(`/events/${eventId}/attachments/${attachmentId}`))
  },
  content(attachmentId: string) {
    return requestBlob(http.get(`/attachments/${attachmentId}/content`, {
      responseType: 'blob',
      timeout: 60000
    }))
  }
}

export const statisticsApi = {
  period(params: { period: StatisticsPeriod; startDate?: string; endDate?: string }) {
    return request<PeriodCount[]>(http.get('/statistics/period', { params }))
  },
  categories(params?: { period?: StatisticsPeriod; startDate?: string; endDate?: string }) {
    return request<CategoryCount[]>(http.get('/statistics/category', { params }))
  }
}
