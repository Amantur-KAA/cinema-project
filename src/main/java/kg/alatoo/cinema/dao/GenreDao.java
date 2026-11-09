package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы genres. Весь SQL жанров — только здесь. */
public class GenreDao {

    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM genres ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                genres.add(new Genre(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении жанров: " + e.getMessage(), e);
        }
        return genres;
    }

    public Genre findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM genres WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Genre(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске жанра: " + e.getMessage(), e);
        }
        return null; // не найден
    }
}
