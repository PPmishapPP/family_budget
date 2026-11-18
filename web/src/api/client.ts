import createClient from 'openapi-fetch'

import type { paths } from './schema'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

export const api = createClient<paths>({
  baseUrl,
  credentials: 'include',
})

export type { components, operations, paths } from './schema'
