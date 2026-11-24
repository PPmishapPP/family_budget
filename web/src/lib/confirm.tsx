import { Text } from '@mantine/core'
import { modals } from '@mantine/modals'

type ConfirmOptions = {
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  color?: string
  onConfirm: () => void | Promise<void>
}

export function confirmAction({
  title,
  message,
  confirmLabel = 'Подтвердить',
  cancelLabel = 'Отмена',
  color = 'red',
  onConfirm,
}: ConfirmOptions) {
  modals.openConfirmModal({
    title,
    children: <Text size="sm">{message}</Text>,
    labels: { confirm: confirmLabel, cancel: cancelLabel },
    confirmProps: { color },
    onConfirm: () => {
      void onConfirm()
    },
  })
}
