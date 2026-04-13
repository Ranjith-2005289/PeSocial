import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth'
import type { User, UserRole } from '../types/user'
import { authService } from '../services/authService'
import {
  ACCESS_EXPIRES_AT_KEY,
  ACCESS_TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_ID_KEY,
  USER_HANDLE_KEY,
  USER_ROLE_KEY,
  clearAuthStorage,
} from '../services/api'

type AuthContextValue = {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (payload: LoginRequest) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
  refreshSession: () => Promise<void>
  applyAuthResponse: (response: AuthResponse, existingUser?: User | null) => void
  hasRole: (...roles: UserRole[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

const toUser = (response: AuthResponse, existingUser?: User | null): User => ({
  id: response.userId,
  role: response.role,
  handle: response.handle,
  email: existingUser?.email ?? '',
  username: existingUser?.username ?? '',
})

const getStoredUser = (): User | null => {
  const id = localStorage.getItem(USER_ID_KEY)
  const role = localStorage.getItem(USER_ROLE_KEY) as UserRole | null
  const handle = localStorage.getItem(USER_HANDLE_KEY)

  if (!id || !role || !handle) {
    return null
  }

  return {
    id,
    role,
    handle,
    username: '',
    email: '',
  }
}

const storeSession = (response: AuthResponse, existingUser?: User | null) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken)
  localStorage.setItem(USER_ID_KEY, response.userId)
  localStorage.setItem(USER_ROLE_KEY, response.role)
  localStorage.setItem(USER_HANDLE_KEY, response.handle)
  localStorage.setItem(ACCESS_EXPIRES_AT_KEY, String(Date.now() + response.accessTokenExpiresInSeconds * 1000))
  return toUser(response, existingUser)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY))
  const [refreshToken, setRefreshToken] = useState<string | null>(localStorage.getItem(REFRESH_TOKEN_KEY))
  const [user, setUser] = useState<User | null>(getStoredUser)
  const [isLoading, setIsLoading] = useState(true)

  const applySession = useCallback(
    (response: AuthResponse, existingUser?: User | null) => {
      const normalizedUser = storeSession(response, existingUser ?? user)
      setAccessToken(response.accessToken)
      setRefreshToken(response.refreshToken)
      setUser(normalizedUser)
    },
    [user],
  )

  const refreshSession = useCallback(async () => {
    const currentRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
    if (!currentRefreshToken) {
      clearAuthStorage()
      setAccessToken(null)
      setRefreshToken(null)
      setUser(null)
      return
    }

    const response = await authService.refresh({ refreshToken: currentRefreshToken })
    applySession(response, user)
  }, [applySession, user])

  const login = useCallback(
    async (payload: LoginRequest) => {
      const response = await authService.login(payload)
      applySession(response)
    },
    [applySession],
  )

  const register = useCallback(
    async (payload: RegisterRequest) => {
      const response = await authService.register(payload)
      applySession(response, {
        id: response.userId,
        role: response.role,
        handle: response.handle,
        username: payload.username,
        email: payload.email,
      })
    },
    [applySession],
  )

  const logout = useCallback(async () => {
    try {
      const currentRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
      if (currentRefreshToken) {
        await authService.logout(currentRefreshToken)
      }
    } catch {
      // Ignore logout network errors and clear local session anyway.
    } finally {
      clearAuthStorage()
      setAccessToken(null)
      setRefreshToken(null)
      setUser(null)
    }
  }, [])

  const hasRole = useCallback(
    (...roles: UserRole[]) => {
      if (!user) {
        return false
      }
      return roles.includes(user.role)
    },
    [user],
  )

  useEffect(() => {
    const initialize = async () => {
      const storedRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
      if (!storedRefreshToken) {
        clearAuthStorage()
        setAccessToken(null)
        setRefreshToken(null)
        setUser(null)
        setIsLoading(false)
        return
      }

      try {
        await refreshSession()
      } catch {
        clearAuthStorage()
        setAccessToken(null)
        setRefreshToken(null)
        setUser(null)
      } finally {
        setIsLoading(false)
      }
    }

    void initialize()
  }, [refreshSession])

  useEffect(() => {
    if (!refreshToken) {
      return
    }

    const expiresAt = Number(localStorage.getItem(ACCESS_EXPIRES_AT_KEY))
    if (!Number.isFinite(expiresAt) || expiresAt <= 0) {
      return
    }

    const refreshLeadMs = 30_000
    const delay = Math.max(expiresAt - Date.now() - refreshLeadMs, 5_000)

    const timer = window.setTimeout(() => {
      void refreshSession().catch(() => {
        void logout()
      })
    }, delay)

    return () => window.clearTimeout(timer)
  }, [refreshSession, refreshToken, logout, accessToken])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      accessToken,
      refreshToken,
      isAuthenticated: Boolean(accessToken && user),
      isLoading,
      login,
      register,
      logout,
      refreshSession,
      applyAuthResponse: applySession,
      hasRole,
    }),
    [accessToken, applySession, hasRole, isLoading, login, logout, refreshSession, refreshToken, register, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuthContext = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider')
  }
  return context
}
