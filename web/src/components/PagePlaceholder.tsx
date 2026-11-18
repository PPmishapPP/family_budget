import { Paper, Stack, Text, Title } from '@mantine/core'

type PagePlaceholderProps = {
  title: string
  description: string
}

export function PagePlaceholder({ title, description }: PagePlaceholderProps) {
  return (
    <Paper withBorder p="xl" radius="md">
      <Stack gap="xs">
        <Title order={2}>{title}</Title>
        <Text c="dimmed">{description}</Text>
      </Stack>
    </Paper>
  )
}
