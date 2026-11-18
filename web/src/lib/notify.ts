import { notifications } from '@mantine/notifications'

export function notifySuccess(message: string): void {
  notifications.show({ color: 'teal', message })
}

export function notifyError(message: string): void {
  notifications.show({ color: 'red', title: 'Ошибка', message })
}
