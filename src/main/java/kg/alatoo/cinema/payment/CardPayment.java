package kg.alatoo.cinema.payment;

/** Оплата картой. Деньги можно вернуть на карту автоматически, поэтому Refundable. */
public class CardPayment extends Payment implements Refundable {

    public CardPayment(double amount) {
        super(amount);
    }

    @Override
    public void pay() {
        System.out.println("Оплата картой: " + amount + " сом. Платёж принят.");
    }

    @Override
    public String getMethod() {
        return "card";
    }

    @Override
    public void refund(double amount) {
        System.out.println("Возврат на карту: " + amount + " сом.");
    }
}
