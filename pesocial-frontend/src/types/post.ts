export type PostVisibility = 'PUBLIC' | 'PRIVATE' | 'FOLLOWERS' | 'EXCLUSIVE'

export type MediaStorageType = 'GRID_FS' | 'EXTERNAL_URL'

export interface MediaUrl {
  storageType: MediaStorageType
  gridFsFileId?: string
  externalUrl?: string
  contentType?: string
  sizeBytes?: number
}

export interface Post {
  id: string
  authorId: string
  authorName?: string
  contentText?: string
  media?: MediaUrl
  mediaId?: string
  mediaType?: string
  visibility: PostVisibility
  likesCount: number
  sharesCount: number
  comments: string[]
  createdAt: string
  updatedAt: string
}

export interface CreatePostPayload {
  authorId: string
  contentText: string
  visibility: PostVisibility
  media?: MediaUrl
  mediaType?: string
}

