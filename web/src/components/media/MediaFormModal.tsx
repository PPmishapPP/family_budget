import { zodResolver } from '@hookform/resolvers/zod'
import { Button, Group, Modal, Select, Stack, Textarea, TextInput } from '@mantine/core'
import { useEffect, useMemo } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'

import type { MediaInput, MediaItem } from '../../api/media.ts'
import type { MediaReference } from '../../api/reference.ts'
import {
  MEDIA_STATUSES,
  MEDIA_TYPES,
  mediaStatusLabel,
  mediaStatusTransitions,
  mediaTypeLabel,
} from '../../lib/media.ts'
import type { MediaStatus, MediaType } from '../../lib/media.ts'

const mediaSchema = z.object({
  name: z.string().trim().min(1, 'Укажите название').max(256, 'Не более 256 символов'),
  type: z.enum(MEDIA_TYPES),
  status: z.enum(MEDIA_STATUSES).nullable(),
  rating: z
    .number()
    .int('Рейтинг должен быть целым числом')
    .min(1, 'Рейтинг должен быть от 1 до 10')
    .max(10, 'Рейтинг должен быть от 1 до 10')
    .nullable(),
  description: z.string().max(2000, 'Не более 2000 символов'),
})

type MediaFormValues = z.infer<typeof mediaSchema>

const RATING_OPTIONS = Array.from({ length: 10 }, (_, index) => {
  const value = String(index + 1)
  return { value, label: value }
})

type MediaFormModalProps = {
  opened: boolean
  media: MediaItem | null
  reference: MediaReference | undefined
  submitting: boolean
  onClose: () => void
  onSubmit: (input: MediaInput) => Promise<void>
}

function toFormValues(media: MediaItem | null): MediaFormValues {
  if (!media) {
    return {
      name: '',
      type: 'MOVIE',
      status: 'PLAN_TO_WATCH',
      rating: null,
      description: '',
    }
  }
  return {
    name: media.name,
    type: media.type,
    status: null,
    rating: media.rating,
    description: media.description ?? '',
  }
}

export function MediaFormModal({
  opened,
  media,
  reference,
  submitting,
  onClose,
  onSubmit,
}: MediaFormModalProps) {
  const defaults = useMemo(() => toFormValues(media), [media])
  const form = useForm<MediaFormValues>({
    resolver: zodResolver(mediaSchema),
    defaultValues: defaults,
  })

  useEffect(() => {
    if (opened) {
      form.reset(defaults)
    }
  }, [opened, defaults, form])

  const typeOptions = useMemo(
    () => MEDIA_TYPES.map((value) => ({ value, label: mediaTypeLabel(value, reference?.types) })),
    [reference],
  )

  const statusOptions = useMemo(() => {
    if (media === null) {
      return MEDIA_STATUSES.map((value) => ({
        value,
        label: mediaStatusLabel(value, reference?.statuses),
      }))
    }
    return mediaStatusTransitions(media.status, reference?.statuses).map((value) => ({
      value,
      label: mediaStatusLabel(value, reference?.statuses),
    }))
  }, [media, reference])

  const currentStatusLabel = media ? mediaStatusLabel(media.status, reference?.statuses) : null

  const submit = form.handleSubmit(async (values) => {
    await onSubmit({
      name: values.name,
      type: values.type,
      status: values.status,
      rating: values.rating,
      description: values.description.trim() === '' ? null : values.description.trim(),
    })
  })

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title={media ? 'Изменить запись' : 'Новая запись'}
      size="md"
      centered
      closeOnClickOutside={!submitting}
      closeOnEscape={!submitting}
      withCloseButton={!submitting}
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
            name="type"
            render={({ field, fieldState }) => (
              <Select
                label="Тип"
                withAsterisk
                data={typeOptions}
                value={field.value}
                onChange={(value) => {
                  if (value) {
                    field.onChange(value as MediaType)
                  }
                }}
                allowDeselect={false}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="status"
            render={({ field, fieldState }) => (
              <Select
                label="Статус"
                description={
                  currentStatusLabel ? `Текущий статус: ${currentStatusLabel}` : undefined
                }
                placeholder={currentStatusLabel ? `Не менять (${currentStatusLabel})` : undefined}
                data={statusOptions}
                value={field.value}
                onChange={(value) => field.onChange(value === null ? null : (value as MediaStatus))}
                clearable={media !== null}
                allowDeselect={media !== null}
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="rating"
            render={({ field, fieldState }) => (
              <Select
                label="Рейтинг"
                description="От 1 до 10, необязательно"
                placeholder="Без оценки"
                data={RATING_OPTIONS}
                value={field.value === null ? null : String(field.value)}
                onChange={(value) => field.onChange(value === null ? null : Number(value))}
                clearable
                error={fieldState.error?.message}
              />
            )}
          />

          <Controller
            control={form.control}
            name="description"
            render={({ field, fieldState }) => (
              <Textarea
                label="Комментарий"
                description="Необязательно"
                autosize
                minRows={2}
                maxRows={5}
                maxLength={2000}
                value={field.value}
                onChange={field.onChange}
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
              {media ? 'Сохранить' : 'Добавить'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  )
}
