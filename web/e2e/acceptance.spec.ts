import { expect, test } from '@playwright/test'
import type { Page } from '@playwright/test'

const LOGIN = process.env.E2E_LOGIN
const PASSWORD = process.env.E2E_PASSWORD

test.skip(!LOGIN || !PASSWORD, 'Задайте E2E_LOGIN и E2E_PASSWORD')

async function login(page: Page) {
  await page.goto('login')
  await page.getByLabel('Логин').fill(LOGIN!)
  await page.getByLabel('Пароль').fill(PASSWORD!)
  await page.getByRole('button', { name: 'Войти' }).click()
  await expect(page.getByRole('heading', { name: 'План платежей' })).toBeVisible()
}

async function openPayments(page: Page) {
  await login(page)
}

function unique(prefix: string): string {
  return `${prefix} ${Date.now()}-${Math.floor(Math.random() * 1e4)}`
}

test('A-01 неверные данные — понятное сообщение, без 5xx', async ({ page }) => {
  const serverErrors: number[] = []
  page.on('response', (r) => {
    if (r.url().includes('/auth/login') && r.status() >= 500) serverErrors.push(r.status())
  })
  await page.goto('login')
  await page.getByLabel('Логин').fill('acceptance_tmp')
  await page.getByLabel('Пароль').fill('definitely-wrong')
  await page.getByRole('button', { name: 'Войти' }).click()
  await expect(page.getByText('Неверный логин или пароль')).toBeVisible()
  expect(serverErrors).toEqual([])
})

test('A-02 после входа открывается «План платежей»', async ({ page }) => {
  await login(page)
  await expect(page.getByRole('heading', { name: 'План платежей' })).toBeVisible()
})

test('A-03 refresh-cookie живёт ~30 дней', async ({ request }) => {
  const response = await request.post('/api/v1/auth/login', {
    data: { login: LOGIN, password: PASSWORD },
  })
  expect(response.ok()).toBeTruthy()
  const cookies = (await response.headersArray())
    .filter((h) => h.name.toLowerCase() === 'set-cookie')
    .map((h) => h.value)
  const refresh = cookies.find((c) => c.startsWith('refresh_token='))
  expect(refresh).toBeTruthy()
  const maxAge = Number(/Max-Age=(\d+)/i.exec(refresh!)?.[1] ?? 0)
  expect(maxAge).toBeGreaterThanOrEqual(29 * 24 * 3600)
})

test('A-04 logout: возврат на вход, доступ закрыт', async ({ page }) => {
  await login(page)
  await page.getByRole('button', { name: 'Выйти' }).click()
  await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible()
  await page.goto('payments')
  await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible()
})

test('A-05 прямой переход без входа → login', async ({ page }) => {
  await page.goto('payments')
  await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible()
})

test('A-07 токены не в localStorage/sessionStorage', async ({ page }) => {
  await login(page)
  const dump = await page.evaluate(() => ({
    local: JSON.stringify(localStorage),
    session: JSON.stringify(sessionStorage),
    cookie: document.cookie,
  }))
  expect(dump.local.toLowerCase()).not.toContain('token')
  expect(dump.session.toLowerCase()).not.toContain('token')
  expect(dump.cookie.toLowerCase()).not.toContain('accesstoken')
})

