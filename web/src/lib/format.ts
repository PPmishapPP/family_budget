const DATE_PATTERN = /^(\d{4})-(\d{2})-(\d{2})/

const RUB_FORMAT = new Intl.NumberFormat('ru-RU', {
  style: 'currency',
  currency: 'RUB',
  maximumFractionDigits: 0,
})

export function formatDate(value: string | null | undefined): string {
  if (!value) {
    return '—'
  }
  const match = DATE_PATTERN.exec(value)
  if (!match) {
    return '—'
  }
  const [, year, month, day] = match
  return `${day}.${month}.${year}`
}

export function formatRub(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '—'
  }
  return RUB_FORMAT.format(value)
}
