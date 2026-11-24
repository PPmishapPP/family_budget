#!/usr/bin/env bash
#
# Первичная настройка Ubuntu-сервера для Family Budget без Docker.
# Запускать от root из каталога deploy/ (рядом лежат service, nginx и deploy.sh):
#
#   scp -r deploy root@mishapp.ru:/root/family_budget-deploy
#   ssh root@mishapp.ru 'bash /root/family_budget-deploy/install-server.sh'
#
# Скрипт идемпотентен: повторный запуск не ломает уже настроенное.
# База данных и пользователь БД создаются отдельно (см. deploy/README.md).
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT=/opt/family-budget
APP_USER=family-budget
ENV_DIR=/etc/family-budget
PG_VERSION=17

log() { printf '\n==> %s\n' "$*"; }
[[ $EUID -eq 0 ]] || { echo "нужен root" >&2; exit 1; }

log "swap 2G (если swap ещё нет)"
if ! swapon --show | grep -q .; then
    if ! fallocate -l 2G /swapfile 2>/dev/null; then
        dd if=/dev/zero of=/swapfile bs=1M count=2048 status=none
    fi
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
echo 'vm.swappiness=10' > /etc/sysctl.d/99-family-budget.conf
sysctl --system >/dev/null

log "базовые пакеты"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y curl gnupg ca-certificates lsb-release ufw nginx

log "Java 25 (Temurin JRE)"
if ! java -version 2>&1 | grep -q 'version "25'; then
    install -d /etc/apt/keyrings
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
        | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo "$VERSION_CODENAME") main" \
        > /etc/apt/sources.list.d/adoptium.list
    apt-get update
    apt-get install -y temurin-25-jre
fi
JAVA_HOME_DIR="$(find /usr/lib/jvm -maxdepth 1 -type d -name 'temurin-25*' | head -n1)"
if [[ -n "$JAVA_HOME_DIR" ]]; then
    update-alternatives --install /usr/bin/java java "$JAVA_HOME_DIR/bin/java" 2500
    update-alternatives --set java "$JAVA_HOME_DIR/bin/java"
fi
JAVA_BIN="$(readlink -f /usr/bin/java 2>/dev/null || true)"
[[ -n "$JAVA_BIN" ]] || { echo "Java 25 не найден в /usr/bin/java" >&2; exit 1; }
log "java: $JAVA_BIN"

log "PostgreSQL $PG_VERSION (PGDG)"
if ! dpkg -s "postgresql-$PG_VERSION" >/dev/null 2>&1; then
    install -d /usr/share/postgresql-common/pgdg
    curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
        -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc
    echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
        > /etc/apt/sources.list.d/pgdg.list
    apt-get update
    apt-get install -y "postgresql-$PG_VERSION"
fi

log "пользователь и каталоги приложения"
id -u "$APP_USER" >/dev/null 2>&1 \
    || useradd --system --home "$APP_ROOT" --shell /usr/sbin/nologin "$APP_USER"
install -d -o "$APP_USER" -g "$APP_USER" "$APP_ROOT/releases" "$APP_ROOT/incoming"

log "файл окружения"
install -d -m 700 "$ENV_DIR"
if [[ ! -f "$ENV_DIR/env" ]]; then
    install -m 600 "$SCRIPT_DIR/env.example" "$ENV_DIR/env"
    echo "!!! Заполните $ENV_DIR/env (пароль БД и JWT_SECRET) перед первым запуском"
fi

log "systemd unit"
install -m 644 "$SCRIPT_DIR/family-budget.service" /etc/systemd/system/family-budget.service
systemctl daemon-reload
systemctl enable family-budget

log "nginx"
install -m 644 "$SCRIPT_DIR/nginx-family_budget.conf" /etc/nginx/sites-available/family_budget
ln -sfn /etc/nginx/sites-available/family_budget /etc/nginx/sites-enabled/family_budget
nginx -t
systemctl reload nginx

log "deploy-скрипт"
install -m 755 "$SCRIPT_DIR/deploy.sh" /usr/local/bin/family-budget-deploy

log "настройка PostgreSQL под 1 ГБ RAM"
cat > "/etc/postgresql/$PG_VERSION/main/conf.d/family-budget.conf" <<'EOF'
listen_addresses = 'localhost'
max_connections = 20
shared_buffers = 128MB
effective_cache_size = 384MB
work_mem = 4MB
maintenance_work_mem = 64MB
wal_compression = on
EOF
systemctl restart postgresql

log "бэкапы БД (ежедневно, хранить 14 дней)"
install -d -m 700 /var/backups/family_budget
cat > /etc/cron.daily/family-budget-backup <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
dir=/var/backups/family_budget
/usr/bin/sudo -u postgres /usr/bin/pg_dump -Fc family_budget > "$dir/family_budget_$(date +%F).dump"
/usr/bin/find "$dir" -type f -name '*.dump' -mtime +14 -delete
EOF
chmod 755 /etc/cron.daily/family-budget-backup

log "ограничение журнала"
install -d /etc/systemd/journald.conf.d
printf '[Journal]\nSystemMaxUse=200M\n' > /etc/systemd/journald.conf.d/99-family-budget.conf
systemctl restart systemd-journald

log "firewall"
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
# ufw в non-interactive SSH иногда возвращает 1, хотя правила применяются.
ufw --force enable || true
ufw status | grep -q 'Status: active'

log "готово. Дальше: создать БД и пользователя, заполнить $ENV_DIR/env, задеплоить jar."
