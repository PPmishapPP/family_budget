import { expect, test } from '@playwright/test'

const LOGIN = process.env.E2E_LOGIN
const PASSWORD = process.env.E2E_PASSWORD

test('логин → разовый платёж → прогноз → медиа', async ({ page }) => {
  if (!LOGIN || !PASSWORD) {
    test.skip(true, 'Задайте E2E_LOGIN и E2E_PASSWORD')
    return
  }

  const stamp = Date.now()
  const paymentName = `E2E платёж ${stamp}`
  const mediaName = `E2E фильм ${stamp}`

  await page.goto('login')
  await page.getByLabel('Логин').fill(LOGIN)
  await page.getByLabel('Пароль').fill(PASSWORD)
  await page.getByRole('button', { name: 'Войти' }).click()
  await expect(page.getByRole('heading', { name: 'План платежей' })).toBeVisible()

  await page.getByRole('button', { name: 'Разовый платёж' }).click()
  const paymentDialog = page.getByRole('dialog')
  await paymentDialog.getByLabel('Название платежа').fill(paymentName)
  await paymentDialog.getByLabel(/Сумма, ₽/).fill('1')
  const paymentResponse = page.waitForResponse(
    (response) =>
      response.url().includes('/payments') &&
      response.request().method() === 'POST' &&
      response.ok(),
  )
  await paymentDialog.getByRole('button', { name: 'Сохранить' }).click()
  await paymentResponse
  await expect(page.getByText('Платёж сохранён')).toBeVisible()

  await page.getByRole('button', { name: 'Выполнить прогноз платежей' }).click()
  const forecastDialog = page.getByRole('dialog')
  await forecastDialog.getByRole('button', { name: '+1 месяц' }).click()
  await expect(forecastDialog.getByLabel('Дата окончания расчёта')).not.toHaveValue('')
  const forecastResponse = page.waitForResponse(
    (response) =>
      response.url().includes('/forecast') &&
      response.request().method() === 'POST' &&
      response.ok(),
  )
  await forecastDialog.getByRole('button', { name: 'Рассчитать прогноз' }).click()
  await forecastResponse
  await expect(forecastDialog.getByRole('button', { name: 'Рассчитать прогноз' })).toBeEnabled()
  await forecastDialog.getByRole('button', { name: 'Отмена' }).click()

  await page.getByRole('link', { name: 'Фильмы и сериалы' }).click()
  await expect(page.getByRole('heading', { name: 'Фильмы и сериалы' })).toBeVisible()

  await page.getByRole('button', { name: 'Добавить', exact: true }).first().click()
  const mediaDialog = page.getByRole('dialog')
  await mediaDialog.getByLabel(/Название/).fill(mediaName)
  const createResponse = page.waitForResponse(
    (response) =>
      response.url().includes('/media') && response.request().method() === 'POST' && response.ok(),
  )
  await mediaDialog.getByRole('button', { name: 'Добавить' }).click()
  await createResponse
  await expect(page.getByText('Сохранено')).toBeVisible()
  await expect(page.getByText(mediaName, { exact: true })).toBeVisible()

  await page.getByRole('button', { name: `Удалить ${mediaName}` }).click()
  const deleteResponse = page.waitForResponse(
    (response) =>
      response.url().includes('/media/') &&
      response.request().method() === 'DELETE' &&
      response.ok(),
  )
  await page.getByRole('button', { name: 'Удалить', exact: true }).click()
  await deleteResponse
  await expect(page.getByText(mediaName, { exact: true })).toHaveCount(0)
})
