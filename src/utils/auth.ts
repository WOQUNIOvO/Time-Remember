import type { LoginResponse } from '@/types/api'

const TOKEN_KEY = 'time_remember_token'
const USER_KEY = 'time_remember_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setSession(login: LoginResponse): void {
  localStorage.setItem(TOKEN_KEY, login.token)
  localStorage.setItem(USER_KEY, JSON.stringify(login))
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getCurrentUser(): LoginResponse | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as LoginResponse
  } catch {
    clearSession()
    return null
  }
}
