# umsoft-backend

Backend для веб-приложения IT-аккредитации с ИИ-модулем для автоматической классификации и маршрутизации заявок.

## О проекте

Сайт-визитка для прохождения компанией государственной IT-аккредитации (Приказ Минцифры № 511). Пользователь оставляет заявку через форму, ИИ-модуль классифицирует её по отделам и отправляет уведомления.

## Стек

- Java 21
- Spring Boot 3.2
- Spring Data JPA, Hibernate
- PostgreSQL 16
- REST API
- GigaChat API (классификация заявок)
- SMTP (отправка писем)
- Thymeleaf (шаблоны писем)
- Docker, Nginx
- Maven

## Что реализовано

- REST API: POST /api/requests, GET /api/requests/{id}
- Классификация заявок через GigaChat API с резервным механизмом по ключевым словам
- Отправка подтверждений клиенту и детального ТЗ в отдел через SMTP
- База данных из 6 таблиц с внешними ключами: submitters, requests, categories, departments, processing_results, emails
- Docker-инфраструктура: backend + PostgreSQL + frontend
- Nginx как reverse proxy

## Архитектура

Клиент (Nuxt 3 SPA + SSR) → REST API (Spring Boot) → PostgreSQL

Дополнительно: GigaChat API, SMTP.

## Запуск

```bash
docker-compose up --build
```

Приложение доступно на http://localhost:8080.

## Объём
15 модулей

67 файлов

~4690 строк кода
