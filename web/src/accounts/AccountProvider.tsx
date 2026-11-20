import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

import { useAccountsQuery } from '../api/accounts.ts'
import { useAuth } from '../auth/authContext.ts'
import { AccountContext } from './accountContext.ts'
import type { AccountContextValue } from './accountContext.ts'

const STORAGE_KEY = 'family-budget.account-id'

function readStoredAccountId(): number | null {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (!stored) {
    return null
  }
  const parsed = Number(stored)
  return Number.isFinite(parsed) ? parsed : null
}

export function AccountProvider({ children }: { children: ReactNode }) {
  const { status } = useAuth()
  const accountsQuery = useAccountsQuery(status === 'authenticated')
  const [storedId, setStoredId] = useState<number | null>(readStoredAccountId)

  const accounts = useMemo(() => accountsQuery.data ?? [], [accountsQuery.data])
  const selectedAccount =
    accounts.find((account) => account.id === storedId) ??
    accounts.find((account) => account.active) ??
    accounts.at(0) ??
    null

  useEffect(() => {
    if (selectedAccount === null) {
      localStorage.removeItem(STORAGE_KEY)
      return
    }
    localStorage.setItem(STORAGE_KEY, String(selectedAccount.id))
  }, [selectedAccount])

  const selectAccount = useCallback((accountId: number) => {
    setStoredId(accountId)
  }, [])

  const { refetch, isLoading, isError } = accountsQuery

  const value = useMemo<AccountContextValue>(
    () => ({
      accounts,
      selectedAccount,
      selectedAccountId: selectedAccount?.id ?? null,
      selectAccount,
      isLoading,
      isError,
      refetch,
    }),
    [accounts, selectedAccount, selectAccount, isLoading, isError, refetch],
  )

  return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>
}
