# =========================================================
# Stage 1: сборка приложения (Vaadin production mode)
# =========================================================
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

# Копируем Gradle wrapper и файлы сборки
COPY gradlew .
COPY gradlew.bat .
COPY gradle/ gradle/
COPY settings.gradle build.gradle gradle.properties ./

# Копируем исходники модуля bot
COPY bot/ bot/

# Собираем исполняемый bootJar.
# -Pvaadin.productionMode=true — сборка фронтенда Vaadin в production mode
# -x test — пропускаем тесты при сборке образа
# sed: нормализуем переводы строк gradlew (Windows CRLF ломает shebang в Linux)
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew :bot:bootJar -Pvaadin.productionMode=true --no-daemon -x test

# =========================================================
# Stage 2: рантайм (только JRE)
# =========================================================
FROM eclipse-temurin:26-jre

WORKDIR /app

# Непривилегированный пользователь для запуска приложения
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /app/bot/build/libs/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
