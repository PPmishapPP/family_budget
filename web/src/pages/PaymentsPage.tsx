import { Button, Group, Paper, Skeleton, Stack, Text, Title } from '@mantine/core'
import { IconCash, IconChartLine, IconPlus, IconTrendingUp } from '@tabler/icons-react'
import { useState } from 'react'

import { useAccount } from '../accounts/accountContext.ts'
import { useBalanceQuery } from '../api/balance.ts'
import type { InstantPaymentInput } from '../api/payments.ts'
import { useRegisterPaymentMutation } from '../api/payments.ts'
import type { PaymentRule, PaymentRuleInput } from '../api/paymentRules.ts'
import {
  useCreatePaymentRuleMutation,
  useDeletePaymentRuleMutation,
  useExecutePaymentRuleMutation,
  usePaymentRulesQuery,
  useUpdatePaymentRuleMutation,
} from '../api/paymentRules.ts'
import { EmptyState } from '../components/states/EmptyState.tsx'
import { ErrorState } from '../components/states/ErrorState.tsx'
import { LoadingState } from '../components/states/LoadingState.tsx'
import { ForecastModal } from '../components/payments/ForecastModal.tsx'
import { IncomeForecastModal } from '../components/payments/IncomeForecastModal.tsx'
import { InstantPaymentModal } from '../components/payments/InstantPaymentModal.tsx'
import { PaymentRuleFormModal } from '../components/payments/PaymentRuleFormModal.tsx'
import { RulesTable } from '../components/payments/RulesTable.tsx'
import { confirmAction } from '../lib/confirm.tsx'
import { errorMessage } from '../lib/errors.ts'
import { formatRub } from '../lib/format.ts'
import { notifyError, notifySuccess } from '../lib/notify.ts'

type RuleModalState = {
  opened: boolean
  rule: PaymentRule | null
}

