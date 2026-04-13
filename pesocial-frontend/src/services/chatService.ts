import api from './api'
import type { ChatMessagePayload, ChatRoom, ChatRoomSummary, ChatMessage } from '../types/chat'
import type { UserSummary } from '../types/user'

export const chatService = {
  createOrGetChatRoom: async (otherUserId: string) => {
    const { data } = await api.post<ChatRoom>(`/api/chat-rooms/${encodeURIComponent(otherUserId)}`)
    return data
  },

  getOrCreateChatRoomByHandle: async (targetHandle: string) => {
    const { data } = await api.get<ChatRoomSummary>(`/api/chat-rooms/room/${encodeURIComponent(targetHandle)}`)
    return data
  },

  getMyChatRooms: async () => {
    const { data } = await api.get<ChatRoomSummary[]>('/api/chat-rooms')
    return data
  },

  getChatHistory: async (chatRoomId: string, limit = 50, offset = 0) => {
    const { data } = await api.get<ChatMessage[]>(`/api/chat-rooms/${encodeURIComponent(chatRoomId)}/messages`, {
      params: { limit, offset },
    })
    return data
  },

  sendMessage: async (payload: ChatMessagePayload) => {
    const { data } = await api.post<ChatMessage>('/api/messages', payload)
    return data
  },

  uploadAttachment: async (file: File) => {
    const form = new FormData()
    form.append('file', file)
    const { data } = await api.post<{ attachmentUrl: string }>('/api/messages/attachments', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data.attachmentUrl
  },

  markAsRead: async (chatRoomId: string) => {
    await api.patch(`/api/chat-rooms/${encodeURIComponent(chatRoomId)}/mark-as-read`)
  },

  searchUsersForChat: async (query: string) => {
    const { data } = await api.get<UserSummary[]>('/api/chat-rooms/search-users', {
      params: { query },
    })
    return data
  },
}
