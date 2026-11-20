import { useQuery } from '@tanstack/react-query'

import { api } from './client.ts'
import type { components } from './client.ts'

type AccountDto = components['schemas']['AccountDto']

export type Account = {
  id: number
  name: string
  active: boolean
}

function normalizeAccounts(items: AccountDto[]): Account[] {
  return items.flatMap((item) => {
    if (item.id === undefined) {
      return []
    }
    return [
      {
        id: item.id,
        name: item.name && item.name.trim() !== '' ? item.name : `Счёт ${item.id}`,
        active: item.active ?? false,
      },
    ]
  })
}

export async function fetchAccounts(): Promise<Account[]> {
  const { data, response } = await api.GET('/api/v1/accounts')
  if (!response.ok || !data) {
    throw new Error('Не удалось загрузить счета')
  }
  return normalizeAccounts(data)
}

export function useAccountsQuery(enabled: boolean) {
  return useQuery({
    queryKey: ['accounts'],
    queryFn: fetchAccounts,
    enabled,
  })
}
