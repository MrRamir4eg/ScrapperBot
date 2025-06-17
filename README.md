![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

## Запуск приложения

0. Запустить docker-compose с файлом compose.yaml (Необходимы переменные DB_USER, DB_PASSWORD)
   *
1. Запустить через IntellijIdea

### или

+
1. Скомпилировать jar для Bot и Scrapper
2. Добавить в переменные окружения токены TELEGRAM_TOKEN и GITHUB_TOKEN
3. Запустить jar-ники через java -jar file.jar

Для дополнительной справки: [HELP.md](./HELP.md)
