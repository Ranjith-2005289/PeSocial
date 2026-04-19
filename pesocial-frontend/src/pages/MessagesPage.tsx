import { useEffect, useMemo, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useAuth } from '../hooks/useAuth'
import { useWebSockets } from '../hooks/useWebSockets'
import { chatService } from '../services/chatService'
import { API_BASE_URL, parseApiError } from '../services/api'
import type { ChatRoomSummary, ChatMessage } from '../types/chat'
import type { UserSummary } from '../types/user'

const subtleBeep = () => {
  try {
    const audioContext = new window.AudioContext()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    oscillator.frequency.value = 740
    oscillator.type = 'sine'
    gainNode.gain.value = 0.03
    oscillator.start()
    oscillator.stop(audioContext.currentTime + 0.08)
  } catch {
    // Ignore audio failures.
  }
}

const resolveProfilePhotoUrl = (profilePhoto?: string) => {
  if (!profilePhoto || profilePhoto.trim().length === 0) {
    return 'https://placehold.co/48x48'
  }

  if (/^https?:\/\//i.test(profilePhoto)) {
    return profilePhoto
  }

  if (profilePhoto.startsWith('/api/media/')) {
    return `${API_BASE_URL}${profilePhoto}`
  }

  return `${API_BASE_URL}/api/media/${profilePhoto}`
}

const resolveAttachmentUrl = (attachmentUrl?: string) => {
  if (!attachmentUrl || attachmentUrl.trim().length === 0) {
    return undefined
  }

  if (attachmentUrl.startsWith('blob:')) {
    return attachmentUrl
  }

  if (attachmentUrl.startsWith('data:')) {
    return attachmentUrl
  }

  if (/^https?:\/\//i.test(attachmentUrl)) {
    return attachmentUrl
  }

  if (attachmentUrl.startsWith('/api/media/')) {
    return `${API_BASE_URL}${attachmentUrl}`
  }

  return `${API_BASE_URL}/api/media/${attachmentUrl}`
}

const isSharedPostMessage = (messageText?: string, attachmentType?: string) => {
  return attachmentType === 'shared-post' || Boolean(messageText?.startsWith('[SHARED_POST]'))
}

const parseSharedPostMessage = (messageText?: string) => {
  const lines = (messageText ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)

  const data = {
    sharedBy: '',
    author: '',
    caption: '',
    postId: '',
  }

  for (const line of lines) {
    if (line.startsWith('Shared by:')) {
      data.sharedBy = line.replace('Shared by:', '').trim()
    } else if (line.startsWith('Author:')) {
      data.author = line.replace('Author:', '').trim()
    } else if (line.startsWith('Caption:')) {
      data.caption = line.replace('Caption:', '').trim()
    } else if (line.startsWith('Post ID:')) {
      data.postId = line.replace('Post ID:', '').trim()
    }
  }

  return data
}

