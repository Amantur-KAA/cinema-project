package kg.alatoo.cinema.discount;

/** Без скидки. */
public class NoDiscount implements DiscountStrategy {

    @Override
    public double apply(double price) {
        return price;
    }

    @Override
    public String getDescription() {
        return "без скидки";
    }
}
