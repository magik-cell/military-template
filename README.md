# Military Warehouse Management API

Навчальний проект для курсу "Робота з СКБД з використанням мови програмування JAVA" (ВІТІ).

## 📚 Практичні Заняття

### ПЗ 1/2 - Основи Spring Data JPA та REST API

**Тема:** Створення базового REST API для системи обліку складу

- 📄 [Завдання та методичні вказівки](./pz_1_2.md)
- 🎯 **Мета:** Розробка backend API з використанням Spring Boot, Spring Data JPA, створення Entity, Repository, Service, Controller, DTO, обробка винятків
- ⏱️ **Обсяг:** 10 годин (4г групові + 6г практика)

### ПЗ 2/2 - Розширена функціональність

**Тема:** JWT Security, Redis Caching, Event-Driven Architecture

- 📄 [Завдання та методичні вказівки](./pz_2_2.md)
- 🎯 **Мета:** Додавання автентифікації, кешування, подійно-орієнтованої архітектури
- ⏱️ **Обсяг:** 14 годин (4г групові + 8г практика + 2г залік)

## 🎲 Варіанти Завдань

Курсанти отримують один з трьох варіантів:

- **Варіант A:** Управління матеріально-технічними засобами (МТЗ)
- **Варіант B:** Управління автотранспортом
- **Варіант C:** Управління особовим складом

## 🛠️ Технологічний Стек

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Spring Security + JWT
- Redis
- Liquibase
- MapStruct
- Swagger/OpenAPI
- Maven

## 🚀 Швидкий Старт

```bash
# Клонування репозиторію
git clone <repository-url>
cd military-template

# Запуск PostgreSQL (через Docker)
docker run -d \
  --name postgres-military \
  -e POSTGRES_DB=military_db \
  -e POSTGRES_USER=military_user \
  -e POSTGRES_PASSWORD=military_pass \
  -p 5432:5432 \
  postgres:15-alpine

# Компіляція та запуск
mvn clean install
mvn spring-boot:run
```

API буде доступне за адресою: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 📋 Структура Проєкту

```
src/main/java/ua/edu/viti/military/
├── entity/          # JPA Entity класи
├── repository/      # Spring Data JPA репозиторії
├── service/         # Бізнес-логіка
├── controller/      # REST контролери
├── dto/             # Data Transfer Objects
│   ├── request/     # Request DTO
│   └── response/    # Response DTO
├── exception/       # Кастомні винятки та обробники
└── config/          # Конфігурація додатку
```

## ✅ Автоматичне Оцінювання

Проєкт використовує GitHub Actions для автоматичної перевірки:

- ✓ Компіляція коду
- ✓ Code style (Checkstyle)
- ✓ Структура проєкту
- ✓ Аналіз Entity, Repository, Service, Controller
- ✓ Integration тести (Postman/Newman)
- ✓ Swagger документація

Workflow автоматично запускається при створенні Pull Request до `main` гілки.

## 📖 Додаткові Ресурси

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 👨‍🏫 Викладач

Кафедра комп'ютерних наук та інтелектуальних технологій  
Військовий інститут телекомунікацій та інформатизації (ВІТІ)

---

**Примітка:** Цей репозиторій є навчальним шаблоном. Курсанти створюють власні варіанти на основі цього template.
