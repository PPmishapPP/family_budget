import { ActionIcon, Badge, Group, Table, Tooltip } from '@mantine/core'
import {
  IconChevronDown,
  IconChevronUp,
  IconPencil,
  IconPlayerPlay,
  IconSelector,
  IconTrash,
} from '@tabler/icons-react'
import { useMemo, useState } from 'react'

import type { PaymentRule } from '../../api/paymentRules.ts'
import { formatDate, formatRub } from '../../lib/format.ts'
import { PAYMENT_RULE_TYPES, PAYMENT_RULE_TYPE_LABELS, isRuleDue } from '../../lib/paymentRules.ts'

type RulesTableProps = {
  rules: PaymentRule[]
  executingId: number | null
  deletingId: number | null
  onEdit: (rule: PaymentRule) => void
  onExecute: (rule: PaymentRule) => void
  onDelete: (rule: PaymentRule) => void
}

type SortField = 'name' | 'sum' | 'type' | 'nextDay' | 'endDate'
type SortDirection = 'asc' | 'desc'

const TYPE_ORDER = new Map(PAYMENT_RULE_TYPES.map((type, index) => [type, index]))

function compareNullableDates(a: string | null, b: string | null): number {
  if (a === null && b === null) {
    return 0
  }
  if (a === null) {
    return 1
  }
  if (b === null) {
    return -1
  }
  return a.localeCompare(b)
}

function compareRules(a: PaymentRule, b: PaymentRule, field: SortField): number {
  switch (field) {
    case 'name':
      return a.name.localeCompare(b.name, 'ru')
    case 'sum':
      return a.sum - b.sum
    case 'type':
      return (TYPE_ORDER.get(a.type) ?? 0) - (TYPE_ORDER.get(b.type) ?? 0)
    case 'nextDay':
      return a.nextDay.localeCompare(b.nextDay)
    case 'endDate':
      return compareNullableDates(a.endDate, b.endDate)
  }
}

function sortRules(
  rules: PaymentRule[],
  field: SortField,
  direction: SortDirection,
): PaymentRule[] {
  return [...rules].sort((a, b) => {
    const result = compareRules(a, b, field)
    if (result !== 0) {
      if (field === 'endDate' && (a.endDate === null || b.endDate === null)) {
        return result
      }
      return direction === 'asc' ? result : -result
    }
    const byNextDay = a.nextDay.localeCompare(b.nextDay)
    if (byNextDay !== 0) {
      return byNextDay
    }
    return a.id - b.id
  })
}

export function RulesTable({
  rules,
  executingId,
  deletingId,
  onEdit,
  onExecute,
  onDelete,
}: RulesTableProps) {
  const [sortField, setSortField] = useState<SortField>('nextDay')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const sortedRules = useMemo(
    () => sortRules(rules, sortField, sortDirection),
    [rules, sortField, sortDirection],
  )

  function toggleSort(field: SortField) {
    if (field === sortField) {
      setSortDirection((current) => (current === 'asc' ? 'desc' : 'asc'))
      return
    }
    setSortField(field)
    setSortDirection('asc')
  }

  function renderSortableHeader(field: SortField, label: string, align?: 'right') {
    const active = sortField === field
    const SortIcon = !active
      ? IconSelector
      : sortDirection === 'asc'
        ? IconChevronUp
        : IconChevronDown
    return (
      <Table.Th
        ta={align}
        aria-sort={active ? (sortDirection === 'asc' ? 'ascending' : 'descending') : 'none'}
        onClick={() => toggleSort(field)}
        style={{ cursor: 'pointer', userSelect: 'none' }}
      >
        <Group gap={4} justify={align === 'right' ? 'flex-end' : 'flex-start'} wrap="nowrap">
          <span>{label}</span>
          <SortIcon size={14} opacity={active ? 1 : 0.4} />
        </Group>
      </Table.Th>
    )
  }

  return (
    <Table.ScrollContainer minWidth={680}>
      <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
        <Table.Thead>
          <Table.Tr>
            {renderSortableHeader('name', 'Название')}
            {renderSortableHeader('sum', 'Сумма', 'right')}
            {renderSortableHeader('type', 'Тип')}
            {renderSortableHeader('nextDay', 'Следующий платёж')}
            {renderSortableHeader('endDate', 'Последний платёж')}
            <Table.Th ta="right">Действия</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {sortedRules.map((rule) => {
            const name = rule.name || '—'
            return (
              <Table.Tr key={rule.id}>
                <Table.Td>{name}</Table.Td>
                <Table.Td ta="right">{formatRub(rule.sum)}</Table.Td>
                <Table.Td>{PAYMENT_RULE_TYPE_LABELS[rule.type]}</Table.Td>
                <Table.Td>
                  <Group gap={6} wrap="nowrap">
                    <span>{formatDate(rule.nextDay)}</span>
                    {isRuleDue(rule.nextDay) && (
                      <Badge size="xs" color="teal" variant="light">
                        К оплате
                      </Badge>
                    )}
                  </Group>
                </Table.Td>
                <Table.Td>{formatDate(rule.endDate)}</Table.Td>
                <Table.Td>
                  <Group gap={4} justify="flex-end" wrap="nowrap">
                    <Tooltip label="Изменить">
                      <ActionIcon
                        variant="subtle"
                        onClick={() => onEdit(rule)}
                        aria-label={`Изменить ${name}`}
                      >
                        <IconPencil size={18} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Выполнить">
                      <ActionIcon
                        variant="subtle"
                        color="teal"
                        loading={executingId === rule.id}
                        onClick={() => onExecute(rule)}
                        aria-label={`Выполнить ${name}`}
                      >
                        <IconPlayerPlay size={18} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Удалить">
                      <ActionIcon
                        variant="subtle"
                        color="red"
                        loading={deletingId === rule.id}
                        onClick={() => onDelete(rule)}
                        aria-label={`Удалить ${name}`}
                      >
                        <IconTrash size={18} />
                      </ActionIcon>
                    </Tooltip>
                  </Group>
                </Table.Td>
              </Table.Tr>
            )
          })}
        </Table.Tbody>
      </Table>
    </Table.ScrollContainer>
  )
}
