package com.irctc;

import com.irctc.pattern.strategy.CreditCardPayment;
import com.irctc.pattern.strategy.NetBankingPayment;
import com.irctc.pattern.strategy.PaymentStrategy;
import com.irctc.pattern.strategy.UPIPayment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentStrategyTest {

    @Test
    @DisplayName("Strategy Pattern: CreditCardPayment processes and masks card number")
    void testCreditCardStrategy() {
        CreditCardPayment card = new CreditCardPayment();
        card.withDetails("4111222233334444", "12/28", "123");

        assertEquals("Credit Card", card.getPaymentMethod());
        assertEquals("4111222233334444", card.getCardNumber());
        assertEquals("12/28", card.getExpiryDate());
        assertEquals("123", card.getCvv());

        boolean result = card.pay(1500.0);
        assertTrue(result);
    }

    @Test
    @DisplayName("Strategy Pattern: UPIPayment processes VPA handle")
    void testUpiStrategy() {
        UPIPayment upi = new UPIPayment();
        upi.withUpiId("traveler@okhdfcbank");

        assertEquals("UPI", upi.getPaymentMethod());
        assertEquals("traveler@okhdfcbank", upi.getUpiId());

        boolean result = upi.pay(850.0);
        assertTrue(result);
    }

    @Test
    @DisplayName("Strategy Pattern: NetBankingPayment processes bank selection")
    void testNetBankingStrategy() {
        NetBankingPayment nb = new NetBankingPayment();
        nb.withDetails("State Bank of India", "USER_SBI_01");

        assertEquals("Net Banking", nb.getPaymentMethod());
        assertEquals("State Bank of India", nb.getBankName());
        assertEquals("USER_SBI_01", nb.getUserId());

        boolean result = nb.pay(2200.0);
        assertTrue(result);
    }
}
