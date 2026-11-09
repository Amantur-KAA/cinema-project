package kg.alatoo.cinema.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Настройки приложения (паттерн Singleton, неделя 7).
 * Читает файл db.properties из src/main/resources один раз.
 */
public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties properties = new Properties();

    private AppConfig() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Файл db.properties не найден в src/main/resources");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать db.properties: " + e.getMessage(), e);
        }
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUser() {
        return properties.getProperty("db.user");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }
}
