import { useMutation } from '@tanstack/react-query'

import { api } from './client.ts'
import type { components } from './client.ts'
import type { PaymentRuleInput } from './paymentRules.ts'
import { readProblemMessage } from './problem.ts'

type ForecastItemDto = components['schemas']['ForecastItemDto']
type IncomeForecastItemDto = components['schemas']['IncomeForecastItemDto']

export type ForecastItem = {
  date: string
  operation: string
  sum: number
  balance: number
}

export type IncomeForecastItem = {
  date: string
  balance: number
  increase: number | null
}

function normalizeForecastItem(dto: ForecastItemDto): ForecastItem | null {
  if (!dto.date) {
    return null
  }
  return {
    date: dto.date,
    operation: dto.operation ?? '',
    sum: dto.sum ?? 0,
    balance: dto.balance ?? 0,
  }
}

function normalizeIncomeItem(dto: IncomeForecastItemDto): IncomeForecastItem | null {
  if (!dto.date) {
    return null
  }
  return {
    date: dto.date,
    balance: dto.balance ?? 0,
    increase: dto.increase ?? null,
  }
}

export type ForecastInput = {
  to: string
  rules?: PaymentRuleInput[]
}

export function useForecastMutation(accountId: number | null) {
  return useMutation({
    mutationFn: async ({ to, rules }: ForecastInput) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.POST('/api/v1/accounts/{accountId}/forecast', {
        params: { path: { accountId } },
        body: {
          to,
          rules: rules?.map((rule) => ({
            name: rule.name,
            sum: rule.sum,
            type: rule.type,
            nextDay: rule.nextDay,
            endDate: rule.endDate ?? undefined,
          })),
        },
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось выполнить прогноз'))
      }
      return data.flatMap((dto) => {
        const item = normalizeForecastItem(dto)
        return item ? [item] : []
      })
    },
  })
}

export function useIncomeForecastMutation(accountId: number | null) {
  return useMutation({
    mutationFn: async (to: string) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.POST('/api/v1/accounts/{accountId}/income-forecast', {
        params: { path: { accountId } },
        body: { to },
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось выполнить расчёт дохода'))
      }
      return data.flatMap((dto) => {
        const item = normalizeIncomeItem(dto)
        return item ? [item] : []
      })
    },
  })
}
