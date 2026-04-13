import type { Post } from '../types/post'
import type { UserRole } from '../types/user'

interface FeedProps {
  posts: Post[]
  loading: boolean
  error: string | null
  currentUserId: string
  currentUserRole: UserRole
  canAccessExclusivePost?: (post: Post) => boolean
}

const formatDate = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return 'Unknown date'
  }
  return date.toLocaleString()
}

export default function Feed({
  posts,
  loading,
  error,
  currentUserId,
  currentUserRole,
  canAccessExclusivePost,
}: FeedProps) {
  if (loading) {
    return <div className="rounded-xl border border-white/20 bg-white/10 p-4 text-white">Loading feed...</div>
  }

  if (error) {
    return <div className="rounded-xl border border-rose-300/40 bg-rose-500/10 p-4 text-rose-100">{error}</div>
  }

  if (posts.length === 0) {
    return <div className="rounded-xl border border-white/20 bg-white/10 p-4 text-white">No posts available.</div>
  }

  return (
    <div className="space-y-4">
      {posts.map((post) => {
        const isExclusive = post.visibility === 'EXCLUSIVE'
        const isAuthor = post.authorId === currentUserId
        const isAdmin = currentUserRole === 'ADMIN'
        const canAccessExclusive = isExclusive && (isAuthor || isAdmin || Boolean(canAccessExclusivePost?.(post)))
        const showPaywallOverlay = isExclusive && !canAccessExclusive

        return (
          <article key={post.id} className="relative overflow-hidden rounded-xl border border-white/20 bg-white/10 p-4 text-white">
            <header className="mb-3 flex items-center justify-between gap-2">
              <div>
                <p className="text-sm text-white/70">Author: {post.authorId}</p>
                <p className="text-xs text-white/60">{formatDate(post.createdAt)}</p>
              </div>

              <div className="flex items-center gap-2">
                <span className="rounded-full border border-white/30 px-3 py-1 text-xs">{post.visibility}</span>
                {isExclusive && (
                  <span className="rounded-full bg-amber-400 px-3 py-1 text-xs font-semibold text-slate-900">Exclusive</span>
                )}
              </div>
            </header>

            <p className={showPaywallOverlay ? 'blur-sm select-none' : ''}>{post.contentText || 'No text content'}</p>

            <footer className="mt-4 flex gap-4 text-xs text-white/75">
              <span>❤️ {post.likesCount}</span>
              <span>💬 {post.comments.length}</span>
              <span>🔁 {post.sharesCount}</span>
            </footer>

            {showPaywallOverlay && (
              <div className="absolute inset-0 flex items-center justify-center bg-slate-900/70 p-4 text-center">
                <div className="max-w-sm rounded-lg border border-amber-300/60 bg-amber-400/10 p-4">
                  <p className="text-sm font-semibold text-amber-100">Exclusive Post</p>
                  <p className="mt-1 text-xs text-amber-50/90">
                    Subscribe or purchase access to unlock this content.
                  </p>
                </div>
              </div>
            )}
          </article>
        )
      })}
    </div>
  )
}
