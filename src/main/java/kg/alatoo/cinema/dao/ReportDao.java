package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.report.CustomerStats;
import kg.alatoo.cinema.report.FilmRevenue;
import kg.alatoo.cinema.report.ScreeningOccupancy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Отчёты: JOIN, агрегаты (COUNT, SUM), GROUP BY, HAVING и подзапрос (неделя 12). */
public class ReportDao {

    /** Выручка и число проданных билетов по каждому фильму. LEFT JOIN — чтобы видеть и фильмы без продаж. */
    public List<FilmRevenue> revenueByFilm() {
        String sql = "SELECT f.title, COUNT(t.id) AS tickets, COALESCE(SUM(t.price), 0) AS revenue "
                + "FROM films f "
                + "LEFT JOIN screenings s ON s.film_id = f.id "
                + "LEFT JOIN tickets t ON t.screening_id = s.id AND t.status = 'PAID' "
                + "GROUP BY f.id, f.title "
                + "ORDER BY revenue DESC, f.title";
        List<FilmRevenue> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new FilmRevenue(rs.getString("title"), rs.getInt("tickets"), rs.getDouble("revenue")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка отчёта по выручке: " + e.getMessage(), e);
        }
        return result;
    }

    /** Сколько мест занято на каждом сеансе. */
    public List<ScreeningOccupancy> occupancyByScreening() {
        String sql = "SELECT s.id, f.title, h.name AS hall_name, h.seats, COUNT(t.id) AS sold "
                + "FROM screenings s "
                + "JOIN films f ON f.id = s.film_id "
                + "JOIN halls h ON h.id = s.hall_id "
                + "LEFT JOIN tickets t ON t.screening_id = s.id AND t.status <> 'CANCELLED' "
                + "GROUP BY s.id, f.title, h.name, h.seats "
                + "ORDER BY s.start_time";
        List<ScreeningOccupancy> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new ScreeningOccupancy(rs.getInt("id"), rs.getString("title"),
                        rs.getString("hall_name"), rs.getInt("seats"), rs.getInt("sold")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка отчёта по заполненности: " + e.getMessage(), e);
        }
        return result;
    }

    /** Постоянные клиенты: купили не меньше minTickets оплаченных билетов (HAVING). */
    public List<CustomerStats> loyalCustomers(int minTickets) {
        String sql = "SELECT c.full_name, COUNT(t.id) AS tickets, SUM(t.price) AS total "
                + "FROM customers c "
                + "JOIN tickets t ON t.customer_id = c.id "
                + "WHERE t.status = 'PAID' "
                + "GROUP BY c.id, c.full_name "
                + "HAVING COUNT(t.id) >= ? "
                + "ORDER BY total DESC";
        List<CustomerStats> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, minTickets);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new CustomerStats(rs.getString("full_name"), rs.getInt("tickets"),
                            rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка отчёта по клиентам: " + e.getMessage(), e);
        }
        return result;
    }

    /** Фильмы, на которые не продано ни одного билета (подзапрос NOT IN). */
    public List<String> filmsWithoutSales() {
        String sql = "SELECT title FROM films "
                + "WHERE id NOT IN ("
                + "    SELECT s.film_id FROM screenings s "
                + "    JOIN tickets t ON t.screening_id = s.id "
                + "    WHERE t.status = 'PAID'"
                + ") ORDER BY title";
        List<String> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка отчёта: " + e.getMessage(), e);
        }
        return result;
    }
}
