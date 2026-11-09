package kg.alatoo.cinema.discount;

import kg.alatoo.cinema.exception.ValidationException;

/** Скидка процентом, например студенческая 20%. */
public class PercentDiscount implements DiscountStrategy {
    private final int percent;

    public PercentDiscount(int percent) {
        if (percent <= 0 || percent >= 100) {
            throw new ValidationException("Процент скидки должен быть от 1 до 99");
        }
        this.percent = percent;
    }

    @Override
    public double apply(double price) {
        return Math.round(price * (100 - percent)) / 100.0;
    }

    @Override
    public String getDescription() {
        return "скидка " + percent + "%";
    }
}
