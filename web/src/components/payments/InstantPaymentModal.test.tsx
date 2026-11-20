import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { renderWithProviders } from '../../test/render.tsx'
import { InstantPaymentModal } from './InstantPaymentModal.tsx'

function setup() {
  const onSubmit = vi.fn().mockResolvedValue(undefined)
  const onClose = vi.fn()
  renderWithProviders(
    <InstantPaymentModal
      opened
      accountName="Основной"
      submitting={false}
      onClose={onClose}
      onSubmit={onSubmit}
    />,
  )
  return { user: userEvent.setup(), onSubmit, onClose }
}

describe('InstantPaymentModal', () => {
  it('требует название и положительную сумму', async () => {
    const { user, onSubmit } = setup()

    await user.click(screen.getByRole('button', { name: 'Сохранить' }))

    expect(await screen.findByText('Укажите название')).toBeInTheDocument()
    expect(
      await screen.findByText('Сумма должна быть целым числом больше нуля'),
    ).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('отправляет доход с суммой и trimmed названием', async () => {
    const { user, onSubmit } = setup()

    await user.type(screen.getByLabelText(/Название платежа/), '  Зарплата  ')
    const amountInput = screen.getByLabelText(/Сумма, ₽/)
    await user.clear(amountInput)
    await user.type(amountInput, '1500')
    await user.click(screen.getByRole('radio', { name: 'Доход' }))
    await user.click(screen.getByRole('button', { name: 'Сохранить' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toEqual({
      name: 'Зарплата',
      direction: 'INCOME',
      amount: 1500,
    })
  })
})
