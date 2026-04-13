import { useEffect, useRef, useState } from 'react'
import toast from 'react-hot-toast'
import { useAuth } from '../hooks/useAuth'
import { storyService } from '../services/storyService'
import { chatService } from '../services/chatService'
import { API_BASE_URL } from '../services/api'
import type { StoryDetailDTO, StoryBarUser } from '../types/story'

interface StoryViewerModalProps {
  open: boolean
  currentUser: StoryBarUser | null
  stories: StoryDetailDTO[]
  onClose: () => void
}

export default function StoryViewerModal({ open, currentUser, stories, onClose }: StoryViewerModalProps) {
  const { user } = useAuth()
  const [currentIndex, setCurrentIndex] = useState(0)
  const [progress, setProgress] = useState(0)
  const [liked, setLiked] = useState(false)
  const [replyText, setReplyText] = useState('')
  const [showViewers, setShowViewers] = useState(false)
  const progressInterval = useRef<ReturnType<typeof setInterval> | null>(null)

  const currentStory = stories[currentIndex]
  const isAuthor = currentStory?.authorId === user?.id

  useEffect(() => {
    if (!open) return

    // Mark story as viewed
    if (currentStory && !currentStory.viewed) {
      void storyService.viewStory(currentStory.id)
    }

    // Setup progress bar
    setProgress(0)
    progressInterval.current = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          handleNextStory()
          return 0
        }
        return prev + (100 / 50) // 5 second duration
      })
    }, 100)

    setLiked(currentStory?.likes?.includes(user?.id || '') || false)

    return () => {
      if (progressInterval.current) {
        clearInterval(progressInterval.current)
      }
    }
  }, [open, currentIndex, currentStory, user?.id])

  const handleNextStory = () => {
    if (currentIndex < stories.length - 1) {
      setCurrentIndex(currentIndex + 1)
    } else {
      onClose()
    }
  }

  const handlePrevStory = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1)
    }
  }

  const handleLike = async () => {
    if (!currentStory || !user) return

    try {
      if (liked) {
        await storyService.unlikeStory(currentStory.id)
      } else {
        await storyService.likeStory(currentStory.id)
      }
      setLiked(!liked)
    } catch (error) {
      console.error('Like failed:', error)
      toast.error('Failed to update like')
    }
  }

  const handleSendReply = async () => {
    if (!replyText.trim() || !currentStory || !user) return

    try {
      const message = `Replied to your story: ${replyText}`
      await chatService.sendMessage({
        senderId: user.id,
        receiverId: currentStory.authorId,
        messageText: message,
      })
      toast.success('Reply sent!')
      setReplyText('')
    } catch (error) {
      console.error('Reply failed:', error)
      toast.error('Failed to send reply')
    }
  }

  if (!open || !currentStory) return null

  const resolveMediaUrl = (url?: string) => {
    if (!url) return ''
    if (/^https?:\/\//i.test(url)) return url
    if (url.startsWith('/api/media/')) return `${API_BASE_URL}${url}`
    return `${API_BASE_URL}/api/media/${url}`
  }

  return (
    <div className="fixed inset-0 z-50 bg-black flex items-center justify-center">
      {/* Progress bar */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-white/20">
        <div
          className="h-full bg-white transition-all duration-100"
          style={{ width: `${progress}%` }}
        />
      </div>

      {/* Story image/video */}
      <div className="relative w-full h-full flex items-center justify-center">
        {currentStory.mediaUrl.endsWith('.mp4') || currentStory.mediaUrl.includes('video') ? (
          <video
            src={resolveMediaUrl(currentStory.mediaUrl)}
            autoPlay
            className="max-w-full max-h-full object-contain"
          />
        ) : (
          <img
            src={resolveMediaUrl(currentStory.mediaUrl)}
            alt="Story"
            className="max-w-full max-h-full object-contain"
          />
        )}
      </div>

      {/* Navigation */}
      <button
        onClick={handlePrevStory}
        className="absolute left-4 top-1/2 -translate-y-1/2 text-white text-2xl hover:scale-110"
      >
        ◀
      </button>
      <button
        onClick={handleNextStory}
        className="absolute right-4 top-1/2 -translate-y-1/2 text-white text-2xl hover:scale-110"
      >
        ▶
      </button>

      {/* Close button */}
      <button
        onClick={onClose}
        className="absolute top-4 right-4 text-white text-2xl hover:scale-110"
      >
        ✕
      </button>

      {/* Author info */}
      <div className="absolute top-8 left-4 flex items-center gap-2">
        <div className="w-10 h-10 rounded-full bg-white/20" />
        <p className="text-white font-semibold">{currentUser?.handle}</p>
      </div>

      {/* Bottom actions */}
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black to-transparent p-4 space-y-3">
        {isAuthor && (
          <button
            onClick={() => setShowViewers(!showViewers)}
            className="w-full rounded-lg bg-white/10 px-4 py-2 text-white text-sm hover:bg-white/20"
          >
            👁 {currentStory.viewerCount} views
          </button>
        )}

        {showViewers && isAuthor && (
          <div className="bg-slate-900/80 rounded-lg p-3 max-h-40 overflow-y-auto">
            <p className="text-sm text-white/80 mb-2">Viewed by:</p>
            {currentStory.viewers?.length ? (
              currentStory.viewers.map((viewerId) => (
                <p key={viewerId} className="text-sm text-white">
                  {viewerId}
                </p>
              ))
            ) : (
              <p className="text-sm text-white/60">No views yet</p>
            )}
          </div>
        )}

        {!isAuthor && (
          <>
            <div className="flex gap-2">
              <input
                type="text"
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                placeholder="Send message to author..."
                className="flex-1 rounded-full bg-white/20 px-4 py-2 text-white placeholder-white/50 outline-none"
              />
              <button
                onClick={handleSendReply}
                disabled={!replyText.trim()}
                className="rounded-full bg-sky-500 px-4 py-2 text-white hover:bg-sky-400 disabled:opacity-50"
              >
                Send
              </button>
            </div>
            <button
              onClick={handleLike}
              className={`w-full rounded-lg px-4 py-2 text-sm font-semibold ${
                liked
                  ? 'bg-red-500 text-white hover:bg-red-400'
                  : 'bg-white/10 text-white hover:bg-white/20'
              }`}
            >
              {liked ? '❤ Liked' : '🤍 Like'}
            </button>
          </>
        )}
      </div>
    </div>
  )
}
