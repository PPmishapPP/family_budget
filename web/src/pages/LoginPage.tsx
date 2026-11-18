import { Button, Center, Paper, Stack, Text, Title } from '@mantine/core'
import { Link } from 'react-router-dom'

export function LoginPage() {
  return (
    <Center mih="100dvh" p="md">
      <Paper withBorder p="xl" radius="md" w="100%" maw={400}>
        <Stack gap="sm">
          <Title order={2}>Вход</Title>
          <Text c="dimmed">Форма входа появится на Этапе 2.4.</Text>
          <Button component={Link} to="/payments" variant="light">
            Перейти к приложению
          </Button>
        </Stack>
      </Paper>
    </Center>
  )
}
