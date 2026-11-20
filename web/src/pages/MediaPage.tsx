import { Button, Group, SegmentedControl, Stack, Text, Title } from '@mantine/core'
import { IconPlus } from '@tabler/icons-react'
import { useState } from 'react'

import { useAccount } from '../accounts/accountContext.ts'
import type { MediaInput, MediaItem } from '../api/media.ts'
import {
  useCreateMediaMutation,
  useDeleteMediaMutation,
  useMediaQuery,
  useUpdateMediaMutation,
} from '../api/media.ts'
import { useMediaReferenceQuery } from '../api/reference.ts'
import { MediaFormModal } from '../components/media/MediaFormModal.tsx'
import { MediaTable } from '../components/media/MediaTable.tsx'
import { EmptyState } from '../components/states/EmptyState.tsx'
import { ErrorState } from '../components/states/ErrorState.tsx'
import { LoadingState } from '../components/states/LoadingState.tsx'
import { confirmAction } from '../lib/confirm.tsx'
import { errorMessage } from '../lib/errors.ts'
import type { MediaType } from '../lib/media.ts'
import { notifyError, notifySuccess } from '../lib/notify.ts'

type MediaFilter = 'ALL' | MediaType

const FILTER_OPTIONS = [
  { value: 'ALL', label: 'Все' },
  { value: 'MOVIE', label: 'Фильмы' },
  { value: 'SERIES', label: 'Сериалы' },
]

type MediaFormState = {
  opened: boolean
  media: MediaItem | null
}

type MediaContentProps = {
  accountId: number
  accountName: string
}

function MediaContent({ accountId, accountName }: MediaContentProps) {
  const [filter, setFilter] = useState<MediaFilter>('ALL')
  const [formState, setFormState] = useState<MediaFormState>({
    opened: false,
    media: null,
  })

  const type = filter === 'ALL' ? null : filter
  const mediaQuery = useMediaQuery(accountId, type)
  const referenceQuery = useMediaReferenceQuery()

  const createMedia = useCreateMediaMutation(accountId)
  const updateMedia = useUpdateMediaMutation(accountId)
  const deleteMedia = useDeleteMediaMutation(accountId)

  const items = mediaQuery.data ?? []

  function openCreate() {
    setFormState({ opened: true, media: null })
  }

  function openEdit(media: MediaItem) {
    setFormState({ opened: true, media })
  }

  function closeForm() {
    setFormState({ opened: false, media: null })
  }

  async function handleSubmit(input: MediaInput) {
    try {
      if (formState.media) {
        await updateMedia.mutateAsync({ mediaId: formState.media.id, input })
      } else {
        await createMedia.mutateAsync(input)
      }
      notifySuccess('Сохранено')
      closeForm()
    } catch (error) {
      notifyError(errorMessage(error))
    }
  }

  function handleDelete(media: MediaItem) {
    confirmAction({
      title: 'Удаление записи',
      message: `Удалить «${media.name}»?`,
      confirmLabel: 'Удалить',
      onConfirm: async () => {
        try {
          await deleteMedia.mutateAsync(media.id)
          notifySuccess('Удалено')
        } catch (error) {
          notifyError(errorMessage(error))
        }
      },
    })
  }

  const deletingId =
    deleteMedia.isPending && deleteMedia.variables !== undefined ? deleteMedia.variables : null
  const submitting = createMedia.isPending || updateMedia.isPending

  const emptyTitle =
    filter === 'MOVIE'
      ? 'Фильмов пока нет'
      : filter === 'SERIES'
        ? 'Сериалов пока нет'
        : 'Записей пока нет'
  const emptyDescription =
    filter === 'MOVIE'
      ? 'Добавьте первый фильм для этого счёта.'
      : filter === 'SERIES'
        ? 'Добавьте первый сериал для этого счёта.'
        : 'Добавьте первый фильм или сериал для этого счёта.'

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-start" gap="md" wrap="wrap">
        <Stack gap={2}>
          <Title order={2}>Фильмы и сериалы</Title>
          <Text size="sm" c="dimmed">
            {accountName}
          </Text>
        </Stack>
        <Button leftSection={<IconPlus size={18} />} onClick={openCreate}>
          Добавить
        </Button>
      </Group>

      <Group justify="space-between" align="center" gap="sm" wrap="wrap">
        <SegmentedControl
          data={FILTER_OPTIONS}
          value={filter}
          onChange={(value) => setFilter(value as MediaFilter)}
        />
        {mediaQuery.isSuccess && (
          <Text size="sm" c="dimmed">
            Записей: {items.length}
          </Text>
        )}
      </Group>

      {mediaQuery.isLoading ? (
        <LoadingState label="Загрузка фильмов и сериалов…" />
      ) : mediaQuery.isError ? (
        <ErrorState
          description={errorMessage(mediaQuery.error, 'Не удалось загрузить фильмы и сериалы.')}
          onRetry={() => void mediaQuery.refetch()}
        />
      ) : items.length === 0 ? (
        <EmptyState
          title={emptyTitle}
          description={emptyDescription}
          action={
            <Button leftSection={<IconPlus size={18} />} onClick={openCreate}>
              Добавить
            </Button>
          }
        />
      ) : (
        <MediaTable
          items={items}
          reference={referenceQuery.data}
          deletingId={deletingId}
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      )}

      <MediaFormModal
        opened={formState.opened}
        media={formState.media}
        reference={referenceQuery.data}
        submitting={submitting}
        onClose={closeForm}
        onSubmit={handleSubmit}
      />
    </Stack>
  )
}

export function MediaPage() {
  const { selectedAccount, isLoading } = useAccount()

  if (isLoading) {
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

  return (
    <MediaContent
      key={selectedAccount.id}
      accountId={selectedAccount.id}
      accountName={selectedAccount.name}
    />
  )
}
