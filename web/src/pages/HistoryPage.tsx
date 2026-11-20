import { Stack, Text, Title } from '@mantine/core'
import { useState } from 'react'

import { useAccount } from '../accounts/accountContext.ts'
import {
  HISTORY_PAGE_SIZE,
  useHistoryQuery,
  type HistorySortDirection,
  type HistorySortKey,
} from '../api/history.ts'
import { HistoryTable, type HistorySort } from '../components/history/HistoryTable.tsx'
import { EmptyState } from '../components/states/EmptyState.tsx'
import { ErrorState } from '../components/states/ErrorState.tsx'
import { LoadingState } from '../components/states/LoadingState.tsx'
import { errorMessage } from '../lib/errors.ts'

const DEFAULT_SORT: HistorySort = { key: 'date', direction: 'desc' }

type HistoryContentProps = {
  accountId: number
  accountName: string
}

function HistoryContent({ accountId, accountName }: HistoryContentProps) {
  const [page, setPage] = useState(1)
  const [sort, setSort] = useState<HistorySort>(DEFAULT_SORT)

  const historyQuery = useHistoryQuery(accountId, {
    page: page - 1,
    size: HISTORY_PAGE_SIZE,
    sort: sort.key,
    direction: sort.direction,
  })

  function handleSort(key: HistorySortKey) {
    setSort((previous) => {
      const direction: HistorySortDirection =
        previous.key === key ? (previous.direction === 'asc' ? 'desc' : 'asc') : 'asc'
      return { key, direction }
    })
    setPage(1)
  }

  const result = historyQuery.data

  return (
    <Stack gap="lg">
      <Stack gap={2}>
        <Title order={2}>История платежей</Title>
        <Text size="sm" c="dimmed">
          {accountName}
        </Text>
      </Stack>

      {historyQuery.isLoading ? (
        <LoadingState label="Загрузка истории…" />
      ) : historyQuery.isError ? (
        <ErrorState
          description={errorMessage(historyQuery.error, 'Не удалось загрузить историю платежей.')}
          onRetry={() => void historyQuery.refetch()}
        />
      ) : (result?.totalElements ?? 0) === 0 ? (
        <EmptyState title="История пуста" description="На этом счёте пока нет операций." />
      ) : (
        <HistoryTable
          entries={result?.entries ?? []}
          totalElements={result?.totalElements ?? 0}
          totalPages={result?.totalPages ?? 0}
          page={page}
          pageSize={HISTORY_PAGE_SIZE}
          sort={sort}
          onSort={handleSort}
          onPageChange={setPage}
        />
      )}
    </Stack>
  )
}

export function HistoryPage() {
  const { selectedAccount, isLoading } = useAccount()

  if (isLoading) {
    return <LoadingState label="Загрузка счёта…" />
  }

  if (!selectedAccount) {
    return (
      <EmptyState
        title="Нет доступных счетов"
        description="Обратитесь к администратору, чтобы получить доступ к счёту."
      />
    )
  }

  return (
    <HistoryContent
      key={selectedAccount.id}
      accountId={selectedAccount.id}
      accountName={selectedAccount.name}
    />
  )
}
