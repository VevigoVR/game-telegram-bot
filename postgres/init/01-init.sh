#!/bin/bash
set -e

echo "Starting database initialization..."

# Создаем пользователя если не существует
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $APP_DB_USER WITH PASSWORD '$APP_DB_PASSWORD';
    CREATE DATABASE $APP_DB_NAME;
    GRANT ALL PRIVILEGES ON DATABASE $APP_DB_NAME TO $APP_DB_USER;
EOSQL

echo "User and database created successfully"

# Создаем схему в новой базе и даем ВСЕ права
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$APP_DB_NAME" <<-EOSQL
    CREATE SCHEMA IF NOT EXISTS $SCHEMA;

    -- Делаем пользователя владельцем схемы
    ALTER SCHEMA $SCHEMA OWNER TO $APP_DB_USER;

    -- Устанавливаем схему по умолчанию
    ALTER USER $APP_DB_USER SET search_path TO $SCHEMA;
EOSQL

echo "Schema $SCHEMA created successfully"
echo "Database initialization completed!"