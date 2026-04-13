import axios from 'axios'
import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import EditProfileModal from '../components/EditProfileModal.tsx'
import UserListModal from '../components/UserListModal'
import { parseApiError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { userService } from '../services/userService'
import type { MyProfile, User } from '../types/user'

export default function MyProfilePage() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const queryClient = useQueryClient()
  const [editOpen, setEditOpen] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [showFollowers, setShowFollowers] = useState(false)
  const [showFollowing, setShowFollowing] = useState(false)

  const { data: profile, isLoading, error } = useQuery<MyProfile>({
    queryKey: ['myProfile'],
    queryFn: async () => {
      const data = await userService.getMyProfile()
      console.log('My profile response:', data)
      return data
    },
    enabled: Boolean(user),
  })

  useEffect(() => {
    if (!error) {
      return
    }

    console.error('My profile load error:', error)

    if (!axios.isAxiosError(error)) {
      return
    }

    if (error.response?.status === 401 || error.response?.status === 403 || error.response?.status === 404) {
      void logout()
      void navigate('/login', { replace: true })
    }
  }, [error, logout, navigate])

  if (!user) {
    return <div className="p-8 text-white">Loading session...</div>
  }

  const updateMutation = useMutation<
    User,
    Error,
    { username: string; handle: string; bio?: string }
  >({
    mutationFn: userService.updateMyProfile,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['myProfile'] })
      toast.success('Profile updated')
    },
    onError: (mutationError) => toast.error(parseApiError(mutationError)),
  })

  const uploadMutation = useMutation<
    { profilePhoto: string },
    Error,
    File
  >({
    mutationFn: async (file) => {
      console.log('[MyProfilePage] Uploading photo:', file.name, file.size, file.type)
      return await userService.uploadProfilePhoto(file)
    },
    onMutate: () => {
      console.log('[MyProfilePage] Upload started')
      setUploading(true)
    },
    onSettled: () => {
      console.log('[MyProfilePage] Upload settled')
      setUploading(false)
    },
    onSuccess: (data) => {
      console.log('[MyProfilePage] Upload successful:', data)
      void queryClient.invalidateQueries({ queryKey: ['myProfile'] })
      toast.success('Profile photo updated')
    },
    onError: (mutationError) => {
      console.error('[MyProfilePage] Upload error:', mutationError)
      toast.error(parseApiError(mutationError))
    },
  })

  const postsCount = profile?.posts?.length ?? 0

  const sortedPosts = useMemo(
    () => [...(profile?.posts ?? [])].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
    [profile?.posts],
  )

  if (isLoading) return <div className="p-8 text-white">Loading...</div>

  if (error || !profile) {
    const errorMsg = error ? parseApiError(error) : 'Failed to load profile'
    return (
      <div className="mx-auto max-w-2xl p-8">
        <div className="rounded-lg border border-rose-300/40 bg-rose-500/10 p-4 text-rose-100">
          <p className="font-semibold">Error</p>
          <p className="mt-1 text-sm">{errorMsg}</p>
        </div>
      </div>
    )
  }

  const handleEdit = (fields: { username: string; handle: string; bio?: string }) => {
    const trimmedHandle = fields.handle.trim()
    if (!/^@[a-zA-Z0-9_]{3,20}$/.test(trimmedHandle)) {
      toast.error('Handle must be in @handle format (3-20 letters/numbers/_)')
      return
    }
    updateMutation.mutate({ ...fields, handle: trimmedHandle })
    setEditOpen(false)
  }

  const handlePhotoChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selected = e.target.files[0]
      uploadMutation.mutate(selected)
    }
  }

  return (
    <div className="mx-auto min-h-screen w-full max-w-6xl p-4 text-white">
      <div className="mb-5 flex items-center justify-between">
        <Link to="/feed" className="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10">
          ← Back to Feed
        </Link>
      </div>

      <section className="rounded-2xl border border-white/20 bg-white/10 p-5 backdrop-blur">
        <div className="flex flex-col gap-6 md:flex-row md:items-center">
          <div className="relative">
            <img
              src={profile?.profilePhoto ? (profile.profilePhoto.startsWith('/api/media/') ? `http://localhost:8080${profile.profilePhoto}` : `http://localhost:8080/api/media/${profile.profilePhoto}`) : '/default-avatar.png'}
              onError={(e) => { e.currentTarget.src = '/default-avatar.png'; }}
              alt="Profile"
              className="h-32 w-32 rounded-full border-2 border-white/40 object-cover"
            />
            <label className="absolute bottom-1 right-1 cursor-pointer rounded-full bg-black/70 p-2 text-xs text-white shadow">
              <input type="file" accept="image/*" className="hidden" onChange={handlePhotoChange} disabled={uploading} />
              Change
            </label>
          </div>

          <div className="flex-1">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-2xl font-semibold">{profile.displayName || profile.handle}</h1>
              <button
                className="rounded-lg border border-white/30 px-3 py-1.5 text-sm hover:bg-white/10"
                onClick={() => setEditOpen(true)}
              >
                Edit profile
              </button>
            </div>

            <p className="mt-1 text-sm text-white/70">{profile.handle}</p>
            {profile.bio && <p className="mt-3 max-w-2xl text-sm text-white/90">{profile.bio}</p>}

            <div className="mt-4 flex gap-6 text-sm">
              <span><strong>{postsCount}</strong> posts</span>
              <button onClick={() => setShowFollowers(true)} className="hover:underline">
                <strong>{profile.followersCount ?? 0}</strong> followers
              </button>
              <button onClick={() => setShowFollowing(true)} className="hover:underline">
                <strong>{profile.followingCount ?? 0}</strong> following
              </button>
            </div>

            {uploading && <p className="mt-3 text-xs text-white/70">Uploading profile photo...</p>}
          </div>
        </div>
      </section>

      <UserListModal
        open={showFollowers}
        title="Your Followers"
        listType="followers"
        isOwnProfile
        useMeEndpoint
        onClose={() => setShowFollowers(false)}
      />
      <UserListModal
        open={showFollowing}
        title="You are Following"
        listType="following"
        isOwnProfile
        useMeEndpoint
        onClose={() => setShowFollowing(false)}
      />

      <section className="mt-6">
        <div className="mb-4 border-t border-white/20 pt-4 text-center text-sm uppercase tracking-[0.2em] text-white/70">
          Posts
        </div>

        {sortedPosts.length === 0 && (
          <div className="rounded-xl border border-white/20 bg-white/10 p-8 text-center text-white/80">
            You have not posted anything yet.
          </div>
        )}

        {sortedPosts.length > 0 && (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {sortedPosts.map((post) => {
              const mediaSrc = post.mediaId ? `http://localhost:8080/api/media/${post.mediaId}` : post.media?.externalUrl

              return (
                <article key={post.id} className="group relative overflow-hidden rounded-xl border border-white/20 bg-black/20">
                  {mediaSrc ? (
                    <img src={mediaSrc} alt="Post" className="h-64 w-full object-cover transition duration-300 group-hover:scale-105" />
                  ) : (
                    <div className="flex h-64 items-center justify-center p-4 text-sm text-white/70">
                      {post.contentText || 'No media'}
                    </div>
                  )}

                  <div className="pointer-events-none absolute inset-0 bg-black/60 opacity-0 transition group-hover:opacity-100">
                    <div className="flex h-full flex-col justify-between p-3 text-sm">
                      <div className="flex gap-4 font-semibold">
                        <span>♥ {post.likesCount ?? 0}</span>
                        <span>💬 {post.comments?.length ?? 0}</span>
                      </div>
                      {post.contentText && <p className="line-clamp-3 text-white/90">{post.contentText}</p>}
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>

      <EditProfileModal
        open={editOpen}
        onClose={() => setEditOpen(false)}
        initial={{ username: profile.displayName || '', handle: profile.handle || '', bio: profile.bio || '' }}
        onSave={handleEdit}
        loading={updateMutation.isPending}
        error={updateMutation.isError ? (updateMutation.error as Error).message : undefined}
      />
    </div>
  )
}
