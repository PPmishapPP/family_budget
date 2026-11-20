import { Group, Pagination, Table, Text, UnstyledButton } from '@mantine/core'
import { IconChevronDown, IconChevronUp, IconSelector } from '@tabler/icons-react'
import { useRef } from 'react'

import type { HistoryEntry, HistorySortDirection, HistorySortKey } from '../../api/history.ts'
import { formatDate, formatRub } from '../../lib/format.ts'

export type HistorySort = {
  key: HistorySortKey
  direction: HistorySortDirection
}

type SortHeaderProps = {
  label: string
  sortKey: HistorySortKey
  sort: HistorySort
  align?: 'left' | 'right'
  onSort: (key: HistorySortKey) => void
}

function SortHeader({ label, sortKey, sort, align = 'left', onSort }: SortHeaderProps) {
  const active = sort.key === sortKey
  const Icon = !active ? IconSelector : sort.direction === 'asc' ? IconChevronUp : IconChevronDown

  return (
    <Table.Th
      ta={align}
      aria-sort={active ? (sort.direction === 'asc' ? 'ascending' : 'descending') : 'none'}
    >
      <UnstyledButton onClick={() => onSort(sortKey)} w="100%">
        <Group gap={4} wrap="nowrap" justify={align === 'right' ? 'flex-end' : 'flex-start'}>
          <span>{label}</span>
          <Icon size={14} opacity={active ? 1 : 0.4} />
        </Group>
      </UnstyledButton>
    </Table.Th>
  )
}

type HistoryTableProps = {
  entries: HistoryEntry[]
  totalElements: number
  totalPages: number
  page: number
  pageSize: number
  sort: HistorySort
  onSort: (key: HistorySortKey) => void
  onPageChange: (page: number) => void
}

export function HistoryTable({
  entries,
  totalElements,
  totalPages,
  page,
  pageSize,
  sort,
  onSort,
  onPageChange,
}: HistoryTableProps) {
  const containerRef = useRef<HTMLDivElement>(null)

  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + entries.length
  const total = Math.max(1, totalPages)

  function handlePageChange(nextPage: number) {
    onPageChange(nextPage)
    containerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div>
      <div ref={containerRef} style={{ scrollMarginTop: 72 }}>
        <Table.ScrollContainer minWidth={640}>
          <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
            <Table.Thead>
              <Table.Tr>
                <SortHeader label="Наименование" sortKey="comment" sort={sort} onSort={onSort} />
                <SortHeader label="Сумма" sortKey="sum" sort={sort} align="right" onSort={onSort} />
                <SortHeader label="Дата" sortKey="date" sort={sort} onSort={onSort} />
                <SortHeader
                  label="Баланс"
                  sortKey="balance"
                  sort={sort}
                  align="right"
                  onSort={onSort}
                />
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {entries.map((item) => (
                <Table.Tr key={item.id}>
                  <Table.Td>
                    {item.comment ?? (
                      <Text component="span" c="dimmed">
                        —
                      </Text>
                    )}
                  </Table.Td>
                  <Table.Td ta="right">
                    <Text
                      component="span"
                      fw={500}
                      c={item.sum > 0 ? 'green' : item.sum < 0 ? 'red' : undefined}
                    >
                      {formatRub(item.sum)}
                    </Text>
                  </Table.Td>
                  <Table.Td>{formatDate(item.dateTime)}</Table.Td>
                  <Table.Td ta="right">{formatRub(item.balance)}</Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </Table.ScrollContainer>
      </div>

      <Group justify="space-between" align="center" gap="sm" wrap="wrap" mt="md">
        <Text size="sm" c="dimmed">
          Показано {entries.length > 0 ? `${startIndex + 1}–${endIndex}` : 0} из {totalElements}
        </Text>
        <Pagination
          total={total}
          value={page}
          onChange={handlePageChange}
          size="sm"
          withEdges={total > 7}
        />
      </Group>
    </div>
  )
}