export default function MessagesPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const currentUserHandle = user?.handle || (user?.username ? `@${user.username}` : '@PESocial')
  const [selectedChatRoomId, setSelectedChatRoomId] = useState<string | null>(null)
  const [selectedOtherUser, setSelectedOtherUser] = useState<UserSummary | null>(null)
  const [messageText, setMessageText] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [attachmentPreview, setAttachmentPreview] = useState<string | null>(null)
  const [attachmentFile, setAttachmentFile] = useState<File | null>(null)
  const [localReactions, setLocalReactions] = useState<Record<string, string>>({})
  const [typing, setTyping] = useState(false)
  const [isInCall, setIsInCall] = useState(false)
  const [incomingCall, setIncomingCall] = useState<{ fromUserId: string; callType: 'audio' | 'video'; sdp?: RTCSessionDescriptionInit } | null>(null)
  const [activeCallType, setActiveCallType] = useState<'audio' | 'video'>('audio')
  const [callPartnerId, setCallPartnerId] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement | null>(null)
  const lastDirectMessageCount = useRef(0)
  const lastCallSignalCount = useRef(0)
  const peerConnectionRef = useRef<RTCPeerConnection | null>(null)
  const localStreamRef = useRef<MediaStream | null>(null)
  const remoteStreamRef = useRef<MediaStream | null>(null)
  const localVideoRef = useRef<HTMLVideoElement | null>(null)
  const remoteVideoRef = useRef<HTMLVideoElement | null>(null)

  const { connected, directMessages, callSignals, sendTyping, sendCallSignal } = useWebSockets({ enabled: true })

  const chatRoomsQuery = useQuery({
    queryKey: ['chat-rooms'],
    queryFn: () => chatService.getMyChatRooms(),
    enabled: Boolean(user),
  })

  const searchQuery = useQuery({
    queryKey: ['chat-search-users', searchTerm],
    queryFn: () => chatService.searchUsersForChat(searchTerm),
    enabled: searchTerm.trim().length >= 2,
  })

  const activeChatRoom = useMemo<ChatRoomSummary | undefined>(
    () => chatRoomsQuery.data?.find((room) => room.chatRoomId === selectedChatRoomId),
    [chatRoomsQuery.data, selectedChatRoomId],
  )

  const chatHistoryQuery = useQuery({
    queryKey: ['chat-history', selectedChatRoomId],
    queryFn: () => (selectedChatRoomId ? chatService.getChatHistory(selectedChatRoomId) : Promise.resolve([] as ChatMessage[])),
    enabled: Boolean(selectedChatRoomId),
  })

  const openConversationByHandle = async (handle: string) => {
    const room = await chatService.getOrCreateChatRoomByHandle(handle)
    setSelectedChatRoomId(room.chatRoomId)
    setSelectedOtherUser({
      id: room.otherUserId,
      handle: room.otherUserHandle,
      profilePhoto: room.otherUserProfilePhoto,
    })
    setSearchTerm('')
    void queryClient.invalidateQueries({ queryKey: ['chat-rooms'] })
    window.requestAnimationFrame(() => inputRef.current?.focus())
  }

  const sendMessageMutation = useMutation({
    mutationFn: async () => {
      if (!user || !selectedOtherUser) {
        throw new Error('Select a chat first')
      }

      let chatRoomId = selectedChatRoomId
      if (!chatRoomId) {
        const room = await chatService.createOrGetChatRoom(selectedOtherUser.id)
        chatRoomId = room.chatRoomId
        setSelectedChatRoomId(chatRoomId)
      }

      const payload = {
        senderId: user.id,
        receiverId: selectedOtherUser.id,
        messageText: messageText.trim(),
        chatRoomId,
        attachmentUrl: attachmentFile ? await chatService.uploadAttachment(attachmentFile) : undefined,
        attachmentType: attachmentFile?.type,
      }

      return chatService.sendMessage(payload)
    },
    onSuccess: async () => {
      setMessageText('')
      setAttachmentPreview(null)
      setAttachmentFile(null)
      await queryClient.invalidateQueries({ queryKey: ['chat-rooms'] })
      await queryClient.invalidateQueries({ queryKey: ['chat-history', selectedChatRoomId] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const displayedRooms = chatRoomsQuery.data ?? []
  const searchResults = searchQuery.data ?? []
  const history = chatHistoryQuery.data ?? []
  const mergedMessages = [...history]
  directMessages.forEach((message) => {
    if (message.chatRoomId === selectedChatRoomId) {
      mergedMessages.push(message as ChatMessage)
    }
  })

  useEffect(() => {
    if (directMessages.length > lastDirectMessageCount.current) {
      subtleBeep()
      void queryClient.invalidateQueries({ queryKey: ['chat-rooms'] })
      if (selectedChatRoomId) {
        void queryClient.invalidateQueries({ queryKey: ['chat-history', selectedChatRoomId] })
      }
    }
    lastDirectMessageCount.current = directMessages.length
  }, [directMessages.length, queryClient, selectedChatRoomId])

  useEffect(() => {
    const latestMessage = directMessages[0]
    if (!latestMessage || !user) {
      return
    }

    const knownRoom = displayedRooms.some((room) => room.chatRoomId === latestMessage.chatRoomId)
    const fromOtherUser = latestMessage.senderId !== user.id

    if (fromOtherUser && !knownRoom) {
      void queryClient.invalidateQueries({ queryKey: ['chat-rooms'] })
    }
  }, [directMessages, displayedRooms, queryClient, user])

  useEffect(() => {
    if (selectedChatRoomId) {
      void chatService.markAsRead(selectedChatRoomId)
      void queryClient.invalidateQueries({ queryKey: ['chat-rooms'] })
    }
  }, [queryClient, selectedChatRoomId])

  const cleanupCallResources = () => {
    if (peerConnectionRef.current) {
      peerConnectionRef.current.close()
      peerConnectionRef.current = null
    }
    if (localStreamRef.current) {
      localStreamRef.current.getTracks().forEach((track) => track.stop())
      localStreamRef.current = null
    }
    if (remoteStreamRef.current) {
      remoteStreamRef.current.getTracks().forEach((track) => track.stop())
      remoteStreamRef.current = null
    }
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null
    }
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = null
    }
  }

  const createPeerConnection = (partnerId: string, callType: 'audio' | 'video') => {
    const pc = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
    })

    pc.onicecandidate = (event) => {
      if (event.candidate && user) {
        sendCallSignal({
          type: 'ice-candidate',
          fromUserId: user.id,
          toUserId: partnerId,
          callType,
          candidate: event.candidate.toJSON(),
          chatRoomId: selectedChatRoomId ?? undefined,
        })
      }
    }

    pc.ontrack = (event) => {
      const incomingTrack = event.track
      let targetStream = remoteStreamRef.current

      if (!targetStream) {
        targetStream = new MediaStream()
        remoteStreamRef.current = targetStream
      }

      targetStream.addTrack(incomingTrack)
      console.log('ontrack: added track', { trackKind: incomingTrack.kind, streamId: targetStream.id })

      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = targetStream
        remoteVideoRef.current.load?.()
        void remoteVideoRef.current.play().catch((err) => console.warn('Remote video play failed:', err))
        console.log('ontrack: bound remote video', { videoRef: Boolean(remoteVideoRef.current), streamId: targetStream.id })
      }
    }

    peerConnectionRef.current = pc
    return pc
  }

  const startCall = async (callType: 'audio' | 'video') => {
    if (!user || !selectedOtherUser) {
      toast.error('Select a user to call')
      return
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: callType === 'video' ? { width: { ideal: 640 }, height: { ideal: 480 } } : false,
      })
      localStreamRef.current = stream
      console.log('startCall: got local stream', { callType, trackCount: stream.getTracks().length })

      if (localVideoRef.current && callType === 'video') {
        localVideoRef.current.srcObject = stream
        void localVideoRef.current.play().catch((err) => console.warn('Local video play failed:', err))
        console.log('startCall: bound local video', { videoRef: Boolean(localVideoRef.current), streamId: stream.id })
      }

      const pc = createPeerConnection(selectedOtherUser.id, callType)
      stream.getTracks().forEach((track) => pc.addTrack(track, stream))

      const offer = await pc.createOffer()
      await pc.setLocalDescription(offer)

      sendCallSignal({
        type: 'offer',
        fromUserId: user.id,
        toUserId: selectedOtherUser.id,
        callType,
        sdp: offer,
        chatRoomId: selectedChatRoomId ?? undefined,
      })

      setIsInCall(true)
      setActiveCallType(callType)
      setCallPartnerId(selectedOtherUser.id)
    } catch (error) {
      cleanupCallResources()
      console.error('startCall error:', error)
      toast.error(parseApiError(error))
    }
  }

  const acceptIncomingCall = async () => {
    if (!incomingCall || !user) return

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: incomingCall.callType === 'video' ? { width: { ideal: 640 }, height: { ideal: 480 } } : false,
      })
      localStreamRef.current = stream
      console.log('acceptIncomingCall: got local stream', { callType: incomingCall.callType, trackCount: stream.getTracks().length })

      if (localVideoRef.current && incomingCall.callType === 'video') {
        localVideoRef.current.srcObject = stream
        void localVideoRef.current.play().catch((err) => console.warn('Local video play failed:', err))
        console.log('acceptIncomingCall: bound local video', { videoRef: Boolean(localVideoRef.current), streamId: stream.id })
      }

      const pc = createPeerConnection(incomingCall.fromUserId, incomingCall.callType)
      stream.getTracks().forEach((track) => pc.addTrack(track, stream))

      if (incomingCall.sdp) {
        await pc.setRemoteDescription(new RTCSessionDescription(incomingCall.sdp))
      }

      const answer = await pc.createAnswer()
      await pc.setLocalDescription(answer)

      sendCallSignal({
        type: 'answer',
        fromUserId: user.id,
        toUserId: incomingCall.fromUserId,
        callType: incomingCall.callType,
        sdp: answer,
        chatRoomId: selectedChatRoomId ?? undefined,
      })

      setIsInCall(true)
      setActiveCallType(incomingCall.callType)
      setCallPartnerId(incomingCall.fromUserId)
      setIncomingCall(null)
    } catch (error) {
      cleanupCallResources()
      console.error('acceptIncomingCall error:', error)
      toast.error(parseApiError(error))
    }
  }

  const rejectIncomingCall = () => {
    if (!incomingCall || !user) return
    sendCallSignal({
      type: 'call-reject',
      fromUserId: user.id,
      toUserId: incomingCall.fromUserId,
      callType: incomingCall.callType,
      chatRoomId: selectedChatRoomId ?? undefined,
    })
    setIncomingCall(null)
  }

  const endCall = (notify = true) => {
    if (notify && user && callPartnerId) {
      sendCallSignal({
        type: 'call-end',
        fromUserId: user.id,
        toUserId: callPartnerId,
        callType: activeCallType,
        chatRoomId: selectedChatRoomId ?? undefined,
      })
    }
    cleanupCallResources()
    setIsInCall(false)
    setCallPartnerId(null)
    setIncomingCall(null)
  }

  useEffect(() => {
    if (!user || callSignals.length === 0) return
    if (callSignals.length === lastCallSignalCount.current) return

    const latest = callSignals[0]
    lastCallSignalCount.current = callSignals.length

    if (!latest) return
    console.log('callSignal handler: processing', { type: latest.type, fromUserId: latest.fromUserId, toUserId: latest.toUserId, userIdMatches: latest.toUserId === user.id || !latest.toUserId })
    
    if (latest.fromUserId === user.id) {
      console.log('callSignal: ignoring self-originated signal')
      return
    }
    
    if (latest.toUserId && latest.toUserId !== user.id) {
      console.log('callSignal: toUserId mismatch, ignoring', { toUserId: latest.toUserId, myId: user.id })
      return
    }

    const pc = peerConnectionRef.current

    if (latest.type === 'offer') {
      console.log('callSignal: incoming offer, showing accept UI')
      setIncomingCall({
        fromUserId: latest.fromUserId,
        callType: latest.callType || 'audio',
        sdp: latest.sdp,
      })
      return
    }

    if (latest.type === 'answer' && pc && latest.sdp) {
      console.log('callSignal: setting remote description (answer)', { sdpType: latest.sdp.type })
      void pc.setRemoteDescription(new RTCSessionDescription(latest.sdp)).then(() => {
        console.log('callSignal: remote description set successfully')
      }).catch((err) => {
        console.error('callSignal: failed to set remote description:', err)
      })
      return
    }

    if (latest.type === 'ice-candidate' && pc && latest.candidate) {
      console.log('callSignal: adding ICE candidate', { candidateType: latest.candidate.candidate?.split(' ')[7] || 'unknown' })
      void pc.addIceCandidate(new RTCIceCandidate(latest.candidate)).catch((err) => {
        console.warn('callSignal: failed to add ICE candidate:', err)
      })
      return
    }

    if (latest.type === 'call-end') {
      console.log('callSignal: remote ended call')
      endCall(false)
      toast('Call ended')
      return
    }

    if (latest.type === 'call-reject') {
      console.log('callSignal: call rejected')
      endCall(false)
      toast.error('Call rejected')
    }
  }, [callSignals, user])

  useEffect(() => {
    return () => {
      cleanupCallResources()
    }
  }, [])

  useEffect(() => {
    if (!isInCall) {
      return
    }

    if (localVideoRef.current && localStreamRef.current) {
      localVideoRef.current.srcObject = localStreamRef.current
      void localVideoRef.current.play().catch(() => undefined)
    }

    if (remoteVideoRef.current && remoteStreamRef.current) {
      remoteVideoRef.current.srcObject = remoteStreamRef.current
      void remoteVideoRef.current.play().catch(() => undefined)
    }
  }, [isInCall, activeCallType])

  const handlePickUser = async (userSummary: UserSummary) => {
    setSelectedOtherUser(userSummary)
    await openConversationByHandle(userSummary.handle)
  }

  const handleAttach = (file: File) => {
    setAttachmentFile(file)
    setAttachmentPreview(URL.createObjectURL(file))
  }

  const currentThreadUser = selectedOtherUser || (activeChatRoom ? {
    id: activeChatRoom.otherUserId,
    handle: activeChatRoom.otherUserHandle,
    profilePhoto: activeChatRoom.otherUserProfilePhoto,
  } : null)

  return (
    <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col p-4 text-white">
      <div className="mb-4 flex items-center justify-between rounded-xl border border-white/20 bg-white/10 p-4 backdrop-blur">
        <div>
          <p className="text-xs uppercase tracking-widest text-white/60">Messaging Hub</p>
          <h1 className="text-2xl font-semibold">PESocial Direct</h1>
          <p className="text-sm text-white/65">{connected ? 'Connected' : 'Disconnected'} · {currentUserHandle}</p>
        </div>
        <div className="w-full max-w-md">
          <input
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search users for chat"
            className="w-full rounded-lg border border-white/20 bg-slate-950/50 px-4 py-2 text-sm outline-none focus:border-sky-400"
          />
          {searchTerm.trim().length >= 2 && (
            <div className="mt-2 max-h-56 overflow-y-auto rounded-lg border border-white/10 bg-slate-950/95 p-1 shadow-2xl">
              {searchQuery.isLoading && <div className="px-3 py-2 text-xs text-white/70">Searching...</div>}
              {searchQuery.isError && <div className="px-3 py-2 text-xs text-rose-300">{parseApiError(searchQuery.error)}</div>}
              {!searchQuery.isLoading && !searchQuery.isError && searchResults.length === 0 && (
                <div className="px-3 py-2 text-xs text-white/70">No users found</div>
              )}
              {searchResults.map((candidate) => (
                <button
                  key={candidate.id}
                  onClick={() => void handlePickUser(candidate)}
                  className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-left hover:bg-white/10"
                >
                  <img
                    src={resolveProfilePhotoUrl(candidate.profilePhoto)}
                    alt={candidate.handle}
                    className="h-9 w-9 rounded-full object-cover"
                  />
                  <div>
                    <div className="font-medium">{candidate.handle}</div>
                    <div className="text-xs text-white/60">Tap to open chat</div>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="grid min-h-[72vh] grid-cols-1 overflow-hidden rounded-2xl border border-white/20 bg-slate-950/70 shadow-2xl lg:grid-cols-[340px_1fr]">
        <aside className="border-b border-white/10 bg-slate-900/80 p-3 lg:border-b-0 lg:border-r">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-widest text-white/60">Conversations</h2>
            <span className="rounded-full bg-blue-500/20 px-2 py-1 text-xs text-blue-200">Live</span>
          </div>

          <div className="space-y-2 overflow-y-auto pr-1">
            {displayedRooms.length === 0 && <div className="rounded-lg border border-white/10 bg-white/5 p-3 text-sm text-white/70">No conversations yet.</div>}
            {displayedRooms.map((room) => {
              const unread = room.unreadCount ?? 0
              const isActive = room.chatRoomId === selectedChatRoomId
              return (
                <button
                  key={room.chatRoomId}
                  onClick={() => {
                    setSelectedChatRoomId(room.chatRoomId)
                    setSelectedOtherUser({
                      id: room.otherUserId,
                      handle: room.otherUserHandle,
                      profilePhoto: room.otherUserProfilePhoto,
                    })
                  }}
                  className={`flex w-full items-center gap-3 rounded-xl border p-3 text-left transition ${isActive ? 'border-sky-400/60 bg-sky-500/10' : 'border-white/10 bg-white/5 hover:bg-white/10'}`}
                >
                  <div className="relative">
                    <img
                      src={resolveProfilePhotoUrl(room.otherUserProfilePhoto)}
                      alt={room.otherUserHandle}
                      className="h-12 w-12 rounded-full object-cover"
                    />
                    {unread > 0 && <span className="absolute -right-1 -top-1 h-3 w-3 rounded-full bg-blue-500 ring-2 ring-slate-950" />}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <p className="truncate font-semibold">{room.otherUserHandle}</p>
                      {room.pinned && <span className="text-xs text-yellow-300">📌</span>}
                    </div>
                    <p className="truncate text-sm text-white/65">{room.lastMessageText || 'No messages yet'}</p>
                  </div>
                </button>
              )
            })}
          </div>
        </aside>

        <main className="flex min-h-[60vh] flex-col">
          <div className="flex items-center justify-between border-b border-white/10 p-4">
            <div className="flex items-center gap-3">
              <img
                src={resolveProfilePhotoUrl(currentThreadUser?.profilePhoto)}
                alt={currentThreadUser?.handle || 'Chat user'}
                className="h-12 w-12 rounded-full object-cover"
              />
              <div>
                <h2 className="text-lg font-semibold">{currentThreadUser?.handle || 'Select a conversation'}</h2>
                <p className="text-xs text-white/60">{typing ? 'Typing...' : 'Active chat thread'}</p>
              </div>
            </div>
            <div className="flex items-center gap-2 text-xl">
              <button
                onClick={() => void startCall('audio')}
                disabled={!selectedOtherUser || isInCall}
                className="rounded-full border border-white/10 bg-white/5 px-3 py-2 hover:bg-white/10 disabled:opacity-40"
              >
                📞
              </button>
              <button
                onClick={() => void startCall('video')}
                disabled={!selectedOtherUser || isInCall}
                className="rounded-full border border-white/10 bg-white/5 px-3 py-2 hover:bg-white/10 disabled:opacity-40"
              >
                📹
              </button>
            </div>
          </div>

          {incomingCall && (
            <div className="mx-4 mt-4 rounded-xl border border-emerald-300/40 bg-emerald-500/10 p-3">
              <p className="text-sm font-medium text-emerald-100">
                Incoming {incomingCall.callType} call
              </p>
              <div className="mt-2 flex gap-2">
                <button onClick={() => void acceptIncomingCall()} className="rounded-md bg-emerald-500 px-3 py-1 text-sm text-white">Accept</button>
                <button onClick={rejectIncomingCall} className="rounded-md bg-rose-500 px-3 py-1 text-sm text-white">Reject</button>
              </div>
            </div>
          )}

          {isInCall && (
            <div className="mx-4 mt-4 rounded-xl border border-cyan-300/40 bg-cyan-500/10 p-3">
              <div className="mb-2 flex items-center justify-between">
                <p className="text-sm font-medium text-cyan-100">{activeCallType === 'video' ? 'Video call' : 'Audio call'} in progress</p>
                <button onClick={() => endCall(true)} className="rounded-md bg-rose-500 px-3 py-1 text-sm text-white">End</button>
              </div>
              {activeCallType === 'video' ? (
                <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                  <video
                    ref={localVideoRef}
                    autoPlay
                    playsInline
                    muted
                    className="h-64 w-full rounded-lg bg-black object-cover"
                  />
                  <video
                    ref={remoteVideoRef}
                    autoPlay
                    playsInline
                    className="h-64 w-full rounded-lg bg-black object-cover"
                  />
                </div>
              ) : (
                <p className="text-sm text-white/80">Audio connected. Keep this tab open.</p>
              )}
            </div>
          )}

          <div className="flex-1 space-y-3 overflow-y-auto p-4">
            {mergedMessages.length === 0 && (
              <div className="rounded-xl border border-dashed border-white/15 bg-white/5 p-6 text-center text-sm text-white/60">
                <div className="mb-2 text-3xl">◉</div>
                Start your first conversation from PESocial Direct.
              </div>
            )}

            {mergedMessages.map((message) => {
              const mine = message.senderId === user?.id
              const reaction = localReactions[message.messageId || message.id || message.createdAt || ''] ?? message.reaction
              return (
                <div
                  key={message.messageId || message.id || `${message.senderId}-${message.createdAt}`}
                  onDoubleClick={() => {
                    const key = message.messageId || message.id || message.createdAt || ''
                    setLocalReactions((prev) => ({ ...prev, [key]: prev[key] === '❤️' ? '' : '❤️' }))
                  }}
                  className={`flex ${mine ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-[70%] rounded-2xl px-4 py-3 ${mine ? 'bg-sky-500 text-white' : 'bg-white/10 text-white'}`}>
                    {isSharedPostMessage(message.messageText, message.attachmentType) ? (
                      (() => {
                        const sharedPost = parseSharedPostMessage(message.messageText)
                        return (
                          <div className="rounded-xl border border-white/15 bg-black/20 p-3">
                            <div className="mb-2 flex items-center gap-2 text-xs uppercase tracking-widest text-cyan-200">
                              <span>📎</span>
                              <span>Shared post</span>
                            </div>

                            {message.attachmentUrl ? (
                              <img
                                src={resolveAttachmentUrl(message.attachmentUrl)}
                                alt="shared post media"
                                className="mb-3 max-h-64 w-full rounded-xl object-cover"
                                onError={(event) => {
                                  const target = event.currentTarget
                                  target.style.display = 'none'
                                }}
                              />
                            ) : null}

                            <p className="text-sm font-semibold">{sharedPost.author || 'Shared post'}</p>
                            <p className="mt-1 whitespace-pre-line text-sm leading-relaxed text-white/90">{sharedPost.caption || 'No caption'}</p>
                            <p className="mt-2 text-xs text-white/60">Post ID: {sharedPost.postId || 'unknown'}</p>
                            {sharedPost.sharedBy && <p className="mt-1 text-xs text-white/60">Shared by: {sharedPost.sharedBy}</p>}
                          </div>
                        )
                      })()
                    ) : (
                      <>
                        {message.attachmentUrl && (
                          <img src={resolveAttachmentUrl(message.attachmentUrl)} alt="attachment" className="mb-2 max-h-64 rounded-xl object-cover" />
                        )}
                        <p className="text-sm leading-relaxed whitespace-pre-line">{message.messageText || message.content}</p>
                      </>
                    )}
                    {reaction && <div className="mt-2 text-right text-lg">{reaction}</div>}
                  </div>
                </div>
              )
            })}
          </div>

          <div className="border-t border-white/10 p-4">
            {attachmentPreview && (
              <div className="mb-3 flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 p-2">
                <img src={attachmentPreview} alt="attachment preview" className="h-14 w-14 rounded-lg object-cover" />
                <button
                  onClick={() => {
                    setAttachmentPreview(null)
                    setAttachmentFile(null)
                  }}
                  className="text-sm text-rose-200 hover:text-rose-100"
                >
                  Remove attachment
                </button>
              </div>
            )}

            <div className="flex items-end gap-3 rounded-2xl border border-white/10 bg-white/5 p-3">
              <label className="cursor-pointer rounded-full border border-white/10 bg-white/10 px-3 py-2 text-lg hover:bg-white/15">
                📎
                <input
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={(event) => {
                    const file = event.target.files?.[0]
                    if (file) {
                      handleAttach(file)
                    }
                  }}
                />
              </label>

              <input
                ref={inputRef}
                value={messageText}
                onChange={(event) => {
                  setMessageText(event.target.value)
                  setTyping(event.target.value.trim().length > 0)
                  if (selectedOtherUser && user) {
                    sendTyping({ toUserId: selectedOtherUser.id, fromUserId: user.id })
                  }
                }}
                placeholder="Type a message..."
                className="min-h-12 flex-1 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 outline-none focus:border-sky-400"
              />

              <button
                onClick={() => void sendMessageMutation.mutateAsync()}
                disabled={!selectedOtherUser || sendMessageMutation.isPending || messageText.trim().length === 0}
                className="rounded-xl bg-sky-500 px-4 py-3 font-semibold text-white hover:bg-sky-400 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Send
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
