#!/usr/bin/env bash
#
# Устанавливается на сервер как /usr/local/bin/family-budget-deploy.
# Принимает путь к новому jar (по умолчанию /opt/family-budget/incoming/app.jar),
# раскладывает релиз, переключает симлинк, перезапускает systemd и проверяет health.
# При неудаче откатывается на предыдущий релиз.
#
set -euo pipefail

APP_NAME=family-budget
APP_ROOT=/opt/family-budget
RELEASES="$APP_ROOT/releases"
CURRENT="$APP_ROOT/current"
INCOMING="${1:-$APP_ROOT/incoming/app.jar}"
HEALTH_URL="http://127.0.0.1:8080/actuator/health"
KEEP_RELEASES=5

log() { printf '[%s] %s\n' "$(date '+%F %T')" "$*"; }
fail() { log "ERROR: $*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || fail "запускать нужно от root (sudo)"
[[ -f "$INCOMING" ]] || fail "jar не найден: $INCOMING"
[[ -s "$INCOMING" ]] || fail "jar пустой: $INCOMING"

mkdir -p "$RELEASES" "$APP_ROOT/incoming"

PREVIOUS=""
if [[ -L "$CURRENT" ]]; then
    PREVIOUS="$(readlink -f "$CURRENT" || true)"
fi

RELEASE="$RELEASES/$(date '+%Y%m%d-%H%M%S')"
mkdir -p "$RELEASE"
install -o family-budget -g family-budget -m 0644 "$INCOMING" "$RELEASE/app.jar"
log "новый релиз: $RELEASE"

switch_to() {
    ln -sfn "$1" "$CURRENT"
    # Не выходим по set -e: при падении старта нужен шанс на откат.
    systemctl restart "$APP_NAME" || true
}

wait_healthy() {
    local attempt
    for attempt in $(seq 1 45); do
        if curl -fsS --max-time 3 "$HEALTH_URL" 2>/dev/null | grep -q '"status":"UP"'; then
            return 0
        fi
        if ! systemctl is-active --quiet "$APP_NAME" && [[ $attempt -gt 3 ]]; then
            return 1
        fi
        sleep 2
    done
    return 1
}

switch_to "$RELEASE"

if wait_healthy; then
    log "приложение поднялось, health = UP"
    rm -f "$INCOMING"
else
    log "health-check не прошёл, откатываюсь"
    journalctl -u "$APP_NAME" -n 60 --no-pager || true
    if [[ -n "$PREVIOUS" && -d "$PREVIOUS" ]]; then
        switch_to "$PREVIOUS" || true
        if wait_healthy; then
            log "откат на $PREVIOUS успешен"
        else
            log "откат не помог — требуется ручное вмешательство"
        fi
    fi
    exit 1
fi

# Чистим старые релизы, оставляя последние KEEP_RELEASES.
mapfile -t OLD < <(ls -1dt "$RELEASES"/*/ 2>/dev/null | tail -n +$((KEEP_RELEASES + 1)) || true)
for dir in "${OLD[@]:-}"; do
    [[ -n "$dir" ]] && rm -rf "$dir"
done

log "готово"
