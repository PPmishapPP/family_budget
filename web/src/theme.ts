import { createTheme } from '@mantine/core'

const fontFamily = 'system-ui, "Segoe UI", Roboto, sans-serif'

export const theme = createTheme({
  primaryColor: 'teal',
  defaultRadius: 'md',
  fontFamily,
  headings: { fontFamily },
})
