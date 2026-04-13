import type { RealtimeNotification } from '../hooks/useWebSockets'

interface NotificationDropdownProps {
  notifications: RealtimeNotification[]
  onFollowBack: (handle: string) => Promise<void>
  followBackPending?: boolean
}

export default function NotificationDropdown({
  notifications,
  onFollowBack,
  followBackPending = false,
}: NotificationDropdownProps) {
  const getNotificationText = (notification: RealtimeNotification): string => {
    const senderHandle = notification.senderHandle || 'Someone'

    switch (notification.type) {
      case 'FOLLOW':
        return `👤 ${senderHandle} started following you`
      case 'LIKE':
        return `❤️ ${senderHandle} liked your post`
      case 'COMMENT':
        return `💬 ${senderHandle} commented: "${notification.commentText || 'commented on your post'}"`
      case 'MESSAGE':
        return `✉️ ${senderHandle} sent you a message`
      default:
        return `${senderHandle} sent a notification`
    }
  }

  if (notifications.length === 0) {
    return (
      <div className="absolute right-0 top-12 z-30 w-80 rounded-xl border border-white/20 bg-slate-900/95 p-3 shadow-2xl">
        <p className="text-sm text-white/75">No notifications yet.</p>
      </div>
    )
  }

  return (
    <div className="absolute right-0 top-12 z-30 w-80 overflow-hidden rounded-xl border border-white/20 bg-slate-900/95 shadow-2xl">
      <div className="max-h-80 overflow-y-auto p-2">
        {notifications.map((notification, index) => {
          const senderHandle = notification.senderHandle || 'Someone'
          const canFollowBack = notification.type === 'FOLLOW' && Boolean(notification.senderHandle)
          const text = getNotificationText(notification)
          const followHandle = notification.senderHandle ?? ''

          return (
            <div
              key={notification.id ?? `${senderHandle}-${notification.timestamp ?? index}-${index}`}
              className="mb-2 rounded-lg border border-white/10 bg-white/5 p-3 last:mb-0"
            >
              <p className="text-sm leading-relaxed text-white">{text}</p>
              {canFollowBack && followHandle && (
                <button
                  onClick={() => void onFollowBack(followHandle)}
                  disabled={followBackPending}
                  className="mt-2 rounded-md border border-indigo-300/60 bg-indigo-500/15 px-2 py-1 text-xs text-indigo-100 hover:bg-indigo-500/25 disabled:opacity-60"
                >
                  {followBackPending ? 'Following...' : 'Follow Back'}
                </button>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
