export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export type UserRole = 'USER' | 'ADMIN'
export type EventLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type StatisticsPeriod = 'WEEK' | 'MONTH' | 'YEAR'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest extends LoginRequest {
  nickname?: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname?: string
  role: UserRole
}

export interface Category {
  id: number
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
  id: number
  eventDate: string
  title: string
  categoryId: number
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
  categoryId: number
  note?: string
  level: EventLevel
}

export interface PeriodCount {
  period: string
  count: number
}

export interface CategoryCount {
  categoryId: number
  categoryName: string
  categoryColor: string
  count: number
}
