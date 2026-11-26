package com.irctc;

import com.irctc.model.Ticket;
import com.irctc.model.Train;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import com.irctc.pattern.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FareCalculatorFactoryTest {

    private FareCalculatorFactory factory;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        GeneralFareCalculator gen = new GeneralFareCalculator();
        TatkalFareCalculator tatkal = new TatkalFareCalculator();
        SeniorCitizenFareCalculator senior = new SeniorCitizenFareCalculator();

        factory = new FareCalculatorFactory(gen, tatkal, senior);

        Train train = new Train();
        train.setTrainNo("12001");
        train.setTrainName("Bhopal Shatabdi");
        Map<String, Double> fares = new HashMap<>();
        fares.put(ClassType.AC_CHAIR_CAR.name(), 1000.0);
        train.setFareStructure(fares);

        ticket = new Ticket();
        ticket.setTrain(train);
        ticket.setClassType(ClassType.AC_CHAIR_CAR);
        ticket.setAllPassengers(new java.util.ArrayList<>());
        ticket.getAllPassengers().add(new com.irctc.model.PassengerDetail("Test", 30, "MALE"));
    }

    @Test
    @DisplayName("Factory Pattern: returns correct calculator based on QuotaType")
    void testFactoryReturnsCorrectCalculator() {
        assertTrue(factory.getCalculator(QuotaType.GENERAL) instanceof GeneralFareCalculator);
        assertTrue(factory.getCalculator(QuotaType.TATKAL) instanceof TatkalFareCalculator);
        assertTrue(factory.getCalculator(QuotaType.SENIOR_CITIZEN) instanceof SeniorCitizenFareCalculator);
    }

    @Test
    @DisplayName("Fare Calculation: General Quota base fare calculation")
    void testGeneralFareCalculation() {
        FareCalculator calc = factory.getCalculator(QuotaType.GENERAL);
        double total = calc.calculateFare(ticket);

        // Base 1000 + 5% GST = 1050
        assertEquals(1050.0, total, 0.01);
        assertEquals(1000.0, ticket.getBaseFare(), 0.01);
        assertEquals(50.0, ticket.getGstAmount(), 0.01);
    }

    @Test
    @DisplayName("Fare Calculation: Tatkal Quota adds 30% premium surcharge")
    void testTatkalFareCalculation() {
        FareCalculator calc = factory.getCalculator(QuotaType.TATKAL);
        double total = calc.calculateFare(ticket);

        // Base 1000 * 1.30 = 1300 + 5% GST (65) = 1365.0
        assertEquals(1365.0, total, 0.01);
        assertEquals(1300.0, ticket.getBaseFare(), 0.01);
        assertEquals(65.0, ticket.getGstAmount(), 0.01);
    }

    @Test
    @DisplayName("Fare Calculation: Senior Citizen Quota applies 50% concession")
    void testSeniorCitizenFareCalculation() {
        FareCalculator calc = factory.getCalculator(QuotaType.SENIOR_CITIZEN);
        double total = calc.calculateFare(ticket);

        // Base 1000 * 0.50 = 500 + 5% GST (25) = 525.0
        assertEquals(525.0, total, 0.01);
        assertEquals(500.0, ticket.getBaseFare(), 0.01);
        assertEquals(25.0, ticket.getGstAmount(), 0.01);
    }
}
