import { zodResolver } from '@hookform/resolvers/zod'
import {
  Box,
  Button,
  Group,
  Modal,
  NumberInput,
  Radio,
  Stack,
  Text,
  TextInput,
} from '@mantine/core'
import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'

import type { InstantPaymentInput } from '../../api/payments.ts'
import { formatDate, toIsoDate } from '../../lib/format.ts'

const instantPaymentSchema = z.object({
  name: z.string().trim().min(1, 'Укажите название').max(256, 'Не более 256 символов'),
  direction: z.enum(['EXPENSE', 'INCOME']),
  amount: z.custom<number>(
    (value) => typeof value === 'number' && Number.isInteger(value) && value >= 1,
    'Сумма должна быть целым числом больше нуля',
  ),
})

type InstantPaymentValues = z.infer<typeof instantPaymentSchema>

type InstantPaymentModalProps = {
  opened: boolean
  accountName: string | null
  submitting: boolean
  onClose: () => void
  onSubmit: (input: InstantPaymentInput) => Promise<void>
}

export function InstantPaymentModal({
  opened,
  accountName,
  submitting,
  onClose,
  onSubmit,
}: InstantPaymentModalProps) {
  const form = useForm<InstantPaymentValues>({
    resolver: zodResolver(instantPaymentSchema),
    defaultValues: {
      name: '',
      direction: 'EXPENSE',
      amount: 0,
    },
  })

  useEffect(() => {
    if (opened) {
      form.reset({ name: '', direction: 'EXPENSE', amount: 0 })
    }
  }, [opened, form])

  const submit = form.handleSubmit(async (values) => {
    await onSubmit({
      name: values.name.trim(),
      direction: values.direction,
      amount: values.amount,
    })
  })

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title="Разовый платёж"
      size="md"
      centered
      closeOnClickOutside={!submitting}
      closeOnEscape={!submitting}
      withCloseButton={!submitting}
    >
      <form onSubmit={submit}>
        <Stack gap="sm">
          <Box p="xs" style={{ backgroundColor: 'var(--mantine-color-gray-0)', borderRadius: 8 }}>
            <Text size="sm">Счёт: {accountName ?? '—'}</Text>
            <Text size="sm">Дата: {formatDate(toIsoDate(new Date()))}</Text>
          </Box>

          <Controller
            control={form.control}
            name="name"
            render={({ field, fieldState }) => (
              <TextInput
                label="Название платежа"
                placeholder="Например, продукты"
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
            name="direction"
            render={({ field, fieldState }) => (
              <Radio.Group
                label="Тип операции"
                withAsterisk
                value={field.value}
                onChange={(value) => field.onChange(value as 'EXPENSE' | 'INCOME')}
                error={fieldState.error?.message}
              >
                <Group mt="xs">
                  <Radio value="EXPENSE" label="Расход" />
                  <Radio value="INCOME" label="Доход" />
                </Group>
              </Radio.Group>
            )}
          />

          <Controller
            control={form.control}
            name="amount"
            render={({ field, fieldState }) => (
              <NumberInput
                label="Сумма, ₽"
                withAsterisk
                min={1}
                step={1}
                allowDecimal={false}
                allowNegative={false}
                value={field.value}
                onChange={(value) => field.onChange(typeof value === 'number' ? value : undefined)}
                onBlur={field.onBlur}
                error={fieldState.error?.message}
              />
            )}
          />

          <Group justify="flex-end" mt="md">
            <Button variant="default" onClick={onClose} disabled={submitting}>
              Отмена
            </Button>
            <Button type="submit" loading={submitting}>
              Сохранить
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  )
}
