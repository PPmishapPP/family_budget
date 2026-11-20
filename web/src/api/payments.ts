import { useMutation, useQueryClient } from '@tanstack/react-query'

import { api } from './client.ts'
import { readProblemMessage } from './problem.ts'
import { queryKeys } from './queryKeys.ts'

export type InstantPaymentInput = {
  name: string
  amount: number
  direction: 'EXPENSE' | 'INCOME'
}

export function useRegisterPaymentMutation(accountId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (input: InstantPaymentInput) => {
      if (accountId === null) {
        throw new Error('Счёт не выбран')
      }
      const { data, response } = await api.POST('/api/v1/accounts/{accountId}/payments', {
        params: { path: { accountId } },
        body: input,
      })
      if (!response.ok || !data) {
        throw new Error(await readProblemMessage(response, 'Не удалось выполнить платёж'))
      }
      return data
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.balance(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.paymentRules(accountId) })
      void queryClient.invalidateQueries({ queryKey: queryKeys.history(accountId) })
    },
  })
}
