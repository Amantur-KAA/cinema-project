-- Тестовые данные. Выполняются только если база пустая.

INSERT INTO genres (name) VALUES
    ('Драма'), ('Комедия'), ('Фантастика'), ('Мультфильм'), ('Триллер');

INSERT INTO films (title, duration_min, age_rating, genre_id) VALUES
    ('Интерстеллар', 169, 12, 3),
    ('Зелёная миля', 189, 16, 1),
    ('Король Лев', 88, 0, 4),
    ('Один дома', 103, 6, 2),
    ('Остров проклятых', 138, 18, 5);

INSERT INTO halls (name, hall_type, seats) VALUES
    ('Зал 1', 'STANDARD', 80),
    ('Зал 2 VIP', 'VIP', 20),
    ('Зал 3 IMAX', 'IMAX', 120);

INSERT INTO screenings (film_id, hall_id, start_time, base_price) VALUES
    (1, 3, CURRENT_DATE + INTERVAL '1 day' + TIME '18:00', 400),
    (1, 1, CURRENT_DATE + INTERVAL '1 day' + TIME '12:00', 300),
    (2, 2, CURRENT_DATE + INTERVAL '1 day' + TIME '20:00', 350),
    (3, 1, CURRENT_DATE + INTERVAL '2 day' + TIME '10:00', 250),
    (4, 1, CURRENT_DATE + INTERVAL '2 day' + TIME '14:00', 250);

INSERT INTO customers (full_name, phone, email, is_student) VALUES
    ('Айбек Токтогулов', '+996700111222', 'aibek@mail.kg', TRUE),
    ('Алина Садыкова', '+996555333444', 'alina@mail.kg', FALSE),
    ('Нурлан Асанов', '+996777555666', NULL, TRUE);

INSERT INTO tickets (screening_id, customer_id, seat_number, price, status, payment_method) VALUES
    (1, 1, 10, 416.00, 'PAID', 'card'),
    (1, 2, 11, 520.00, 'PAID', 'cash'),
    (3, 2, 5, 525.00, 'PAID', 'card'),
    (2, 3, 20, 240.00, 'BOOKED', NULL),
    (4, 1, 7, 200.00, 'PAID', 'cash');
