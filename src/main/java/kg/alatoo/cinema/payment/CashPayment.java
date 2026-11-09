package kg.alatoo.cinema.payment;

/** Оплата наличными в кассе. Автоматического возврата нет — только в кассе. */
public class CashPayment extends Payment {

    public CashPayment(double amount) {
        super(amount);
    }

    @Override
    public void pay() {
        System.out.println("Оплата наличными: " + amount + " сом. Выдайте чек.");
    }

    @Override
    public String getMethod() {
        return "cash";
    }
}
