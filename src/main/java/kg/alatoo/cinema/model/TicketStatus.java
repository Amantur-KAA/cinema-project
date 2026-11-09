package kg.alatoo.cinema.model;

/** Статус билета (enum, неделя 5). */
public enum TicketStatus {
    BOOKED,     // забронирован, ещё не оплачен
    PAID,       // оплачен
    CANCELLED   // отменён
}
