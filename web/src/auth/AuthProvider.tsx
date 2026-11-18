import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'

import { baseClient } from '../api/baseClient.ts'
import { api } from '../api/client.ts'
import { readProblemMessage } from '../api/problem.ts'
import { queryClient } from '../api/queryClient.ts'
import { AuthContext } from './authContext.ts'
import type { AuthStatus, User } from './authContext.ts'
import { refreshAccessToken, setSessionExpiredHandler } from './refresh.ts'
import { clearAccessToken, setAccessToken } from './tokenStore.ts'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [user, setUser] = useState<User | null>(null)
  const started = useRef(false)

  useEffect(() => {
    setSessionExpiredHandler(() => {
      clearAccessToken()
      queryClient.clear()
      setUser(null)
      setStatus('anonymous')
    })
    return () => setSessionExpiredHandler(null)
  }, [])

  useEffect(() => {
    if (started.current) {
      return
    }
    started.current = true

    void (async () => {
      const refreshed = await refreshAccessToken()
      if (!refreshed) {
        setStatus('anonymous')
        return
      }

      const { data } = await api.GET('/api/v1/auth/me')
      if (data) {
        setUser(data)
        setStatus('authenticated')
      } else {
        clearAccessToken()
        setStatus('anonymous')
      }
    })()
  }, [])

  const login = useCallback(async (loginValue: string, password: string) => {
    const { data, response } = await baseClient.POST('/api/v1/auth/login', {
      body: { login: loginValue, password },
    })
    if (!response.ok || !data?.accessToken) {
      throw new Error(
        await readProblemMessage(
          response,
          response.status === 401 ? 'Неверный логин или пароль' : 'Не удалось выполнить вход',
        ),
      )
    }

    setAccessToken(data.accessToken)

    const me = await api.GET('/api/v1/auth/me')
    if (!me.data) {
      clearAccessToken()
      throw new Error('Не удалось загрузить профиль')
    }

    setUser(me.data)
    setStatus('authenticated')
  }, [])

  const logout = useCallback(async () => {
    await baseClient.POST('/api/v1/auth/logout', { body: {} }).catch(() => undefined)
    clearAccessToken()
    queryClient.clear()
    setUser(null)
    setStatus('anonymous')
  }, [])

  const value = useMemo(() => ({ status, user, login, logout }), [status, user, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
