import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { AuthResponse, BackendErrorResponse } from '../types/auth'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const ACCESS_TOKEN_KEY = 'accessToken'
export const REFRESH_TOKEN_KEY = 'refreshToken'
export const USER_ID_KEY = 'userId'
export const USER_ROLE_KEY = 'userRole'
export const USER_HANDLE_KEY = 'userHandle'
export const ACCESS_EXPIRES_AT_KEY = 'accessExpiresAt'

type RetryableRequestConfig = InternalAxiosRequestConfig & { _retry?: boolean }

const shouldSkipRefresh = (url?: string) => {
  if (!url) {
    return false
  }
  return url.includes('/api/auth/login') || url.includes('/api/auth/register') || url.includes('/api/auth/refresh')
}

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
})

let isRefreshing = false
let refreshSubscribers: Array<(token: string | null) => void> = []

const subscribeToRefresh = (callback: (token: string | null) => void) => {
  refreshSubscribers.push(callback)
}

const onRefreshed = (token: string | null) => {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

export const clearAuthStorage = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(USER_ROLE_KEY)
  localStorage.removeItem(USER_HANDLE_KEY)
  localStorage.removeItem(ACCESS_EXPIRES_AT_KEY)
}

const saveAuthResponse = (auth: AuthResponse) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken)
  localStorage.setItem(USER_ID_KEY, auth.userId)
  localStorage.setItem(USER_ROLE_KEY, auth.role)
  localStorage.setItem(USER_HANDLE_KEY, auth.handle)
  localStorage.setItem(ACCESS_EXPIRES_AT_KEY, String(Date.now() + auth.accessTokenExpiresInSeconds * 1000))
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<BackendErrorResponse>) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

    if (!originalRequest || shouldSkipRefresh(originalRequest.url)) {
      return Promise.reject(error)
    }

    if (error.response?.status !== 401 || !refreshToken) {
      return Promise.reject(error)
    }

    if (originalRequest._retry) {
      clearAuthStorage()
      return Promise.reject(error)
    }

    originalRequest._retry = true

    if (!isRefreshing) {
      isRefreshing = true
      try {
        const { data } = await axios.post<AuthResponse>(`${API_BASE_URL}/api/auth/refresh`, {
          refreshToken,
        })
        saveAuthResponse(data)
        onRefreshed(data.accessToken)
      } catch (refreshError) {
        clearAuthStorage()
        onRefreshed(null)
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return new Promise((resolve, reject) => {
      subscribeToRefresh((token) => {
        if (!token) {
          reject(error)
          return
        }
        originalRequest.headers.Authorization = `Bearer ${token}`
        resolve(api(originalRequest))
      })
    })
  },
)

export const parseApiError = (error: unknown) => {
  if (axios.isAxiosError<BackendErrorResponse>(error)) {
    const backendMessage = error.response?.data?.message
    if (typeof backendMessage === 'string' && backendMessage.trim().length > 0) {
      return backendMessage
    }
    if (typeof error.message === 'string' && error.message.trim().length > 0) {
      return error.message
    }
  }
  return 'Unexpected error occurred. Please try again.'
}

export default api
