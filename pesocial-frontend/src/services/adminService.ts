import api from './api'
import type { User } from '../types/user'

export const adminService = {
  getAllUsers: async () => {
    const { data } = await api.get<User[]>('/api/admin/users')
    return data
  },

  suspendUser: async (userId: string) => {
    const { data } = await api.post<User>(`/api/admin/users/${encodeURIComponent(userId)}/suspend`)
    return data
  },

  banUser: async (userId: string) => {
    const { data } = await api.post<User>(`/api/admin/users/${encodeURIComponent(userId)}/ban`)
    return data
  },

  approveCreator: async (userId: string) => {
    const { data } = await api.post<User>(`/api/admin/users/${encodeURIComponent(userId)}/approve-creator`)
    return data
  },

  reviewReports: async () => {
    const { data } = await api.get<string[]>('/api/admin/reports/review')
    return data
  },

  getSystemReport: async () => {
    const { data } = await api.get<string>('/api/admin/reports/system')
    return data
  },

  sendAnnouncement: async (message: string) => {
    const { data } = await api.post<string>('/api/admin/announcements', null, {
      params: { message },
    })
    return data
  },
}