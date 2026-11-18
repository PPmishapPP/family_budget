import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { renderWithProviders } from '../../test/render.tsx'
import { PaymentRuleFormModal } from './PaymentRuleFormModal.tsx'

function setup() {
  const onSubmit = vi.fn().mockResolvedValue(undefined)
  const onClose = vi.fn()
  renderWithProviders(
    <PaymentRuleFormModal
      opened
      rule={null}
      submitting={false}
      onClose={onClose}
      onSubmit={onSubmit}
    />,
  )
  return { user: userEvent.setup(), onSubmit, onClose }
}

describe('PaymentRuleFormModal', () => {
  it('не отправляет форму без названия', async () => {
    const { user, onSubmit } = setup()

    await user.click(screen.getByRole('button', { name: 'Добавить' }))

    expect(await screen.findByText('Укажите название')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('разрешает отрицательную сумму и отправляет trimmed название', async () => {
    const { user, onSubmit } = setup()

    await user.type(screen.getByLabelText(/Название/), '  Аренда  ')
    const sumInput = screen.getByLabelText(/Сумма, ₽/)
    await user.clear(sumInput)
    await user.type(sumInput, '-1500')
    await user.click(screen.getByRole('button', { name: 'Добавить' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      name: 'Аренда',
      sum: -1500,
      type: 'MONTHLY',
      active: true,
      endDate: null,
    })
  })
})
