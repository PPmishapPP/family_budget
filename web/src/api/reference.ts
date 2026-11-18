import { useQuery } from '@tanstack/react-query'

import {
  MEDIA_STATUSES,
  MEDIA_STATUS_FALLBACK_TRANSITIONS,
  MEDIA_STATUS_LABELS,
  MEDIA_TYPES,
  MEDIA_TYPE_LABELS,
  isMediaStatus,
  isMediaType,
} from '../lib/media.ts'
import type { MediaStatus, MediaType } from '../lib/media.ts'
import { api } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

export type MediaTypeReference = {
  value: MediaType
  label: string
}

export type MediaStatusReference = {
  value: MediaStatus
  label: string
  availableTransitions: MediaStatus[]
}

export type MediaReference = {
  types: MediaTypeReference[]
  statuses: MediaStatusReference[]
}

function fallbackReference(): MediaReference {
  return {
    types: MEDIA_TYPES.map((value) => ({ value, label: MEDIA_TYPE_LABELS[value] })),
    statuses: MEDIA_STATUSES.map((value) => ({
      value,
      label: MEDIA_STATUS_LABELS[value],
      availableTransitions: [...MEDIA_STATUS_FALLBACK_TRANSITIONS[value]],
    })),
  }
}

async function fetchMediaReference(): Promise<MediaReference> {
  const [typesResponse, statusesResponse] = await Promise.all([
    api.GET('/api/v1/reference/media-types'),
    api.GET('/api/v1/reference/media-statuses'),
  ])

  if (!typesResponse.response.ok || !typesResponse.data) {
    throw new Error(
      await readProblemMessage(typesResponse.response, 'Не удалось загрузить типы медиа'),
    )
  }
  if (!statusesResponse.response.ok || !statusesResponse.data) {
    throw new Error(
      await readProblemMessage(statusesResponse.response, 'Не удалось загрузить статусы медиа'),
    )
  }

  const types = typesResponse.data.flatMap((item) => {
    if (!isMediaType(item.value)) {
      return []
    }
    const label = item.label?.trim()
    return [{ value: item.value, label: label ? label : MEDIA_TYPE_LABELS[item.value] }]
  })

  const statuses = statusesResponse.data.flatMap((item) => {
    if (!isMediaStatus(item.value)) {
      return []
    }
    const label = item.label?.trim()
    return [
      {
        value: item.value,
        label: label ? label : MEDIA_STATUS_LABELS[item.value],
        availableTransitions: (item.availableTransitions ?? []).filter(isMediaStatus),
      },
    ]
  })

  const fallback = fallbackReference()
  return {
    types: types.length > 0 ? types : fallback.types,
    statuses: statuses.length > 0 ? statuses : fallback.statuses,
  }
}

export function useMediaReferenceQuery() {
  return useQuery({
    queryKey: queryKeys.mediaReference,
    queryFn: fetchMediaReference,
    staleTime: 5 * 60_000,
  })
}
