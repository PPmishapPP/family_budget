import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { MEDIA_TYPE_LABELS, isMediaStatus, isMediaType } from '../lib/media.ts'
import type { MediaStatus, MediaType } from '../lib/media.ts'
import { api } from './client.ts'
import type { components } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

type MediaDto = components['schemas']['MediaDto']
type MediaRequest = components['schemas']['MediaRequest']

export type MediaItem = {
  id: number
  name: string
  type: MediaType
  status: MediaStatus
  rating: number | null
  description: string | null
}

export type MediaInput = {
  name: string
  type: MediaType
  status: MediaStatus | null
  rating: number | null
  description: string | null
}

function normalizeMedia(dto: MediaDto): MediaItem | null {
  if (dto.id === undefined || !isMediaType(dto.type)) {
    return null
  }
  const rating =
    typeof dto.rating === 'number' && dto.rating >= 1 && dto.rating <= 10 ? dto.rating : null
  const description = dto.description?.trim()
  return {
    id: dto.id,
    name: dto.name?.trim() || `${MEDIA_TYPE_LABELS[dto.type]} ${dto.id}`,
    type: dto.type,
    status: isMediaStatus(dto.status) ? dto.status : 'PLAN_TO_WATCH',
    rating,
    description: description ? description : null,
  }
}

function toRequestBody(input: MediaInput): MediaRequest {
  return {
    name: input.name,
    type: input.type,
    status: input.status ?? undefined,
    rating: input.rating ?? undefined,
    description: input.description ?? undefined,
  }
}

async function fetchMedia(accountId: number, type: MediaType | null): Promise<MediaItem[]> {
  const { data, response } = await api.GET('/api/v1/accounts/{accountId}/media', {
    params: {
      path: { accountId },
      query: { type: type ?? undefined },
    },
  })
  if (!response.ok || !data) {
    throw new Error(await readProblemMessage(response, 'Не удалось загрузить фильмы и сериалы'))
  }
  return data.flatMap((dto) => {
    const item = normalizeMedia(dto)
    return item ? [item] : []
  })
}

export function useMediaQuery(accountId: number | null, type: MediaType | null) {
  return useQuery({
    queryKey: queryKeys.media(accountId, type),
    queryFn: () => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      return fetchMedia(accountId, type)
    },
    enabled: accountId !== null,
  })
}

export function useCreateMediaMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (input: MediaInput) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.POST('/api/v1/accounts/{accountId}/media', {
        params: { path: { accountId } },
        body: toRequestBody(input),
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось сохранить запись'))
      }
      return normalizeMedia(data)
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mediaRoot(accountId) })
    },
  })
}

export function useUpdateMediaMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ mediaId, input }: { mediaId: number; input: MediaInput }) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.PUT('/api/v1/accounts/{accountId}/media/{mediaId}', {
        params: { path: { accountId, mediaId } },
        body: toRequestBody(input),
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось сохранить запись'))
      }
      return normalizeMedia(data)
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mediaRoot(accountId) })
    },
  })
}

export function useDeleteMediaMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (mediaId: number) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { response } = await api.DELETE('/api/v1/accounts/{accountId}/media/{mediaId}', {
        params: { path: { accountId, mediaId } },
      })
      if (!response.ok) {
        throw new Error(await readProblemMessage(response, 'Не удалось удалить запись'))
      }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mediaRoot(accountId) })
    },
  })
}
