import { Button, Group, Paper, Skeleton, Stack, Text, Title } from '@mantine/core'
import { IconCash, IconChartLine, IconPlus, IconTrendingUp } from '@tabler/icons-react'
import { useEffect, useRef, useState } from 'react'

import { useAccount } from '../accounts/accountContext.ts'
import { useBalanceQuery } from '../api/balance.ts'
import type { InstantPaymentInput } from '../api/payments.ts'
import { useRegisterPaymentMutation } from '../api/payments.ts'
import type {
  PaymentRule,
  PaymentRuleDraft,
  PaymentRuleInput,
  PaymentRulesBatch,
} from '../api/paymentRules.ts'
import {
  createDraftFromInput,
  createDraftFromRule,
  isDraftModified,
  isDraftPending,
  useExecutePaymentRuleMutation,
  usePaymentRulesQuery,
  useSavePaymentRulesMutation,
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
  draftKey: string | null
}

export function PaymentsPage() {
  const { selectedAccount, isLoading: accountsLoading } = useAccount()
  const accountId = selectedAccount?.id ?? null

  const balanceQuery = useBalanceQuery(accountId)
  const rulesQuery = usePaymentRulesQuery(accountId)

  const saveRules = useSavePaymentRulesMutation(accountId)
  const executeRule = useExecutePaymentRuleMutation(accountId)
  const registerPayment = useRegisterPaymentMutation(accountId)

  const [drafts, setDrafts] = useState<PaymentRuleDraft[]>([])
  const [editMode, setEditMode] = useState(false)
  const [stateAccountId, setStateAccountId] = useState(accountId)
  const [ruleModal, setRuleModal] = useState<RuleModalState>({
    opened: false,
    draftKey: null,
  })
  const [instantOpened, setInstantOpened] = useState(false)
  const [forecastOpened, setForecastOpened] = useState(false)
  const [incomeOpened, setIncomeOpened] = useState(false)

  if (stateAccountId !== accountId) {
    setStateAccountId(accountId)
    setDrafts([])
    setEditMode(false)
    setRuleModal({ opened: false, draftKey: null })
  }

  const hasChanges = drafts.some(isDraftPending)
  const hasChangesRef = useRef(hasChanges)
  const lastSyncedRef = useRef<{ accountId: number | null; data: PaymentRule[] | undefined }>({
    accountId: null,
    data: undefined,
  })

  useEffect(() => {
    hasChangesRef.current = hasChanges
  }, [hasChanges])

  useEffect(() => {
    const data = rulesQuery.data
    if (!data) {
      return
    }
    const last = lastSyncedRef.current
    const accountChanged = last.accountId !== accountId
    if (!accountChanged && last.data === data) {
      return
    }
    if (!accountChanged && hasChangesRef.current) {
      return
    }
    lastSyncedRef.current = { accountId, data }
    setDrafts(data.map(createDraftFromRule))
  }, [accountId, rulesQuery.data])

  const activeDrafts = drafts.filter((draft) => !draft.markedForDeletion)
  const canForecast = activeDrafts.length > 0
  const modalDraft =
    ruleModal.draftKey === null
      ? null
      : (drafts.find((draft) => draft.key === ruleModal.draftKey) ?? null)

  function openCreateRule() {
    setRuleModal({ opened: true, draftKey: null })
  }

  function openEditRule(draft: PaymentRuleDraft) {
    setRuleModal({ opened: true, draftKey: draft.key })
  }

  function closeRuleModal() {
    setRuleModal({ opened: false, draftKey: null })
  }

  function handleRuleSubmit(input: PaymentRuleInput) {
    if (ruleModal.draftKey === null) {
      const draft = createDraftFromInput(input)
      setDrafts((current) => [draft, ...current])
    } else {
      const key = ruleModal.draftKey
      setDrafts((current) =>
        current.map((draft) =>
          draft.key === key ? { ...draft, current: input, markedForDeletion: false } : draft,
        ),
      )
    }
    closeRuleModal()
  }

  async function handleExecuteRule(draft: PaymentRuleDraft) {
    if (draft.id === null) {
      return
    }
    try {
      await executeRule.mutateAsync(draft.id)
      notifySuccess(`Платёж «${draft.current.name}» выполнен`)
    } catch (error) {
      notifyError(errorMessage(error))
    }
  }

  function handleDeleteRule(draft: PaymentRuleDraft) {
    if (draft.id === null) {
      setDrafts((current) => current.filter((item) => item.key !== draft.key))
      return
    }
    confirmAction({
      title: 'Удаление правила',
      message: `Удалить правило «${draft.current.name}»?`,
      confirmLabel: 'Удалить',
      onConfirm: () => {
        setDrafts((current) =>
          current.map((item) =>
            item.key === draft.key ? { ...item, markedForDeletion: true } : item,
          ),
        )
      },
    })
  }

  function handleCancelChanges() {
    lastSyncedRef.current = { accountId, data: rulesQuery.data }
    setDrafts((rulesQuery.data ?? []).map(createDraftFromRule))
    notifySuccess('Изменения отменены')
  }

  async function handleSaveChanges() {
    const batch: PaymentRulesBatch = { creates: [], updates: [], deletes: [] }
    for (const draft of drafts) {
      if (draft.markedForDeletion) {
        if (draft.id !== null) {
          batch.deletes.push(draft.id)
        }
        continue
      }
      if (draft.id === null) {
        batch.creates.push(draft.current)
        continue
      }
      if (isDraftModified(draft)) {
        batch.updates.push({ ruleId: draft.id, input: draft.current })
      }
    }

    try {
      await saveRules.mutateAsync(batch)
      const fresh = await rulesQuery.refetch()
      lastSyncedRef.current = { accountId, data: fresh.data }
      setDrafts((fresh.data ?? []).map(createDraftFromRule))
      notifySuccess('Изменения сохранены')
    } catch (error) {
      notifyError(errorMessage(error))
    }
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
        <Button leftSection={<IconPlus size={18} />} onClick={openCreateRule} disabled={!editMode}>
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
          disabled={!canForecast}
        >
          Выполнить прогноз платежей
        </Button>
        <Button
          variant="light"
          leftSection={<IconTrendingUp size={18} />}
          onClick={() => setIncomeOpened(true)}
          disabled={!canForecast}
        >
          Расчет дохода
        </Button>
        <Button
          variant={editMode ? 'light' : 'filled'}
          onClick={() => setEditMode((current) => !current)}
        >
          {editMode ? 'Режим просмотра' : 'Режим редактирования'}
        </Button>
        <Button
          variant="default"
          onClick={handleCancelChanges}
          disabled={!hasChanges || saveRules.isPending}
        >
          Отменить изменения
        </Button>
        <Button
          onClick={() => void handleSaveChanges()}
          loading={saveRules.isPending}
          disabled={!hasChanges || saveRules.isPending}
        >
          Сохранить
        </Button>
      </Group>

      {rulesQuery.isLoading ? (
        <LoadingState label="Загрузка правил…" />
      ) : rulesQuery.isError ? (
        <ErrorState
          description={errorMessage(rulesQuery.error, 'Не удалось загрузить правила платежей.')}
          onRetry={() => void rulesQuery.refetch()}
        />
      ) : drafts.length === 0 ? (
        <EmptyState
          title="Правил пока нет"
          description="Добавьте первое правило, чтобы планировать платежи."
          action={
            <Button
              leftSection={<IconPlus size={18} />}
              onClick={() => {
                setEditMode(true)
                openCreateRule()
              }}
            >
              Добавить
            </Button>
          }
        />
      ) : (
        <RulesTable
          drafts={drafts}
          editMode={editMode}
          executingId={executingId}
          onEdit={openEditRule}
          onExecute={(draft) => void handleExecuteRule(draft)}
          onDelete={handleDeleteRule}
        />
      )}

      <PaymentRuleFormModal
        opened={ruleModal.opened}
        draft={modalDraft}
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
        rules={activeDrafts.map((draft) => draft.current)}
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
