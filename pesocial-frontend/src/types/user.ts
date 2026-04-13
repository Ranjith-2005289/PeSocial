export type UserRole = 'REGULAR_USER' | 'CREATOR' | 'ADMIN'

export interface User {
  id: string
  handle: string
  displayName?: string
  username?: string
  email: string
  role: UserRole
  profilePhoto?: string
  bio?: string
  followersCount?: number
  followingCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface RegularUser extends User {
  role: 'REGULAR_USER'
  followingIds?: string[]
  followerIds?: string[]
}

export interface Creator extends User {
  role: 'CREATOR'
  isVerified?: boolean
  subscriptionPrice?: number
  specialty?: string
}

export interface Admin extends User {
  role: 'ADMIN'
  permissions?: string[]
}

export interface UserSummary {
  id: string
  handle: string
  profilePhoto?: string
}

export interface MyProfile {
  id: string
  displayName: string
  handle: string
  email: string
  profilePhoto?: string
  bio?: string
  role: UserRole
  followersCount: number
  followingCount: number
  posts: import('./post').Post[]
}
