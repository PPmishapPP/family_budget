import { MantineProvider } from '@mantine/core'
import { ModalsProvider } from '@mantine/modals'
import { render } from '@testing-library/react'
import type { ReactElement, ReactNode } from 'react'

import { theme } from '../theme.ts'

function Providers({ children }: { children: ReactNode }) {
  return (
    <MantineProvider theme={theme} defaultColorScheme="light" env="test">
      <ModalsProvider>{children}</ModalsProvider>
    </MantineProvider>
  )
}

export function renderWithProviders(ui: ReactElement) {
  return render(ui, { wrapper: Providers })
}
