import {
  Alert,
  Button,
  Center,
  Paper,
  PasswordInput,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core'
import { IconAlertCircle } from '@tabler/icons-react'
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/authContext.ts'

type LocationState = { from?: { pathname?: string } }

export function LoginPage() {
  const { status, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as LocationState | null)?.from?.pathname ?? '/payments'

  const [loginValue, setLoginValue] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (status === 'authenticated') {
      navigate(from, { replace: true })
    }
  }, [status, from, navigate])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(loginValue.trim(), password)
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Не удалось выполнить вход')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Center mih="100dvh" p="md">
      <Paper withBorder p="xl" radius="md" w="100%" maw={400}>
        <form onSubmit={handleSubmit}>
          <Stack gap="md">
            <div>
              <Title order={2}>Вход</Title>
              <Text c="dimmed" size="sm">
                Family Budget
              </Text>
            </div>

            {error && (
              <Alert color="red" icon={<IconAlertCircle size={16} />}>
                {error}
              </Alert>
            )}

            <TextInput
              label="Логин"
              value={loginValue}
              onChange={(event) => setLoginValue(event.currentTarget.value)}
              autoComplete="username"
              autoFocus
              required
            />
            <PasswordInput
              label="Пароль"
              value={password}
              onChange={(event) => setPassword(event.currentTarget.value)}
              autoComplete="current-password"
              required
            />
            <Button type="submit" loading={submitting} disabled={status === 'loading'}>
              Войти
            </Button>
          </Stack>
        </form>
      </Paper>
    </Center>
  )
}
