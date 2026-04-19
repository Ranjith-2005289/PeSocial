import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { chatService } from '../services/chatService'
import { parseApiError } from '../services/api'
import type { Post } from '../types/post'
import type { UserSummary } from '../types/user'

interface SharePostModalProps {
  open: boolean
  post: Post | null
  currentUserId: string
  sharing: boolean
  onClose: () => void
  onShare: (targetUser: UserSummary, post: Post) => Promise<void>
}

export default function SharePostModal({
  open,
  post,
  currentUserId,
  sharing,
  onClose,
  onShare,
}: SharePostModalProps) {
  const [searchTerm, setSearchTerm] = useState('')

  const query = useQuery({
    queryKey: ['share-post-user-search', searchTerm],
    queryFn: () => chatService.searchUsersForChat(searchTerm),
    enabled: open && searchTerm.trim().length >= 2,
  })

  const users = useMemo(
    () => (query.data ?? []).filter((user) => user.id !== currentUserId),
    [currentUserId, query.data],
  )

  if (!open || !post) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4" onClick={onClose}>
      <div
        className="w-full max-w-lg rounded-xl border border-white/20 bg-slate-900 p-4 text-white shadow-2xl"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-lg font-semibold">Share post</h3>
          <button className="rounded-md border border-white/30 px-2 py-1 text-xs hover:bg-white/10" onClick={onClose}>
            Close
          </button>
        </div>

        <p className="mb-3 line-clamp-2 text-sm text-white/75">
          {post.contentText?.trim() ? post.contentText : 'No caption'}
        </p>

        <input
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
          placeholder="Search users by handle or username"
          className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 text-sm outline-none focus:border-indigo-400"
        />

        <div className="mt-3 max-h-72 space-y-2 overflow-auto">
          {searchTerm.trim().length < 2 && <p className="text-sm text-white/65">Type at least 2 characters</p>}

          {query.isLoading && <p className="text-sm text-white/65">Searching users...</p>}

          {query.isError && (
            <p className="rounded-lg border border-rose-300/40 bg-rose-500/10 p-2 text-sm text-rose-100">
              {parseApiError(query.error)}
            </p>
          )}

          {!query.isLoading && searchTerm.trim().length >= 2 && users.length === 0 && (
            <p className="text-sm text-white/65">No users found</p>
          )}

          {users.map((user) => (
            <button
              key={user.id}
              onClick={() => void onShare(user, post)}
              disabled={sharing}
              className="flex w-full items-center justify-between rounded-lg border border-white/20 bg-white/5 px-3 py-2 text-left hover:bg-white/10 disabled:opacity-60"
            >
              <span className="truncate text-sm">{user.handle}</span>
              <span className="text-xs text-white/70">Send</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
