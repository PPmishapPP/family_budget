export const MEDIA_TYPES = ['MOVIE', 'SERIES'] as const
export type MediaType = (typeof MEDIA_TYPES)[number]

export const MEDIA_TYPE_LABELS: Record<MediaType, string> = {
  MOVIE: 'Фильм',
  SERIES: 'Сериал',
}

export const MEDIA_STATUSES = [
  'PLAN_TO_WATCH',
  'IN_PROGRESS',
  'WATCHED',
  'WAITING_FOR_CONTINUATION',
  'STOPPED',
  'CANCELED',
] as const
export type MediaStatus = (typeof MEDIA_STATUSES)[number]

export const MEDIA_STATUS_LABELS: Record<MediaStatus, string> = {
  PLAN_TO_WATCH: 'Ожидает просмотра',
  IN_PROGRESS: 'В процессе',
  WATCHED: 'Просмотрено',
  WAITING_FOR_CONTINUATION: 'Ожидает продолжения',
  STOPPED: 'Остановлен просмотр',
  CANCELED: 'Отмена просмотра',
}

export const MEDIA_STATUS_COLORS: Record<MediaStatus, string> = {
  PLAN_TO_WATCH: 'gray',
  IN_PROGRESS: 'blue',
  WATCHED: 'teal',
  WAITING_FOR_CONTINUATION: 'orange',
  STOPPED: 'red',
  CANCELED: 'dark',
}

export const MEDIA_STATUS_FALLBACK_TRANSITIONS: Record<MediaStatus, readonly MediaStatus[]> = {
  PLAN_TO_WATCH: ['IN_PROGRESS', 'WATCHED', 'WAITING_FOR_CONTINUATION', 'CANCELED'],
  IN_PROGRESS: MEDIA_STATUSES,
  WATCHED: ['IN_PROGRESS', 'WAITING_FOR_CONTINUATION'],
  WAITING_FOR_CONTINUATION: ['IN_PROGRESS', 'WATCHED', 'STOPPED', 'CANCELED'],
  STOPPED: ['IN_PROGRESS', 'PLAN_TO_WATCH', 'WATCHED', 'WAITING_FOR_CONTINUATION', 'CANCELED'],
  CANCELED: ['IN_PROGRESS', 'PLAN_TO_WATCH'],
}

export function isMediaType(value: string | undefined): value is MediaType {
  return value !== undefined && (MEDIA_TYPES as readonly string[]).includes(value)
}

export function isMediaStatus(value: string | undefined): value is MediaStatus {
  return value !== undefined && (MEDIA_STATUSES as readonly string[]).includes(value)
}

type LabeledMediaType = { value: MediaType; label: string }
type LabeledMediaStatus = { value: MediaStatus; label: string; availableTransitions: MediaStatus[] }

export function mediaTypeLabel(
  type: MediaType,
  types?: readonly LabeledMediaType[] | null,
): string {
  return types?.find((item) => item.value === type)?.label ?? MEDIA_TYPE_LABELS[type]
}

export function mediaStatusLabel(
  status: MediaStatus,
  statuses?: readonly LabeledMediaStatus[] | null,
): string {
  return statuses?.find((item) => item.value === status)?.label ?? MEDIA_STATUS_LABELS[status]
}

export function mediaStatusTransitions(
  status: MediaStatus,
  statuses?: readonly LabeledMediaStatus[] | null,
): MediaStatus[] {
  const transitions =
    statuses?.find((item) => item.value === status)?.availableTransitions ??
    MEDIA_STATUS_FALLBACK_TRANSITIONS[status]
  return transitions.filter((value) => value !== status)
}
