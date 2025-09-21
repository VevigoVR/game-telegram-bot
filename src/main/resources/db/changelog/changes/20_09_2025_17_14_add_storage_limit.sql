-- liquibase formatted sql
-- changeset Vevigo:add_storage_limit_to_buildings_table

ALTER TABLE buildings
ADD COLUMN storage_limit BIGINT NOT NULL DEFAULT 100;

-- comment: Добавляем колонку storage_limit в таблицу buildings со значением по умолчанию 100