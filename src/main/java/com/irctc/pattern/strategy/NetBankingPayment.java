package com.irctc.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class NetBankingPayment implements PaymentStrategy {
    
    private String bankName;
    private String userId;
    
    public NetBankingPayment withDetails(String bankName, String userId) {
        this.bankName = bankName;
        this.userId = userId;
        return this;
    }
    
    @Override
    public boolean pay(double amount) {
        System.out.println("Processing Net Banking payment of ₹" + amount);
        System.out.println("Bank: " + bankName + ", User: " + userId);
        return true;
    }
    
    @Override
    public String getPaymentMethod() {
        return "Net Banking";
    }

    public String getBankName() { return bankName; }
    public String getUserId() { return userId; }
}
