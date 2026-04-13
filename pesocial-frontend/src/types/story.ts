export interface Story {
  id: string
  authorId: string
  mediaUrl: string
  timestamp: string
  viewers: string[]
  likes: string[]
}

export interface StoryDetailDTO {
  id: string
  authorId: string
  mediaUrl: string
  timestamp: string
  viewerCount: number
  likeCount: number
  viewers: string[]
  likes: string[]
  viewed: boolean
}

export interface StoryBarUser {
  id: string
  handle: string
  profilePhoto?: string
  hasUnseenStory: boolean
}
