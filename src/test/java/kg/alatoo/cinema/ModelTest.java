package kg.alatoo.cinema;

import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Genre;
import kg.alatoo.cinema.model.Hall;
import kg.alatoo.cinema.model.HallFactory;
import kg.alatoo.cinema.model.Screening;
import kg.alatoo.cinema.model.Ticket;
import kg.alatoo.cinema.model.TicketStatus;
import kg.alatoo.cinema.model.VipHall;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты моделей: валидация, полиморфизм залов, equals/hashCode, статусы билета. */
class ModelTest {

    private final Genre drama = new Genre(1, "Драма");

    @Test
    void filmValidation() {
        assertThrows(ValidationException.class, () -> new Film("", 120, 12, drama));
        assertThrows(ValidationException.class, () -> new Film("Фильм", 0, 12, drama));
        assertThrows(ValidationException.class, () -> new Film("Фильм", 120, 13, drama));
        assertThrows(ValidationException.class, () -> new Film("Фильм", 120, 12, null));
    }

    @Test
    void customerValidation() {
        assertThrows(ValidationException.class, () -> new Customer("Алина", "abc", null, false));
        assertThrows(ValidationException.class, () -> new Customer("Алина", "+996700111222", "bad-email", false));
        Customer c = new Customer("  Алина  ", "+996700111222", "", true);
        assertEquals("Алина", c.getFullName());
        assertEquals(null, c.getEmail());
    }

    @Test
    void hallPolymorphism() {
        Hall standard = HallFactory.create("STANDARD", 1, "Зал 1", 80);
        Hall vip = HallFactory.create("vip", 2, "VIP", 20);
        Hall imax = HallFactory.create("IMAX", 3, "IMAX", 120);
        assertTrue(vip instanceof VipHall);
        assertEquals(300.0, standard.calculatePrice(300));
        assertEquals(450.0, vip.calculatePrice(300));
        assertEquals(390.0, imax.calculatePrice(300));
        assertThrows(ValidationException.class, () -> HallFactory.create("4DX", 4, "X", 50));
    }

    @Test
    void equalsAndHashCode() {
        Film a = new Film(5, "1984", 110, 16, drama);
        Film b = new Film(5, "1984", 110, 16, drama);
        assertNotSame(a, b);       // два разных объекта (== даст false)
        assertEquals(a, b);        // но по смыслу равны
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void screeningsOverlap() {
        Film film = new Film(1, "Фильм", 120, 12, drama);
        Hall hall = HallFactory.create("STANDARD", 1, "Зал 1", 80);
        LocalDateTime t = LocalDateTime.of(2030, 1, 1, 18, 0);
        Screening first = new Screening(1, film, hall, t, 300);
        Screening overlapping = new Screening(2, film, hall, t.plusMinutes(60), 300);
        Screening after = new Screening(3, film, hall, t.plusMinutes(120), 300);
        assertTrue(first.overlapsWith(overlapping));
        assertFalse(first.overlapsWith(after));
    }

    @Test
    void ticketStatusFlow() {
        Film film = new Film(1, "Фильм", 120, 12, drama);
        Hall hall = HallFactory.create("STANDARD", 1, "Зал 1", 80);
        Screening s = new Screening(1, film, hall, LocalDateTime.of(2030, 1, 1, 18, 0), 300);
        Customer c = new Customer(1, "Алина", "+996700111222", null, false);

        assertThrows(ValidationException.class, () -> new Ticket(s, c, 81, 300)); // такого места нет

        Ticket ticket = new Ticket(s, c, 10, 300);
        assertEquals(TicketStatus.BOOKED, ticket.getStatus());
        ticket.pay("card");
        assertEquals(TicketStatus.PAID, ticket.getStatus());
        assertThrows(ValidationException.class, () -> ticket.pay("cash")); // второй раз нельзя
        ticket.cancel();
        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertThrows(ValidationException.class, ticket::cancel);
    }
}
