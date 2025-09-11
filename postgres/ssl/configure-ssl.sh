#!/bin/bash
# Генерация SSL сертификатов для PostgreSQL
openssl req -new -text -passout pass:abcd -subj /CN=localhost -out server.req
openssl rsa -in privkey.pem -passin pass:abcd -out server.key
openssl req -x509 -in server.req -text -key server.key -out server.crt

# Устанавливаем правильные права
chmod 600 server.key
chown postgres:postgres server.key server.crt

# Перемещаем в правильную директорию
mv server.key server.crt /var/lib/postgresql/data/