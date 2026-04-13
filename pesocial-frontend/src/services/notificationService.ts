import api from './api'

export const notificationService = {
  markAllAsRead: async (recipientId: string): Promise<void> => {
    await api.post(`/api/notifications/mark-all-read`, null, {
      params: { recipientId },
    })
  },

  countUnread: async (recipientId: string): Promise<number> => {
    const response = await api.get<number>(`/api/notifications/unread-count`, {
      params: { recipientId },
    })
    return response.data
  },

  getNotifications: async (recipientId: string) => {
    const response = await api.get(`/api/notifications`, {
      params: { recipientId },
    })
    return response.data
  },
}
