package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

/** Simple Factory (неделя 7): по типу из базы создаёт нужный подкласс Hall. */
public class HallFactory {

    private HallFactory() {
    }

    public static Hall create(String type, int id, String name, int seats) {
        if (type == null) {
            throw new ValidationException("Тип зала не указан");
        }
        return switch (type.toUpperCase()) {
            case "STANDARD" -> new StandardHall(id, name, seats);
            case "VIP" -> new VipHall(id, name, seats);
            case "IMAX" -> new ImaxHall(id, name, seats);
            default -> throw new ValidationException("Неизвестный тип зала: " + type);
        };
    }
}
