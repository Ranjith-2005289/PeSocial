import type { Message } from './message'

export interface ChatRoom {
  chatRoomId: string
  user1Id: string
  user2Id: string
  lastMessageId?: string
  lastMessageText?: string
  lastMessageTimestamp?: string
  unreadCountUser1?: number
  unreadCountUser2?: number
  createdAt?: string
  updatedAt?: string
  archived?: boolean
  pinned?: boolean
}

export interface ChatRoomSummary {
  chatRoomId: string
  otherUserId: string
  otherUserHandle: string
  otherUserUsername: string
  otherUserProfilePhoto?: string
  lastMessageText?: string
  lastMessageTimestamp?: string
  unreadCount?: number
  pinned?: boolean
  archived?: boolean
}

export interface ChatMessagePayload {
  senderId: string
  receiverId: string
  messageText: string
  chatRoomId?: string
  attachmentUrl?: string
  attachmentType?: string
}

export interface ChatMessage extends Message {
  chatRoomId?: string
}
