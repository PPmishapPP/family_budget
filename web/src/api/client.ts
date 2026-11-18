import createClient from 'openapi-fetch'

import { refreshAccessToken } from '../auth/refresh.ts'
import { getAccessToken } from '../auth/tokenStore.ts'
import { apiBaseUrl } from './baseClient.ts'
import type { paths } from './schema'

const AUTH_PATHS = ['/auth/login', '/auth/refresh', '/auth/logout']

function isAuthPath(url: string): boolean {
  return AUTH_PATHS.some((path) => url.includes(path))
}

async function authFetch(request: Request): Promise<Response> {
  const token = getAccessToken()
  if (token) {
    request.headers.set('Authorization', `Bearer ${token}`)
  }

  const retry = request.clone()
  let response = await fetch(request)

  if (response.status === 401 && !isAuthPath(request.url)) {
    const refreshed = await refreshAccessToken()
    const nextToken = getAccessToken()
    if (refreshed && nextToken) {
      retry.headers.set('Authorization', `Bearer ${nextToken}`)
      response = await fetch(retry)
    }
  }

  return response
}

export const api = createClient<paths>({
  baseUrl: apiBaseUrl,
  credentials: 'include',
  fetch: authFetch,
})

export type { components, operations, paths } from './schema'
