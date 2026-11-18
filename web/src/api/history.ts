import { useQuery } from '@tanstack/react-query'

import { api } from './client.ts'
import type { components } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

type AccountHistoryDto = components['schemas']['AccountHistoryDto']
type HistoryPageDto = components['schemas']['PageResponseAccountHistoryDto']

const PAGE_SIZE = 100

export type HistoryEntry = {
  id: number
  sum: number
  balance: number
  dateTime: string
  comment: string | null
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

async function fetchHistoryPage(accountId: number, page: number): Promise<HistoryPageDto> {
  const { data, response } = await api.GET('/api/v1/accounts/{accountId}/history', {
    params: {
      path: { accountId },
      query: { page, size: PAGE_SIZE },
    },
  })
  if (!response.ok || !data) {
    throw new Error(await readProblemMessage(response, 'Не удалось загрузить историю платежей'))
  }
  return data
}

export async function fetchHistory(accountId: number): Promise<HistoryEntry[]> {
  const first = await fetchHistoryPage(accountId, 0)
  const totalPages = first.totalPages ?? 0
  const rest =
    totalPages > 1
      ? await Promise.all(
          Array.from({ length: totalPages - 1 }, (_, index) =>
            fetchHistoryPage(accountId, index + 1),
          ),
        )
      : []
  return [first, ...rest]
    .flatMap((page) => page.content ?? [])
    .flatMap((dto) => {
      const entry = normalizeEntry(dto)
      return entry ? [entry] : []
    })
}

export function useHistoryQuery(accountId: number | null) {
  return useQuery({
    queryKey: queryKeys.history(accountId),
    queryFn: () => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      return fetchHistory(accountId)
    },
    enabled: accountId !== null,
  })
}
