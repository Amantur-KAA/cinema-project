package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Hall;
import kg.alatoo.cinema.model.Screening;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы screenings. Чтение — через JOIN четырёх таблиц. */
public class ScreeningDao {

    static final String SELECT_SCREENINGS =
            "SELECT s.id AS s_id, s.start_time, s.base_price, "
                    + "f.id AS f_id, f.title, f.duration_min, f.age_rating, g.id AS g_id, g.name AS g_name, "
                    + "h.id AS h_id, h.name AS h_name, h.hall_type, h.seats "
                    + "FROM screenings s "
                    + "JOIN films f ON f.id = s.film_id "
                    + "JOIN genres g ON g.id = f.genre_id "
                    + "JOIN halls h ON h.id = s.hall_id ";

    public Screening create(Screening screening) {
        String sql = "INSERT INTO screenings (film_id, hall_id, start_time, base_price) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, screening.getFilm().getId());
            ps.setInt(2, screening.getHall().getId());
            ps.setTimestamp(3, Timestamp.valueOf(screening.getStartTime()));
            ps.setDouble(4, screening.getBasePrice());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                screening.setId(rs.getInt(1));
            }
            return screening;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить сеанс: " + e.getMessage(), e);
        }
    }

    public Screening findById(int id) {
        List<Screening> list = query(SELECT_SCREENINGS + "WHERE s.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Screening> findAll() {
        return query(SELECT_SCREENINGS + "ORDER BY s.start_time", null);
    }

    /** Сеансы, которые ещё не начались. */
    public List<Screening> findUpcoming() {
        return query(SELECT_SCREENINGS + "WHERE s.start_time > NOW() ORDER BY s.start_time", null);
    }

    public List<Screening> findByHall(int hallId) {
        return query(SELECT_SCREENINGS + "WHERE h.id = ? ORDER BY s.start_time", hallId);
    }

    /** Поиск сеансов по части названия фильма (JOIN films). */
    public List<Screening> searchByFilmTitle(String text) {
        List<Screening> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SCREENINGS + "WHERE f.title ILIKE ? ORDER BY s.start_time")) {
            ps.setString(1, "%" + text + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapScreening(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске сеансов: " + e.getMessage(), e);
        }
        return result;
    }

    public void update(Screening screening) {
        String sql = "UPDATE screenings SET film_id = ?, hall_id = ?, start_time = ?, base_price = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, screening.getFilm().getId());
            ps.setInt(2, screening.getHall().getId());
            ps.setTimestamp(3, Timestamp.valueOf(screening.getStartTime()));
            ps.setDouble(4, screening.getBasePrice());
            ps.setInt(5, screening.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось обновить сеанс: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM screenings WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить сеанс: " + e.getMessage(), e);
        }
    }

    private List<Screening> query(String sql, Integer param) {
        List<Screening> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapScreening(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении сеансов: " + e.getMessage(), e);
        }
        return result;
    }

    static Screening mapScreening(ResultSet rs) throws SQLException {
        Film film = FilmDao.mapFilm(rs);
        Hall hall = HallDao.mapHall(rs);
        return new Screening(rs.getInt("s_id"), film, hall, rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getDouble("base_price"));
    }
}
