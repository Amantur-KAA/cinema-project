package kg.alatoo.cinema.config;

import kg.alatoo.cinema.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * При первом запуске создаёт таблицы (schema.sql) и заполняет их тестовыми данными (data.sql).
 * Здесь нет данных от пользователя, поэтому обычный Statement безопасен.
 */
public class DatabaseInitializer {

    public void init() {
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement()) {
            st.execute(readResource("schema.sql"));

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM genres")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.execute(readResource("data.sql"));
                    System.out.println("База пустая: добавлены тестовые данные.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось подключиться к базе или создать таблицы: " + e.getMessage(), e);
        }
    }

    private String readResource(String name) {
        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException("Файл " + name + " не найден в resources");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + name, e);
        }
    }
}
