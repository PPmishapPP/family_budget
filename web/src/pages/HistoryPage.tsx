import { Stack, Text, Title } from '@mantine/core'

import { useAccount } from '../accounts/accountContext.ts'
import { useHistoryQuery } from '../api/history.ts'
import { HistoryTable } from '../components/history/HistoryTable.tsx'
import { EmptyState } from '../components/states/EmptyState.tsx'
import { ErrorState } from '../components/states/ErrorState.tsx'
import { LoadingState } from '../components/states/LoadingState.tsx'
import { errorMessage } from '../lib/errors.ts'

type HistoryContentProps = {
  accountId: number
  accountName: string
}

function HistoryContent({ accountId, accountName }: HistoryContentProps) {
  const historyQuery = useHistoryQuery(accountId)

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
      ) : (historyQuery.data?.length ?? 0) === 0 ? (
        <EmptyState title="История пуста" description="На этом счёте пока нет операций." />
      ) : (
        <HistoryTable entries={historyQuery.data ?? []} />
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
