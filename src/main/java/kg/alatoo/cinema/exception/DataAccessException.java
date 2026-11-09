package kg.alatoo.cinema.exception;

/**
 * Ошибка при работе с базой данных.
 * DAO превращает checked SQLException в это unchecked-исключение,
 * чтобы сервис и меню не зависели от JDBC.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
