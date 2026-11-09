package kg.alatoo.cinema.service;

import kg.alatoo.cinema.dao.CustomerDao;
import kg.alatoo.cinema.dao.ScreeningDao;
import kg.alatoo.cinema.dao.TicketDao;
import kg.alatoo.cinema.discount.DiscountStrategy;
import kg.alatoo.cinema.discount.FixedDiscount;
import kg.alatoo.cinema.discount.NoDiscount;
import kg.alatoo.cinema.discount.PercentDiscount;
import kg.alatoo.cinema.exception.NotFoundException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;
import kg.alatoo.cinema.model.Screening;
import kg.alatoo.cinema.model.Ticket;
import kg.alatoo.cinema.model.TicketStatus;
import kg.alatoo.cinema.payment.Payment;
import kg.alatoo.cinema.payment.PaymentFactory;
import kg.alatoo.cinema.payment.Refundable;

import java.time.LocalDateTime;
import java.util.List;

/** Продажа, оплата и отмена билетов. */
public class BookingService {
    public static final int STUDENT_DISCOUNT_PERCENT = 20;
    public static final String PROMO_CODE = "KINO100";
    public static final double PROMO_AMOUNT = 100;

    private final TicketDao ticketDao;
    private final ScreeningDao screeningDao;
    private final CustomerDao customerDao;

    public BookingService(TicketDao ticketDao, ScreeningDao screeningDao, CustomerDao customerDao) {
        this.ticketDao = ticketDao;
        this.screeningDao = screeningDao;
        this.customerDao = customerDao;
    }

    public DiscountStrategy noDiscount() {
        return new NoDiscount();
    }

    /** Студенческая скидка только для клиентов-студентов. */
    public DiscountStrategy studentDiscount(Customer customer) {
        if (!customer.isStudent()) {
            throw new ValidationException("Клиент " + customer.getFullName() + " не студент, скидка не положена");
        }
        return new PercentDiscount(STUDENT_DISCOUNT_PERCENT);
    }

    public DiscountStrategy promoDiscount(String code) {
        if (code == null || !PROMO_CODE.equalsIgnoreCase(code.trim())) {
            throw new ValidationException("Промокод не найден");
        }
        return new FixedDiscount(PROMO_AMOUNT);
    }

    public Customer getCustomer(int id) {
        Customer found = customerDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Клиент #" + id + " не найден");
        }
        return found;
    }

    public Screening getScreening(int id) {
        Screening found = screeningDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Сеанс #" + id + " не найден");
        }
        return found;
    }

    public List<Integer> getTakenSeats(int screeningId) {
        return ticketDao.findTakenSeats(screeningId);
    }

    /** Бронирование: проверяем время сеанса и место, считаем цену зала, применяем скидку. */
    public Ticket book(Screening screening, Customer customer, int seat, DiscountStrategy discount) {
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Сеанс уже начался, продажа закрыта");
        }
        if (!screening.getHall().isValidSeat(seat)) {
            throw new ValidationException("В этом зале места с 1 по " + screening.getHall().getSeats());
        }
        if (ticketDao.findTakenSeats(screening.getId()).contains(seat)) {
            throw new ValidationException("Место " + seat + " уже занято");
        }
        double price = discount.apply(screening.getHallPrice());
        return ticketDao.create(new Ticket(screening, customer, seat, price));
    }

    public Ticket pay(int ticketId, String method) {
        Ticket ticket = getTicket(ticketId);
        Payment payment = PaymentFactory.create(method, ticket.getPrice());
        ticket.pay(payment.getMethod()); // проверка статуса внутри Ticket
        payment.pay();
        ticketDao.updateStatus(ticket);
        return ticket;
    }

    public Ticket cancel(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        if (ticket.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Сеанс уже начался, билет отменить нельзя");
        }
        boolean wasPaid = ticket.getStatus() == TicketStatus.PAID;
        ticket.cancel();
        if (wasPaid) {
            Payment payment = PaymentFactory.create(ticket.getPaymentMethod(), ticket.getPrice());
            if (payment instanceof Refundable) {  // instanceof перед приведением (неделя 4)
                Refundable refundable = (Refundable) payment;
                refundable.printPolicy();
                refundable.refund(ticket.getPrice());
            } else {
                System.out.println("Билет оплачен наличными: верните " + ticket.getPrice() + " сом в кассе.");
            }
        }
        ticketDao.updateStatus(ticket);
        return ticket;
    }

    public Ticket getTicket(int id) {
        Ticket found = ticketDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Билет #" + id + " не найден");
        }
        return found;
    }

    public List<Ticket> getAllTickets() {
        return ticketDao.findAll();
    }

    public List<Ticket> getTicketsOfCustomer(int customerId) {
        return ticketDao.findByCustomer(customerId);
    }
}
