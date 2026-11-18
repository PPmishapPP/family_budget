type ProblemDetail = {
  detail?: string
  title?: string
  errors?: Record<string, string>
}

export async function readProblemMessage(
  response: Response,
  fallback = 'Не удалось выполнить запрос',
): Promise<string> {
  try {
    const problem = (await response.json()) as ProblemDetail
    if (problem.detail) {
      return problem.detail
    }
    if (problem.errors) {
      const first = Object.values(problem.errors).find((value) => Boolean(value))
      if (first) {
        return first
      }
    }
    if (problem.title) {
      return problem.title
    }
  } catch {
    return fallback
  }
  return fallback
}
