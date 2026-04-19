import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import CreatePost from '../components/CreatePost'
import NotificationDropdown from '../components/NotificationDropdown'
import PostCard from '../components/PostCard'
import SharePostModal from '../components/SharePostModal'
import UserSearchBar from '../components/UserSearchBar'
import StoryBar from '../components/StoryBar'
import StoryViewerModal from '../components/StoryViewerModal'
import { useAuth } from '../hooks/useAuth'
import { useWebSockets } from '../hooks/useWebSockets'
import { chatService } from '../services/chatService'
import { parseApiError } from '../services/api'
import { notificationService } from '../services/notificationService'
import { postService } from '../services/postService'
import { userService } from '../services/userService'
import type { CreatePostPayload } from '../types/post'
import type { StoryDetailDTO, StoryBarUser } from '../types/story'
import type { UserSummary } from '../types/user'
import toast from 'react-hot-toast'

export default function FeedPage() {
  const { user, logout, applyAuthResponse } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showNotifications, setShowNotifications] = useState(false)
  const [selectedStoryUser, setSelectedStoryUser] = useState<StoryBarUser | null>(null)
  const [selectedStories, setSelectedStories] = useState<StoryDetailDTO[]>([])
  const [storyModalOpen, setStoryModalOpen] = useState(false)
  const [shareModalOpen, setShareModalOpen] = useState(false)
  const [postToShare, setPostToShare] = useState<import('../types/post').Post | null>(null)

  const { connected, notifications } = useWebSockets({ enabled: true })

  const notificationsQuery = useQuery({
    queryKey: ['notifications', user?.id],
    queryFn: () => notificationService.getNotifications(user?.id || ''),
    enabled: Boolean(user?.id),
  })

  const unreadCountQuery = useQuery({
    queryKey: ['notifications-unread', user?.id],
    queryFn: () => notificationService.countUnread(user?.id || ''),
    enabled: Boolean(user?.id),
  })

  const feedQuery = useQuery({
    queryKey: ['feed-posts'],
    queryFn: () => postService.getFeedPosts(),
    enabled: Boolean(user),
  })


  const createPostMutation = useMutation({
    mutationFn: (payload: CreatePostPayload) => postService.createPost(payload),
    onSuccess: () => {
      toast.success('Post created')
      void queryClient.invalidateQueries({ queryKey: ['feed-posts'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const likeMutation = useMutation({
    mutationFn: (postId: string) => postService.likePost(postId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['feed-posts'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const commentMutation = useMutation({
    mutationFn: ({ postId, value }: { postId: string; value: string }) => postService.commentPost(postId, value),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['feed-posts'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const deleteMutation = useMutation({
    mutationFn: (postId: string) => postService.deletePost(postId),
    onSuccess: () => {
      toast.success('Post deleted')
      void queryClient.invalidateQueries({ queryKey: ['feed-posts'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const shareToUserMutation = useMutation({
    mutationFn: async ({ targetUser, post }: { targetUser: UserSummary; post: import('../types/post').Post }) => {
      if (!user) {
        throw new Error('Login required')
      }

      const room = await chatService.createOrGetChatRoom(targetUser.id)

      const senderName = user.handle || user.username || 'A user'
      const authorName = post.authorName || post.authorId
      const caption = post.contentText?.trim() ? post.contentText.trim() : 'No caption'
      const messageText = [
        '[SHARED_POST]',
        `Shared by: ${senderName}`,
        `Author: ${authorName}`,
        `Caption: ${caption}`,
        `Post ID: ${post.id}`,
      ].join('\n')

      const attachmentUrl = post.mediaId ? `/api/media/${post.mediaId}` : post.media?.externalUrl

      await chatService.sendMessage({
        senderId: user.id,
        receiverId: targetUser.id,
        chatRoomId: room.chatRoomId,
        messageText,
        attachmentUrl,
        attachmentType: 'shared-post',
      })

      await postService.sharePost(post.id)
    },
    onSuccess: () => {
      toast.success('Post shared in chat')
      setShareModalOpen(false)
      setPostToShare(null)
      void queryClient.invalidateQueries({ queryKey: ['feed-posts'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const becomeCreatorMutation = useMutation({
    mutationFn: () => userService.becomeCreator(),
    onSuccess: (authResponse) => {
      applyAuthResponse(authResponse, user)
      toast.success('You are now a Creator')
      void navigate('/analytics')
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const followBackMutation = useMutation({
    mutationFn: (handle: string) => userService.followHandle(handle),
    onSuccess: () => {
      toast.success('Followed back')
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationService.markAllAsRead(user?.id || ''),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications', user?.id] })
      void queryClient.invalidateQueries({ queryKey: ['notifications-unread', user?.id] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  useEffect(() => {
    if (notifications.length === 0 || !user?.id) {
      return
    }

    void queryClient.invalidateQueries({ queryKey: ['notifications', user.id] })
    void queryClient.invalidateQueries({ queryKey: ['notifications-unread', user.id] })
  }, [notifications, queryClient, user?.id])

  const displayNotifications = useMemo(() => {
    if (Array.isArray(notificationsQuery.data) && notificationsQuery.data.length > 0) {
      return notificationsQuery.data
    }
    return notifications
  }, [notificationsQuery.data, notifications])

  const unreadNotifications = unreadCountQuery.data ?? notifications.length

  const handleNotificationToggle = async () => {
    if (!showNotifications && unreadNotifications > 0) {
      // When opening dropdown, mark all as read
      await markAllReadMutation.mutateAsync()
    }
    setShowNotifications((prev) => !prev)
  }

  if (!user) {
    return null
  }

  return (
    <div className="mx-auto min-h-screen w-full max-w-5xl p-4 text-white">
      <header className="mb-6 rounded-xl border border-white/20 bg-white/10 p-4 backdrop-blur">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-xs uppercase tracking-widest text-white/70">PESocial Feed</p>
            <h1 className="text-2xl font-semibold">Welcome, {user.handle}</h1>
            <p className="text-sm text-white/70">Role: {user.role}</p>
          </div>

          <UserSearchBar />

          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <button
                onClick={() => void handleNotificationToggle()}
                disabled={markAllReadMutation.isPending}
                className="relative rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10 disabled:opacity-60"
              >
                🔔
                {unreadNotifications > 0 && (
                  <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-rose-500 px-1.5 py-0.5 text-[10px] font-semibold leading-none text-white">
                    {unreadNotifications}
                  </span>
                )}
              </button>

              {showNotifications && (
                <NotificationDropdown
                  notifications={displayNotifications}
                  followBackPending={followBackMutation.isPending}
                  onFollowBack={(handle) => followBackMutation.mutateAsync(handle)}
                />
              )}
            </div>

            <Link to="/my-profile" className="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10">
              My Profile
            </Link>
            <Link to="/messages" className="rounded-lg border border-cyan-300/40 px-3 py-2 text-sm hover:bg-cyan-500/10">
              Messages
            </Link>
            {user.role === 'REGULAR_USER' && (
              <button
                onClick={() => becomeCreatorMutation.mutate()}
                disabled={becomeCreatorMutation.isPending}
                className="rounded-lg border border-emerald-300/60 bg-emerald-500/15 px-3 py-2 text-sm hover:bg-emerald-500/25 disabled:opacity-60"
              >
                {becomeCreatorMutation.isPending ? 'Upgrading...' : 'Become a Creator'}
              </button>
            )}
            {user.role === 'CREATOR' && (
              <Link to="/analytics" className="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10">
                Analytics
              </Link>
            )}
            {user.role === 'ADMIN' && (
              <Link to="/admin" className="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10">
                Admin
              </Link>
            )}
            <button onClick={() => void logout()} className="rounded-lg bg-white px-3 py-2 text-sm font-medium text-black">
              Logout
            </button>
          </div>
        </div>

        <div className="mt-3 flex items-center gap-4 text-xs text-white/75">
          <span className={`h-2.5 w-2.5 rounded-full ${connected ? 'bg-emerald-400' : 'bg-rose-400'}`} />
          <span>Realtime: {connected ? 'Connected' : 'Disconnected'}</span>
          <span>Notifications: {unreadNotifications}</span>
        </div>
      </header>

      <CreatePost
        authorId={user.id}
        isSubmitting={createPostMutation.isPending}
        onCreate={(payload) => createPostMutation.mutateAsync(payload)}
      />

      <StoryBar onStorySelect={(storyUser, stories) => {
        setSelectedStoryUser(storyUser)
        setSelectedStories(stories)
        setStoryModalOpen(true)
      }} />

      <StoryViewerModal
        open={storyModalOpen}
        currentUser={selectedStoryUser}
        stories={selectedStories}
        onClose={() => setStoryModalOpen(false)}
      />

      <SharePostModal
        open={shareModalOpen}
        post={postToShare}
        currentUserId={user.id}
        sharing={shareToUserMutation.isPending}
        onClose={() => {
          if (!shareToUserMutation.isPending) {
            setShareModalOpen(false)
            setPostToShare(null)
          }
        }}
        onShare={(targetUser, post) => shareToUserMutation.mutateAsync({ targetUser, post })}
      />

      {feedQuery.isLoading && <div className="rounded-xl border border-white/20 bg-white/10 p-4 text-white">Loading feed...</div>}

      {feedQuery.isError && (
        <div className="rounded-xl border border-rose-300/40 bg-rose-500/10 p-4 text-rose-100">{parseApiError(feedQuery.error)}</div>
      )}

      {!feedQuery.isLoading && !feedQuery.isError && (feedQuery.data?.length ?? 0) === 0 && (
        <div className="rounded-xl border border-white/20 bg-white/10 p-4 text-white">No posts available.</div>
      )}

      {!feedQuery.isLoading && !feedQuery.isError && (feedQuery.data?.length ?? 0) > 0 && (
        <div className="space-y-4">
          {feedQuery.data?.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              currentUserId={user.id}
              currentUserRole={user.role}
              onLike={(postId) => likeMutation.mutateAsync(postId).then(() => undefined)}
              onComment={(postId, value) => commentMutation.mutateAsync({ postId, value }).then(() => undefined)}
              onDelete={(postId) => deleteMutation.mutateAsync(postId).then(() => undefined)}
              onShare={async (post) => {
                setPostToShare(post)
                setShareModalOpen(true)
              }}
            />
          ))}
        </div>
      )}
    </div>
  )
}
