import { describe, expect, it } from 'vitest'

import {
  MEDIA_STATUSES,
  isMediaStatus,
  isMediaType,
  mediaStatusLabel,
  mediaStatusTransitions,
  mediaTypeLabel,
} from './media.ts'

describe('mediaStatusTransitions', () => {
  it('не предлагает текущий статус при переходах', () => {
    const transitions = mediaStatusTransitions('WATCHED')

    expect(transitions).toEqual(['IN_PROGRESS', 'WAITING_FOR_CONTINUATION'])
    expect(transitions).not.toContain('WATCHED')
  })

  it('для IN_PROGRESS оставляет все статусы, кроме текущего', () => {
    const transitions = mediaStatusTransitions('IN_PROGRESS')

    expect(transitions).toHaveLength(MEDIA_STATUSES.length - 1)
    expect(transitions).not.toContain('IN_PROGRESS')
  })

  it('использует переходы из справочника', () => {
    const transitions = mediaStatusTransitions('WATCHED', [
      {
        value: 'WATCHED',
        label: 'Просмотрено',
        availableTransitions: ['IN_PROGRESS', 'WATCHED'],
      },
    ])

    expect(transitions).toEqual(['IN_PROGRESS'])
  })

  it('откатывается на локальную таблицу, если справочник пуст', () => {
    expect(mediaStatusTransitions('CANCELED', [])).toEqual(['IN_PROGRESS', 'PLAN_TO_WATCH'])
  })
})

describe('media labels and guards', () => {
  it('берёт подписи из справочника, иначе из fallback', () => {
    expect(mediaTypeLabel('MOVIE', [{ value: 'MOVIE', label: 'Кино' }])).toBe('Кино')
    expect(mediaTypeLabel('SERIES')).toBe('Сериал')
    expect(
      mediaStatusLabel('WATCHED', [
        { value: 'WATCHED', label: 'Посмотрели', availableTransitions: [] },
      ]),
    ).toBe('Посмотрели')
    expect(mediaStatusLabel('WATCHED')).toBe('Просмотрено')
  })

  it('распознаёт типы и статусы', () => {
    expect(isMediaType('MOVIE')).toBe(true)
    expect(isMediaType('SERIES')).toBe(true)
    expect(isMediaType('BOOK')).toBe(false)
    expect(isMediaType(undefined)).toBe(false)

    expect(isMediaStatus('PLAN_TO_WATCH')).toBe(true)
    expect(isMediaStatus('UNKNOWN')).toBe(false)
    expect(isMediaStatus(undefined)).toBe(false)
  })
})
