package kg.alatoo.cinema;

import kg.alatoo.cinema.config.DatabaseInitializer;
import kg.alatoo.cinema.dao.CustomerDao;
import kg.alatoo.cinema.dao.FilmDao;
import kg.alatoo.cinema.dao.GenreDao;
import kg.alatoo.cinema.dao.HallDao;
import kg.alatoo.cinema.dao.ReportDao;
import kg.alatoo.cinema.dao.ScreeningDao;
import kg.alatoo.cinema.dao.TicketDao;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.service.BookingService;
import kg.alatoo.cinema.service.CustomerService;
import kg.alatoo.cinema.service.FilmService;
import kg.alatoo.cinema.service.ReportService;
import kg.alatoo.cinema.service.ScreeningService;
import kg.alatoo.cinema.ui.ConsoleMenu;
import kg.alatoo.cinema.ui.InputReader;

import java.util.Scanner;

/** Точка входа. Здесь только сборка объектов: DAO → сервисы → меню. */
public class Main {

    public static void main(String[] args) {
        try {
            new DatabaseInitializer().init();
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            System.out.println("Проверьте, что PostgreSQL запущен, база cinema_db создана, "
                    + "а логин и пароль в src/main/resources/db.properties верные.");
            return;
        }

        FilmDao filmDao = new FilmDao();
        GenreDao genreDao = new GenreDao();
        HallDao hallDao = new HallDao();
        ScreeningDao screeningDao = new ScreeningDao();
        CustomerDao customerDao = new CustomerDao();
        TicketDao ticketDao = new TicketDao();

        FilmService filmService = new FilmService(filmDao, genreDao);
        ScreeningService screeningService = new ScreeningService(screeningDao, filmDao, hallDao);
        CustomerService customerService = new CustomerService(customerDao);
        BookingService bookingService = new BookingService(ticketDao, screeningDao, customerDao);
        ReportService reportService = new ReportService(new ReportDao());

        Scanner scanner = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(new InputReader(scanner), filmService, screeningService,
                customerService, bookingService, reportService);
        menu.run();
    }
}
