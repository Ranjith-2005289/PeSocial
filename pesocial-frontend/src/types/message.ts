export interface Message {
  id?: string
  messageId?: string
  chatRoomId?: string
  senderId: string
  recipientId: string
  content: string
  messageText?: string
  createdAt?: string
  timestamp?: string
  read?: boolean
  isRead?: boolean
  reaction?: string
  attachmentUrl?: string
  attachmentType?: string
  messageStatus?: string
}
