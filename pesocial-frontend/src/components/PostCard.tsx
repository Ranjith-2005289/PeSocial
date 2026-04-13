import { useState } from 'react'
import type { Post } from '../types/post'
import type { UserRole } from '../types/user'

interface PostCardProps {
  post: Post
  currentUserId: string
  currentUserRole: UserRole
  onLike: (postId: string) => Promise<void>
  onComment: (postId: string, value: string) => Promise<void>
  onDelete: (postId: string) => Promise<void>
}

const formatDate = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return 'Unknown date'
  }
  return date.toLocaleString()
}

export default function PostCard({ post, currentUserId, currentUserRole, onLike, onComment, onDelete }: PostCardProps) {
  const [commentText, setCommentText] = useState('')
  const [commenting, setCommenting] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const submitComment = async () => {
    if (!commentText.trim()) {
      return
    }
    setCommenting(true)
    try {
      await onComment(post.id, commentText.trim())
      setCommentText('')
    } finally {
      setCommenting(false)
    }
  }

  const isExclusive = post.visibility === 'EXCLUSIVE'
  const canDelete = post.authorId === currentUserId || currentUserRole === 'ADMIN'
  const mediaSrc = post.mediaId
    ? `http://localhost:8080/api/media/${post.mediaId}`
    : post.media?.externalUrl

  const deletePost = async () => {
    const confirmed = window.confirm('Are you sure you want to delete this post?')
    if (!confirmed) {
      return
    }

    setDeleting(true)
    try {
      await onDelete(post.id)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <article className="rounded-xl border border-white/20 bg-white/10 p-4 text-white">
      <header className="mb-3 flex items-center justify-between gap-2">
        <div>
          <p className="text-sm text-white/70">Author: {post.authorName || post.authorId}</p>
          <p className="text-xs text-white/60">{formatDate(post.createdAt)}</p>
        </div>

        <div className="flex items-center gap-2">
          <span className="rounded-full border border-white/30 px-3 py-1 text-xs">{post.visibility}</span>
          {isExclusive && <span className="rounded-full bg-amber-400 px-3 py-1 text-xs font-semibold text-slate-900">Exclusive</span>}
          {canDelete && (
            <button
              onClick={() => void deletePost()}
              disabled={deleting}
              className="rounded-md border border-rose-300/50 px-2 py-1 text-xs text-rose-200 hover:bg-rose-500/20 disabled:opacity-60"
              aria-label="Delete post"
              title="Delete post"
            >
              {deleting ? 'Deleting...' : '🗑 Delete'}
            </button>
          )}
        </div>
      </header>

      <p>{post.contentText || 'No text content'}</p>

      {mediaSrc && (
        <img
          src={mediaSrc}
          alt="Post media"
          className="mt-3 max-h-96 w-full rounded-lg border border-white/20 object-cover"
        />
      )}

      <footer className="mt-4 flex flex-wrap items-center gap-2">
        <button
          onClick={() => void onLike(post.id)}
          className="rounded-lg border border-white/30 px-3 py-1 text-sm hover:bg-white/10"
        >
          Like ({post.likesCount})
        </button>

        <input
          value={commentText}
          onChange={(event) => setCommentText(event.target.value)}
          placeholder="Write a comment"
          className="min-w-48 flex-1 rounded-lg border border-white/30 bg-black/20 px-3 py-1.5 text-sm outline-none focus:border-indigo-400"
        />

        <button
          onClick={() => void submitComment()}
          disabled={commenting || !commentText.trim()}
          className="rounded-lg border border-white/30 px-3 py-1 text-sm hover:bg-white/10 disabled:opacity-60"
        >
          {commenting ? 'Sending...' : `Comment (${post.comments.length})`}
        </button>
      </footer>
    </article>
  )
}
