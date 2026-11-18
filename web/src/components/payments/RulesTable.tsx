import { ActionIcon, Badge, Group, Table, Tooltip } from '@mantine/core'
import { IconPencil, IconPlayerPlay, IconTrash } from '@tabler/icons-react'

import type { PaymentRule } from '../../api/paymentRules.ts'
import { formatDate, formatRub } from '../../lib/format.ts'
import { PAYMENT_RULE_TYPE_LABELS, isRuleDue } from '../../lib/paymentRules.ts'

type RulesTableProps = {
  rules: PaymentRule[]
  executingId: number | null
  deletingId: number | null
  onEdit: (rule: PaymentRule) => void
  onExecute: (rule: PaymentRule) => void
  onDelete: (rule: PaymentRule) => void
}

export function RulesTable({
  rules,
  executingId,
  deletingId,
  onEdit,
  onExecute,
  onDelete,
}: RulesTableProps) {
  return (
    <Table.ScrollContainer minWidth={760}>
      <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Название</Table.Th>
            <Table.Th ta="right">Сумма</Table.Th>
            <Table.Th>Тип</Table.Th>
            <Table.Th>Следующий платёж</Table.Th>
            <Table.Th>Активность</Table.Th>
            <Table.Th>Последний платёж</Table.Th>
            <Table.Th ta="right">Действия</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {rules.map((rule) => (
            <Table.Tr key={rule.id} style={{ opacity: rule.active ? 1 : 0.6 }}>
              <Table.Td>{rule.name || '—'}</Table.Td>
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
              <Table.Td>
                <Badge color={rule.active ? 'teal' : 'gray'} variant="light">
                  {rule.active ? 'Активно' : 'Неактивно'}
                </Badge>
              </Table.Td>
              <Table.Td>{formatDate(rule.endDate)}</Table.Td>
              <Table.Td>
                <Group gap={4} justify="flex-end" wrap="nowrap">
                  <Tooltip label="Изменить">
                    <ActionIcon
                      variant="subtle"
                      onClick={() => onEdit(rule)}
                      aria-label={`Изменить ${rule.name}`}
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
                      aria-label={`Выполнить ${rule.name}`}
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
                      aria-label={`Удалить ${rule.name}`}
                    >
                      <IconTrash size={18} />
                    </ActionIcon>
                  </Tooltip>
                </Group>
              </Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>
    </Table.ScrollContainer>
  )
}
