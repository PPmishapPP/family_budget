import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { MantineProvider } from '@mantine/core'
import { ModalsProvider } from '@mantine/modals'
import { Notifications } from '@mantine/notifications'
import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

import '@mantine/core/styles.css'
import '@mantine/notifications/styles.css'
import './index.css'

import App from './App.tsx'
import { AccountProvider } from './accounts/AccountProvider.tsx'
import { queryClient } from './api/queryClient.ts'
import { AuthProvider } from './auth/AuthProvider.tsx'
import { theme } from './theme.ts'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="auto">
      <QueryClientProvider client={queryClient}>
        <Notifications />
        <ModalsProvider>
          <BrowserRouter basename={import.meta.env.BASE_URL}>
            <AuthProvider>
              <AccountProvider>
                <App />
              </AccountProvider>
            </AuthProvider>
          </BrowserRouter>
        </ModalsProvider>
      </QueryClientProvider>
    </MantineProvider>
  </StrictMode>,
)
