import { ActionIcon, Badge, Group, Table, Text, Tooltip } from '@mantine/core'
import { IconPencil, IconTrash } from '@tabler/icons-react'

import type { MediaItem } from '../../api/media.ts'
import type { MediaReference } from '../../api/reference.ts'
import { MEDIA_STATUS_COLORS, mediaStatusLabel, mediaTypeLabel } from '../../lib/media.ts'

type MediaTableProps = {
  items: MediaItem[]
  reference: MediaReference | undefined
  deletingId: number | null
  onEdit: (media: MediaItem) => void
  onDelete: (media: MediaItem) => void
}

export function MediaTable({ items, reference, deletingId, onEdit, onDelete }: MediaTableProps) {
  return (
    <Table.ScrollContainer minWidth={880}>
      <Table striped highlightOnHover withTableBorder withColumnBorders verticalSpacing="xs">
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Название</Table.Th>
            <Table.Th>Тип</Table.Th>
            <Table.Th>Статус</Table.Th>
            <Table.Th>Рейтинг</Table.Th>
            <Table.Th>Комментарий</Table.Th>
            <Table.Th ta="right">Действия</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {items.map((item) => (
            <Table.Tr key={item.id}>
              <Table.Td>
                <Text component="span" fw={500}>
                  {item.name}
                </Text>
              </Table.Td>
              <Table.Td>
                <Badge color={item.type === 'MOVIE' ? 'blue' : 'grape'} variant="light">
                  {mediaTypeLabel(item.type, reference?.types)}
                </Badge>
              </Table.Td>
              <Table.Td>
                <Badge color={MEDIA_STATUS_COLORS[item.status]} variant="light">
                  {mediaStatusLabel(item.status, reference?.statuses)}
                </Badge>
              </Table.Td>
              <Table.Td>
                {item.rating === null ? (
                  <Text component="span" c="dimmed">
                    —
                  </Text>
                ) : (
                  `${item.rating}/10`
                )}
              </Table.Td>
              <Table.Td maw={360}>
                {item.description === null ? (
                  <Text component="span" c="dimmed">
                    —
                  </Text>
                ) : (
                  <Text size="sm" lineClamp={2}>
                    {item.description}
                  </Text>
                )}
              </Table.Td>
              <Table.Td>
                <Group gap={4} justify="flex-end" wrap="nowrap">
                  <Tooltip label="Изменить">
                    <ActionIcon
                      variant="subtle"
                      onClick={() => onEdit(item)}
                      aria-label={`Изменить ${item.name}`}
                    >
                      <IconPencil size={18} />
                    </ActionIcon>
                  </Tooltip>
                  <Tooltip label="Удалить">
                    <ActionIcon
                      variant="subtle"
                      color="red"
                      loading={deletingId === item.id}
                      onClick={() => onDelete(item)}
                      aria-label={`Удалить ${item.name}`}
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
