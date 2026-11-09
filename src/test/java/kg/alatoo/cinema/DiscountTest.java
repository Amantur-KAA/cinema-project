package kg.alatoo.cinema;

import kg.alatoo.cinema.discount.DiscountStrategy;
import kg.alatoo.cinema.discount.FixedDiscount;
import kg.alatoo.cinema.discount.NoDiscount;
import kg.alatoo.cinema.discount.PercentDiscount;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.payment.CardPayment;
import kg.alatoo.cinema.payment.Payment;
import kg.alatoo.cinema.payment.PaymentFactory;
import kg.alatoo.cinema.payment.Refundable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты паттернов Strategy (скидки) и Factory (оплата). */
class DiscountTest {

    @Test
    void discounts() {
        DiscountStrategy none = new NoDiscount();
        DiscountStrategy student = new PercentDiscount(20);
        DiscountStrategy promo = new FixedDiscount(100);
        assertEquals(500.0, none.apply(500));
        assertEquals(400.0, student.apply(500));
        assertEquals(400.0, promo.apply(500));
        assertEquals(0.0, promo.apply(50)); // цена не уходит в минус
        assertThrows(ValidationException.class, () -> new PercentDiscount(150));
    }

    @Test
    void paymentFactory() {
        Payment card = PaymentFactory.create("card", 300);
        Payment cash = PaymentFactory.create("cash", 300);
        assertTrue(card instanceof CardPayment);
        assertTrue(card instanceof Refundable);
        assertFalse(cash instanceof Refundable);
        assertEquals("cash", cash.getMethod());
        assertThrows(ValidationException.class, () -> PaymentFactory.create("crypto", 300));
    }
}
