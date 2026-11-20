# syntax=docker/dockerfile:1
# ^ директива обязательна для --mount=type=cache (BuildKit).
# В Docker Desktop BuildKit включён по умолчанию.

# =========================================================
# Stage 0: сборка SPA (React) в web/
# =========================================================
FROM node:24-alpine AS web

WORKDIR /web

COPY web/package.json web/package-lock.json ./

RUN --mount=type=cache,target=/root/.npm npm ci

COPY web/ ./

RUN npm run build

# =========================================================
# Stage 1: сборка приложения (Vaadin production mode)
# =========================================================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Копируем Gradle wrapper и файлы сборки
COPY gradlew .
COPY gradlew.bat .
COPY gradle/ gradle/
COPY settings.gradle build.gradle gradle.properties ./

# build.gradle модуля bot копируем отдельно от исходников:
# правка кода не должна инвалидировать слой с зависимостями
COPY bot/build.gradle bot/

# Нормализуем gradlew (Windows CRLF ломает shebang в Linux) ДО первого вызова
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew

# Предзагрузка Gradle-зависимостей в отдельном слое.
# --mount=type=cache сохраняет кеш между сборками образа,
# поэтому повторные сборки не качают зависимости заново.
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew :bot:dependencies --no-daemon > /dev/null 2>&1 || true

# Копируем исходники модуля bot и собранный SPA (упаковывается в jar)
COPY bot/ bot/
COPY --from=web /web/dist web/dist

# Собираем исполняемый bootJar.
# -Pvaadin.productionMode=true — сборка фронтенда Vaadin в production mode
# -PskipWebBuild — SPA уже собран на стадии web, npm в этом образе нет
# -x test — пропускаем тесты при сборке образа
# --mount=type=cache — кеш Gradle и npm сохраняется между сборками на хосте
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    --mount=type=cache,target=/root/.npm \
    ./gradlew :bot:bootJar -Pvaadin.productionMode=true -PskipWebBuild --no-daemon -x test

# =========================================================
# Stage 2: рантайм (только JRE)
# =========================================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Непривилегированный пользователь для запуска приложения
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /app/bot/build/libs/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
