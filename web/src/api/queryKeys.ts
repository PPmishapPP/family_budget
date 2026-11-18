import type { MediaType } from '../lib/media.ts'

export const queryKeys = {
  accounts: ['accounts'] as const,
  balance: (accountId: number | null) => ['balance', accountId] as const,
  paymentRules: (accountId: number | null) => ['payment-rules', accountId] as const,
  history: (accountId: number | null) => ['history', accountId] as const,
  media: (accountId: number | null, type: MediaType | null) => ['media', accountId, type] as const,
  mediaRoot: (accountId: number | null) => ['media', accountId] as const,
  mediaReference: ['media-reference'] as const,
}
