package kg.alatoo.cinema.discount;

import kg.alatoo.cinema.exception.ValidationException;

/** Фиксированная скидка в сомах (промокод). Цена не может стать меньше 0. */
public class FixedDiscount implements DiscountStrategy {
    private final double amount;

    public FixedDiscount(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Сумма скидки должна быть больше 0");
        }
        this.amount = amount;
    }

    @Override
    public double apply(double price) {
        return Math.max(0, price - amount);
    }

    @Override
    public String getDescription() {
        return "скидка " + amount + " сом";
    }
}
