# План миграции: Vaadin -> REST API + Web SPA + Android

Статус: в работе (Этап 0 выполнен; Этап 1 выполнен (1.1–1.12);
Этап 2 выполнен (2.1–2.11); ветка `feature/web` от `dev`)
Обновлено: 2026-11-19
Связанные документы: [DECISIONS.md](DECISIONS.md)

## 1. Контекст и цель

Сейчас приложение `family_budget` — Spring Boot 4.1.1 (Java 25, Spring Data JDBC,
PostgreSQL + Flyway) с UI на Vaadin 25.2.6. Доступно только из браузера.

Цель:

1. Вынести функциональность в REST API (`/api/v1`), пригодное для переиспользования.
2. Переписать web-фронтенд с Vaadin на стандартный SPA-стек.
3. Сделать Android-приложение с тем же функционалом.
4. После достижения паритета удалить Vaadin и упростить сборку/деплой.

Ключевой принцип — **strangler**: REST API добавляется рядом с Vaadin, приложение
остаётся рабочим на каждом шаге, Vaadin удаляется только после паритета web и Android.

## 2. Целевая архитектура

```text
┌──────────────────────┐        ┌──────────────────────┐
│  Web SPA             │        │  Android (Kotlin)    │
│  React + TS          │        │  Jetpack Compose     │
└──────────┬───────────┘        └──────────┬───────────┘
           │  HTTPS /api/v1 (JWT)          │
           └───────────────┬───────────────┘
                           ▼
              ┌──────────────────────────┐
              │ Spring Boot (bot)        │
              │ Controllers -> Services  │
              │ -> Repositories (JDBC)   │
              │ + OpenAPI + JWT          │
              │ + legacy Vaadin (временно)│
              └────────────┬─────────────┘
                           ▼
                     PostgreSQL + Flyway
```

## 3. Стек и решения

Подробности и статусы — в [DECISIONS.md](DECISIONS.md).

| Слой | Выбор |
|---|---|
| Web | React 19 + TypeScript + Vite + React Router |
| UI | Mantine (или shadcn/Tailwind) |
| Данные/формы web | TanStack Query + React Hook Form + Zod |
| Android | Kotlin + Jetpack Compose + Material 3 + Hilt |
| Android IDE | **Android Studio** (обязательно; проект `android/` открывается в нём) |
| Сеть Android | Retrofit/OkHttp + kotlinx.serialization |
| API | REST `/api/v1` + OpenAPI (springdoc) |
| Auth | JWT access 15 мин + refresh 30 дней |
| Деплой | SPA раздаётся тем же Spring Boot jar |

## 4. Структура репозитория

```text
family_budget/
  bot/        # backend (домен, REST, временно Vaadin)
  web/        # React SPA
  android/    # отдельный Gradle-проект, открывается в Android Studio
  docs/       # планы, решения, контракт API
  docs/api/   # OpenAPI/коллекция запросов (появится на Этапе 1)
```

`android/` не включается в корневой `settings.gradle`: у него собственный Gradle
wrapper и Android Gradle Plugin, чтобы не конфликтовать со сборкой backend.
`bot` переименовывается в `backend` только в конце (Этап 4), чтобы не ломать сборку.

## 5. Контракт API (черновик)

Все операции с счётом проверяют, что счёт принадлежит текущему пользователю
(`user_account`), иначе `404` (чтобы не раскрывать чужие id).

