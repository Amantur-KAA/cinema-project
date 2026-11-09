package kg.alatoo.cinema.exception;

/**
 * Неверные данные: пустое имя, отрицательная цена, занятое место и т.д.
 * Unchecked-исключение (extends RuntimeException), поэтому throws писать не нужно.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
