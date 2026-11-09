package kg.alatoo.cinema.exception;

/** Объект с таким id не найден в базе. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
