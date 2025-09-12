#!/bin/bash

# Проверяем, передано ли сообщение коммита
if [ $# -eq 0 ]; then
    echo "Ошибка: Не указано сообщение для коммита"
    echo "Использование: $0 \"Ваше сообщение коммита в кавычках\""
    exit 1
fi

# Объединяем все аргументы в одно сообщение
COMMIT_MESSAGE="$*"

# Проверяем, является ли текущая директория репозиторием Git
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo "Ошибка: Текущая директория не является репозиторием Git"
    exit 1
fi

# Проверяем, есть ли изменения для коммита
if [ -z "$(git status --porcelain)" ]; then
    echo "Нет изменений для коммита"
    exit 0
fi

# Выполняем Git команды с проверкой ошибок
echo "Добавляем все файлы в индекс..."
if ! git add .; then
    echo "Ошибка при выполнении 'git add .'"
    exit 1
fi

echo "Создаем коммит с сообщением: '$COMMIT_MESSAGE'..."
if ! git commit -m "$COMMIT_MESSAGE"; then
    echo "Ошибка при выполнении 'git commit'"
    exit 1
fi

echo "Отправляем изменения в удаленный репозиторий..."
if ! git push; then
    echo "Ошибка при выполнении 'git push'"
    exit 1
fi

echo "Операция завершена успешно!"