import api from './api'
import type { AuthResponse, LoginRequest, RefreshTokenRequest, RegisterRequest } from '../types/auth'

export const authService = {
  login: async (payload: LoginRequest) => {
    const { data } = await api.post<AuthResponse>('/api/auth/login', payload)
    return data
  },

  register: async (payload: RegisterRequest) => {
    const { data } = await api.post<AuthResponse>('/api/auth/register', payload)
    return data
  },

  refresh: async (payload: RefreshTokenRequest) => {
    const { data } = await api.post<AuthResponse>('/api/auth/refresh', payload)
    return data
  },

  logout: async (refreshToken: string) => {
    await api.post('/api/auth/logout-service', { refreshToken })
  },
}
