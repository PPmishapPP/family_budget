import { keepPreviousData, useQuery } from '@tanstack/react-query'

import { api } from './client.ts'
import type { components } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

type AccountHistoryDto = components['schemas']['AccountHistoryDto']
type HistoryPageDto = components['schemas']['PageResponseAccountHistoryDto']

export const HISTORY_PAGE_SIZE = 20

export type HistorySortKey = 'comment' | 'date' | 'sum' | 'balance'
export type HistorySortDirection = 'asc' | 'desc'

export type HistoryQueryParams = {
  page: number
  size: number
  sort: HistorySortKey
  direction: HistorySortDirection
}

export type HistoryEntry = {
  id: number
  sum: number
  balance: number
  dateTime: string
  comment: string | null
}

export type HistoryPageResult = {
  entries: HistoryEntry[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

function normalizeEntry(dto: AccountHistoryDto): HistoryEntry | null {
  if (dto.id === undefined || dto.sum === undefined || dto.balance === undefined || !dto.dateTime) {
    return null
  }
  const comment = dto.comment?.trim()
  return {
    id: dto.id,
    sum: dto.sum,
    balance: dto.balance,
    dateTime: dto.dateTime,
    comment: comment ? comment : null,
  }
}

async function fetchHistoryPage(
  accountId: number,
  params: HistoryQueryParams,
): Promise<HistoryPageResult> {
  const { data, response } = await api.GET('/api/v1/accounts/{accountId}/history', {
    params: {
      path: { accountId },
      query: {
        page: params.page,
        size: params.size,
        sort: params.sort,
        direction: params.direction,
      },
    },
  })
  if (!response.ok || !data) {
    throw new Error(await readProblemMessage(response, 'Не удалось загрузить историю платежей'))
  }
  return toHistoryPage(data)
}

function toHistoryPage(dto: HistoryPageDto): HistoryPageResult {
  return {
    entries: (dto.content ?? []).flatMap((item) => {
      const entry = normalizeEntry(item)
      return entry ? [entry] : []
    }),
    page: dto.page ?? 0,
    size: dto.size ?? HISTORY_PAGE_SIZE,
    totalElements: dto.totalElements ?? 0,
    totalPages: dto.totalPages ?? 0,
  }
}

export function useHistoryQuery(accountId: number | null, params: HistoryQueryParams) {
  return useQuery({
    queryKey: queryKeys.historyPage(accountId, params),
    queryFn: () => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      return fetchHistoryPage(accountId, params)
    },
    enabled: accountId !== null,
    placeholderData: keepPreviousData,
  })
}
