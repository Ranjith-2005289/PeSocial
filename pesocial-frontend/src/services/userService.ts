import type { AuthResponse } from '../types/auth'
import type { MyProfile, User, UserSummary } from '../types/user'
import api from './api'

export const userService = {
  searchUsers: async (handle: string) => {
    const { data } = await api.get<User[]>('/api/users/search', {
      params: { handle },
    })
    return data
  },

  getUserProfileByHandle: async (handle: string) => {
    const { data } = await api.get<User>(`/api/users/handle/${encodeURIComponent(handle)}`)
    return data
  },

  getMyProfile: async () => {
    const { data } = await api.get<MyProfile>('/api/users/me')
    return data
  },

  updateMyProfile: async (payload: { username: string; handle: string; bio?: string }) => {
    const { data } = await api.put<User>('/api/users/update', payload)
    return data
  },

  uploadProfilePhoto: async (file: File) => {
    const form = new FormData()
    form.append('file', file)
    const { data } = await api.post<{ profilePhoto: string }>('/api/users/profile-picture', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  },

  followByHandle: async (handle: string) => {
    await api.post(`/api/users/me/follow/${encodeURIComponent(handle)}`)
  },

  followHandle: async (handle: string) => {
    await api.post(`/api/users/follow/${encodeURIComponent(handle)}`)
  },

  unfollowByHandle: async (handle: string) => {
    await api.post(`/api/users/me/unfollow/${encodeURIComponent(handle)}`)
  },

  removeFollowerByHandle: async (handle: string) => {
    await api.post(`/api/users/me/remove-follower/${encodeURIComponent(handle)}`)
  },

  getFollowers: async (handle: string, page = 0, size = 20) => {
    const { data } = await api.get<UserSummary[]>(`/api/users/${encodeURIComponent(handle)}/followers`, {
      params: { page, size },
    })
    return data
  },

  getMyFollowers: async (page = 0, size = 20) => {
    const { data } = await api.get<UserSummary[]>('/api/users/me/followers', {
      params: { page, size },
    })
    return data
  },

  getFollowing: async (handle: string, page = 0, size = 20) => {
    const { data } = await api.get<UserSummary[]>(`/api/users/${encodeURIComponent(handle)}/following`, {
      params: { page, size },
    })
    return data
  },

  getMyFollowing: async (page = 0, size = 20) => {
    const { data } = await api.get<UserSummary[]>('/api/users/me/following', {
      params: { page, size },
    })
    return data
  },

  becomeCreator: async () => {
    const { data } = await api.post<AuthResponse>('/api/users/me/become-creator')
    return data
  },
}
