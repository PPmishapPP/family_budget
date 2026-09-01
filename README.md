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

## Деплой на слабый сервер

Сервер, на котором живёт приложение, слабый и не справляется со сборкой образа
(Vaadin production mode — тяжёлая сборка). Поэтому образ собирается локально,
сохраняется в файл, переносится по SSH и загружается на сервере.

Схема:

```mermaid
flowchart LR
    A[Локальная машина<br/>собираем образ] --> B[docker save<br/>образ -> tar файл]
    B --> C[scp<br/>tar на сервер]
    C --> D[На сервере<br/>docker load]
    D --> E[docker compose up -d<br/>без --build]
```

### Шаги

1. **Сборка образа локально** (из корня проекта):

   ```bash
   docker build -t family_budget:latest .
   ```

2. **Сохранение образа в файл:**

   ```bash
   docker save family_budget:latest -o family_budget.tar
   ```

3. **Копирование на сервер по SSH:**

   ```bash
   scp -C family_budget.tar user@server:/home/user/family_budget/
   ```

   Флаг `-C` включает сжатие при передаче. При первом деплое так же переносится
   конфигурация compose:

   ```bash
   scp -r docker user@server:/home/user/family_budget/
   ```

4. **На сервере — загрузка образа и запуск:**

   ```bash
   ssh user@server
   cd /home/user/family_budget
   ```

   При первом деплое создать `.env` из примера (обязательно сменить пароль БД):

   ```bash
   cp docker/.env.example docker/.env
   nano docker/.env   # измените DATASOURCE_PASSWORD
   ```

   Загрузить образ и запустить контейнеры (важно: **без** `--build`, чтобы не
   собирать на слабом сервере):

   ```bash
   docker load -i family_budget.tar
   docker images | grep family_budget      # проверка, что образ на месте
   docker compose -f docker/docker-compose.yml up -d --force-recreate
   ```

   `--force-recreate` страхует от случая, когда compose не замечает, что образ
   с тегом `latest` заменился. Том `db_data` (PostgreSQL) при этом не
   пересоздаётся — данные и миграции Flyway сохраняются.

   Проверка:

   ```bash
   docker compose -f docker/docker-compose.yml ps
   docker compose -f docker/docker-compose.yml logs -f bot
   ```

### Кеширование зависимостей при сборке

[`Dockerfile`](Dockerfile:1) использует BuildKit cache mounts
(`--mount=type=cache`): кеш Gradle и npm сохраняется на хосте между сборками
и не скачивается заново при каждом `docker build`. Правка Java-кода или
фронтенда не приводит к повторной загрузке зависимостей.

Слои сборки выстроены так, чтобы зависимости загружались до копирования
исходников (`COPY bot/ bot/`), поэтому изменения в коде не инвалидируют слой
с зависимостями.

Требуется Docker с BuildKit (в Docker Desktop включён по умолчанию).

Для принудительной очистки кеша (например, при подозрении на битый кеш):

```bash
docker builder prune   # или docker system prune
```

### Обновление приложения

Повторяются шаги 1 → 3 → 4:

```bash
docker build -t family_budget:latest .
docker save family_budget:latest -o family_budget.tar
scp -C family_budget.tar user@server:/home/user/family_budget/
# на сервере:
docker load -i family_budget.tar
docker compose -f docker/docker-compose.yml up -d --force-recreate
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
