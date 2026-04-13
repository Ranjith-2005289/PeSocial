import { useEffect, useMemo, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import { API_BASE_URL } from '../services/api'
import { useAuth } from './useAuth'
import type { Message } from '../types/message'

export interface RealtimeNotification {
  id?: string
  recipientId?: string
  senderHandle?: string
  type?: 'FOLLOW' | 'MESSAGE' | 'LIKE' | 'COMMENT'
  timestamp?: string
  message?: string
  commentText?: string
}

export interface CallSignal {
  type: 'offer' | 'answer' | 'ice-candidate' | 'call-end' | 'call-reject'
  fromUserId: string
  toUserId: string
  callType?: 'audio' | 'video'
  sdp?: RTCSessionDescriptionInit
  candidate?: RTCIceCandidateInit
  chatRoomId?: string
}

interface UseWebSocketsOptions {
  enabled?: boolean
}

const playSubtleMessageSound = () => {
  try {
    const audioContext = new window.AudioContext()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()

    oscillator.type = 'sine'
    oscillator.frequency.value = 660
    gainNode.gain.value = 0.02

    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    oscillator.start()
    oscillator.stop(audioContext.currentTime + 0.07)
  } catch {
    // Ignore audio failures in browsers that block autoplay.
  }
}

export const useWebSockets = ({ enabled = true }: UseWebSocketsOptions = {}) => {
  const { isAuthenticated, accessToken, user } = useAuth()
  const [connected, setConnected] = useState(false)
  const [notifications, setNotifications] = useState<RealtimeNotification[]>([])
  const [directMessages, setDirectMessages] = useState<Message[]>([])
  const [callSignals, setCallSignals] = useState<CallSignal[]>([])
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!enabled || !isAuthenticated || !accessToken) {
      return
    }

    let mounted = true
    let localClient: Client | null = null

    const setup = async () => {
      try {
        const wsBaseUrl = API_BASE_URL.replace(/^http/i, 'ws')

        if (!mounted) {
          return
        }

        const client = new Client({
          brokerURL: `${wsBaseUrl}/ws`,
          connectHeaders: {
            Authorization: `Bearer ${accessToken}`,
          },
          reconnectDelay: 5000,
          onConnect: () => {
            setConnected(true)

            client.subscribe('/user/queue/notifications', (frame) => {
              try {
                const payload = JSON.parse(frame.body) as RealtimeNotification
                setNotifications((prev) => [payload, ...prev].slice(0, 30))
              } catch {
                setNotifications((prev) => [{ message: frame.body }, ...prev].slice(0, 30))
              }
            })

            client.subscribe('/topic/notifications', (frame) => {
              try {
                const payload = JSON.parse(frame.body) as RealtimeNotification
                setNotifications((prev) => [payload, ...prev].slice(0, 30))
              } catch {
                setNotifications((prev) => [{ message: frame.body }, ...prev].slice(0, 30))
              }
            })

            client.subscribe('/user/queue/messages', (frame) => {
              try {
                const payload = JSON.parse(frame.body) as Message
                playSubtleMessageSound()
                setDirectMessages((prev) => [payload, ...prev].slice(0, 50))
              } catch {
                // Ignore malformed payloads.
              }
            })

            client.subscribe('/user/queue/calls', (frame) => {
              try {
                const payload = JSON.parse(frame.body) as CallSignal
                setCallSignals((prev) => [payload, ...prev].slice(0, 30))
              } catch {
                // Ignore malformed payloads.
              }
            })

            if (user?.id) {
              client.subscribe(`/topic/calls/${user.id}`, (frame) => {
                try {
                  const payload = JSON.parse(frame.body) as CallSignal
                  setCallSignals((prev) => [payload, ...prev].slice(0, 30))
                } catch {
                  // Ignore malformed payloads.
                }
              })
            }
          },
          onDisconnect: () => {
            setConnected(false)
          },
          onStompError: () => {
            setConnected(false)
          },
          onWebSocketClose: () => {
            setConnected(false)
          },
          onWebSocketError: () => {
            setConnected(false)
          },
        })

        // Fallback to SockJS endpoint when native websocket fails.
        client.onWebSocketError = async () => {
          setConnected(false)
          try {
            const sockJsModule = await import('sockjs-client')
            const SockJS = sockJsModule.default
            client.deactivate()
            const fallbackClient = new Client({
              webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws-sockjs`),
              connectHeaders: {
                Authorization: `Bearer ${accessToken}`,
              },
              reconnectDelay: 5000,
              onConnect: client.onConnect,
              onDisconnect: client.onDisconnect,
              onStompError: client.onStompError,
              onWebSocketClose: client.onWebSocketClose,
            })
            localClient = fallbackClient
            clientRef.current = fallbackClient
            fallbackClient.activate()
          } catch {
            setConnected(false)
          }
        }

        localClient = client
        clientRef.current = client
        client.activate()
      } catch {
        setConnected(false)
      }
    }

    void setup()

    return () => {
      mounted = false
      if (localClient) {
        void localClient.deactivate()
      }
      clientRef.current = null
      setConnected(false)
    }
  }, [enabled, isAuthenticated, accessToken, user?.id])

  const actions = useMemo(
    () => ({
      sendDirectMessage: (message: Message) => {
        clientRef.current?.publish({
          destination: '/app/dm',
          body: JSON.stringify(message),
        })
      },
      sendDirectChatMessage: (payload: {
        senderId: string
        receiverId: string
        messageText: string
        chatRoomId?: string
        attachmentUrl?: string
        attachmentType?: string
      }) => {
        clientRef.current?.publish({
          destination: '/app/dm',
          body: JSON.stringify(payload),
        })
      },
      sendTyping: (payload: { toUserId: string; fromUserId: string }) => {
        clientRef.current?.publish({
          destination: '/app/typing',
          body: JSON.stringify(payload),
        })
      },
      sendSeen: (payload: { chatRoomId: string; messageId: string; senderId: string; receiverId: string }) => {
        clientRef.current?.publish({
          destination: '/app/seen',
          body: JSON.stringify(payload),
        })
      },
      sendCallSignal: (payload: CallSignal) => {
        clientRef.current?.publish({
          destination: '/app/call-signal',
          body: JSON.stringify(payload),
        })
      },
      sendNotification: (payload: RealtimeNotification) => {
        clientRef.current?.publish({
          destination: '/app/notify',
          body: JSON.stringify(payload),
        })
      },
    }),
    [],
  )

  return {
    connected,
    notifications,
    directMessages,
    callSignals,
    ...actions,
  }
}
