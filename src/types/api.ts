export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export type UserRole = 'USER' | 'ADMIN'
export type EventLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type AttachmentType = 'IMAGE' | 'GIF' | 'VIDEO'
export type StatisticsPeriod = 'WEEK' | 'MONTH' | 'YEAR'
export type ApiId = string

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest extends LoginRequest {
  nickname?: string
}

export interface LoginResponse {
  token: string
  userId: ApiId
  username: string
  nickname?: string
  role: UserRole
}

export interface Category {
  id: ApiId
  name: string
  color: string
  createdAt: string
  updatedAt: string
}

export interface CategoryPayload {
  name: string
  color: string
}

export interface EventRecord {
  id: ApiId
  eventDate: string
  title: string
  categoryId: ApiId
  categoryName: string
  categoryColor: string
  note?: string
  level: EventLevel
  createdAt: string
  updatedAt: string
}

export interface EventPayload {
  eventDate: string
  title: string
  categoryId: ApiId
  note?: string
  level: EventLevel
}

export interface EventAttachment {
  id: ApiId
  eventId: ApiId
  type: AttachmentType
  originalName: string
  contentType: string
  fileSize: number
  url: string
  createdAt: string
}

export interface PeriodCount {
  period: string
  count: number
}

export interface CategoryCount {
  categoryId: ApiId
  categoryName: string
  categoryColor: string
  count: number
}
