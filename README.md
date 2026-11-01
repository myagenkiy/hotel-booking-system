# Hotel Booking System (микросервисы)

## Компоненты
- `eureka-server` (8761) — реестр сервисов
- `hotel-service` (8082) — отели и номера
- `booking-service` (8083) — пользователи и бронирования
- `api-gateway` (8080) — маршрутизация

## Стек
Java 17, Spring Boot 4.1.2, Spring Cloud 2025.1.4,
Spring Data JPA + H2 (in-memory), Spring Security + JWT (jjwt 0.12.6),
Spring Cloud Gateway (WebFlux), Eureka, OpenFeign,
Lombok, springdoc-openapi 3.0.1.

## Запуск
1. Запустить `eureka-server`, дождаться `http://localhost:8761`.
2. Запустить `hotel-service` и `booking-service`, дождаться `registration status: 204`.
3. Запустить `api-gateway` последним.
4. Все запросы — через `http://localhost:8080`.

## Регистрация пользователей
POST /user/register
- USER:  {"username":"alice","password":"pass123"}
- ADMIN: {"username":"admin","password":"admin123","adminCode":"SECRET_ADMIN_CODE"}

POST /user/auth — вход, возвращает JWT (срок 1 час).

## Эндпоинты
- POST /booking — создать бронирование (USER)
  {"userId":1,"roomId":1,"startDate":"2027-07-01","endDate":"2027-07-05","requestId":"req-1"}
- GET /bookings?userId=1 — история (USER)
- GET /booking/{id} — бронирование по id (USER)
- DELETE /booking/{id} — отмена (USER)
- POST /api/hotels — создать отель (ADMIN)
- POST /api/rooms — создать номер (ADMIN)
- GET /api/hotels — список отелей (USER/ADMIN)
- GET /api/rooms — свободные номера (USER/ADMIN)
- GET /api/rooms/recommend — рекомендации по timesBooked (USER/ADMIN)
- POST /user, PATCH /user, DELETE /user — управление пользователями (ADMIN)

## Согласованность бронирования
1. Booking создаётся в статусе PENDING (локальная транзакция).
2. Feign вызывает hotel-service `POST /api/rooms/{id}/confirm-availability` (таймаут 3 сек).
3. При успехе — CONFIRMED; при ошибке/таймауте — CANCELLED + `POST /api/rooms/{id}/release` (компенсация).
4. Идемпотентность по `requestId`: повторный запрос возвращает существующий результат.
5. Конкурентные бронирования на пересекающиеся даты → 409 Conflict.

## Swagger
- booking: http://localhost:8080/swagger-ui.html
- hotel:   http://localhost:8080/hotel-swagger/swagger-ui.html

## Тесты
В IDEA: правой кнопкой на `HotelControllerTest` / `AuthControllerTest` → Run.
Или `mvn test` в корне каждого сервиса (если Maven установлен).

## Примечание
Базы H2 in-memory: после перезапуска сервиса данные очищаются.
После каждого перезапуска `booking-service` нужно заново зарегистрировать пользователей.
