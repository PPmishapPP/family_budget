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

import type { PaymentRuleDraft } from '../../api/paymentRules.ts'
import { formatDate, formatRub } from '../../lib/format.ts'
import { PAYMENT_RULE_TYPES, PAYMENT_RULE_TYPE_LABELS, isRuleDue } from '../../lib/paymentRules.ts'

type RulesTableProps = {
  drafts: PaymentRuleDraft[]
  editMode: boolean
  executingId: number | null
  onEdit: (draft: PaymentRuleDraft) => void
  onExecute: (draft: PaymentRuleDraft) => void
  onDelete: (draft: PaymentRuleDraft) => void
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

function compareDrafts(a: PaymentRuleDraft, b: PaymentRuleDraft, field: SortField): number {
  const left = a.current
  const right = b.current
  switch (field) {
    case 'name':
      return left.name.localeCompare(right.name, 'ru')
    case 'sum':
      return left.sum - right.sum
    case 'type':
      return (TYPE_ORDER.get(left.type) ?? 0) - (TYPE_ORDER.get(right.type) ?? 0)
    case 'nextDay':
      return left.nextDay.localeCompare(right.nextDay)
    case 'endDate':
      return compareNullableDates(left.endDate, right.endDate)
  }
}

function sortDrafts(
  drafts: PaymentRuleDraft[],
  field: SortField,
  direction: SortDirection,
): PaymentRuleDraft[] {
  return [...drafts].sort((a, b) => {
    const aNew = a.id === null
    const bNew = b.id === null
    if (aNew !== bNew) {
      return aNew ? -1 : 1
    }
    const result = compareDrafts(a, b, field)
    if (result !== 0) {
      if (field === 'endDate' && (a.current.endDate === null || b.current.endDate === null)) {
        return result
      }
      return direction === 'asc' ? result : -result
    }
    const byNextDay = a.current.nextDay.localeCompare(b.current.nextDay)
    if (byNextDay !== 0) {
      return byNextDay
    }
    return (a.id ?? 0) - (b.id ?? 0)
  })
}

export function RulesTable({
  drafts,
  editMode,
  executingId,
  onEdit,
  onExecute,
  onDelete,
}: RulesTableProps) {
  const [sortField, setSortField] = useState<SortField>('nextDay')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const sortedDrafts = useMemo(
    () => sortDrafts(drafts, sortField, sortDirection),
    [drafts, sortField, sortDirection],
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
    <Table.ScrollContainer minWidth={760}>
      <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
        <Table.Thead>
          <Table.Tr>
            {renderSortableHeader('name', 'Название')}
            {renderSortableHeader('sum', 'Сумма', 'right')}
            {renderSortableHeader('type', 'Тип')}
            {renderSortableHeader('nextDay', 'Следующий платёж')}
            <Table.Th>Активность</Table.Th>
            {renderSortableHeader('endDate', 'Последний платёж')}
            <Table.Th ta="right">Действия</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {sortedDrafts.map((draft) => {
            const rule = draft.current
            const marked = draft.markedForDeletion
            const isNew = draft.id === null
            const original = draft.original
            const nameMarked = original !== null && original.name !== rule.name
            const sumMarked = original !== null && original.sum !== rule.sum
            const typeMarked = original !== null && original.type !== rule.type
            const activeMarked = original !== null && original.active !== rule.active
            const name = rule.name || '—'
            return (
              <Table.Tr
                key={draft.key}
                style={{
                  opacity: marked ? 0.6 : rule.active ? 1 : 0.6,
                  backgroundColor: marked ? 'var(--mantine-color-red-0)' : undefined,
                  textDecoration: marked ? 'line-through' : undefined,
                }}
              >
                <Table.Td>
                  {nameMarked ? '✎ ' : ''}
                  {name}
                </Table.Td>
                <Table.Td ta="right">
                  {formatRub(rule.sum)}
                  {sumMarked ? ' ✎' : ''}
                </Table.Td>
                <Table.Td>
                  {PAYMENT_RULE_TYPE_LABELS[rule.type]}
                  {typeMarked ? ' ✎' : ''}
                </Table.Td>
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
                <Table.Td>
                  <Badge color={rule.active ? 'teal' : 'gray'} variant="light">
                    {activeMarked ? '✎ ' : ''}
                    {rule.active ? 'Активно' : 'Неактивно'}
                  </Badge>
                </Table.Td>
                <Table.Td>{formatDate(rule.endDate)}</Table.Td>
                <Table.Td>
                  <Group gap={4} justify="flex-end" wrap="nowrap">
                    <Tooltip label="Изменить">
                      <ActionIcon
                        variant="subtle"
                        onClick={() => onEdit(draft)}
                        disabled={!editMode || marked}
                        aria-label={`Изменить ${name}`}
                      >
                        <IconPencil size={18} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Выполнить">
                      <ActionIcon
                        variant="subtle"
                        color="teal"
                        loading={!isNew && executingId === draft.id}
                        onClick={() => onExecute(draft)}
                        disabled={editMode || marked || isNew}
                        aria-label={`Выполнить ${name}`}
                      >
                        <IconPlayerPlay size={18} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Удалить">
                      <ActionIcon
                        variant="subtle"
                        color="red"
                        onClick={() => onDelete(draft)}
                        disabled={!editMode || marked}
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
