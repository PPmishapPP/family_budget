import { baseClient } from '../api/baseClient.ts'
import { clearAccessToken, setAccessToken } from './tokenStore.ts'

type SessionExpiredHandler = () => void

let inFlight: Promise<boolean> | null = null
let sessionExpiredHandler: SessionExpiredHandler | null = null

export function setSessionExpiredHandler(handler: SessionExpiredHandler | null): void {
  sessionExpiredHandler = handler
}

export function refreshAccessToken(): Promise<boolean> {
  if (!inFlight) {
    inFlight = performRefresh().finally(() => {
      inFlight = null
    })
  }
  return inFlight
}

async function performRefresh(): Promise<boolean> {
  try {
    const { data, response } = await baseClient.POST('/api/v1/auth/refresh', { body: {} })
    if (response.ok && data?.accessToken) {
      setAccessToken(data.accessToken)
      return true
    }
  } catch {
    return expireSession()
  }

  return expireSession()
}

function expireSession(): false {
  clearAccessToken()
  sessionExpiredHandler?.()
  return false
}
