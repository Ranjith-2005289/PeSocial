import { useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useAuth } from '../hooks/useAuth'
import { storyService } from '../services/storyService'
import { API_BASE_URL } from '../services/api'
import type { StoryDetailDTO, StoryBarUser } from '../types/story'

interface StoryBarProps {
  onStorySelect: (user: StoryBarUser, stories: StoryDetailDTO[]) => void
}

const resolveProfilePhotoUrl = (profilePhoto?: string) => {
  if (!profilePhoto || profilePhoto.trim().length === 0) {
    return 'https://placehold.co/56x56'
  }
  if (/^https?:\/\//i.test(profilePhoto)) {
    return profilePhoto
  }
  if (profilePhoto.startsWith('/api/media/')) {
    return `${API_BASE_URL}${profilePhoto}`
  }
  return `${API_BASE_URL}/api/media/${profilePhoto}`
}

export default function StoryBar({ onStorySelect }: StoryBarProps) {
  const { user } = useAuth()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)

  const storiesQuery = useQuery({
    queryKey: ['active-stories'],
    queryFn: () => storyService.getActiveStories(),
    enabled: Boolean(user),
    refetchInterval: 30000,
  })

  const stories = storiesQuery.data || []

  const storyUsers: StoryBarUser[] = stories.reduce((acc, story) => {
    const existing = acc.find((u) => u.id === story.authorId)
    if (!existing) {
      acc.push({
        id: story.authorId,
        handle: '', // Will be set from user data if available
        profilePhoto: '',
        hasUnseenStory: !story.viewed,
      })
    } else if (!story.viewed && existing.hasUnseenStory === false) {
      existing.hasUnseenStory = true
    }
    return acc
  }, [] as StoryBarUser[])

  const handleUploadClick = () => {
    fileInputRef.current?.click()
  }

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) return

    setUploading(true)
    try {
      console.log('Uploading story file:', file.name, file.size, file.type)
      const mediaUrl = await storyService.uploadStoryMedia(file)
      console.log('Media uploaded:', mediaUrl)
      
      await storyService.createStory(mediaUrl)
      await storiesQuery.refetch()
      toast.success('Story posted!')
    } catch (error) {
      console.error('Story upload failed:', error)
      toast.error(`Failed to upload story: ${error instanceof Error ? error.message : 'Unknown error'}`)
    } finally {
      setUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleStoryUserClick = (storyUser: StoryBarUser) => {
    const userStories = stories.filter((s) => s.authorId === storyUser.id)
    if (userStories.length > 0) {
      onStorySelect(storyUser, userStories)
    }
  }

  return (
    <div className="flex gap-2 overflow-x-auto pb-2 px-4 bg-gradient-to-r from-slate-900/50 to-transparent rounded-lg">
      <button
        onClick={handleUploadClick}
        disabled={uploading}
        className="flex flex-col items-center gap-1 flex-shrink-0"
      >
        <div className="relative">
          <img
            src={resolveProfilePhotoUrl(user?.profilePhoto)}
            alt="Your story"
            className="h-14 w-14 rounded-full object-cover border-2 border-gray-600 hover:border-sky-400"
          />
          <div className="absolute -bottom-1 -right-1 h-5 w-5 rounded-full bg-sky-500 flex items-center justify-center text-white text-xs font-bold">
            +
          </div>
        </div>
        <p className="text-xs text-white/80">Your story</p>
      </button>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*,video/*"
        onChange={handleFileSelect}
        className="hidden"
      />

      {storyUsers.map((storyUser) => {
        const userStory = stories.find((s) => s.authorId === storyUser.id)
        const hasUnseenStory = userStory && !userStory.viewed

        return (
          <button
            key={storyUser.id}
            onClick={() => handleStoryUserClick(storyUser)}
            className="flex flex-col items-center gap-1 flex-shrink-0"
          >
            <div className="relative">
              <img
                src={resolveProfilePhotoUrl(userStory?.mediaUrl)}
                alt={storyUser.handle}
                className={`h-14 w-14 rounded-full object-cover border-2 ${
                  hasUnseenStory
                    ? 'border-transparent bg-gradient-to-r from-pink-500 via-red-500 to-yellow-500'
                    : 'border-gray-600'
                }`}
              />
            </div>
            <p className="text-xs text-white/80 truncate w-14 text-center">{storyUser.handle || 'User'}</p>
          </button>
        )
      })}
    </div>
  )
}
