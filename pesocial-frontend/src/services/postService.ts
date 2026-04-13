import api from './api'
import type { CreatePostPayload, Post } from '../types/post'

export const postService = {
  getFeedPosts: async () => {
    const { data } = await api.get<Post[]>('/api/posts')
    return data
  },

  getPostsByAuthor: async (authorId: string) => {
    const { data } = await api.get<Post[]>(`/api/posts/author/${authorId}`)
    return data
  },

  createPost: async (payload: CreatePostPayload) => {
    const { data } = await api.post<Post>('/api/posts', payload)
    return data
  },

  likePost: async (postId: string) => {
    const { data } = await api.patch<Post>(`/api/posts/${postId}/like`)
    return data
  },

  commentPost: async (postId: string, value: string) => {
    const { data } = await api.patch<Post>(`/api/posts/${postId}/comment`, null, {
      params: { value },
    })
    return data
  },

  deletePost: async (postId: string) => {
    await api.delete(`/api/posts/${postId}`)
  },
}
