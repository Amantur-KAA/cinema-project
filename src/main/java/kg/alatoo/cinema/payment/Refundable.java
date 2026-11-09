package kg.alatoo.cinema.payment;

/** Интерфейс-контракт «умею возвращать деньги» (неделя 4). */
public interface Refundable {
    void refund(double amount);

    default void printPolicy() {
        System.out.println("Возврат денег за билет возможен до начала сеанса.");
    }
}
