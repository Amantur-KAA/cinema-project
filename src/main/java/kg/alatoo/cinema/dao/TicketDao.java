package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;
import kg.alatoo.cinema.model.Screening;
import kg.alatoo.cinema.model.Ticket;
import kg.alatoo.cinema.model.TicketStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы tickets. */
public class TicketDao {

    private static final String SELECT_TICKETS =
            "SELECT t.id AS t_id, t.seat_number, t.price, t.status, t.payment_method, t.sold_at, "
                    + "s.id AS s_id, s.start_time, s.base_price, "
                    + "f.id AS f_id, f.title, f.duration_min, f.age_rating, g.id AS g_id, g.name AS g_name, "
                    + "h.id AS h_id, h.name AS h_name, h.hall_type, h.seats, "
                    + "c.id AS c_id, c.full_name, c.phone, c.email, c.is_student "
                    + "FROM tickets t "
                    + "JOIN screenings s ON s.id = t.screening_id "
                    + "JOIN films f ON f.id = s.film_id "
                    + "JOIN genres g ON g.id = f.genre_id "
                    + "JOIN halls h ON h.id = s.hall_id "
                    + "JOIN customers c ON c.id = t.customer_id ";

    public Ticket create(Ticket ticket) {
        String sql = "INSERT INTO tickets (screening_id, customer_id, seat_number, price, status, payment_method, sold_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ticket.getScreening().getId());
            ps.setInt(2, ticket.getCustomer().getId());
            ps.setInt(3, ticket.getSeatNumber());
            ps.setDouble(4, ticket.getPrice());
            ps.setString(5, ticket.getStatus().name());
            ps.setString(6, ticket.getPaymentMethod());
            ps.setTimestamp(7, Timestamp.valueOf(ticket.getSoldAt()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                ticket.setId(rs.getInt(1));
            }
            return ticket;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ValidationException("Место " + ticket.getSeatNumber() + " уже занято");
            }
            throw new DataAccessException("Не удалось сохранить билет: " + e.getMessage(), e);
        }
    }

    public Ticket findById(int id) {
        List<Ticket> list = query(SELECT_TICKETS + "WHERE t.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Ticket> findAll() {
        return query(SELECT_TICKETS + "ORDER BY t.id", null);
    }

    public List<Ticket> findByCustomer(int customerId) {
        return query(SELECT_TICKETS + "WHERE c.id = ? ORDER BY s.start_time", customerId);
    }

    /** Номера занятых мест на сеансе (отменённые билеты место не занимают). */
    public List<Integer> findTakenSeats(int screeningId) {
        List<Integer> seats = new ArrayList<>();
        String sql = "SELECT seat_number FROM tickets WHERE screening_id = ? AND status <> 'CANCELLED' ORDER BY seat_number";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, screeningId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(rs.getInt("seat_number"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении мест: " + e.getMessage(), e);
        }
        return seats;
    }

    /** Сохраняет новый статус и способ оплаты (после pay() или cancel()). */
    public void updateStatus(Ticket ticket) {
        String sql = "UPDATE tickets SET status = ?, payment_method = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ticket.getStatus().name());
            ps.setString(2, ticket.getPaymentMethod());
            ps.setInt(3, ticket.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось обновить билет: " + e.getMessage(), e);
        }
    }

    private List<Ticket> query(String sql, Integer param) {
        List<Ticket> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении билетов: " + e.getMessage(), e);
        }
        return result;
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Screening screening = ScreeningDao.mapScreening(rs);
        Customer customer = CustomerDao.mapCustomer(rs);
        return new Ticket(rs.getInt("t_id"), screening, customer, rs.getInt("seat_number"), rs.getDouble("price"),
                TicketStatus.valueOf(rs.getString("status")), rs.getString("payment_method"),
                rs.getTimestamp("sold_at").toLocalDateTime());
    }
}
