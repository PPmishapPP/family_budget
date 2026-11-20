import { createContext, useContext } from 'react'

import type { Account } from '../api/accounts.ts'

export type AccountContextValue = {
  accounts: Account[]
  selectedAccount: Account | null
  selectedAccountId: number | null
  selectAccount: (accountId: number) => void
  isLoading: boolean
  isError: boolean
  refetch: () => void
}

export const AccountContext = createContext<AccountContextValue | null>(null)

export function useAccount(): AccountContextValue {
  const context = useContext(AccountContext)
  if (!context) {
    throw new Error('useAccount должен использоваться внутри AccountProvider')
  }
  return context
}
