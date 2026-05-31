# OTP Service - One-Time Password Service

Микросервис для генерации и управления одноразовыми паролями (OTP) с поддержкой SMS и Email уведомлений.

**Технологии:** Java 11+, Custom HTTP Server, PostgreSQL, JWT Authentication

---

## Содержание

- [Описание](#описание)
- [Функциональность](#функциональность)
- [Архитектура проекта](#архитектура-проекта)
- [Структура кода](#структура-кода)
- [Установка и запуск](#установка-и-запуск)
- [База данных](#база-данных)
- [API Endpoints](#api-endpoints)
- [Конфигурация](#конфигурация)
- [Безопасность](#безопасность)
- [Тестирование](#тестирование)
- [Лицензия](#лицензия)

---

## Описание

OTP Service — это легковесный микросервис на чистом Java, предназначенный для:

- Генерации одноразовых паролей (OTP)
- Отправки уведомлений через SMS и Email
- Управления пользователями с ролевой моделью
- Автоматической очистки истекших OTP кодов

Сервер запускается на порту **8080** и использует PostgreSQL для хранения данных.

---

## Функциональность

| Функция | Класс/Компонент | Описание |
|---------|-----------------|----------|
| Генерация OTP | `OtpGenerator`, `OtpService` | Создание одноразовых паролей |
| Авторизация | `AuthController`, `JwtUtil` | JWT токены для аутентификации |
| Управление пользователями | `UserController`, `UserService` | CRUD операции |
| SMS уведомления | `SmsNotificationService` | Отправка SMS сообщений |
| Email уведомления | `EmailNotificationService` | Отправка Email |
| Очистка истекших OTP | `ExpiredOtpCleanupService` | Фоновый процесс очистки |
| Логирование | `LoggingFilter` | Логи всех запросов |
| Аутентификация | `AuthFilter` | Проверка JWT токенов |

---

## Архитектура проекта

```
otpservice/
├── src/main/java/otpservice/
│   ├── Main.java              # Точка входа, настройка HTTP сервера
│   ├── controller/            # REST контроллеры
│   │   ├── AuthController.java    # Авторизация
│   │   ├── UserController.java    # Управление пользователями
│   │   ├── AdminController.java   # Административные функции
│   │   └── BaseController.java    # Базовый контроллер
│   ├── service/               # Бизнес логика
│   │   ├── AuthService.java
│   │   ├── OtpService.java
│   │   ├── UserService.java
│   │   ├── NotificationService.java
│   │   ├── SmsNotificationService.java
│   │   ├── EmailNotificationService.java
│   │   └── ExpiredOtpCleanupService.java
│   ├── model/                 # Entity классы
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── OtpCode.java
│   │   ├── OtpStatus.java
│   │   └── OtpConfig.java
│   ├── dao/                   # Data Access Objects
│   │   ├── UserDao.java
│   │   ├── OtpCodeDao.java
│   │   ├── OtpConfigDao.java
│   │   └── DatabaseConnection.java
│   ├── util/                  # Утилиты
│   │   ├── JwtUtil.java
│   │   ├── PasswordUtil.java
│   │   ├── OtpGenerator.java
│   │   └── JsonUtil.java
│   └── middleware/            # Фильтры
│       ├── LoggingFilter.java
│       └── AuthFilter.java
└── src/main/resources/        # Ресурсы
    └── logback.xml            # Конфигурация логирования
```

---

## Структура кода

### Контроллеры (Controller Layer)
- Обрабатывают HTTP запросы и возвращают JSON ответы
- Применяют фильтры для логирования и аутентификации

### Сервисы (Service Layer)
- Содержат бизнес логику
- Используют DAO для работы с базой данных

### DAO (Data Access Layer)
- Работают напрямую с PostgreSQL через JDBC
- Выполняют CRUD операции

### Утилиты (Utility Layer)
- Генерация OTP кодов
- Хеширование паролей
- Работа с JWT токенами
- Парсинг JSON

### Middleware (Фильтры)
- `LoggingFilter` — логирование всех запросов
- `AuthFilter` — проверка JWT токенов для защищенных endpoints

---

## Установка и запуск

### Требования

- Java 11 или выше
- PostgreSQL 12+
- Maven (для сборки, если есть pom.xml)

### Установка

#### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd otpservice
```

#### 2. Сборка проекта
```bash
# Если есть pom.xml
mvn clean package

# Или компиляция вручную
javac -d target src/main/java/otpservice/*.java
```

#### 3. Запуск сервера
```bash
java -cp target/classes otpservice.Main
```

Сервер запустится на порту **8080**.

### Проверка запуска
```bash
curl http://localhost:8080/api/auth/health
```

### Остановка сервера
Нажмите `Ctrl+C` в терминале или используйте shutdown hook.

---

## База данных

Сервер автоматически создает базу данных и таблицы при первом запуске.

### Таблица users
| Колонка | Тип | Описание |
|---------|-----|----------|
| id | BIGSERIAL | Первичный ключ |
| username | VARCHAR(255) | Уникальное имя пользователя |
| password_hash | VARCHAR(255) | Хешированный пароль |
| role | VARCHAR(50) | Роль пользователя |

### Таблица otp_config
| Колонка | Тип | Описание |
|---------|-----|----------|
| id | INT | Первичный ключ (фиксировано = 1) |
| code_length | INT | Длина OTP кода |
| ttl_seconds | INT | Время жизни OTP в секундах |

### Таблица otp_codes
| Колонка | Тип | Описание |
|---------|-----|----------|
| id | BIGSERIAL | Первичный ключ |
| user_id | BIGINT | Ссылка на пользователя |
| operation_id | VARCHAR(255) | ID операции |
| code | VARCHAR(255) | OTP код |
| status | VARCHAR(50) | Статус (pending/used/expired) |
| created_at | TIMESTAMP | Время создания |
| expires_at | TIMESTAMP | Время истечения |

### Автоматическая инициализация

При запуске сервер автоматически:
1. Создает базу данных `otp_db`, если её нет
2. Создает таблицы с `IF NOT EXISTS`
3. Инициализирует конфигурацию OTP

```java
// В Main.java
private static void initDatabase() {
    // Создание БД, если её нет
    // Создание таблиц с IF NOT EXISTS
}
```

---

## API Endpoints

Сервер предоставляет REST API на порту 8080.

### Публичные endpoints (без аутентификации)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/auth/register` | Регистрация нового пользователя |
| POST | `/api/auth/login` | Авторизация и получение JWT токена |
| POST | `/api/otp/generate` | Генерация OTP кода |

### Защищенные endpoints (требуют JWT токен)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/user` | Получить список пользователей |
| GET | `/api/user/{id}` | Получить пользователя по ID |
| POST | `/api/user` | Создать нового пользователя |
| POST | `/api/otp/generate` | Генерация OTP кода |
| POST | `/api/user/otp/generate/mail={email}` | Генерация и отправка кода по email |
| POST | `api/user/otp/generate/sms={sms}` | Генерация и отправка кода по sms |
| PUT | `/api/user/{id}` | Обновить пользователя |
| DELETE | `/api/user/{id}` | Удалить пользователя |

### Административные endpoints (требуют админ роль)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/admin/users` | Получить всех пользователей |
| POST | `/api/admin/users/bulk` | Массовая регистрация |
| DELETE | `/api/admin/users/{id}` | Удалить пользователя |

### Пример запроса

#### Регистрация
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "secret123"}'
```

#### Авторизация
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "secret123"}'
```

#### Генерация OTP (после авторизации)
```bash
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"operation_id": "login"}'
```

---

## Конфигурация

### База данных (в Main.java)

```java
String url = "jdbc:postgresql://localhost:5432/";
String dbName = "otp_db";
String user = "postgres";
String password = "postgres";
```

### Настройка логирования

Файл `src/main/resources/logback.xml` содержит конфигурацию логирования.

### Фоновая очистка OTP

```java
ExpiredOtpCleanupService cleanupService = new ExpiredOtpCleanupService();
cleanupService.start(); // Запускается как фоновый процесс
```

### Настройка сервера

```java
HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
// PORT = 8080 по умолчанию
```

---

## Безопасность

### JWT Аутентификация
- Все защищенные endpoints требуют валидный JWT токен
- Токен передается в заголовке `Authorization: Bearer <token>`

### Хеширование паролей
```java
// PasswordUtil.java
String hash = PasswordUtil.hashPassword("plain_password");
```

### Очистка истекших OTP
- Фоновый процесс автоматически удаляет просроченные коды
- Защита от накопления неактивных записей в БД
