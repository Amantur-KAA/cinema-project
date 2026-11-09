package kg.alatoo.cinema.ui;

import kg.alatoo.cinema.discount.DiscountStrategy;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.exception.NotFoundException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Genre;
import kg.alatoo.cinema.model.Hall;
import kg.alatoo.cinema.model.Screening;
import kg.alatoo.cinema.model.Ticket;
import kg.alatoo.cinema.report.CustomerStats;
import kg.alatoo.cinema.report.FilmRevenue;
import kg.alatoo.cinema.report.ScreeningOccupancy;
import kg.alatoo.cinema.service.BookingService;
import kg.alatoo.cinema.service.CustomerService;
import kg.alatoo.cinema.service.FilmService;
import kg.alatoo.cinema.service.ReportService;
import kg.alatoo.cinema.service.ScreeningService;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/** Консольное меню (слой UI). Здесь нет SQL — только вызовы сервисов. */
public class ConsoleMenu {
    private final InputReader in;
    private final FilmService filmService;
    private final ScreeningService screeningService;
    private final CustomerService customerService;
    private final BookingService bookingService;
    private final ReportService reportService;

    public ConsoleMenu(InputReader in, FilmService filmService, ScreeningService screeningService,
                       CustomerService customerService, BookingService bookingService, ReportService reportService) {
        this.in = in;
        this.filmService = filmService;
        this.screeningService = screeningService;
        this.customerService = customerService;
        this.bookingService = bookingService;
        this.reportService = reportService;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("========== КИНОТЕАТР ==========");
            System.out.println("1. Фильмы");
            System.out.println("2. Сеансы");
            System.out.println("3. Клиенты");
            System.out.println("4. Билеты");
            System.out.println("5. Отчёты");
            System.out.println("0. Выход");
            int choice = in.readInt("Выберите пункт: ", 0, 5);
            switch (choice) {
                case 1 -> filmsMenu();
                case 2 -> screeningsMenu();
                case 3 -> customersMenu();
                case 4 -> ticketsMenu();
                case 5 -> reportsMenu();
                default -> {
                    System.out.println("До свидания!");
                    return;
                }
            }
        }
    }

    // Ошибки ввода (ValidationException, NotFoundException) и базы (DataAccessException)
    // ловим в каждом подменю: программа пишет понятное сообщение и не падает.

    // ---------------------------- Фильмы ----------------------------

    private void filmsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Фильмы ---");
            System.out.println("1. Список фильмов");
            System.out.println("2. Добавить фильм");
            System.out.println("3. Найти фильм по названию");
            System.out.println("4. Изменить фильм");
            System.out.println("5. Удалить фильм");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите пункт: ", 0, 5);
            try {
                switch (choice) {
                    case 1 -> printList(filmService.getAllFilms(), "Фильмов пока нет.");
                    case 2 -> addFilm();
                    case 3 -> printList(filmService.searchFilms(in.readNonEmpty("Часть названия: ")),
                            "Ничего не найдено.");
                    case 4 -> updateFilm();
                    case 5 -> deleteFilm();
                    default -> {
                        return;
                    }
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void addFilm() {
        String title = in.readNonEmpty("Название: ");
        int duration = in.readInt("Длительность (мин): ", 1, 400);
        int age = in.readInt("Возрастной рейтинг (0, 6, 12, 16, 18): ", 0, 18);
        int genreId = chooseGenre();
        Film film = filmService.addFilm(title, duration, age, genreId);
        System.out.println("Добавлен: " + film);
    }

    private void updateFilm() {
        printList(filmService.getAllFilms(), "Фильмов пока нет.");
        Film film = filmService.getFilm(in.readInt("id фильма: ", 1, Integer.MAX_VALUE));
        System.out.println("Сейчас: " + film + ". Пустой ввод — оставить как есть.");
        String title = in.readLine("Новое название: ");
        String duration = in.readLine("Новая длительность: ");
        String age = in.readLine("Новый рейтинг: ");
        boolean changeGenre = in.readYesNo("Изменить жанр?");
        int genreId = changeGenre ? chooseGenre() : film.getGenre().getId();
        Film updated = filmService.updateFilm(film.getId(),
                title.isEmpty() ? film.getTitle() : title,
                duration.isEmpty() ? film.getDurationMinutes() : parseInt(duration),
                age.isEmpty() ? film.getAgeRating() : parseInt(age),
                genreId);
        System.out.println("Сохранено: " + updated);
    }

    private void deleteFilm() {
        printList(filmService.getAllFilms(), "Фильмов пока нет.");
        int id = in.readInt("id фильма для удаления: ", 1, Integer.MAX_VALUE);
        if (in.readYesNo("Удалить фильм вместе со всеми его сеансами и билетами?")) {
            filmService.deleteFilm(id);
            System.out.println("Фильм удалён.");
        }
    }

    private int chooseGenre() {
        List<Genre> genres = filmService.getAllGenres();
        for (Genre g : genres) {
            System.out.println("  " + g.getId() + ". " + g.getName());
        }
        return in.readInt("id жанра: ", 1, Integer.MAX_VALUE);
    }

    // ---------------------------- Сеансы ----------------------------

    private void screeningsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Сеансы ---");
            System.out.println("1. Все сеансы");
            System.out.println("2. Ближайшие сеансы");
            System.out.println("3. Найти сеансы по названию фильма");
            System.out.println("4. Добавить сеанс");
            System.out.println("5. Изменить время и цену сеанса");
            System.out.println("6. Удалить сеанс");
            System.out.println("7. Список залов");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите пункт: ", 0, 7);
            try {
                switch (choice) {
                    case 1 -> printList(screeningService.getAllScreenings(), "Сеансов нет.");
                    case 2 -> printList(screeningService.getUpcomingScreenings(), "Ближайших сеансов нет.");
                    case 3 -> printList(
                            screeningService.searchByFilmTitle(in.readNonEmpty("Часть названия фильма: ")),
                            "Ничего не найдено.");
                    case 4 -> addScreening();
                    case 5 -> updateScreening();
                    case 6 -> deleteScreening();
                    case 7 -> printList(screeningService.getAllHalls(), "Залов нет.");
                    default -> {
                        return;
                    }
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void addScreening() {
        printList(filmService.getAllFilms(), "Сначала добавьте фильм.");
        int filmId = in.readInt("id фильма: ", 1, Integer.MAX_VALUE);
        printList(screeningService.getAllHalls(), "Залов нет.");
        int hallId = in.readInt("id зала: ", 1, Integer.MAX_VALUE);
        LocalDateTime start = in.readDateTime("Начало сеанса");
        double price = in.readDouble("Базовая цена (сом): ", 1, 10000);
        Screening s = screeningService.addScreening(filmId, hallId, start, price);
        System.out.println("Сеанс добавлен: " + s);
    }

    private void updateScreening() {
        printList(screeningService.getAllScreenings(), "Сеансов нет.");
        int id = in.readInt("id сеанса: ", 1, Integer.MAX_VALUE);
        Screening current = screeningService.getScreening(id);
        System.out.println("Сейчас: " + current);
        LocalDateTime start = in.readDateTime("Новое начало");
        double price = in.readDouble("Новая базовая цена (сом): ", 1, 10000);
        System.out.println("Сохранено: " + screeningService.updateScreening(id, start, price));
    }

    private void deleteScreening() {
        printList(screeningService.getAllScreenings(), "Сеансов нет.");
        int id = in.readInt("id сеанса для удаления: ", 1, Integer.MAX_VALUE);
        if (in.readYesNo("Удалить сеанс и все билеты на него?")) {
            screeningService.deleteScreening(id);
            System.out.println("Сеанс удалён.");
        }
    }

    // ---------------------------- Клиенты ----------------------------

    private void customersMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Клиенты ---");
            System.out.println("1. Список клиентов");
            System.out.println("2. Добавить клиента");
            System.out.println("3. Найти клиента (имя или телефон)");
            System.out.println("4. Изменить клиента");
            System.out.println("5. Удалить клиента");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите пункт: ", 0, 5);
            try {
                switch (choice) {
                    case 1 -> printList(customerService.getAllCustomers(), "Клиентов нет.");
                    case 2 -> addCustomer();
                    case 3 -> printList(customerService.searchCustomers(in.readNonEmpty("Имя или телефон: ")),
                            "Ничего не найдено.");
                    case 4 -> updateCustomer();
                    case 5 -> deleteCustomer();
                    default -> {
                        return;
                    }
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void addCustomer() {
        String name = in.readNonEmpty("ФИО: ");
        String phone = in.readNonEmpty("Телефон (+996...): ");
        String email = in.readLine("Email (можно пусто): ");
        boolean student = in.readYesNo("Студент?");
        System.out.println("Добавлен: " + customerService.addCustomer(name, phone, email, student));
    }

    private void updateCustomer() {
        printList(customerService.getAllCustomers(), "Клиентов нет.");
        Customer c = customerService.getCustomer(in.readInt("id клиента: ", 1, Integer.MAX_VALUE));
        System.out.println("Сейчас: " + c + ". Пустой ввод — оставить как есть.");
        String name = in.readLine("Новое ФИО: ");
        String phone = in.readLine("Новый телефон: ");
        String email = in.readLine("Новый email (- чтобы удалить): ");
        boolean student = in.readYesNo("Студент?");
        Customer updated = customerService.updateCustomer(c.getId(),
                name.isEmpty() ? c.getFullName() : name,
                phone.isEmpty() ? c.getPhone() : phone,
                email.isEmpty() ? c.getEmail() : (email.equals("-") ? null : email),
                student);
        System.out.println("Сохранено: " + updated);
    }

    private void deleteCustomer() {
        printList(customerService.getAllCustomers(), "Клиентов нет.");
        int id = in.readInt("id клиента для удаления: ", 1, Integer.MAX_VALUE);
        if (in.readYesNo("Удалить клиента и все его билеты?")) {
            customerService.deleteCustomer(id);
            System.out.println("Клиент удалён.");
        }
    }

    // ---------------------------- Билеты ----------------------------

    private void ticketsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Билеты ---");
            System.out.println("1. Купить (забронировать) билет");
            System.out.println("2. Оплатить билет");
            System.out.println("3. Отменить билет");
            System.out.println("4. Билеты клиента");
            System.out.println("5. Все билеты");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите пункт: ", 0, 5);
            try {
                switch (choice) {
                    case 1 -> bookTicket();
                    case 2 -> payTicket();
                    case 3 -> cancelTicket();
                    case 4 -> printList(
                            bookingService.getTicketsOfCustomer(in.readInt("id клиента: ", 1, Integer.MAX_VALUE)),
                            "У клиента нет билетов.");
                    case 5 -> printList(bookingService.getAllTickets(), "Билетов нет.");
                    default -> {
                        return;
                    }
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void bookTicket() {
        printList(screeningService.getUpcomingScreenings(), "Ближайших сеансов нет.");
        Screening screening = bookingService.getScreening(in.readInt("id сеанса: ", 1, Integer.MAX_VALUE));
        System.out.println("Мест в зале: " + screening.getHall().getSeats()
                + ". Заняты: " + bookingService.getTakenSeats(screening.getId()));
        int seat = in.readInt("Номер места: ", 1, screening.getHall().getSeats());

        printList(customerService.getAllCustomers(), "Сначала добавьте клиента.");
        Customer customer = bookingService.getCustomer(in.readInt("id клиента: ", 1, Integer.MAX_VALUE));

        System.out.println("Скидка: 1 — нет, 2 — студенческая " + BookingService.STUDENT_DISCOUNT_PERCENT
                + "%, 3 — промокод");
        int option = in.readInt("Выберите: ", 1, 3);
        DiscountStrategy discount = switch (option) {
            case 2 -> bookingService.studentDiscount(customer);
            case 3 -> bookingService.promoDiscount(in.readNonEmpty("Промокод: "));
            default -> bookingService.noDiscount();
        };

        Ticket ticket = bookingService.book(screening, customer, seat, discount);
        System.out.println("Забронировано (" + discount.getDescription() + "): " + ticket);
        if (in.readYesNo("Оплатить сейчас?")) {
            pay(ticket.getId());
        }
    }

    private void payTicket() {
        pay(in.readInt("id билета: ", 1, Integer.MAX_VALUE));
    }

    private void pay(int ticketId) {
        int method = in.readInt("Способ оплаты: 1 — карта, 2 — наличные: ", 1, 2);
        Ticket ticket = bookingService.pay(ticketId, method == 1 ? "card" : "cash");
        System.out.println("Оплачено: " + ticket);
    }

    private void cancelTicket() {
        int id = in.readInt("id билета: ", 1, Integer.MAX_VALUE);
        Ticket ticket = bookingService.cancel(id);
        System.out.println("Отменён: " + ticket);
    }

    // ---------------------------- Отчёты ----------------------------

    private void reportsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Отчёты ---");
            System.out.println("1. Выручка по фильмам");
            System.out.println("2. Заполненность сеансов");
            System.out.println("3. Постоянные клиенты");
            System.out.println("4. Фильмы без продаж");
            System.out.println("5. Сохранить отчёт в файл");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите пункт: ", 0, 5);
            try {
                switch (choice) {
                    case 1 -> printRevenue();
                    case 2 -> printList(reportService.occupancy(), "Сеансов нет.");
                    case 3 -> printLoyalCustomers();
                    case 4 -> printList(reportService.filmsWithoutSales(), "Все фильмы продаются.");
                    case 5 -> exportReport();
                    default -> {
                        return;
                    }
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void printLoyalCustomers() {
        int min = in.readInt("Минимум оплаченных билетов: ", 1, 1000);
        List<CustomerStats> rows = reportService.loyalCustomers(min);
        printList(rows, "Таких клиентов нет.");
    }

    private void printRevenue() {
        List<FilmRevenue> rows = reportService.revenueByFilm();
        printList(rows, "Нет данных.");
        System.out.printf("ИТОГО: %.2f сом%n", reportService.totalRevenue());
    }

    private void exportReport() {
        try {
            Path path = reportService.exportToFile(Path.of("reports", "report.txt"));
            System.out.println("Отчёт сохранён: " + path);
        } catch (IOException e) {
            System.out.println("Файл недоступен: " + e.getMessage());
        }
    }

    // ---------------------------- Помощники ----------------------------

    private void printList(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Object item : items) {
            System.out.println("  " + item);
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("Это не целое число: " + value);
        }
    }
}
