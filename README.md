# Family Budget

Приложение для управления семейным бюджетом: Spring Boot REST API + React SPA,
PostgreSQL, Flyway, JWT-аутентификация. Android-клиент использует тот же API.

## Стек

| Слой | Технологии |
|---|---|
| Backend | Java 25, Spring Boot 4.1.1, Spring Data JDBC, Spring Security, Flyway, springdoc |
| Web | React 19, TypeScript, Vite, Mantine, TanStack Query, React Hook Form + Zod |
| БД | PostgreSQL 17 |
| Деплой | systemd + Nginx + нативный PostgreSQL (без Docker) |

## Структура

```text
bot/       # backend: домен, REST API /api/v1, раздача SPA
web/       # React SPA (Vite)
android/   # Android-клиент (отдельный Gradle-проект)
docs/      # планы, решения, OpenAPI
deploy/    # systemd, Nginx, скрипты установки и деплоя
```

SPA собирается в `web/dist` и упаковывается в `bootJar` (`static/`), поэтому
отдельный фронтенд-сервер не нужен. Nginx терминирует TLS и проксирует запросы
на `127.0.0.1:8080`.

## Локальный запуск

Требования: JDK 25, Node 24+, PostgreSQL 17 (или Docker только для БД).
Для сборки APK дополнительно нужны Android SDK и JDK 21 (см. ниже).

```bash
# БД (если нет локального PostgreSQL)
docker run -d --name family_budget_db \
  -e POSTGRES_DB=family_budget \
  -e POSTGRES_USER=usr \
  -e POSTGRES_PASSWORD=usr \
  -p 5432:5432 postgres:17

# переменные окружения
export DATASOURCE_URL=jdbc:postgresql://localhost:5432/family_budget
export DATASOURCE_USERNAME=usr
export DATASOURCE_PASSWORD=usr

# backend + собранная SPA на http://localhost:8080/
./gradlew :bot:bootRun
```

`bootRun`/`bootJar` сами собирают SPA через `npm run build`, если npm найден в
`PATH`. Пропустить сборку SPA: `-PskipWebBuild` (используется готовый `web/dist`).

Разработка SPA с hot-reload:

```bash
cd web
npm ci
npm run dev        # http://localhost:5173, /api проксируется на :8080
```

Полезные команды:

```bash
./gradlew :bot:test                 # backend-тесты (нужен Docker для Testcontainers)
cd web && npm test                  # Vitest
cd web && npm run lint && npm run typecheck
cd web && npm run test:e2e          # Playwright (нужны E2E_LOGIN/E2E_PASSWORD)
```

Документация API: `http://localhost:8080/swagger-ui.html`, контракт —
[`docs/openapi.json`](docs/openapi.json).

## Сборка артефакта

```bash
cd web
npm ci
npm run build
cd ..
./gradlew :bot:bootJar -PskipWebBuild -x test
# результат: bot/build/libs/app.jar
```

CI (GitHub Actions) собирает SPA, запускает тесты и публикует `app.jar` как
artifact: [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Android APK на сайте

`bootRun`/`bootJar` также собирают APK и кладут его в `app.jar` — он раздаётся
по адресу `/download/family-budget.apk`, а ссылка на скачивание находится в
шапке web-приложения.

Для сборки APK нужны Android SDK и JDK 21 (AGP не поддерживает JDK 25):
`ANDROID_JAVA_HOME` или каталог `~/.jdks/*21*`. Если их нет или задан
`-PskipApkBuild`, шаг пропускается с предупреждением, а в jar попадает ранее
собранный APK (если он есть). Локальная проверка без keystore:
`-PapkBuildType=debug`.

Release по умолчанию использует `https://mishapp.ru/`; адрес можно
переопределить: `-PapiBaseUrl=https://host/` (обязателен завершающий слэш).

Подпись release задаётся переменными окружения (keystore в git не хранится):

| Переменная | Значение |
|---|---|
| `ANDROID_KEYSTORE_PATH` | путь к `.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | пароль хранилища |
| `ANDROID_KEY_ALIAS` | alias ключа |
| `ANDROID_KEY_PASSWORD` | пароль ключа |

Без этих переменных `assembleRelease` создаёт неподписанный APK, и он **не**
попадает на сайт. Сгенерировать keystore один раз:

```bash
keytool -genkeypair -v -keystore family-budget-release.jks -alias family-budget \
  -keyalg RSA -keysize 2048 -validity 10000
```

В GitHub Actions те же значения хранятся в секретах: `ANDROID_KEYSTORE_BASE64`
(base64 от файла `.jks`), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
`ANDROID_KEY_PASSWORD`. Получить base64 в Windows PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('family-budget-release.jks')) | Set-Clipboard
```

## Деплой

Прод — `mishapp.ru`: Nginx (TLS) → Spring Boot под systemd → нативный
PostgreSQL. Docker на сервере не используется.

Полный runbook: [`deploy/README.md`](deploy/README.md). Кратко:

```bash
scp -C bot/build/libs/app.jar root@mishapp.ru:/opt/family-budget/incoming/app.jar
ssh root@mishapp.ru "family-budget-deploy /opt/family-budget/incoming/app.jar"
```

Скрипт деплоя хранит релизы, переключает симлинк, проверяет
`/actuator/health` и откатывается при ошибке. Автодеплой — workflow
[`.github/workflows/deploy.yml`](.github/workflows/deploy.yml).

### Конфигурация

| Переменная | Описание | По умолчанию |
|---|---|---|
| `DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/family_budget_v3` |
| `DATASOURCE_USERNAME` | Пользователь БД | — |
| `DATASOURCE_PASSWORD` | Пароль БД | — |
| `JWT_SECRET` | Секрет подписи JWT, **обязателен в проде** | dev-значение |
| `CORS_ALLOWED_ORIGINS` | Разрешённые origin для `/api/**` | `http://localhost:5173` |

`server.forward-headers-strategy=framework` включён, чтобы за Nginx корректно
работали `X-Forwarded-Proto` и флаг `Secure` у refresh-cookie.

## Docker (опционально)

`Dockerfile` и `docker/docker-compose.yml` оставлены для локального запуска или
альтернативного хостинга:

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

Для прода этот путь больше не используется — сборка на слабом сервере не
запускается, а нативный запуск экономит ~135 МБ RAM.
