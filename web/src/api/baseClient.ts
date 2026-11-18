import createClient from 'openapi-fetch'

import type { paths } from './schema'

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

export const baseClient = createClient<paths>({
  baseUrl: apiBaseUrl,
  credentials: 'include',
})
