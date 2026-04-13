import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { userService } from '../services/userService'
import type { User } from '../types/user'
import { parseApiError } from '../services/api'

export default function UserSearchBar() {
  const [term, setTerm] = useState('')
  const navigate = useNavigate()

  const normalizedTerm = term.trim()
  const shouldSearch = normalizedTerm.length >= 2

  const usersQuery = useQuery({
    queryKey: ['user-search', normalizedTerm],
    queryFn: () => userService.searchUsers(normalizedTerm),
    enabled: shouldSearch,
  })

  const followMutation = useMutation({
    mutationFn: (handle: string) => userService.followByHandle(handle),
    onSuccess: () => toast.success('Followed user'),
    onError: (error) => toast.error(parseApiError(error)),
  })

  const results = useMemo<User[]>(() => usersQuery.data ?? [], [usersQuery.data])

  return (
    <div className="relative w-full max-w-md">
      <input
        value={term}
        onChange={(event) => setTerm(event.target.value)}
        placeholder="Search by @handle"
        className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 text-sm outline-none focus:border-indigo-400"
      />

      {shouldSearch && (
        <div className="absolute z-20 mt-2 max-h-72 w-full overflow-y-auto rounded-lg border border-white/20 bg-slate-900/95 p-1 shadow-2xl">
          {usersQuery.isLoading && <p className="px-3 py-2 text-xs text-white/75">Searching...</p>}

          {!usersQuery.isLoading && results.length === 0 && <p className="px-3 py-2 text-xs text-white/75">No users found</p>}

          {!usersQuery.isLoading &&
            results.map((user) => (
              <div key={user.id} className="flex items-center justify-between rounded-md px-3 py-2 text-left text-sm hover:bg-white/10">
                <button
                  onClick={() => {
                    setTerm('')
                    void navigate(`/profile/${encodeURIComponent(user.handle)}`)
                  }}
                  className="text-left"
                >
                  <p className="font-medium text-white">{user.displayName || user.username || user.handle}</p>
                  <p className="text-xs text-white/70">{user.handle}</p>
                </button>

                <button
                  onClick={() => followMutation.mutate(user.handle)}
                  disabled={followMutation.isPending}
                  className="rounded-md border border-white/30 px-2 py-1 text-xs hover:bg-white/10 disabled:opacity-60"
                >
                  Follow
                </button>
              </div>
            ))}
        </div>
      )}
    </div>
  )
}
