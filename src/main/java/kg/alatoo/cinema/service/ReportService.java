package kg.alatoo.cinema.service;

import kg.alatoo.cinema.dao.ReportDao;
import kg.alatoo.cinema.report.CustomerStats;
import kg.alatoo.cinema.report.FilmRevenue;
import kg.alatoo.cinema.report.ScreeningOccupancy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/** Отчёты и сохранение отчёта в текстовый файл (Files, неделя 5). */
public class ReportService {
    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public List<FilmRevenue> revenueByFilm() {
        return reportDao.revenueByFilm();
    }

    public double totalRevenue() {
        double total = 0;
        for (FilmRevenue row : reportDao.revenueByFilm()) {
            total += row.getRevenue();
        }
        return total;
    }

    public List<ScreeningOccupancy> occupancy() {
        return reportDao.occupancyByScreening();
    }

    public List<CustomerStats> loyalCustomers(int minTickets) {
        return reportDao.loyalCustomers(minTickets);
    }

    public List<String> filmsWithoutSales() {
        return reportDao.filmsWithoutSales();
    }

    /** Собирает текст отчёта и пишет его в файл. IOException — checked, поэтому throws. */
    public Path exportToFile(Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("ОТЧЁТ КИНОТЕАТРА от ").append(LocalDateTime.now().withNano(0)).append("\n\n");
        sb.append("Выручка по фильмам:\n");
        for (FilmRevenue row : revenueByFilm()) {
            sb.append("  ").append(row).append("\n");
        }
        sb.append(String.format("  ИТОГО: %.2f сом%n%n", totalRevenue()));
        sb.append("Заполненность сеансов:\n");
        for (ScreeningOccupancy row : occupancy()) {
            sb.append("  ").append(row).append("\n");
        }
        sb.append("\nФильмы без продаж: ").append(filmsWithoutSales()).append("\n");

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, sb.toString());
        return file.toAbsolutePath();
    }
}
