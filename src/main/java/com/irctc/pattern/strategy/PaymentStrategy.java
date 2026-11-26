// src/main/java/com/irctc/pattern/strategy/PaymentStrategy.java
package com.irctc.pattern.strategy;

public interface PaymentStrategy {
    boolean pay(double amount);
    String getPaymentMethod();
}