-- Запросы для демонстрации на защите (можно выполнить в DBeaver / pgAdmin)

-- 1. JOIN: расписание сеансов с фильмом, жанром и залом
SELECT s.id, s.start_time, f.title, g.name AS genre, h.name AS hall, h.hall_type, s.base_price
FROM screenings s
JOIN films f ON f.id = s.film_id
JOIN genres g ON g.id = f.genre_id
JOIN halls h ON h.id = s.hall_id
ORDER BY s.start_time;

-- 2. Агрегат: выручка по фильмам (LEFT JOIN — видны и фильмы без продаж)
SELECT f.title, COUNT(t.id) AS tickets, COALESCE(SUM(t.price), 0) AS revenue
FROM films f
LEFT JOIN screenings s ON s.film_id = f.id
LEFT JOIN tickets t ON t.screening_id = s.id AND t.status = 'PAID'
GROUP BY f.id, f.title
ORDER BY revenue DESC;

-- 3. HAVING: клиенты, купившие 2 и больше билетов
SELECT c.full_name, COUNT(t.id) AS tickets, SUM(t.price) AS total
FROM customers c
JOIN tickets t ON t.customer_id = c.id
WHERE t.status = 'PAID'
GROUP BY c.id, c.full_name
HAVING COUNT(t.id) >= 2;

-- 4. Подзапрос: фильмы без продаж
SELECT title FROM films
WHERE id NOT IN (SELECT s.film_id FROM screenings s
                 JOIN tickets t ON t.screening_id = s.id
                 WHERE t.status = 'PAID');

-- 5. Средняя цена билета по типу зала
SELECT h.hall_type, ROUND(AVG(t.price), 2) AS avg_price, COUNT(*) AS tickets
FROM tickets t
JOIN screenings s ON s.id = t.screening_id
JOIN halls h ON h.id = s.hall_id
WHERE t.status <> 'CANCELLED'
GROUP BY h.hall_type;

-- 6. Транзакция (неделя 13): пересадить клиента на другое место — всё или ничего
BEGIN;
UPDATE tickets SET status = 'CANCELLED' WHERE id = 4;
INSERT INTO tickets (screening_id, customer_id, seat_number, price, status)
VALUES (2, 3, 21, 240.00, 'BOOKED');
COMMIT;
-- если ошибка → ROLLBACK;
