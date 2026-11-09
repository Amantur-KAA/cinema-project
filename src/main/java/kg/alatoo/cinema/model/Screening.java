package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Сеанс: какой фильм, в каком зале и когда (таблица screenings). */
public class Screening {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private Film film;
    private Hall hall;
    private LocalDateTime startTime;
    private double basePrice;

    public Screening(int id, Film film, Hall hall, LocalDateTime startTime, double basePrice) {
        this.id = id;
        setFilm(film);
        setHall(hall);
        setStartTime(startTime);
        setBasePrice(basePrice);
    }

    public Screening(Film film, Hall hall, LocalDateTime startTime, double basePrice) {
        this(0, film, hall, startTime, basePrice);
    }

    /** Когда сеанс закончится: начало + длительность фильма. */
    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(film.getDurationMinutes());
    }

    /** Пересекается ли этот сеанс по времени с другим. */
    public boolean overlapsWith(Screening other) {
        return startTime.isBefore(other.getEndTime()) && other.startTime.isBefore(getEndTime());
    }

    /** Цена билета в этом зале без скидки. */
    public double getHallPrice() {
        return hall.calculatePrice(basePrice);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        if (film == null) {
            throw new ValidationException("У сеанса должен быть фильм");
        }
        this.film = film;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        if (hall == null) {
            throw new ValidationException("У сеанса должен быть зал");
        }
        this.hall = hall;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new ValidationException("Не указано время начала сеанса");
        }
        this.startTime = startTime;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        if (basePrice <= 0 || basePrice > 10000) {
            throw new ValidationException("Базовая цена должна быть больше 0 и не больше 10000 сом");
        }
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Screening other = (Screening) obj;
        return id == other.id && Objects.equals(startTime, other.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startTime);
    }

    @Override
    public String toString() {
        return "#" + id + " " + startTime.format(FORMAT) + " | " + film.getTitle() + " | " + hall.getName()
                + " (" + hall.getType() + ") | цена " + getHallPrice() + " сом";
    }
}
