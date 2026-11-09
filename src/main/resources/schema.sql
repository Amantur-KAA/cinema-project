-- Схема базы данных «Кинотеатр» (6 таблиц, 3NF)

CREATE TABLE IF NOT EXISTS genres (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    duration_min INT NOT NULL CHECK (duration_min BETWEEN 1 AND 400),
    age_rating   INT NOT NULL CHECK (age_rating IN (0, 6, 12, 16, 18)),
    genre_id     INT NOT NULL REFERENCES genres (id)
);

CREATE TABLE IF NOT EXISTS halls (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL UNIQUE,
    hall_type VARCHAR(20) NOT NULL CHECK (hall_type IN ('STANDARD', 'VIP', 'IMAX')),
    seats     INT NOT NULL CHECK (seats BETWEEN 10 AND 500)
);

CREATE TABLE IF NOT EXISTS screenings (
    id         SERIAL PRIMARY KEY,
    film_id    INT NOT NULL REFERENCES films (id) ON DELETE CASCADE,
    hall_id    INT NOT NULL REFERENCES halls (id),
    start_time TIMESTAMP NOT NULL,
    base_price NUMERIC(8, 2) NOT NULL CHECK (base_price > 0)
);

CREATE TABLE IF NOT EXISTS customers (
    id         SERIAL PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(20) NOT NULL UNIQUE,
    email      VARCHAR(100),
    is_student BOOLEAN NOT NULL DEFAULT FALSE
);

-- tickets — промежуточная таблица M:N между customers и screenings
CREATE TABLE IF NOT EXISTS tickets (
    id             SERIAL PRIMARY KEY,
    screening_id   INT NOT NULL REFERENCES screenings (id) ON DELETE CASCADE,
    customer_id    INT NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    seat_number    INT NOT NULL CHECK (seat_number > 0),
    price          NUMERIC(8, 2) NOT NULL CHECK (price >= 0),
    status         VARCHAR(20) NOT NULL DEFAULT 'BOOKED' CHECK (status IN ('BOOKED', 'PAID', 'CANCELLED')),
    payment_method VARCHAR(20) CHECK (payment_method IN ('card', 'cash')),
    sold_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Одно место на сеансе нельзя продать дважды (отменённые билеты не считаются)
CREATE UNIQUE INDEX IF NOT EXISTS ux_tickets_seat
    ON tickets (screening_id, seat_number)
    WHERE status <> 'CANCELLED';