| Группа | Методы |
|---|---|
| Auth | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` |
| Accounts | `GET /accounts`, `GET /accounts/{id}`, `GET /accounts/{id}/balance` |
| History | `GET /accounts/{id}/history?page&size` |
| Payment rules | `GET/POST /accounts/{id}/payment-rules`, `PUT/DELETE .../{ruleId}`, `POST .../{ruleId}/execute` |
| Instant payment | `POST /accounts/{id}/payments` `{name, amount, direction: EXPENSE\|INCOME}` |
| Forecast | `POST /accounts/{id}/forecast` `{to, rules?}` |
| Income forecast | `POST /accounts/{id}/income-forecast` `{to}` |
| Media | `GET /accounts/{id}/media?type=`, `POST`, `PUT .../{mediaId}`, `DELETE .../{mediaId}` |
| Reference | `GET /reference/payment-types`, `/media-types`, `/media-statuses` (с `availableTransitions`) |

Важные решения по контракту:

- `forecast` принимает необязательный `rules`, потому что `PaymentsView` сейчас
  считает прогноз по несохранённым правкам в гриде (`ForecastView` получает `rules`
  из памяти). Без этого паритета не будет.
- `IncomeDto` сейчас возвращает отформатированные строки (`RUB`, `dd.MM.yyyy`).
  В API отдаём числа и `LocalDate`, форматирование — на клиенте.
- Статусы медиа валидируются на сервере (`MediaStatus.getAvailableTransitions`),
  а не только в UI.
- `MediaService` расширяем create/update + MapStruct-маппингом (закрываем TODO
  `MediaView.saveMediaItem`).
- `direction` разового платежа: сервер сам превращает в `+/-` и валидирует
  (логика уже есть в `AccountService.registerPayment`).

## 6. Этапы и подзадачи

### Этап 0. Подготовка (0.5–1 день)

- [x] 0.1. Нормализовать переводы строк: `.gitattributes`, локальный
      `core.autocrlf=false`, `git add --renormalize`, перечитать рабочее дерево.
      Выполнено 2026-11-13. Изменения подготовлены и застейджены; коммит
      baseline выполняет пользователь (git identity в окружении не настроен).
- [x] 0.2. Зафиксировать стек и решения: [DECISIONS.md](DECISIONS.md).
      Выполнено 2026-11-13.
- [x] 0.3. Создать ветку `feature/rest-api` от `dev`; проверить, что
      `:bot:test` и `:bot:bootJar` зелёные. Выполнено 2026-11-13:
      ветка создана, `AccountServiceTest` — 6/6 PASSED, `:bot:bootJar` — BUILD SUCCESSFUL.
- [x] 0.4. Создать каркас каталогов `web/`, `android/`, `docs/api/`.
      Выполнено 2026-11-13 (пустые каталоги с `.gitkeep`; наполнение — этапы 2.1 и 3.1).
- [x] 0.5. Записать чек-лист паритета функций в `docs/` (см. раздел 9).
      Выполнено 2026-11-16: [PARITY_CHECKLIST.md](PARITY_CHECKLIST.md).

**Готово, когда:** есть чистый baseline-коммит и зафиксированный список
экранов/действий.

### Этап 1. Backend REST API (5–8 дней)

- [x] 1.1. Добавить зависимости: `spring-boot-starter-web`, validation,
      `spring-boot-starter-oauth2-resource-server` (JWT), springdoc-openapi,
      Testcontainers. Vaadin-зависимости пока не трогаем.
      Выполнено 2026-11-16: `spring-boot-starter-webmvc`, validation, oauth2-resource-server,
      springdoc 3.2.0, Testcontainers 2.0.5; `:bot:test` и `:bot:bootJar` зелёные.
- [x] 1.2. Пакет `ru.mishapp.api`: `controller`, `dto`, `error`, `security`, `config`.
      Выполнено 2026-11-17: `dto`, `error`, `config`, `security`, `controller`, `mapper`;
      контроллеры `Auth`, `Account`, `History`, `Payment`, `PaymentRule`, `Forecast`,
      `Media`, `Reference`; транспортные детали вынесены в `AuthCookieService`,
      `ReferenceApiMapper` и `ApiPaths`.
- [x] 1.3. Отдельные API-DTO + Jakarta Validation. Сущности наружу не отдаём.
      Выполнено 2026-11-16: `ru.mishapp.api.dto` (auth, accounts, history, rules,
      payments, forecast, media, reference, `PageResponse`).
- [x] 1.4. `GlobalExceptionHandler` на RFC 7807 `ProblemDetail` (`400/401/403/404/409`).
      Выполнено 2026-11-16: `ProblemDetail` + `errors` для валидации, 400/403/404/409/500.
- [x] 1.5. `AccountAccessService`: проверка принадлежности счёта/правила/медиа
      пользователю; применять во всех эндпоинтах.
      Выполнено 2026-11-16: `ru.mishapp.api.security.AccountAccessService`
      (+ запросы в репозиториях), тест `AccountAccessServiceTest` — 7/7.
- [x] 1.6. Auth:
  - выдача access/refresh JWT (claims: `sub`, `uid`, `exp`);
  - таблица `refresh_token` (V4, хранить SHA-256 хеш, `expires_at`, `revoked`);
  - ротация refresh, отзыв при logout;
  - refresh cookie `HttpOnly` для web + токен в body для Android;
    `/refresh` принимает и cookie, и body.
      Выполнено 2026-11-16: `JwtService`, `AuthService`, `AuthController`,
      `V4__refresh_tokens.sql`; access 15 мин / refresh 30 дней, секрет из
      `JWT_SECRET` (dev-значение по умолчанию), тесты `AuthServiceTest` 11/11,
      `JwtServiceTest` 2/2, `AuthControllerTest` 8/8.
- [x] 1.7. Два `SecurityFilterChain`: `/api/**` — stateless JWT, CSRF off;
      остальное — текущая Vaadin-сессия. CORS из property.
      Выполнено 2026-11-16: `ApiSecurityConfiguration` (`@Order(1)`), Vaadin-цепочка
      `@Order(2)`; CORS из `app.cors.allowed-origins`; 401/403 — `ProblemDetail`.
- [x] 1.8. Реализовать контроллеры по таблице выше, переиспользуя существующие
      сервисы (не дублировать бизнес-логику).
      Выполнено 2026-11-16: счета, баланс, история с `page/size`, разовый платёж,
      CRUD/execute правил, прогноз и доход, медиа, справочники; ownership через
      `AccountAccessService`; создание — `201 + Location`, удаление/execute — `204`.
- [x] 1.9. Дописать недостающее в домене: `MediaService.create/update`, валидация
      медиа, серверная валидация статусов/рейтинга/длин имён.
      Выполнено 2026-11-16: `MediaService` create/update + проверка переходов
      статусов, `MediaView.loadMediaForAccount/saveMediaItem` больше не TODO,
      новая строка правила получает `nextDay = today`, расчёт прогноза терпит
      отсутствие истории (`balance = 0`).
- [x] 1.10. OpenAPI: `/v3/api-docs` + Swagger UI; зафиксировать `openapi.json`
      в репозитории.
      Выполнено 2026-11-16: `OpenApiConfiguration` (info + `bearerAuth`),
      `ApiControllersTest.shouldExposeOpenApiDocument` проверяет `/v3/api-docs`
      и пишет `bot/build/openapi.json`; контракт сохранён в `docs/openapi.json`.
- [x] 1.11. Индексы БД (V3): `account_history(account_id, date_time)`,
      `media(account_id, type)`, `periodic_change_rule(target_account_id)`.
      Выполнено 2026-11-16: `V3__api_indexes.sql`.
- [x] 1.12. Тесты:
  - unit — сервисы/валидация;
  - `MockMvc` — контракт, коды ошибок;
  - `@SpringBootTest` + Testcontainers PostgreSQL — auth, ownership, refresh-ротация;
  - регресс: Vaadin-экран открывается, старые тесты зелёные.
      Выполнено 2026-11-17: тесты приведены в соответствие с `*Command`/сущностями;
      `61/61` успешно, включая `ApiIntegrationTest` (`@SpringBootTest` +
      Testcontainers PostgreSQL: login/refresh/rotation, ownership). Покрытие:
      unit `AuthServiceTest`/`JwtServiceTest`, MockMvc `AuthControllerTest` и
      `ApiControllersTest`, unit `PeriodicChangeRuleServiceTest`, `MediaServiceTest`,
      `RuleExecuteServiceTest`, `ForecastServiceTest`, `ForecastCalculatorTest`.
      Локальный `:bot:test` не стартует из-за лимита длины командной строки Windows
      (208 записей classpath = 33 187 символов при лимите 32 767); прогон выполнен
      через JUnit Platform Launcher. На CI/Linux или с коротким `GRADLE_USER_HOME`
      Gradle-прогон должен работать штатно. Ручной smoke Vaadin — перед этапом 4.

**Готово, когда:** все функции UI доступны через API, контракт в OpenAPI,
тесты покрывают auth/ownership/CRUD/forecast, Vaadin не сломан.

### Этап 2. Web SPA (7–10 дней)

- [x] 2.1. Scaffold `web/` (Vite + React + TS + ESLint/Prettier).
- [x] 2.2. UI-kit, роутер, layout, тема; мобильная адаптивность.
- [x] 2.3. Генерация API-клиента (orval/openapi-typescript) из `openapi.json`,
      npm-скрипт `api:gen`.
- [x] 2.4. Auth: login-экран, access в памяти, refresh — HttpOnly cookie,
      silent refresh при загрузке, logout.
- [x] 2.5. Общие компоненты: выбор счёта, формат `dd.MM.yyyy` и `ru-RU` RUB,
      loading/empty/error, confirm-диалоги, toast.
- [x] 2.6. Экран «План платежей»: баланс, список правил, CRUD, «Выполнить»,
      «Разовый платёж», «Прогноз», «Расчёт дохода».
- [x] 2.7. Экран «История платежей»: выбор счёта, пагинация, сортировка.
- [x] 2.8. Экран «Фильмы/сериалы»: фильтры все/фильмы/сериалы, CRUD, статусы
      с допустимыми переходами, рейтинг 1–10, описание.
- [x] 2.9. Тесты: Vitest + RTL на формы/валидацию; Playwright smoke
      (логин -> платёж -> прогноз -> медиа).
      Выполнено 2026-11-18: Vitest/RTL на формы и утилиты, Playwright smoke
      (запускается при `E2E_LOGIN`/`E2E_PASSWORD`).
- [x] 2.10. Интеграция раздачи: сборка SPA -> static ресурсы Spring Boot,
      SPA-fallback; dev через Vite proxy на `:8080`.
      Выполнено 2026-11-19: `SpaWebConfiguration` (resource handler `/app/**`,
      fallback на `index.html`, redirect `/app` -> `/app/`),
      `SpaSecurityConfiguration` (`@Order(0)`, `/app/**` публично),
      задачи `webBuild`/`stageWebApp` в `bot/build.gradle`, Vite proxy
      `/api` -> `:8080`; `SpaWebConfigurationTest` — 6/6.
- [x] 2.11. Публикация SPA на `/app` (Vaadin пока на `/`), Dockerfile multi-stage:
      web build -> jar.
      Выполнено 2026-11-19: SPA публикуется на `/app`, Vaadin остаётся на `/`;
      multi-stage `Dockerfile` (node:24 `npm ci`/`npm run build` -> JDK 25
      `bootJar -PskipWebBuild` -> JRE 25); `web/dist` попадает в
      `BOOT-INF/classes/static/app`; обновлены `.dockerignore` и README.
      Проверено: `npm run typecheck/lint/build`, `:bot:bootJar`, содержимое jar,
      `SpaWebConfigurationTest` — 6/6 (через JUnit Platform Launcher из-за
      лимита длины командной строки Windows).

**Готово, когда:** web-версия на `/app` полностью повторяет текущий функционал
и ей можно пользоваться с телефона в браузере.

### Этап 3. Android-приложение (8–12 дней)

Разработка ведётся в **Android Studio**. Проект `android/` — отдельный Gradle-проект.

- [ ] 3.1. Каркас `android/` (Kotlin, Compose, Material 3, Hilt, minSdk 26,
      актуальные compile/target SDK).
- [ ] 3.2. Сеть: Retrofit + OkHttp + kotlinx.serialization; генерация
      моделей/интерфейсов из OpenAPI (при проблемах — ручные Retrofit-интерфейсы
      + сгенерированные модели).
- [ ] 3.3. Токены: DataStore/EncryptedSharedPreferences, OkHttp `Authenticator`
      для авто-refresh, logout при неудаче; `BuildConfig` base URL
      (`10.0.2.2` для debug, HTTPS для release).
- [ ] 3.4. Навигация и общий каркас: login -> выбор счёта -> вкладки
      (Платежи/История/Медиа).
- [ ] 3.5. Экраны: план платежей (CRUD, выполнить, разовый платёж, баланс),
      прогноз, расчёт дохода, история, медиа.
- [ ] 3.6. UX: pull-to-refresh, обработка ошибок/401/офлайна, пустые состояния,
      подтверждения удаления, русские форматы.
- [ ] 3.7. Тесты: ViewModel/репозиторий (JUnit + MockK + Turbine), Compose
      UI-тесты на критические сценарии; ручной прогон на эмуляторе.
- [ ] 3.8. Release: signing keystore (не в git), `assembleRelease`,
      internal testing (APK/Play Internal).

**Готово, когда:** Android-приложение проходит тот же чек-лист паритета, что web.

### Этап 4. Вывод Vaadin и упрощение (2–3 дня)

- [ ] 4.1. Парный прогон чек-листа паритета (web + Android).
- [ ] 4.2. Переключить SPA на `/`, Vaadin больше не монтируется.
- [ ] 4.3. Удалить Vaadin: `vaadin`-плагин, `vaadin-spring-boot-starter`,
      `vaadin/`-пакеты, `LoginView`, `MainLayout`, `src/main/frontend`, `bundles`,
      `productionMode`.
- [ ] 4.4. Переписать `SecurityConfiguration` на одну цепочку: статика SPA
      публична, `/api/**` — JWT, login — экран SPA.
- [ ] 4.5. Упростить `bot/build.gradle` (убрать `jar`-таск с `vaadinBuildFrontend`),
      Dockerfile, обновить README и run-config.
- [ ] 4.6. Удалить мусор из корня (`src/main/frontend/generated`, root `package.json`,
      `tsconfig.json`, `types.d.ts`, `target/`), если он не нужен.
- [ ] 4.7. Опционально: переименовать `bot` -> `backend`, `BotApp` -> `BudgetApplication`.
- [ ] 4.8. Финальная регрессия, сборка образа, деплой по текущей схеме.

**Готово, когда:** в зависимостях нет Vaadin, образ собирается быстрее,
оба клиента работают с одного API.

## 7. Порядок выполнения и зависимости

```text
0 Подготовка
   └─> 1 Backend API ─┬─> 2 Web SPA ──┐
                      │               ├─> 4 Вывод Vaadin
                      └─> 3 Android ──┘
```

Web и Android можно вести параллельно после Этапа 1. Этап 4 — только после
паритета обоих клиентов.

Предлагаемая нарезка на PR:

1. `chore: нормализация переводов строк` (Этап 0.1) — изменения подготовлены, коммит вручную.
2. `docs: план миграции и решения` (Этап 0.2) — в этом коммите.
3. `feat(api): каркас API, ошибки, OpenAPI`
4. `feat(api): auth + refresh tokens`
5. `feat(api): accounts, history, payments, forecast`
6. `feat(api): media + reference + валидация`
7. `feat(web): каркас, авторизация, платежи`
8. `feat(web): история, медиа, полировка`
9. `feat(android): каркас, сеть, платежи`
10. `feat(android): история, медиа, релиз`
11. `refactor: удаление Vaadin`

## 8. Риски и подводные камни

1. **Spring Boot 4 / Spring Security 7 / springdoc** — совместимость версий
   проверить в задаче 1.1; если springdoc не подходит — контракт ведём вручную
   (`docs/api/openapi.yaml`).
2. **Сосуществование Vaadin-сессии и JWT** — нужны две цепочки
   `SecurityFilterChain` с корректным порядком; тестировать оба входа.
3. **Даты и время** — `LocalDateTime` в БД без зоны, `User.userZone` не используется.
   Сначала сохраняем текущую семантику, в API отдаём ISO; отдельной задачей можно
   перейти на `Instant`/UTC.
4. **Медиа не сохранялось** — без доработки `MediaService` паритет недостижим.
5. **Батч-сохранение правил** — в UI сохраняются только изменённые; в REST лучше
   per-resource CRUD, web-клиент может сохранять по одному или через bulk-эндпоинт.
6. **Android-релиз** — keystore, base URL, HTTPS и network security config;
   cleartext только в debug.
7. **Переводы строк** — закрыто в Этапе 0.1; при работе в Windows IDE учитывать,
   что `.gitattributes` хранит LF в репозитории.

## 9. Чек-лист паритета

Детальный чек-лист по экранам и действиям с отметками Web/Android —
[PARITY_CHECKLIST.md](PARITY_CHECKLIST.md). Ниже — краткая сводка.

- [ ] Логин/логаут, сессия живёт 30 дней.
- [ ] Список счетов, выбор счёта, текущий баланс.
- [ ] Правила платежей: список, создание, редактирование, удаление, выполнение.
- [ ] Разовый платёж (расход/доход) с валидацией названия/суммы.
- [ ] Прогноз баланса на дату, в т.ч. по несохранённым правилам.
- [ ] Расчёт дохода на дату.
- [ ] История платежей по счёту.
- [ ] Медиа: фильтры, CRUD, статусы и переходы, рейтинг 1–10, описание.
- [ ] Ошибки/пустые состояния, русские даты и рубли.

## 10. Открытые вопросы

См. [DECISIONS.md](DECISIONS.md), раздел «Ожидают подтверждения».