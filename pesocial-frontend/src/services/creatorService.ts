import api from './api'
import type { CreatorAnalytics } from '../types/analytics'

export const creatorService = {
  getAnalytics: async (creatorId: string) => {
    const { data } = await api.get<CreatorAnalytics>(`/api/creators/${creatorId}/analytics`)
    return data
  },
}
