package kg.alatoo.cinema.payment;

import kg.alatoo.cinema.exception.ValidationException;

/** Simple Factory из пособия (неделя 7). */
public class PaymentFactory {

    private PaymentFactory() {
    }

    public static Payment create(String type, double amount) {
        if (type == null) {
            throw new ValidationException("Не указан способ оплаты");
        }
        return switch (type) {
            case "card" -> new CardPayment(amount);
            case "cash" -> new CashPayment(amount);
            default -> throw new ValidationException("Неизвестный способ оплаты: " + type);
        };
    }
}
