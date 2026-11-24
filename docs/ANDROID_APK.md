# Android APK: сборка, подпись, раздача

Обновлено: 2026-11-24
Связанные документы: [README.md](../README.md), [deploy/README.md](../deploy/README.md), [MIGRATION_PLAN.md](MIGRATION_PLAN.md) (п. 3.8)

Ссылка на APK лежит в шапке web-приложения (только для авторизованных), файл
раздаётся сайтом по адресу `/download/family-budget.apk`, а сам APK собирается
**вместе с backend-сборкой** (`bootRun`/`bootJar`) и упаковывается в `app.jar`.

---

## 1. Что нужно для локальной сборки

| Требование | Зачем |
|---|---|
| JDK 25 | backend (`bootJar`/`bootRun`) |
| Node 24+ | SPA (можно пропустить `-PskipWebBuild`) |
| Android SDK | `android/local.properties` или `ANDROID_HOME`/`ANDROID_SDK_ROOT` |
| JDK 21 | сборка Android (AGP не поддерживает JDK 25) |

JDK 21 ищется так (по порядку): `ANDROID_JAVA_HOME` → текущий `JAVA_HOME`, если
это JDK 21 → каталог `~/.jdks/*21*`. Если JDK 21 или Android SDK не найдены,
шаг APK **пропускается с предупреждением** (сборка backend при этом не падает).

## 2. Команды сборки

### Release (как на проде), подписанный

Задайте переменные окружения подписи (см. §3) и запустите обычную сборку:

```bash
./gradlew :bot:bootJar -PskipWebBuild -x test
```

Windows PowerShell (тот же вызов):

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.1"
.\gradlew.bat :bot:bootJar -PskipWebBuild -x test --console=plain
```

Результат: `bot/build/libs/app.jar`, внутри — `static/download/family-budget.apk`.

### Debug (локальная проверка UI без keystore)

```bash
./gradlew :bot:bootJar -PskipWebBuild -x test -PapkBuildType=debug
```

Debug-сборка использует `applicationId` с суффиксом `.debug`
(`ru.mishapp.budget.debug`) и адрес API `http://10.0.2.2:8080/` (эмулятор).
Debug APK на сайт **не** раздаётся под именем release — его нужно смотреть как
`android/app/build/outputs/apk/debug/app-debug.apk`.

### Быстрые переключатели

| Флаг | Действие |
|---|---|
| `-PskipApkBuild` | не собирать APK (используется в Dockerfile и CI) |
| `-PapkBuildType=debug` | собрать debug-вариант |
| `-PapiBaseUrl=https://host/` | переопределить адрес API (слэш в конце обязателен) |
| `-PskipWebBuild` | пропустить сборку SPA (если нет Node) |

`-PapiBaseUrl` прокидывается во вложенную Android-сборку и подставляется в
`BuildConfig.API_BASE_URL`. По умолчанию: release → `https://mishapp.ru/`,
debug → `http://10.0.2.2:8080/`.

## 3. Подпись release

Keystore **не хранится в git**. Подпись включается, только если заданы все
четыре переменные окружения:

| Переменная | Значение |
|---|---|
| `ANDROID_KEYSTORE_PATH` | путь к `.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | пароль хранилища |
| `ANDROID_KEY_ALIAS` | alias ключа |
| `ANDROID_KEY_PASSWORD` | пароль ключа |

Без них `assembleRelease` создаёт `app-release-unsigned.apk`, и он **не**
попадает на сайт (staging-каталог очищается — старый APK тоже не раздаётся).

### Создать keystore (один раз)

```bash
keytool -genkeypair -v -keystore family-budget-release.jks -alias family-budget \
  -keyalg RSA -keysize 2048 -validity 10000
```

Сохраните файл и пароли в надёжном месте. **Потеря keystore = невозможность
обновлять приложение** в Google Play и на устройстве без переустановки.

### Локальный запуск подписанной сборки (PowerShell)

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.1"
$env:ANDROID_KEYSTORE_PATH     = "C:\keys\family-budget-release.jks"
$env:ANDROID_KEYSTORE_PASSWORD = "<пароль хранилища>"
$env:ANDROID_KEY_ALIAS         = "family-budget"
$env:ANDROID_KEY_PASSWORD      = "<пароль ключа>"
.\gradlew.bat :bot:bootJar -PskipWebBuild -x test --console=plain
```

## 4. Проверка собранного APK

```powershell
# 1) APK есть в jar
& "$env:JAVA_HOME\bin\jar.exe" tf bot/build/libs/app.jar | Select-String "download/"

# 2) APK из jar совпадает с выводом Android-сборки
$staged = "bot\build\generated\apk\static\download\family-budget.apk"
(Get-FileHash $staged -Algorithm SHA256).Hash
(Get-FileHash "android\app\build\outputs\apk\release\app-release.apk" -Algorithm SHA256).Hash

# 3) Подпись (build-tools нужной версии)
C:\Android\Sdk\build-tools\37.0.0\apksigner.bat verify --print-certs $staged
```

`apksigner` должен показать `V2 Signer: certificate DN: CN=...`.

Проверка адреса API, зашитого в APK:

```bash
# из корня android/
./gradlew :app:assembleRelease
# затем посмотреть сгенерированный BuildConfig:
# app/build/generated/source/buildConfig/release/ru/mishapp/budget/BuildConfig.java
```

## 5. Раздача в проде

Серверу **не нужны** Android SDK и JDK 21: APK уже лежит внутри `app.jar` и
отдаётся Spring Boot как статика по `/download/family-budget.apk`. Проверка:

```bash
curl -sI https://mishapp.ru/download/family-budget.apk | head -1
# ожидаемо: HTTP/2 200
```

## 6. GitHub Actions: секреты

В workflow **CI** отдельная job `android` собирает подписанный release APK и
вкладывает его в `app.jar`. Если секретов нет — CI собирает debug-вариант.

Workflow **Deploy** требует все четыре секрета: без них деплой падает, чтобы на
сайте не оказалось битой ссылки.

| Секрет | Значение |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64 от файла `.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | пароль хранилища |
| `ANDROID_KEY_ALIAS` | alias ключа |
| `ANDROID_KEY_PASSWORD` | пароль ключа |

Получить base64 (Linux/macOS):

```bash
base64 -w0 family-budget-release.jks
```

Windows PowerShell (копирует в буфер обмена):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('family-budget-release.jks')) | Set-Clipboard
```

## 7. Частые проблемы

| Симптом | Причина и решение |
|---|---|
| `APK: Android SDK не найден` | нет `android/local.properties` и `ANDROID_HOME` — задайте SDK |
| `APK: JDK 21 не найден` | установите JDK 21 и задайте `ANDROID_JAVA_HOME` |
| APK не появляется на сайте | сборка release без подписи: задайте `ANDROID_KEYSTORE_*` |
| `ClassNotFoundException` в тестовом воркере | кириллический `%USERPROFILE%` + OEM-кодировка консоли; тест проверяйте через CI или прямой запуск JUnit в обход Gradle |
| Docker-сборка падает на APK | в `Dockerfile` стоит `-PskipApkBuild`; так и должно быть |
| Хочу собрать только backend, без APK | `-PskipApkBuild` |
