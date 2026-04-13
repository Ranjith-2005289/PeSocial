import { useState, type FormEvent } from 'react'
import type { CreatePostPayload, MediaUrl, Post, PostVisibility } from '../types/post'

interface CreatePostProps {
  authorId: string
  onCreate: (payload: CreatePostPayload) => Promise<Post>
  isSubmitting: boolean
}

const visibilities: PostVisibility[] = ['PUBLIC', 'PRIVATE', 'FOLLOWERS', 'EXCLUSIVE']

const fileToExternalMedia = async (file: File): Promise<MediaUrl> => {
  const dataUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ''))
    reader.onerror = () => reject(new Error('Unable to read file'))
    reader.readAsDataURL(file)
  })

  return {
    storageType: 'EXTERNAL_URL',
    externalUrl: dataUrl,
    contentType: file.type,
    sizeBytes: file.size,
  }
}

export default function CreatePost({ authorId, onCreate, isSubmitting }: CreatePostProps) {
  const [contentText, setContentText] = useState('')
  const [visibility, setVisibility] = useState<PostVisibility>('PUBLIC')
  const [file, setFile] = useState<File | null>(null)

  const submit = async (event: FormEvent) => {
    event.preventDefault()

    const payload: CreatePostPayload = {
      authorId,
      contentText,
      visibility,
    }

    if (file) {
      payload.media = await fileToExternalMedia(file)
      payload.mediaType = file.type
    }

    await onCreate(payload)
    setContentText('')
    setVisibility('PUBLIC')
    setFile(null)
  }

  return (
    <form onSubmit={submit} className="mb-5 rounded-xl border border-white/20 bg-white/10 p-4 text-white">
      <h2 className="text-lg font-semibold">Create Post</h2>

      <textarea
        className="mt-3 w-full rounded-lg border border-white/30 bg-black/20 p-3 outline-none focus:border-indigo-400"
        rows={3}
        value={contentText}
        onChange={(event) => setContentText(event.target.value)}
        placeholder="Share something with your followers..."
      />

      <div className="mt-3 grid gap-3 md:grid-cols-2">
        <select
          value={visibility}
          onChange={(event) => setVisibility(event.target.value as PostVisibility)}
          className="rounded-lg border border-white/30 bg-black/20 px-3 py-2 outline-none focus:border-indigo-400"
        >
          {visibilities.map((item) => (
            <option key={item} value={item} className="text-black">
              {item}
            </option>
          ))}
        </select>

        <input
          type="file"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          className="rounded-lg border border-white/30 bg-black/20 px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-white file:px-3 file:py-1 file:text-black"
        />
      </div>

      <button
        type="submit"
        disabled={isSubmitting || (!contentText.trim() && !file)}
        className="mt-3 rounded-lg bg-white px-4 py-2 font-medium text-black disabled:opacity-60"
      >
        {isSubmitting ? 'Posting...' : 'Post'}
      </button>
    </form>
  )
}
