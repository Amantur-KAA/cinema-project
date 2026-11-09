package kg.alatoo.cinema.dao;

import kg.alatoo.cinema.config.Database;
import kg.alatoo.cinema.exception.DataAccessException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы customers: полный CRUD. */
public class CustomerDao {

    private static final String SELECT_CUSTOMERS =
            "SELECT id AS c_id, full_name, phone, email, is_student FROM customers ";

    /** Код ошибки PostgreSQL «нарушена уникальность» (например, такой телефон уже есть). */
    private static final String UNIQUE_VIOLATION = "23505";

    public Customer create(Customer customer) {
        String sql = "INSERT INTO customers (full_name, phone, email, is_student) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setBoolean(4, customer.isStudent());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                customer.setId(rs.getInt(1));
            }
            return customer;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new ValidationException("Клиент с телефоном " + customer.getPhone() + " уже есть");
            }
            throw new DataAccessException("Не удалось добавить клиента: " + e.getMessage(), e);
        }
    }

    public Customer findById(int id) {
        List<Customer> list = query(SELECT_CUSTOMERS + "WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Customer> findAll() {
        return query(SELECT_CUSTOMERS + "ORDER BY id", null);
    }

    /** Поиск по части имени или телефона. */
    public List<Customer> search(String text) {
        List<Customer> result = new ArrayList<>();
        String sql = SELECT_CUSTOMERS + "WHERE full_name ILIKE ? OR phone LIKE ? ORDER BY full_name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + text + "%");
            ps.setString(2, "%" + text + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске клиентов: " + e.getMessage(), e);
        }
        return result;
    }

    public void update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, is_student = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setBoolean(4, customer.isStudent());
            ps.setInt(5, customer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new ValidationException("Клиент с телефоном " + customer.getPhone() + " уже есть");
            }
            throw new DataAccessException("Не удалось обновить клиента: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить клиента: " + e.getMessage(), e);
        }
    }

    private List<Customer> query(String sql, Integer param) {
        List<Customer> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при чтении клиентов: " + e.getMessage(), e);
        }
        return result;
    }

    static Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("c_id"), rs.getString("full_name"), rs.getString("phone"),
                rs.getString("email"), rs.getBoolean("is_student"));
    }
}
