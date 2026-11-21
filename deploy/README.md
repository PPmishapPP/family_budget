# Деплой без Docker

Целевая схема на сервере `mishapp.ru` (1 vCPU, 1 ГБ RAM):

```text
GitHub Actions / локальная сборка
  └─ app.jar (~30 МБ)
       └─ scp → /opt/family-budget/incoming/app.jar
            └─ family-budget-deploy: релиз + симлинк + systemd + health-check
Nginx (443, TLS) → 127.0.0.1:8080 (Spring Boot: SPA + /api)
PostgreSQL 17 (127.0.0.1:5432)
```

Docker в проде больше не используется. `Dockerfile` остаётся в репозитории как
альтернативный способ локального запуска.

## 1. Первичная настройка сервера

```bash
scp -r deploy root@mishapp.ru:/root/family-budget-deploy
ssh root@mishapp.ru 'bash /root/family-budget-deploy/install-server.sh'
```

Скрипт ставит Java 25 (Temurin JRE), PostgreSQL 17 (PGDG), настраивает swap 2 ГБ,
systemd-юнит, Nginx, UFW и `/usr/local/bin/family-budget-deploy`.

Затем создайте пользователя и БД (пароль придумайте свой):

```bash
sudo -u postgres psql <<'SQL'
CREATE ROLE family_budget LOGIN PASSWORD 'ЗАМЕНИТЕ_ПАРОЛЬ';
CREATE DATABASE family_budget OWNER family_budget;
SQL
```

Заполните `/etc/family-budget/env` (шаблон — `deploy/env.example`):

```bash
install -m 600 /dev/null /etc/family-budget/env
nano /etc/family-budget/env
# DATASOURCE_PASSWORD=...
# JWT_SECRET=$(openssl rand -base64 48)
```

Проверьте, что секрет JWT не остался dev-значением из `application.yml`.

## 2. Перенос данных из Docker

Старый контейнер использует том `docker_db_data` (compose-проект называется
`docker`). **Не выполняйте `docker compose down -v`** — это удалит том.

```bash
# дамп
docker exec family_budget_db pg_dump -U budget_user -Fc family_budget \
  > /root/family_budget_$(date +%F).dump

# восстановление в нативный PostgreSQL
sudo -u postgres pg_restore -d family_budget --no-owner --no-acl --role=family_budget \
  /root/family_budget_2026-11-21.dump

# проверка
sudo -u postgres psql -d family_budget -c '\dt'
sudo -u postgres psql -d family_budget -c \
  'select version, success from flyway_schema_history order by installed_rank;'
```

Проверьте, что количество строк в ключевых таблицах совпадает. После проверки
остановите контейнеры, но **не удаляйте том** ещё 1–2 недели:

```bash
docker compose -f /root/family_budget/docker/docker-compose.yml down   # без -v
```

## 3. Первый деплой

### Вариант A. GitHub Actions

В настройках репозитория добавьте секреты:

| Секрет | Значение |
|---|---|
| `SSH_HOST` | `mishapp.ru` |
| `SSH_USER` | `root` |
| `SSH_PORT` | `22` (можно не задавать) |
| `SSH_PRIVATE_KEY` | приватный ключ, которым вы ходите на сервер |

Затем запустите workflow **Deploy** (`workflow_dispatch`) для нужной ветки.

### Вариант B. Локально

```powershell
# из корня проекта, Windows PowerShell
cd web; npm ci; npm run build; cd ..
./gradlew :bot:bootJar -PskipWebBuild -x test
scp -C bot/build/libs/app.jar root@mishapp.ru:/opt/family-budget/incoming/app.jar
ssh root@mishapp.ru "family-budget-deploy /opt/family-budget/incoming/app.jar"
```

Скрипт `family-budget-deploy`:
1. кладёт jar в `/opt/family-budget/releases/<дата>/`;
2. переключает симлинк `current`;
3. перезапускает `family-budget.service`;
4. ждёт `http://127.0.0.1:8080/actuator/health` = `UP`;
5. при неудаче возвращает предыдущий релиз и перезапускает его;
6. удаляет старые релизы, оставляя 5.

## 4. Проверка и откат

```bash
systemctl status family-budget
journalctl -u family-budget -f
curl -s http://127.0.0.1:8080/actuator/health
ls -1dt /opt/family-budget/releases/*
readlink -f /opt/family-budget/current
```

Ручной откат:

```bash
ln -sfn /opt/family-budget/releases/<предыдущий> /opt/family-budget/current
systemctl restart family-budget
```

## 5. Бэкапы БД

Ежедневный дамп в `/var/backups/family_budget` с ротацией 14 дней:

```bash
install -d -m 700 /var/backups/family_budget
cat >/etc/cron.daily/family-budget-backup <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
dir=/var/backups/family_budget
sudo -u postgres pg_dump -Fc family_budget > "$dir/family_budget_$(date +%F).dump"
find "$dir" -type f -name '*.dump' -mtime +14 -delete
EOF
chmod 755 /etc/cron.daily/family-budget-backup
```

Docker volume — не бэкап. Раз в неделю копируйте дамп с сервера:

```bash
scp root@mishapp.ru:/var/backups/family_budget/family_budget_$(date +%F).dump .
```

## 6. Уборка Docker

После 1–2 недель стабильной работы:

```bash
docker system df
docker system prune -a        # образы и build cache
rm -f /root/family_budget.tar
```

Том `docker_db_data` удаляйте только после того, как убедитесь в бэкапе:

```bash
docker volume rm docker_db_data
```

## 7. Полезное

- Логи приложения — только в `journalctl` (stdout). Ограничение journald:
  `/etc/systemd/journald.conf` → `SystemMaxUse=200M`.
- Nginx: `nginx -t && systemctl reload nginx`.
- Конфиг Nginx лежит в `deploy/nginx-family_budget.conf`, при изменении скопируйте
  его в `/etc/nginx/sites-available/family_budget`.
- Java обновляется через `apt` (репозиторий Adoptium), PostgreSQL — через PGDG.
- `unattended-upgrades` на сервере уже включён.
