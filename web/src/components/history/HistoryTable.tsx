import { Group, Pagination, Table, Text, UnstyledButton } from '@mantine/core'
import { IconChevronDown, IconChevronUp, IconSelector } from '@tabler/icons-react'
import { useMemo, useRef, useState } from 'react'

import type { HistoryEntry } from '../../api/history.ts'
import { formatDate, formatRub } from '../../lib/format.ts'

const PAGE_SIZE = 20

type SortKey = 'comment' | 'date' | 'sum' | 'balance'
type SortDirection = 'asc' | 'desc'

type SortState = {
  key: SortKey
  direction: SortDirection
}

const DEFAULT_SORT: SortState = { key: 'date', direction: 'desc' }

function compareComments(a: HistoryEntry, b: HistoryEntry): number {
  if (a.comment === null && b.comment === null) {
    return 0
  }
  if (a.comment === null) {
    return -1
  }
  if (b.comment === null) {
    return 1
  }
  return a.comment.localeCompare(b.comment, 'ru')
}

function compareEntries(a: HistoryEntry, b: HistoryEntry, key: SortKey): number {
  switch (key) {
    case 'comment':
      return compareComments(a, b)
    case 'sum':
      return a.sum - b.sum
    case 'balance':
      return a.balance - b.balance
    case 'date':
      return a.dateTime.localeCompare(b.dateTime)
  }
}

type SortHeaderProps = {
  label: string
  sortKey: SortKey
  sort: SortState
  align?: 'left' | 'right'
  onSort: (key: SortKey) => void
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

export function HistoryTable({ entries }: { entries: HistoryEntry[] }) {
  const [sort, setSort] = useState<SortState>(DEFAULT_SORT)
  const [page, setPage] = useState(1)
  const containerRef = useRef<HTMLDivElement>(null)

  const sorted = useMemo(() => {
    const direction = sort.direction === 'asc' ? 1 : -1
    return [...entries].sort((a, b) => {
      const primary = compareEntries(a, b, sort.key)
      if (primary !== 0) {
        return primary * direction
      }
      return b.id - a.id
    })
  }, [entries, sort])

  const totalPages = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE))
  const currentPage = Math.min(page, totalPages)
  const startIndex = (currentPage - 1) * PAGE_SIZE
  const visible = sorted.slice(startIndex, startIndex + PAGE_SIZE)

  function handleSort(key: SortKey) {
    setSort((previous) =>
      previous.key === key
        ? { key, direction: previous.direction === 'asc' ? 'desc' : 'asc' }
        : { key, direction: 'asc' },
    )
    setPage(1)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    containerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div>
      <div ref={containerRef} style={{ scrollMarginTop: 72 }}>
        <Table.ScrollContainer minWidth={640}>
          <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
            <Table.Thead>
              <Table.Tr>
                <SortHeader
                  label="Наименование"
                  sortKey="comment"
                  sort={sort}
                  onSort={handleSort}
                />
                <SortHeader
                  label="Сумма"
                  sortKey="sum"
                  sort={sort}
                  align="right"
                  onSort={handleSort}
                />
                <SortHeader label="Дата" sortKey="date" sort={sort} onSort={handleSort} />
                <SortHeader
                  label="Баланс"
                  sortKey="balance"
                  sort={sort}
                  align="right"
                  onSort={handleSort}
                />
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {visible.map((item) => (
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
          Показано {startIndex + 1}–{startIndex + visible.length} из {sorted.length}
        </Text>
        <Pagination
          total={totalPages}
          value={currentPage}
          onChange={handlePageChange}
          size="sm"
          withEdges={totalPages > 7}
        />
      </Group>
    </div>
  )
}
