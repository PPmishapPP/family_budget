# Family Budget

Приложение для управления семейным бюджетом (Spring Boot + Vaadin + PostgreSQL + Flyway).

## Запуск в Docker

### Требования

- Docker Engine (с Docker Compose v2), запущенный daemon (например, Docker Desktop)

### Шаги

1. Создайте файл переменных окружения (при необходимости):

   ```bash
   cp docker/.env.example docker/.env
   ```

   Значения по умолчанию: пользователь БД `usr`, пароль `usr`. Для продакшена обязательно измените пароль!

2. Запустите сборку и старт контейнеров:

   ```bash
   docker compose -f docker/docker-compose.yml up -d --build
   ```

3. Проверьте статус:

   ```bash
   docker compose -f docker/docker-compose.yml ps
   ```

4. Приложение будет доступно по адресу: http://localhost:8080

   База данных PostgreSQL — на порту 5432 внутри сети Docker (наружу не проброшен).

### Управление

```bash
# Просмотр логов приложения
docker compose -f docker/docker-compose.yml logs -f bot

# Остановка контейнеров
docker compose -f docker/docker-compose.yml down

# Остановка с удалением тома БД (все данные будут потеряны!)
docker compose -f docker/docker-compose.yml down -v

# Пересборка после изменений кода
docker compose -f docker/docker-compose.yml up -d --build
```

### Как это устроено

- [`Dockerfile`](Dockerfile) — многоступенчатая сборка:
  - **Stage 1 (build)**: JDK 26 + Gradle, сборка `bootJar` с Vaadin в production mode (`-Pvaadin.productionMode=true`), тесты пропущены (`-x test`);
  - **Stage 2 (runtime)**: JRE 26, запуск под непривилегированным пользователем.
- [`docker/docker-compose.yml`](docker/docker-compose.yml) — сервисы:
  - `db` — PostgreSQL 16 (данные в volume `db_data`);
  - `bot` — само приложение, зависит от готовности БД (healthcheck).

### Конфигурация

| Переменная окружения | Описание | По умолчанию |
|---|---|---|
| `DATASOURCE_URL` | JDBC URL базы данных | `jdbc:postgresql://localhost:5432/family_budget` (для локального запуска) |
| `DATASOURCE_USERNAME` | Пользователь БД | `usr` |
| `DATASOURCE_PASSWORD` | Пароль БД | `usr` |

В Docker Compose `DATASOURCE_URL` автоматически указывает на контейнер `db`.

### Локальный запуск без Docker

```bash
./gradlew :bot:bootRun
```

Для локального запуска задайте переменные окружения `DATASOURCE_USERNAME` и `DATASOURCE_PASSWORD` (см. [`application.yml`](bot/src/main/resources/application.yml)).
