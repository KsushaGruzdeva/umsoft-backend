# Этап 1: Сборка
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем сертификат из хоста в контейнер
COPY russian_trusted_root_ca.cer /tmp/russian_trusted_root_ca.cer

# Импортируем сертификат в Java truststore
RUN keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -alias rus_root_ca -file /tmp/russian_trusted_root_ca.cer && \
    rm /tmp/russian_trusted_root_ca.cer

# Копируем JAR из этапа сборки
COPY --from=builder /app/target/*.jar app.jar

# Создаем пользователя без прав
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]