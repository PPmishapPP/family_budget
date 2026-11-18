export function errorMessage(error: unknown, fallback = 'Не удалось выполнить операцию'): string {
  return error instanceof Error && error.message ? error.message : fallback
}
