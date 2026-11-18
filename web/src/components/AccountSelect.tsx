import { Select } from '@mantine/core'

import { useAccount } from '../accounts/accountContext.ts'

export function AccountSelect() {
  const { accounts, selectedAccountId, selectAccount, isLoading } = useAccount()

  return (
    <Select
      label="Счёт"
      placeholder={isLoading ? 'Загрузка…' : 'Выберите счёт'}
      data={accounts.map((account) => ({ value: String(account.id), label: account.name }))}
      value={selectedAccountId === null ? null : String(selectedAccountId)}
      onChange={(value) => {
        if (value !== null) {
          selectAccount(Number(value))
        }
      }}
      allowDeselect={false}
      disabled={isLoading || accounts.length === 0}
      size="sm"
      checkIconPosition="right"
    />
  )
}
