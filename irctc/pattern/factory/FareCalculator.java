// src/main/java/com/irctc/pattern/factory/FareCalculator.java
package com.irctc.pattern.factory;

import com.irctc.model.Ticket;

public interface FareCalculator {
    double calculateFare(Ticket ticket);
}