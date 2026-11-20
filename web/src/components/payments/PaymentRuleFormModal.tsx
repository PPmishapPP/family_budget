import { zodResolver } from '@hookform/resolvers/zod'
import { Button, Group, Modal, NumberInput, Select, Stack, Switch, TextInput } from '@mantine/core'
import { useEffect, useMemo } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'

import type { PaymentRuleDraft, PaymentRuleInput } from '../../api/paymentRules.ts'
import { toIsoDate } from '../../lib/format.ts'
import { PAYMENT_RULE_TYPE_OPTIONS, PAYMENT_RULE_TYPES } from '../../lib/paymentRules.ts'
import type { PaymentRuleType } from '../../lib/paymentRules.ts'

const ruleSchema = z.object({
  name: z.string().trim().min(1, 'Укажите название').max(256, 'Не более 256 символов'),
  sum: z.custom<number>(
    (value) => typeof value === 'number' && Number.isInteger(value),
    'Сумма должна быть целым числом',
  ),
  type: z.enum(PAYMENT_RULE_TYPES),
  nextDay: z.custom<string>(
    (value) => typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value),
    'Укажите дату',
  ),
  active: z.boolean(),
  endDate: z
    .string()
    .nullable()
    .refine(
      (value) => value === null || value === '' || /^\d{4}-\d{2}-\d{2}$/.test(value),
      'Некорректная дата',
    ),
})

type RuleFormValues = z.infer<typeof ruleSchema>

type PaymentRuleFormModalProps = {
  opened: boolean
  draft: PaymentRuleDraft | null
  onClose: () => void
  onSubmit: (input: PaymentRuleInput) => void
}

function toFormValues(draft: PaymentRuleDraft | null): RuleFormValues {
  if (!draft) {
    return {
      name: '',
      sum: 0,
      type: 'MONTHLY',
      nextDay: toIsoDate(new Date()),
      active: true,
      endDate: null,
    }
  }
  return {
    name: draft.current.name,
    sum: draft.current.sum,
    type: draft.current.type,
    nextDay: draft.current.nextDay,
    active: draft.current.active,
    endDate: draft.current.endDate,
  }
}

export function PaymentRuleFormModal({
  opened,
  draft,
  onClose,
  onSubmit,
}: PaymentRuleFormModalProps) {
  const defaults = useMemo(() => toFormValues(draft), [draft])
  const form = useForm<RuleFormValues>({
    resolver: zodResolver(ruleSchema),
    defaultValues: defaults,
  })

  useEffect(() => {
    if (opened) {
      form.reset(defaults)
    }
  }, [opened, defaults, form])

  const isNew = draft === null || draft.id === null

  const submit = form.handleSubmit((values) => {
    onSubmit({
      name: values.name.trim(),
      sum: values.sum,
      type: values.type,
      nextDay: values.nextDay,
      active: values.active,
      endDate: values.endDate === '' ? null : values.endDate,
    })
  })

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title={isNew ? 'Новое правило' : 'Изменить правило'}
      size="md"
      centered
    >
      <form onSubmit={submit}>
        <Stack gap="sm">
          <Controller
            control={form.control}
            name="name"
            render={({ field, fieldState }) => (
              <TextInput
                label="Название"
                withAsterisk
                maxLength={256}
                value={field.value}
                onChange={field.onChange}
                onBlur={field.onBlur}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="sum"
            render={({ field, fieldState }) => (
              <NumberInput
                label="Сумма, ₽"
                description="Отрицательная — расход, положительная — доход"
                withAsterisk
                step={1}
                allowDecimal={false}
                value={field.value}
                onChange={(value) => field.onChange(typeof value === 'number' ? value : undefined)}
                onBlur={field.onBlur}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="type"
            render={({ field, fieldState }) => (
              <Select
                label="Тип"
                withAsterisk
                data={PAYMENT_RULE_TYPE_OPTIONS}
                value={field.value}
                onChange={(value) => {
                  if (value) {
                    field.onChange(value as PaymentRuleType)
                  }
                }}
                allowDeselect={false}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="nextDay"
            render={({ field, fieldState }) => (
              <TextInput
                label="Следующий платёж"
                type="date"
                withAsterisk
                value={field.value}
                onChange={(event) => field.onChange(event.currentTarget.value)}
                onBlur={field.onBlur}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="endDate"
            render={({ field, fieldState }) => (
              <TextInput
                label="Дата окончания"
                type="date"
                description="Необязательно"
                value={field.value ?? ''}
                onChange={(event) =>
                  field.onChange(
                    event.currentTarget.value === '' ? null : event.currentTarget.value,
                  )
                }
                onBlur={field.onBlur}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="active"
            render={({ field }) => (
              <Switch
                label="Активно"
                checked={field.value}
                onChange={(event) => field.onChange(event.currentTarget.checked)}
              />
            )}
          />

          <Group justify="flex-end" mt="md">
            <Button variant="default" onClick={onClose}>
              Отмена
            </Button>
            <Button type="submit">{isNew ? 'Добавить' : 'Сохранить'}</Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  )
}
