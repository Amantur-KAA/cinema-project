package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.util.Objects;

/**
 * Кинозал (таблица halls). Абстрактный класс (неделя 4):
 * общие поля здесь, а наценку за тип зала определяет каждый подкласс.
 */
public abstract class Hall {
    private int id;
    private String name;
    private int seats;

    protected Hall(int id, String name, int seats) {
        this.id = id;
        setName(name);
        setSeats(seats);
    }

    /** Во сколько раз билет в этом зале дороже базовой цены. */
    public abstract double getPriceMultiplier();

    /** Тип зала так, как он хранится в базе: STANDARD, VIP, IMAX. */
    public abstract String getType();

    /** Полиморфизм: метод один, а результат зависит от реального типа зала. */
    public double calculatePrice(double basePrice) {
        return Math.round(basePrice * getPriceMultiplier() * 100) / 100.0;
    }

    public boolean isValidSeat(int seatNumber) {
        return seatNumber >= 1 && seatNumber <= seats;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название зала не может быть пустым");
        }
        this.name = name.trim();
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats < 10 || seats > 500) {
            throw new ValidationException("В зале должно быть от 10 до 500 мест");
        }
        this.seats = seats;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hall other = (Hall) obj;
        return id == other.id && seats == other.seats && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, seats);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " [" + getType() + ", мест: " + seats + ", наценка x" + getPriceMultiplier() + "]";
    }
}
