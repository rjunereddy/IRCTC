package com.irctc.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class UPIPayment implements PaymentStrategy {
    
    private String upiId;
    
    public UPIPayment withUpiId(String upiId) {
        this.upiId = upiId;
        return this;
    }
    
    @Override
    public boolean pay(double amount) {
        System.out.println("Processing UPI payment of ₹" + amount);
        System.out.println("UPI ID: " + upiId);
        return true;
    }
    
    @Override
    public String getPaymentMethod() {
        return "UPI";
    }

    public String getUpiId() { return upiId; }
}
