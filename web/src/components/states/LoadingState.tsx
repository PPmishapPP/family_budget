import { Center, Loader, Stack, Text } from '@mantine/core'

type LoadingStateProps = {
  label?: string
}

export function LoadingState({ label = 'Загрузка…' }: LoadingStateProps) {
  return (
    <Center py="xl">
      <Stack align="center" gap="xs">
        <Loader />
        <Text size="sm" c="dimmed">
          {label}
        </Text>
      </Stack>
    </Center>
  )
}
