import { defineConfig } from '@playwright/test'

const baseURL = process.env.E2E_BASE_URL ?? 'http://127.0.0.1:5173/app/'
const hasCredentials = Boolean(process.env.E2E_LOGIN && process.env.E2E_PASSWORD)

export default defineConfig({
  testDir: './e2e',
  testIgnore: hasCredentials ? undefined : ['**/smoke.spec.ts'],
  timeout: 90_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  webServer:
    hasCredentials && !process.env.E2E_BASE_URL
      ? {
          command: 'npm run dev -- --host 127.0.0.1',
          url: baseURL,
          reuseExistingServer: !process.env.CI,
          timeout: 120_000,
        }
      : undefined,
})
