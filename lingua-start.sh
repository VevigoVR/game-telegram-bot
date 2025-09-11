#!/bin/bash

# Скрипт для сборки и запуска проекта Lingua

# Переходим в директорию скрипта
# cd "lingua-space/"

echo "🔨 Сборка проекта с помощью Gradle..."
./gradlew clean build -x test

if [ $? -eq 0 ]; then
    echo "✅ Сборка успешно завершена"
    echo "🐳 Запуск Docker Compose..."
    docker-compose up --build
else
    echo "❌ Ошибка при сборке проекта"
    exit 1
fi
