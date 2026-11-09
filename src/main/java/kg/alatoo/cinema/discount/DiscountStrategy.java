package kg.alatoo.cinema.discount;

/**
 * Паттерн Strategy (неделя 7): алгоритм скидки вынесен в отдельный объект.
 * Новую скидку добавляем новым классом, не трогая старый код (OCP).
 */
public interface DiscountStrategy {
    double apply(double price);

    String getDescription();
}
