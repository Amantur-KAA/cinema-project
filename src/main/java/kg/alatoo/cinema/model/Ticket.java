package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Билет: клиент + сеанс + место (таблица tickets).
 * Статус меняется не сеттером, а методами pay() и cancel() с проверками
 * (как deposit/withdraw вместо setBalance в пособии).
 */
public class Ticket {
    private int id;
    private Screening screening;
    private Customer customer;
    private int seatNumber;
    private double price;
    private TicketStatus status;
    private String paymentMethod;
    private LocalDateTime soldAt;

    public Ticket(int id, Screening screening, Customer customer, int seatNumber, double price,
                  TicketStatus status, String paymentMethod, LocalDateTime soldAt) {
        if (screening == null || customer == null) {
            throw new ValidationException("У билета должны быть сеанс и клиент");
        }
        if (!screening.getHall().isValidSeat(seatNumber)) {
            throw new ValidationException("В зале «" + screening.getHall().getName() + "» места с 1 по "
                    + screening.getHall().getSeats());
        }
        if (price < 0) {
            throw new ValidationException("Цена билета не может быть отрицательной");
        }
        this.id = id;
        this.screening = screening;
        this.customer = customer;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status == null ? TicketStatus.BOOKED : status;
        this.paymentMethod = paymentMethod;
        this.soldAt = soldAt == null ? LocalDateTime.now() : soldAt;
    }

    /** Новый забронированный билет. */
    public Ticket(Screening screening, Customer customer, int seatNumber, double price) {
        this(0, screening, customer, seatNumber, price, TicketStatus.BOOKED, null, null);
    }

    public void pay(String method) {
        if (status != TicketStatus.BOOKED) {
            throw new ValidationException("Оплатить можно только забронированный билет. Сейчас: " + status);
        }
        this.status = TicketStatus.PAID;
        this.paymentMethod = method;
    }

    public void cancel() {
        if (status == TicketStatus.CANCELLED) {
            throw new ValidationException("Билет уже отменён");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Screening getScreening() {
        return screening;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public double getPrice() {
        return price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ticket other = (Ticket) obj;
        return id == other.id && seatNumber == other.seatNumber
                && Objects.equals(screening, other.screening);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seatNumber, screening);
    }

    @Override
    public String toString() {
        return "Билет #" + id + " | " + screening.getFilm().getTitle() + " | "
                + screening.getStartTime().format(Screening.FORMAT) + " | " + screening.getHall().getName()
                + " | место " + seatNumber + " | " + customer.getFullName() + " | " + price + " сом | " + status
                + (paymentMethod != null ? " (" + paymentMethod + ")" : "");
    }
}
