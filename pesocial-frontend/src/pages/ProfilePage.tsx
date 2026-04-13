import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { userService } from '../services/userService'
import { postService } from '../services/postService'
import { parseApiError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import UserListModal from '../components/UserListModal'

export default function ProfilePage() {
  const { user: currentUser } = useAuth()
  const { handle } = useParams<{ handle: string }>()
  const decodedHandle = handle ? decodeURIComponent(handle) : ''
  const [showFollowers, setShowFollowers] = useState(false)
  const [showFollowing, setShowFollowing] = useState(false)

  const profileQuery = useQuery({
    queryKey: ['profile-by-handle', decodedHandle],
    queryFn: () => userService.getUserProfileByHandle(decodedHandle),
    enabled: Boolean(decodedHandle),
  })

  const postsQuery = useQuery({
    queryKey: ['profile-posts', profileQuery.data?.id],
    queryFn: () => postService.getPostsByAuthor(profileQuery.data!.id),
    enabled: Boolean(profileQuery.data?.id),
  })

  return (
    <div className="mx-auto min-h-screen w-full max-w-6xl p-4 text-white">
      <div className="mb-5 flex items-center justify-between">
        <Link to="/feed" className="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10">
          ← Back to Feed
        </Link>
      </div>

      {profileQuery.isLoading && <div className="rounded-xl border border-white/20 bg-white/10 p-4">Loading profile...</div>}

      {profileQuery.isError && (
        <div className="rounded-xl border border-rose-300/40 bg-rose-500/10 p-4 text-rose-100">
          {parseApiError(profileQuery.error)}
        </div>
      )}

      {profileQuery.data && (
        <header className="mb-6 rounded-xl border border-white/20 bg-white/10 p-5">
          <h1 className="text-2xl font-semibold">{profileQuery.data.displayName || profileQuery.data.username || profileQuery.data.handle}</h1>
          <p className="mt-1 text-sm text-white/75">{profileQuery.data.handle}</p>
          <p className="mt-1 text-sm text-white/75">Role: {profileQuery.data.role}</p>
          {profileQuery.data.bio && <p className="mt-3 text-sm text-white/85">{profileQuery.data.bio}</p>}
          <div className="mt-3 flex gap-4 text-xs text-white/75">
            <button onClick={() => setShowFollowers(true)} className="hover:underline">
              Followers: {profileQuery.data.followersCount ?? 0}
            </button>
            <button onClick={() => setShowFollowing(true)} className="hover:underline">
              Following: {profileQuery.data.followingCount ?? 0}
            </button>
          </div>
        </header>
      )}

      {profileQuery.data && (
        <>
          <UserListModal
            open={showFollowers}
            title={`Followers of ${profileQuery.data.handle}`}
            profileHandle={profileQuery.data.handle}
            listType="followers"
            isOwnProfile={currentUser?.handle === profileQuery.data.handle}
            onClose={() => setShowFollowers(false)}
          />
          <UserListModal
            open={showFollowing}
            title={`Following of ${profileQuery.data.handle}`}
            profileHandle={profileQuery.data.handle}
            listType="following"
            isOwnProfile={currentUser?.handle === profileQuery.data.handle}
            onClose={() => setShowFollowing(false)}
          />
        </>
      )}

      <section>
        <h2 className="mb-3 text-lg font-semibold">Posts</h2>

        {postsQuery.isLoading && <div className="rounded-xl border border-white/20 bg-white/10 p-4">Loading posts...</div>}

        {postsQuery.isError && (
          <div className="rounded-xl border border-rose-300/40 bg-rose-500/10 p-4 text-rose-100">
            {parseApiError(postsQuery.error)}
          </div>
        )}

        {!postsQuery.isLoading && !postsQuery.isError && (postsQuery.data?.length ?? 0) === 0 && (
          <div className="rounded-xl border border-white/20 bg-white/10 p-4">No posts by this user.</div>
        )}

        {!postsQuery.isLoading && !postsQuery.isError && (postsQuery.data?.length ?? 0) > 0 && (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {postsQuery.data?.map((post) => {
              const mediaSrc = post.mediaId
                ? `http://localhost:8080/api/media/${post.mediaId}`
                : post.media?.externalUrl

              return (
                <article key={post.id} className="overflow-hidden rounded-xl border border-white/20 bg-white/10">
                  {mediaSrc ? (
                    <img src={mediaSrc} alt="Profile post" className="h-56 w-full object-cover" />
                  ) : (
                    <div className="flex h-56 items-center justify-center bg-black/20 text-sm text-white/70">No media</div>
                  )}
                  <div className="p-3">
                    <p className="line-clamp-2 text-sm">{post.contentText || 'No caption'}</p>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}
