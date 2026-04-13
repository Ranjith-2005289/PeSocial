import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { userService } from '../services/userService'
import { parseApiError } from '../services/api'

interface UserListModalProps {
  open: boolean
  title: string
  profileHandle?: string
  listType: 'followers' | 'following'
  isOwnProfile: boolean
  useMeEndpoint?: boolean
  onClose: () => void
}

export default function UserListModal({ open, title, profileHandle, listType, isOwnProfile, useMeEndpoint = false, onClose }: UserListModalProps) {
  const [page, setPage] = useState(0)
  const size = 10

  const query = useQuery({
    queryKey: ['user-list', listType, profileHandle ?? 'me', page, useMeEndpoint],
    queryFn: async () => {
      try {
        if (useMeEndpoint) {
          return listType === 'followers'
            ? await userService.getMyFollowers(page, size)
            : await userService.getMyFollowing(page, size)
        } else {
          if (!profileHandle) {
            throw new Error('Profile handle is required when not using /me endpoint')
          }
          return listType === 'followers'
            ? await userService.getFollowers(profileHandle, page, size)
            : await userService.getFollowing(profileHandle, page, size)
        }
      } catch (err) {
        console.error(`[UserListModal] ${listType} query error:`, err)
        throw err
      }
    },
    enabled: open && (useMeEndpoint || Boolean(profileHandle)),
  })

  const actionMutation = useMutation({
    mutationFn: (handle: string) =>
      listType === 'followers' ? userService.removeFollowerByHandle(handle) : userService.unfollowByHandle(handle),
    onSuccess: () => {
      toast.success(listType === 'followers' ? 'Follower removed' : 'Unfollowed')
      void query.refetch()
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  if (!open) {
    return null
  }

  const canGoNext = (query.data?.length ?? 0) === size

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/60 p-4" onClick={onClose}>
      <div
        className="w-full max-w-lg rounded-xl border border-white/20 bg-slate-900 p-4 text-white shadow-2xl"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-lg font-semibold">{title}</h3>
          <button onClick={onClose} className="rounded-md border border-white/30 px-2 py-1 text-xs hover:bg-white/10">
            Close
          </button>
        </div>

        {query.isLoading && <p className="text-sm text-white/75">Loading...</p>}
        {query.isError && <p className="text-sm text-rose-200">{parseApiError(query.error)}</p>}

        {!query.isLoading && !query.isError && (query.data?.length ?? 0) === 0 && <p className="text-sm text-white/75">No users found.</p>}

        {!query.isLoading && !query.isError && (query.data?.length ?? 0) > 0 && (
          <div className="space-y-2">
            {query.data?.map((user) => (
              <div key={user.id} className="flex items-center justify-between rounded-lg border border-white/15 p-2">
                <div className="flex items-center gap-3">
                  <img
                    src={user.profilePhoto ? (user.profilePhoto.startsWith('/api/media/') ? `http://localhost:8080${user.profilePhoto}` : `http://localhost:8080/api/media/${user.profilePhoto}`) : '/default-avatar.png'}
                    onError={(e) => { e.currentTarget.src = '/default-avatar.png'; }}
                    alt={user.handle}
                    className="h-9 w-9 rounded-full border border-white/20 object-cover"
                  />
                  <div className="flex flex-col">
                    <span className="text-sm font-medium">{user.handle}</span>
                    <Link to={`/profile/${encodeURIComponent(user.handle)}`} className="text-xs text-white/70 hover:underline" onClick={onClose}>
                      View Profile
                    </Link>
                  </div>
                </div>

                {isOwnProfile && (
                  <button
                    onClick={() => actionMutation.mutate(user.handle)}
                    disabled={actionMutation.isPending}
                    className="rounded-md border border-white/30 px-2 py-1 text-xs hover:bg-white/10 disabled:opacity-60"
                  >
                    {listType === 'followers' ? 'Remove' : 'Unfollow'}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}

        <div className="mt-4 flex items-center justify-between">
          <button
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            disabled={page === 0}
            className="rounded-md border border-white/30 px-3 py-1 text-xs hover:bg-white/10 disabled:opacity-60"
          >
            Previous
          </button>

          <span className="text-xs text-white/70">Page {page + 1}</span>

          <button
            onClick={() => setPage((prev) => prev + 1)}
            disabled={!canGoNext}
            className="rounded-md border border-white/30 px-3 py-1 text-xs hover:bg-white/10 disabled:opacity-60"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  )
}