export function PaymentsPage() {
  const { selectedAccount, isLoading: accountsLoading } = useAccount()
  const accountId = selectedAccount?.id ?? null

  const balanceQuery = useBalanceQuery(accountId)
  const rulesQuery = usePaymentRulesQuery(accountId)

  const createRule = useCreatePaymentRuleMutation(accountId)
  const updateRule = useUpdatePaymentRuleMutation(accountId)
  const deleteRule = useDeletePaymentRuleMutation(accountId)
  const executeRule = useExecutePaymentRuleMutation(accountId)
  const registerPayment = useRegisterPaymentMutation(accountId)

  const [ruleModal, setRuleModal] = useState<RuleModalState>({
    opened: false,
    rule: null,
  })
  const [instantOpened, setInstantOpened] = useState(false)
  const [forecastOpened, setForecastOpened] = useState(false)
  const [incomeOpened, setIncomeOpened] = useState(false)

  const rules = rulesQuery.data ?? []

  function openCreateRule() {
    setRuleModal({ opened: true, rule: null })
  }

  function openEditRule(rule: PaymentRule) {
    setRuleModal({ opened: true, rule })
  }

  function closeRuleModal() {
    setRuleModal({ opened: false, rule: null })
  }

  async function handleRuleSubmit(input: PaymentRuleInput) {
    try {
      if (ruleModal.rule) {
        await updateRule.mutateAsync({ ruleId: ruleModal.rule.id, input })
        notifySuccess('Правило обновлено')
      } else {
        await createRule.mutateAsync(input)
        notifySuccess('Правило добавлено')
      }
      closeRuleModal()
    } catch (error) {
      notifyError(errorMessage(error))
    }
  }

  async function handleExecuteRule(rule: PaymentRule) {
    try {
      await executeRule.mutateAsync(rule.id)
      notifySuccess(`Платёж «${rule.name}» выполнен`)
    } catch (error) {
      notifyError(errorMessage(error))
    }
  }

  function handleDeleteRule(rule: PaymentRule) {
    confirmAction({
      title: 'Удаление правила',
      message: `Удалить правило «${rule.name}»?`,
      confirmLabel: 'Удалить',
      onConfirm: async () => {
        try {
          await deleteRule.mutateAsync(rule.id)
          notifySuccess('Правило удалено')
        } catch (error) {
          notifyError(errorMessage(error))
        }
      },
    })
  }

  async function handleInstantPayment(input: InstantPaymentInput) {
    try {
      await registerPayment.mutateAsync(input)
      notifySuccess('Платёж сохранён')
      setInstantOpened(false)
    } catch (error) {
      notifyError(errorMessage(error))
    }
  }

  if (accountsLoading) {
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

  const balance = balanceQuery.data ?? 0
  const executingId =
    executeRule.isPending && executeRule.variables !== undefined ? executeRule.variables : null
  const deletingId =
    deleteRule.isPending && deleteRule.variables !== undefined ? deleteRule.variables : null

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-start" gap="md" wrap="wrap">
        <Stack gap={2}>
          <Title order={2}>План платежей</Title>
          <Text size="sm" c="dimmed">
            {selectedAccount.name}
          </Text>
        </Stack>

        <Paper withBorder radius="md" px="lg" py="sm" miw={220}>
          <Text size="xs" c="dimmed" fw={600} tt="uppercase">
            Текущий баланс
          </Text>
          {balanceQuery.isLoading ? (
            <Skeleton height={32} width={170} mt={4} />
          ) : balanceQuery.isError ? (
            <Text size="sm" c="red" mt={4}>
              Не удалось загрузить баланс
            </Text>
          ) : (
            <Text fz={26} fw={700} c={balance < 0 ? 'red' : undefined}>
              {formatRub(balance)}
            </Text>
          )}
        </Paper>
      </Group>

      <Group gap="xs" wrap="wrap">
        <Button leftSection={<IconPlus size={18} />} onClick={openCreateRule}>
          Добавить
        </Button>
        <Button
          variant="light"
          leftSection={<IconCash size={18} />}
          onClick={() => setInstantOpened(true)}
        >
          Разовый платёж
        </Button>
        <Button
          variant="light"
          leftSection={<IconChartLine size={18} />}
          onClick={() => setForecastOpened(true)}
        >
          Выполнить прогноз платежей
        </Button>
        <Button
          variant="light"
          leftSection={<IconTrendingUp size={18} />}
          onClick={() => setIncomeOpened(true)}
        >
          Расчет дохода
        </Button>
      </Group>

      {rulesQuery.isLoading ? (
        <LoadingState label="Загрузка правил…" />
      ) : rulesQuery.isError ? (
        <ErrorState
          description={errorMessage(rulesQuery.error, 'Не удалось загрузить правила платежей.')}
          onRetry={() => void rulesQuery.refetch()}
        />
      ) : rules.length === 0 ? (
        <EmptyState
          title="Правил пока нет"
          description="Добавьте первое правило, чтобы планировать платежи."
          action={
            <Button leftSection={<IconPlus size={18} />} onClick={openCreateRule}>
              Добавить
            </Button>
          }
        />
      ) : (
        <RulesTable
          rules={rules}
          executingId={executingId}
          deletingId={deletingId}
          onEdit={openEditRule}
          onExecute={(rule) => void handleExecuteRule(rule)}
          onDelete={handleDeleteRule}
        />
      )}

      <PaymentRuleFormModal
        opened={ruleModal.opened}
        rule={ruleModal.rule}
        submitting={createRule.isPending || updateRule.isPending}
        onClose={closeRuleModal}
        onSubmit={handleRuleSubmit}
      />

      <InstantPaymentModal
        opened={instantOpened}
        accountName={selectedAccount.name}
        submitting={registerPayment.isPending}
        onClose={() => setInstantOpened(false)}
        onSubmit={handleInstantPayment}
      />

      <ForecastModal
        opened={forecastOpened}
        accountId={accountId}
        onClose={() => setForecastOpened(false)}
      />

      <IncomeForecastModal
        opened={incomeOpened}
        accountId={accountId}
        onClose={() => setIncomeOpened(false)}
      />
    </Stack>
  )
}
