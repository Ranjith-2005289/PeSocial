import api from './api'
import type { Story, StoryDetailDTO } from '../types/story'

class StoryService {
  async createStory(mediaUrl: string): Promise<Story> {
    const { data } = await api.post<Story>('/api/stories', { mediaUrl })
    return data
  }

  async getActiveStories(): Promise<StoryDetailDTO[]> {
    const { data } = await api.get<StoryDetailDTO[]>('/api/stories/active')
    return data
  }

  async getUserStories(userId: string): Promise<StoryDetailDTO[]> {
    const { data } = await api.get<StoryDetailDTO[]>(`/api/stories/user/${encodeURIComponent(userId)}`)
    return data
  }

  async viewStory(storyId: string): Promise<void> {
    await api.post(`/api/stories/${encodeURIComponent(storyId)}/view`)
  }

  async likeStory(storyId: string): Promise<void> {
    await api.post(`/api/stories/${encodeURIComponent(storyId)}/like`)
  }

  async unlikeStory(storyId: string): Promise<void> {
    await api.post(`/api/stories/${encodeURIComponent(storyId)}/unlike`)
  }

  async getStoryAnalytics(storyId: string): Promise<StoryDetailDTO> {
    const { data } = await api.get<StoryDetailDTO>(`/api/stories/${encodeURIComponent(storyId)}/analytics`)
    return data
  }

  async deleteStory(storyId: string): Promise<void> {
    await api.delete(`/api/stories/${encodeURIComponent(storyId)}`)
  }

  async uploadStoryMedia(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)

    // Reuse proven GridFS upload flow already used by chat attachments.
    const { data } = await api.post<{ attachmentUrl: string }>('/api/messages/attachments', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    return data.attachmentUrl
  }
}

export const storyService = new StoryService()
