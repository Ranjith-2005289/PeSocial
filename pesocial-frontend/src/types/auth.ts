import type { UserRole } from './user'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  accessTokenExpiresInSeconds: number
  userId: string
  role: UserRole
  handle: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  username: string
  handle: string
  email: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface BackendErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
