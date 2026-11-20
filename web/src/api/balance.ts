import { useQuery } from '@tanstack/react-query'

import { api } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

async function fetchBalance(accountId: number): Promise<number> {
  const { data, response } = await api.GET('/api/v1/accounts/{accountId}/balance', {
    params: { path: { accountId } },
  })
  if (!response.ok || !data) {
    throw new Error(await readProblemMessage(response, 'Не удалось загрузить баланс'))
  }
  return data.balance ?? 0
}

export function useBalanceQuery(accountId: number | null) {
  return useQuery({
    queryKey: queryKeys.balance(accountId),
    queryFn: () => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      return fetchBalance(accountId)
    },
    enabled: accountId !== null,
  })
}
