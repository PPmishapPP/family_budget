import { Alert, Button, Group, Modal, Stack, Table, Text, TextInput } from '@mantine/core'
import { IconAlertTriangle, IconCalculator } from '@tabler/icons-react'
import { useState } from 'react'

import { useIncomeForecastMutation } from '../../api/forecast.ts'
import { formatDate, formatRub, toIsoDate } from '../../lib/format.ts'
import { EmptyState } from '../states/EmptyState.tsx'
import { LoadingState } from '../states/LoadingState.tsx'

type IncomeForecastModalProps = {
  opened: boolean
  accountId: number | null
  onClose: () => void
}

function addDays(days: number): string {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return toIsoDate(date)
}

function addYears(years: number): string {
  const date = new Date()
  date.setFullYear(date.getFullYear() + years)
  return toIsoDate(date)
}

function firstDayAfterMonths(months: number): string {
  const date = new Date()
  date.setMonth(date.getMonth() + months)
  date.setDate(1)
  return toIsoDate(date)
}

function firstDayAfterYears(years: number): string {
  const date = new Date()
  date.setFullYear(date.getFullYear() + years)
  date.setDate(1)
  return toIsoDate(date)
}

export function IncomeForecastModal({ opened, accountId, onClose }: IncomeForecastModalProps) {
  const [date, setDate] = useState('')
  const incomeForecast = useIncomeForecastMutation(accountId)

  const minDate = addDays(1)
  const maxDate = addYears(3)
  const canCalculate = date !== '' && date >= minDate && date <= maxDate

  function handleClose() {
    incomeForecast.reset()
    setDate('')
    onClose()
  }

  function selectDate(value: string) {
    setDate(value)
    incomeForecast.reset()
  }

  function handleCalculate() {
    if (canCalculate) {
      incomeForecast.mutate(date)
    }
  }

  return (
    <Modal opened={opened} onClose={handleClose} title="Расчёт дохода" size="xl" centered>
      <Stack gap="md">
        <Group align="flex-end" gap="xs" wrap="wrap">
          <TextInput
            label="Дата окончания расчёта"
            type="date"
            value={date}
            onChange={(event) => selectDate(event.currentTarget.value)}
            min={minDate}
            max={maxDate}
            disabled={incomeForecast.isPending}
            style={{ flex: '1 1 220px' }}
          />
          <Button
            variant="subtle"
            onClick={() => selectDate(firstDayAfterMonths(1))}
            disabled={incomeForecast.isPending}
          >
            +1 месяц
          </Button>
          <Button
            variant="subtle"
            onClick={() => selectDate(firstDayAfterYears(1))}
            disabled={incomeForecast.isPending}
          >
            +1 год
          </Button>
          <Button
            variant="subtle"
            onClick={() => selectDate(firstDayAfterYears(2))}
            disabled={incomeForecast.isPending}
          >
            +2 года
          </Button>
        </Group>

        {incomeForecast.isError && (
          <Alert color="red" icon={<IconAlertTriangle size={18} />}>
            <Text size="sm">
              {incomeForecast.error instanceof Error
                ? incomeForecast.error.message
                : 'Не удалось выполнить расчёт дохода'}
            </Text>
          </Alert>
        )}

        {incomeForecast.isPending && <LoadingState label="Расчёт дохода…" />}

        {incomeForecast.isSuccess &&
          (incomeForecast.data.length === 0 ? (
            <EmptyState
              title="Расчёт пуст"
              description="На выбранную дату доходов по правилам нет."
            />
          ) : (
            <Table.ScrollContainer minWidth={480} mah={360} style={{ overflow: 'auto' }}>
              <Table striped highlightOnHover withTableBorder withColumnBorders>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>Дата</Table.Th>
                    <Table.Th ta="right">Баланс</Table.Th>
                    <Table.Th ta="right">Прибавка</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {incomeForecast.data.map((item, index) => (
                    <Table.Tr key={`${item.date}-${index}`}>
                      <Table.Td>{formatDate(item.date)}</Table.Td>
                      <Table.Td ta="right">{formatRub(item.balance)}</Table.Td>
                      <Table.Td ta="right">
                        {item.increase === null ? '—' : `+${formatRub(item.increase)}`}
                      </Table.Td>
                    </Table.Tr>
                  ))}
                </Table.Tbody>
              </Table>
            </Table.ScrollContainer>
          ))}

        <Group justify="flex-end">
          <Button variant="default" onClick={handleClose} disabled={incomeForecast.isPending}>
            Отмена
          </Button>
          <Button
            leftSection={<IconCalculator size={18} />}
            onClick={handleCalculate}
            loading={incomeForecast.isPending}
            disabled={!canCalculate}
          >
            Рассчитать доход
          </Button>
        </Group>
      </Stack>
    </Modal>
  )
}
