import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { isPaymentRuleType } from '../lib/paymentRules.ts'
import type { PaymentRuleType } from '../lib/paymentRules.ts'
import { api } from './client.ts'
import type { components } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

type PaymentRuleDto = components['schemas']['PaymentRuleDto']
type PaymentRuleRequest = components['schemas']['PaymentRuleRequest']

export type PaymentRule = {
  id: number
  name: string
  sum: number
  type: PaymentRuleType
  pass: number
  nextDay: string
  endDate: string | null
}

export type PaymentRuleInput = {
  name: string
  sum: number
  type: PaymentRuleType
  nextDay: string
  endDate: string | null
}

function normalizeRule(dto: PaymentRuleDto): PaymentRule | null {
  if (
    dto.id === undefined ||
    dto.sum === undefined ||
    dto.type === undefined ||
    !dto.nextDay ||
    !isPaymentRuleType(dto.type)
  ) {
    return null
  }
  return {
    id: dto.id,
    name: dto.name ?? '',
    sum: dto.sum,
    type: dto.type,
    pass: dto.pass ?? 0,
    nextDay: dto.nextDay,
    endDate: dto.endDate ?? null,
  }
}

function toRequestBody(input: PaymentRuleInput): PaymentRuleRequest {
  return {
    name: input.name,
    sum: input.sum,
    type: input.type,
    nextDay: input.nextDay,
    endDate: input.endDate ?? undefined,
  }
}

async function fetchPaymentRules(accountId: number): Promise<PaymentRule[]> {
  const { data, response } = await api.GET('/api/v1/accounts/{accountId}/payment-rules', {
    params: { path: { accountId } },
  })
  if (!response.ok || !data) {
    throw new Error(await readProblemMessage(response, 'Не удалось загрузить правила платежей'))
  }
  return data
    .flatMap((dto) => {
      const rule = normalizeRule(dto)
      return rule ? [rule] : []
    })
    .sort((a, b) => a.nextDay.localeCompare(b.nextDay))
}

export function usePaymentRulesQuery(accountId: number | null) {
  return useQuery({
    queryKey: queryKeys.paymentRules(accountId),
    queryFn: () => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      return fetchPaymentRules(accountId)
    },
    enabled: accountId !== null,
  })
}

export function useCreatePaymentRuleMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (input: PaymentRuleInput) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.POST('/api/v1/accounts/{accountId}/payment-rules', {
        params: { path: { accountId } },
        body: toRequestBody(input),
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось добавить правило'))
      }
      const rule = normalizeRule(data)
      if (!rule) {
        throw new Error('Сервер вернул некорректное правило')
      }
      return rule
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.paymentRules(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.balance(accountId) })
    },
  })
}

export function useUpdatePaymentRuleMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ ruleId, input }: { ruleId: number; input: PaymentRuleInput }) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.PUT(
        '/api/v1/accounts/{accountId}/payment-rules/{ruleId}',
        {
          params: { path: { accountId, ruleId } },
          body: toRequestBody(input),
        },
      )
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось сохранить правило'))
      }
      const rule = normalizeRule(data)
      if (!rule) {
        throw new Error('Сервер вернул некорректное правило')
      }
      return rule
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.paymentRules(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.balance(accountId) })
    },
  })
}

export function useDeletePaymentRuleMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (ruleId: number) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { response } = await api.DELETE('/api/v1/accounts/{accountId}/payment-rules/{ruleId}', {
        params: { path: { accountId, ruleId } },
      })
      if (!response.ok) {
        throw new Error(await readProblemMessage(response, 'Не удалось удалить правило'))
      }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.paymentRules(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.balance(accountId) })
    },
  })
}

export function useExecutePaymentRuleMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (ruleId: number) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { response } = await api.POST(
        '/api/v1/accounts/{accountId}/payment-rules/{ruleId}/execute',
        { params: { path: { accountId, ruleId } } },
      )
      if (!response.ok) {
        throw new Error(await readProblemMessage(response, 'Не удалось выполнить платёж'))
      }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.paymentRules(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.balance(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.history(accountId) })
    },
  })
}
