import { Alert, Button, Group, Text } from '@mantine/core'
import { IconAlertTriangle } from '@tabler/icons-react'

type ErrorStateProps = {
  title?: string
  description?: string
  onRetry?: () => void
}

export function ErrorState({
  title = 'Не удалось загрузить данные',
  description = 'Проверьте подключение и попробуйте ещё раз.',
  onRetry,
}: ErrorStateProps) {
  return (
    <Alert color="red" icon={<IconAlertTriangle size={18} />} title={title}>
      <Group justify="space-between" align="center" gap="sm" wrap="wrap">
        <Text size="sm">{description}</Text>
        {onRetry && (
          <Button size="xs" color="red" variant="light" onClick={onRetry}>
            Повторить
          </Button>
        )}
      </Group>
    </Alert>
  )
}