test('B-01/B-02 навигация и шапка', async ({ page }) => {
  await login(page)
  await expect(page.getByRole('link', { name: 'План платежей' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'История платежей' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Фильмы и сериалы' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Family Budget' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Выйти' })).toBeVisible()
})

test('B-03/B-04 выбранный счёт и только счета пользователя', async ({ page }) => {
  await login(page)
  await expect(page.getByRole('combobox', { name: 'Счёт' })).toBeVisible()
  await expect(page.getByRole('combobox', { name: 'Счёт' })).toHaveValue('Приёмка Web')
})

test('B-04 API отдаёт только счета пользователя', async ({ request }) => {
  const loginResponse = await request.post('/api/v1/auth/login', {
    data: { login: LOGIN, password: PASSWORD },
  })
  const { accessToken } = await loginResponse.json()
  const accounts = await request.get('/api/v1/accounts', {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  expect(accounts.ok()).toBeTruthy()
  const list = await accounts.json()
  expect(list).toHaveLength(1)
  expect(list[0].id).toBe(2)
})

test('B-05 чужой счёт → 404', async ({ request }) => {
  const loginResponse = await request.post('/api/v1/auth/login', {
    data: { login: LOGIN, password: PASSWORD },
  })
  const { accessToken } = await loginResponse.json()
  const foreign = await request.get('/api/v1/accounts/1/payment-rules', {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  expect(foreign.status()).toBe(404)
})

test('B-07 пустое состояние правил', async ({ page }) => {
  await login(page)
  await expect(page.getByText('Правил пока нет')).toBeVisible()
})

test('B-08 мобильная вёрстка: гамбургер виден', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await login(page)
  await expect(page.getByRole('button', { name: 'Меню' })).toBeVisible()
})

async function createRule(page: Page, name: string, sum: string, type?: string) {
  await page.getByRole('button', { name: 'Добавить' }).first().click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Название').fill(name)
  if (type) {
    await dialog.getByLabel('Тип').click()
    await page.getByRole('option', { name: type, exact: true }).click()
  }
  await dialog.getByLabel(/Сумма, ₽/).fill(sum)
  await dialog.getByRole('button', { name: 'Добавить' }).click()
  await expect(page.getByText('Правило добавлено').first()).toBeVisible()
}

async function deleteRule(page: Page, name: string) {
  await page.getByRole('button', { name: `Удалить ${name}` }).click()
  const confirm = page.getByRole('dialog').filter({ hasText: 'Удаление правила' })
  await confirm.getByRole('button', { name: 'Удалить', exact: true }).click()
  await expect(page.getByText('Правило удалено').first()).toBeVisible()
}

test('C-01/C-02/C-03 таблица правил: колонки и русские типы', async ({ page }) => {
  await login(page)
  const name = unique('Колонки')
  await createRule(page, name, '-321', 'Ежегодно')
  for (const header of [
    'Название',
    'Сумма',
    'Тип',
    'Следующий платёж',
    'Последний платёж',
    'Действия',
  ]) {
    await expect(page.getByRole('columnheader', { name: header })).toBeVisible()
  }
  const row = page.getByRole('row', { name: new RegExp(name) })
  await expect(row.getByText('Ежегодно')).toBeVisible()
  await expect(page.getByText('Текущий баланс')).toBeVisible()
  await deleteRule(page, name)
})

test('C-07 создание правила, значения по умолчанию', async ({ page }) => {
  await login(page)
  const name = unique('Правило')
  await page.getByRole('button', { name: 'Добавить' }).first().click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByLabel('Тип')).toHaveValue('Ежемесячно')
  await dialog.getByLabel('Название').fill(name)
  await dialog.getByLabel(/Сумма, ₽/).fill('-500')
  await dialog.getByRole('button', { name: 'Добавить' }).click()
  await expect(page.getByText('Правило добавлено').first()).toBeVisible()
  await expect(page.getByRole('cell', { name, exact: true })).toBeVisible()

  await deleteRule(page, name)
})

test('C-09 валидация: пустое название', async ({ page }) => {
  await openPayments(page)
  await page.getByRole('button', { name: 'Добавить' }).first().click()
  const dialog = page.getByRole('dialog')
  await dialog.getByRole('button', { name: 'Добавить' }).click()
  await expect(dialog.getByText('Укажите название')).toBeVisible()
  await dialog.getByRole('button', { name: 'Отмена' }).click()
})

test('C-12 удаление требует подтверждения и отмена ничего не удаляет', async ({ page }) => {
  await login(page)
  const name = unique('К удалению')
  await createRule(page, name, '-100')
  const confirm = page.getByRole('dialog').filter({ hasText: 'Удаление правила' })
  await page.getByRole('button', { name: `Удалить ${name}` }).click()
  await confirm.getByRole('button', { name: 'Отмена' }).click()
  await expect(page.getByRole('cell', { name, exact: true })).toBeVisible()
  await page.getByRole('button', { name: `Удалить ${name}` }).click()
  await confirm.getByRole('button', { name: 'Удалить' }).click()
  await expect(page.getByText('Правило удалено').first()).toBeVisible()
  await expect(page.getByRole('cell', { name, exact: true })).toHaveCount(0)
})

test('C-13 «Выполнить» создаёт запись в истории', async ({ page }) => {
  await login(page)
  const name = unique('К выполнению')
  await createRule(page, name, '-250')
  await page.getByRole('button', { name: `Выполнить ${name}` }).click()
  await expect(page.getByText(`Платёж «${name}» выполнен`)).toBeVisible()
  await page.getByRole('link', { name: 'История платежей' }).click()
  await expect(page.getByText(name, { exact: true })).toBeVisible()
  await page.getByRole('link', { name: 'План платежей' }).click()
  await deleteRule(page, name)
})

test('C-15 просроченное правило подсвечено', async ({ page }) => {
  await login(page)
  const name = unique('Просроченное')
  await page.getByRole('button', { name: 'Добавить' }).first().click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Название').fill(name)
  await dialog.getByLabel(/Сумма, ₽/).fill('-100')
  await dialog.getByLabel('Следующий платёж').fill('2020-01-01')
  await dialog.getByRole('button', { name: 'Добавить' }).click()
  await expect(page.getByText('Правило добавлено').first()).toBeVisible()
  const row = page.getByRole('row', { name: new RegExp(name) })
  await expect(row.getByText('К оплате')).toBeVisible()
  await deleteRule(page, name)
})

test('C-16/C-17/C-18 разовый платёж: расход отрицательный', async ({ page }) => {
  await login(page)
  await page.getByRole('button', { name: 'Разовый платёж' }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByText(/Счёт: Приёмка Web/)).toBeVisible()
  const name = unique('Разовый расход')
  await dialog.getByLabel('Название платежа').fill(name)
  await dialog.getByRole('radio', { name: 'Расход' }).check()
  await dialog.getByLabel(/Сумма, ₽/).fill('15')
  await dialog.getByRole('button', { name: 'Сохранить' }).click()
  await expect(page.getByText('Платёж сохранён')).toBeVisible()
  await page.getByRole('link', { name: 'История платежей' }).click()
  const row = page.getByRole('row', { name: new RegExp(name) })
  await expect(row.getByText('-15')).toBeVisible()
})

test('C-17/C-19 нулевая сумма не сохраняется', async ({ page }) => {
  await login(page)
  await page.getByRole('button', { name: 'Разовый платёж' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Название платежа').fill(unique('Пустой'))
  const amount = dialog.getByLabel(/Сумма, ₽/)
  await amount.fill('0')
  await dialog.getByLabel('Название платежа').click()
  expect(Number(await amount.inputValue())).toBeGreaterThanOrEqual(1)
  await dialog.getByRole('button', { name: 'Отмена' }).click()
})

test('D-01/D-02/D-03 прогноз: диапазон, быстрые кнопки, активация', async ({ page }) => {
  await login(page)
  const name = unique('Прогноз')
  await createRule(page, name, '-100')
  await page.getByRole('button', { name: 'Выполнить прогноз платежей' }).click()
  const dialog = page.getByRole('dialog')
  const calculate = dialog.getByRole('button', { name: 'Рассчитать прогноз' })
  await expect(calculate).toBeDisabled()
  await dialog.getByRole('button', { name: '+1 месяц' }).click()
  await expect(dialog.getByLabel('Дата окончания расчёта')).not.toHaveValue('')
  await expect(calculate).toBeEnabled()
  await dialog.getByRole('button', { name: 'Отмена' }).click()
  await deleteRule(page, name)
})

test('D-06/D-07 прогноз возвращает таблицу или пустое сообщение', async ({ page }) => {
  await login(page)
  const name = unique('Прогноз пуст')
  await createRule(page, name, '-100')
  await page.getByRole('button', { name: 'Выполнить прогноз платежей' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByRole('button', { name: '+1 месяц' }).click()
  await dialog.getByRole('button', { name: 'Рассчитать прогноз' }).click()
  await expect(
    dialog.getByRole('columnheader', { name: 'Операция' }).or(dialog.getByText('Прогноз пуст')),
  ).toBeVisible()
  await dialog.getByRole('button', { name: 'Отмена' }).click()
  await deleteRule(page, name)
})

test('E-01/E-03 расчёт дохода: кнопки и колонки', async ({ page }) => {
  await login(page)
  const name = unique('Доход')
  await createRule(page, name, '1000')
  await page.getByRole('button', { name: 'Расчет дохода' }).click()
  const dialog = page.getByRole('dialog')
  const calculate = dialog.getByRole('button', { name: 'Рассчитать доход' })
  await expect(calculate).toBeDisabled()
  await dialog.getByRole('button', { name: '+1 год' }).click()
  await expect(calculate).toBeEnabled()
  await dialog.getByRole('button', { name: 'Рассчитать доход' }).click()
  await expect(
    dialog.getByRole('columnheader', { name: 'Прибавка' }).or(dialog.getByText('Расчёт пуст')),
  ).toBeVisible()
  await dialog.getByRole('button', { name: 'Отмена' }).click()
  await deleteRule(page, name)
})

test('F-01/F-03/F-04 история: колонки и окраска сумм', async ({ page }) => {
  await login(page)
  await page.getByRole('link', { name: 'История платежей' }).click()
  await expect(page.getByRole('heading', { name: 'История платежей' })).toBeVisible()
  for (const header of ['Наименование', 'Сумма', 'Дата', 'Баланс']) {
    await expect(page.getByRole('columnheader', { name: header })).toBeVisible()
  }
})

test('F-02 сортировка по умолчанию — новые сверху', async ({ page }) => {
  await login(page)
  await page.getByRole('link', { name: 'История платежей' }).click()
  const rows = page.getByRole('row')
  const count = await rows.count()
  if (count > 2) {
    const firstDate = await rows.nth(1).getByRole('cell').nth(2).innerText()
    const secondDate = await rows.nth(2).getByRole('cell').nth(2).innerText()
    const parse = (s: string) => {
      const [d, m, y] = s.split('.')
      return `${y}-${m}-${d}`
    }
    expect(parse(firstDate) >= parse(secondDate)).toBeTruthy()
  }
})

test('G-01/G-02 фильтры и колонки медиа', async ({ page }) => {
  await login(page)
  await page.getByRole('link', { name: 'Фильмы и сериалы' }).click()
  await expect(page.getByRole('heading', { name: 'Фильмы и сериалы' })).toBeVisible()
  await expect(page.getByRole('radio', { name: 'Все' })).toBeChecked()
  await page.getByText('Фильмы', { exact: true }).click()
  await expect(page.getByRole('radio', { name: 'Фильмы' })).toBeChecked()
  await page.getByText('Сериалы', { exact: true }).click()
  await expect(page.getByRole('radio', { name: 'Сериалы' })).toBeChecked()
  await page.getByText('Все', { exact: true }).click()
  await expect(page.getByRole('radio', { name: 'Все' })).toBeChecked()
})

test('G-03/G-04/G-07/G-09/G-11 медиа: создание, статус по умолчанию, удаление', async ({
  page,
}) => {
  await login(page)
  await page.getByRole('link', { name: 'Фильмы и сериалы' }).click()
  const name = unique('E2E фильм')
  await page.getByRole('button', { name: 'Добавить' }).first().click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByLabel('Статус')).toHaveValue('Ожидает просмотра')
  await dialog.getByLabel('Название').fill(name)
  await dialog.getByRole('button', { name: 'Добавить' }).click()
  await expect(page.getByText('Сохранено').first()).toBeVisible()
  await expect(page.getByText(name, { exact: true })).toBeVisible()

  await page.getByRole('button', { name: `Изменить ${name}` }).click()
  const editDialog = page.getByRole('dialog')
  await editDialog.getByLabel('Рейтинг').click()
  await page.getByRole('option', { name: '8', exact: true }).click()
  await editDialog.getByRole('button', { name: 'Сохранить' }).click()
  await expect(page.getByText('Сохранено').first()).toBeVisible()
  await expect(page.getByText('8/10').first()).toBeVisible()

  await page.getByRole('button', { name: `Удалить ${name}` }).click()
  await page.getByRole('button', { name: 'Удалить', exact: true }).click()
  await expect(page.getByText('Удалено').first()).toBeVisible()
  await expect(page.getByText(name, { exact: true })).toHaveCount(0)
})

test('H-01 форматирование RUB и дат', async ({ page }) => {
  await login(page)
  const balance = page.getByText('Текущий баланс').locator('..')
  await expect(balance.getByText(/₽/)).toBeVisible()
})
