package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.model.Hall;
import kg.alatoo.cinema.model.HallFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы halls. Объекты создаёт HallFactory по столбцу hall_type. */
public class HallDao {

    private static final String SELECT_HALLS = "SELECT id AS h_id, name AS h_name, hall_type, seats FROM halls ";

    public List<Hall> findAll() {
        List<Hall> halls = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_HALLS + "ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                halls.add(mapHall(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении залов: " + e.getMessage(), e);
        }
        return halls;
    }

    public Hall findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_HALLS + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapHall(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске зала: " + e.getMessage(), e);
        }
        return null; // не найден
    }

    static Hall mapHall(ResultSet rs) throws SQLException {
        return HallFactory.create(rs.getString("hall_type"), rs.getInt("h_id"), rs.getString("h_name"),
                rs.getInt("seats"));
    }
}
