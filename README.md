# Creazione - Игровой Telegram бот

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-24.0-blue?style=for-the-badge&logo=docker)

[![Telegram Bot](https://img.shields.io/badge/Telegram-Bot-blue?style=flat-square&logo=telegram)](https://t.me/YourBotName)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

</div>

## 📖 Описание

Creazione - это серверная часть игрового Telegram-бота с собственной экономической моделью.
Игроки могут строить и улучшать здания, накапливать ресурсы, приглашать друзей через реферальную систему и получать ежедневные награды.

## ✨ Возможности

- 🏗️ Система строительства и улучшения зданий с нелинейной прогрессией
- 💰 Экономическая модель с производством ресурсов
- 👥 Реферальная программа с вознаграждениями
- 🎁 Ежедневные бонусы и лутбоксы
- ⚡ Кэширование данных с использованием Redis
- 🔧 Автоматические задачи с Spring Scheduling
- 🐳 Полная контейнеризация с Docker Compose

## 🛠 Технологический стек

### Backend
- **Java 21** - основной язык программирования
- **Spring Boot 3.5.4** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Scheduling** - планировщик задач

### Базы данных
- **PostgreSQL 15** - основное хранилище данных
- **Redis 7** - кэширование и хранение сессий

### Инфраструктура
- **Docker** - контейнеризация приложения
- **Docker Compose** - оркестрация контейнеров
- **Liquibase** - управление миграциями базы данных

### Внешние интеграции
- **Telegram Bot API** - взаимодействие с Telegram
- **Gradle** - система сборки

## 📦 Установка и запуск

### Предварительные требования

- Docker 20.10+
- Docker Compose 2.0+
- Java 21

### Запуск с помощью Docker Compose

1. Клонируйте репозиторий:
```bash
git clone https://github.com/VevigoVR/game-telegram-bot.git
```

2. Создайте файл `.env` в корневой директории (на основе `.env.example`):
```bash
cp .env.example .env
```

3. Заполните файл `.env` необходимыми значениями:
```env
# Настройки базы данных
POSTGRES_DB=creazione
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_secure_password

# Настройки Redis
REDIS_PASSWORD=your_redis_password

# Настройки Telegram бота
BOT_TOKEN=your_telegram_bot_token
BOT_USERNAME=your_bot_username

# Настройки приложения
SPRING_PROFILES_ACTIVE=prod
```

4. Запустите приложение:
```bash
docker-compose up -d
```

Приложение будет доступно внутри Docker-сети. Для доступа к базе данных извне можно раскомментировать и настроить порты в `docker-compose.yml`.

### Локальная разработка

1. Убедитесь, что установлены Java 21 и Gradle
2. Запустите PostgreSQL и Redis через Docker Compose:
```bash
docker-compose up db redis -d
```
3. Запустите приложение в режиме разработки:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📁 Структура проекта

```
creazione-bot/
├── src/main/java
│   └── com/creazione/space_learning
│       ├── controllers/      # REST контроллеры
│       ├── entities/         # Сущности БД
│       ├── repositories/     # Репозитории Spring Data
│       ├── services/         # Бизнес-логика
│       ├── config/           # Конфигурационные классы
│       └── Application.java  # Главный класс приложения
├── src/main/resources
│   ├── application.yml       # Основные настройки
│   ├── application-dev.yml   # Настройки для разработки
│   └── application-prod.yml  # Настройки для production
├── docker-compose.yml        # Docker Compose конфигурация
├── Dockerfile               # Docker образ приложения
└── .env                     # Переменные окружения (не в репозитории)
```

## 🔧 Настройка

### Конфигурация базы данных

Настройки подключения к PostgreSQL находятся в `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/creazione
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Конфигурация Redis

Настройки Redis находятся в том же файле:

```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD}
```

### Конфигурация Telegram бота

Настройки бота задаются через переменные окружения в `.env` файле.

## 🧪 Тестирование

Проект включает unit- и интеграционные тесты:

```bash
# Запуск всех тестов
./gradlew test

# Запуск только unit-тестов
./gradlew test --tests "*UnitTest"

# Запуск только интеграционных тестов
./gradlew test --tests "*IntegrationTest"
```

## 📊 Миграции базы данных

Миграции управляются с помощью Liquibase. Файлы миграций находятся в `src/main/resources/db/changelog`. Для создания новой миграции:

```bash
./gradlew diffChangeLog
```

## 🚀 Деплой

### Production-сборка

1. Соберите JAR-файл:
```bash
./gradlew bootJar
```

2. Соберите Docker-образ:
```bash
docker build -t creazione-bot .
```

3. Запустите с production-конфигурацией:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Мониторинг

Приложение предоставляет эндпоинты для мониторинга через Spring Boot Actuator:
- `/actuator/health` - состояние приложения
- `/actuator/metrics` - метрики производительности
- `/actuator/info` - информация о приложении

## 🤝 Вклад в проект

Если вы хотите внести вклад в проект:

1. Форкните репозиторий
2. Создайте ветку для вашей функции (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add some amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для подробностей.

## 📞 Контакты

Виктор Рябченко - [@Vevigo](https://t.me/Vevigo) в Telegram

Ссылка на проект: [https://github.com/your-username/creazione-bot](https://github.com/your-username/creazione-bot)

## 🙏 Благодарности

- Команда Skillbox за качественное обучение
- Сообщество Spring за отличную документацию
- Разработчикам Telegram Bot API за простой и мощный API

---

<div align="center">

**Наслаждайтесь игрой!** 🎮

</div>