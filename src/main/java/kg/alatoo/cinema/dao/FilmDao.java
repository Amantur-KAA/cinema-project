package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы films: полный CRUD (create, read, update, delete). */
public class FilmDao {

    /** SELECT с JOIN, чтобы сразу получить и жанр фильма. */
    private static final String SELECT_FILMS =
            "SELECT f.id AS f_id, f.title, f.duration_min, f.age_rating, g.id AS g_id, g.name AS g_name "
                    + "FROM films f JOIN genres g ON g.id = f.genre_id ";

    public Film create(Film film) {
        String sql = "INSERT INTO films (title, duration_min, age_rating, genre_id) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, film.getTitle());
            ps.setInt(2, film.getDurationMinutes());
            ps.setInt(3, film.getAgeRating());
            ps.setInt(4, film.getGenre().getId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                film.setId(rs.getInt(1));
            }
            return film;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить фильм: " + e.getMessage(), e);
        }
    }

    public Film findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_FILMS + "WHERE f.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFilm(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске фильма: " + e.getMessage(), e);
        }
        return null; // не найден
    }

    public List<Film> findAll() {
        return query(SELECT_FILMS + "ORDER BY f.id", null);
    }

    /** Поиск по части названия (без учёта регистра). */
    public List<Film> searchByTitle(String text) {
        return query(SELECT_FILMS + "WHERE f.title ILIKE ? ORDER BY f.title", "%" + text + "%");
    }

    public void update(Film film) {
        String sql = "UPDATE films SET title = ?, duration_min = ?, age_rating = ?, genre_id = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, film.getTitle());
            ps.setInt(2, film.getDurationMinutes());
            ps.setInt(3, film.getAgeRating());
            ps.setInt(4, film.getGenre().getId());
            ps.setInt(5, film.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось обновить фильм: " + e.getMessage(), e);
        }
    }

    /** Удаление. Сеансы и билеты фильма удалятся каскадно (ON DELETE CASCADE). */
    public boolean delete(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM films WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить фильм: " + e.getMessage(), e);
        }
    }

    private List<Film> query(String sql, String param) {
        List<Film> films = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapFilm(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении фильмов: " + e.getMessage(), e);
        }
        return films;
    }

    /** ResultSet сразу превращаем в объект (наружу ResultSet не отдаём). */
    static Film mapFilm(ResultSet rs) throws SQLException {
        Genre genre = new Genre(rs.getInt("g_id"), rs.getString("g_name"));
        return new Film(rs.getInt("f_id"), rs.getString("title"), rs.getInt("duration_min"),
                rs.getInt("age_rating"), genre);
    }
}
