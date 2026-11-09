package kg.alatoo.cinema.payment;

/** Оплата (абстрактный класс, неделя 4). Каждый способ оплаты сам решает, как платить. */
public abstract class Payment {
    protected double amount;

    protected Payment(double amount) {
        this.amount = amount;
    }

    public abstract void pay();

    /** Код способа оплаты, как он хранится в базе: card или cash. */
    public abstract String getMethod();

    public double getAmount() {
        return amount;
    }
}
