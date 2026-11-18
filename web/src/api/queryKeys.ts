export const queryKeys = {
  accounts: ['accounts'] as const,
  balance: (accountId: number | null) => ['balance', accountId] as const,
  paymentRules: (accountId: number | null) => ['payment-rules', accountId] as const,
  history: (accountId: number | null) => ['history', accountId] as const,
}
