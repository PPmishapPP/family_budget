import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import type { MediaInput, MediaItem } from '../../api/media.ts'
import { renderWithProviders } from '../../test/render.tsx'
import { MediaFormModal } from './MediaFormModal.tsx'

const MOVIE: MediaItem = {
  id: 7,
  name: 'Сопрано',
  type: 'SERIES',
  status: 'WATCHED',
  rating: 9,
  description: 'Финальный сезон',
}

function setup(media: MediaItem | null) {
  const onSubmit = vi.fn<(input: MediaInput) => Promise<void>>().mockResolvedValue(undefined)
  const onClose = vi.fn()
  renderWithProviders(
    <MediaFormModal
      opened
      media={media}
      reference={undefined}
      submitting={false}
      onClose={onClose}
      onSubmit={onSubmit}
    />,
  )
  return { user: userEvent.setup(), onSubmit, onClose }
}

describe('MediaFormModal', () => {
  it('не отправляет форму без названия', async () => {
    const { user, onSubmit } = setup(null)

    await user.click(screen.getByRole('button', { name: 'Добавить' }))

    expect(await screen.findByText('Укажите название')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('создаёт запись со статусом PLAN_TO_WATCH и выбранным рейтингом', async () => {
    const { user, onSubmit } = setup(null)

    await user.type(screen.getByLabelText(/Название/), '  Дюна  ')
    await user.click(screen.getByRole('combobox', { name: 'Рейтинг' }))
    await user.click(screen.getByRole('option', { name: '8' }))
    await user.click(screen.getByRole('button', { name: 'Добавить' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toEqual({
      name: 'Дюна',
      type: 'MOVIE',
      status: 'PLAN_TO_WATCH',
      rating: 8,
      description: null,
    })
  })

  it('при редактировании предлагает только допустимые переходы и сохраняет текущий статус', async () => {
    const { user, onSubmit } = setup(MOVIE)

    expect(screen.getByText('Текущий статус: Просмотрено')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Не менять (Просмотрено)')).toBeInTheDocument()

    await user.click(screen.getByRole('combobox', { name: 'Статус' }))

    const options = screen.getAllByRole('option').map((option) => option.textContent)
    expect(options).toEqual(['В процессе', 'Ожидает продолжения'])
    expect(screen.queryByRole('option', { name: 'Просмотрено' })).not.toBeInTheDocument()

    await user.keyboard('{Escape}')
    await user.click(screen.getByRole('button', { name: 'Сохранить' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      name: 'Сопрано',
      type: 'SERIES',
      status: null,
      rating: 9,
      description: 'Финальный сезон',
    })
  })
})
