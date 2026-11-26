package com.irctc.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class CreditCardPayment implements PaymentStrategy {
    
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    
    public CreditCardPayment withDetails(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        return this;
    }
    
    @Override
    public boolean pay(double amount) {
        System.out.println("Processing Credit Card payment of ₹" + amount);
        System.out.println("Card: " + maskCardNumber(cardNumber) + " (Exp: " + expiryDate + ")");
        return true;
    }
    
    @Override
    public String getPaymentMethod() {
        return "Credit Card";
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String clean = cardNumber.replaceAll("\\s+", "");
        if (clean.length() < 4) return "****";
        return "****-****-****-" + clean.substring(clean.length() - 4);
    }

    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
}
