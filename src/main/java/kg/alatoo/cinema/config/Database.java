package kg.alatoo.cinema.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Выдаёт соединения с PostgreSQL. Единственное место, где вызывается DriverManager. */
public class Database {

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        return DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
    }
}
