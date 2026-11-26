package com.irctc;

import com.irctc.dto.BookingRequest;
import com.irctc.model.Passenger;
import com.irctc.model.Train;
import com.irctc.model.enums.QuotaType;
import com.irctc.model.enums.TicketStatus;
import com.irctc.pattern.chain.BookingContext;
import com.irctc.pattern.chain.BookingValidationPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingValidationChainTest {

    private BookingValidationPipeline pipeline;
    private Train train;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        pipeline = new BookingValidationPipeline();

        train = new Train();
        train.setTrainNo("12951");
        train.setTrainName("Mumbai Rajdhani");
        train.setTotalSeats(100);
        train.setRacCapacity(20);
        train.setWaitlistCapacity(50);

        passenger = new Passenger();
        passenger.setUsername("testuser");
    }

    @Test
    @DisplayName("Chain of Responsibility: Rejects booking with empty passenger list")
    void testEmptyPassengerList() {
        BookingRequest request = new BookingRequest();
        request.setPassengers(new ArrayList<>());
        request.setPaymentMethod("WALLET");

        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(2),
                true, 5000.0, 10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> pipeline.execute(context));
    }

    @Test
    @DisplayName("Chain of Responsibility: Rejects Senior Citizen quota if age < 60")
    void testSeniorCitizenAgeCheck() {
        BookingRequest request = new BookingRequest();
        request.setQuotaType(QuotaType.SENIOR_CITIZEN);
        request.setPaymentMethod("WALLET");

        BookingRequest.PassengerInfo p = new BookingRequest.PassengerInfo();
        p.setName("John Doe");
        p.setAge(45); // < 60
        p.setGender("MALE");
        request.setPassengers(List.of(p));

        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(2),
                true, 5000.0, 10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> pipeline.execute(context));
    }

    @Test
    @DisplayName("Chain of Responsibility: Rejects Tatkal quota when not enabled")
    void testTatkalNotEnabled() {
        BookingRequest request = new BookingRequest();
        request.setQuotaType(QuotaType.TATKAL);
        request.setPaymentMethod("WALLET");

        BookingRequest.PassengerInfo p = new BookingRequest.PassengerInfo();
        p.setName("Alice");
        p.setAge(28);
        p.setGender("FEMALE");
        request.setPassengers(List.of(p));

        // tatkalEnabled = false
        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(1),
                false, 5000.0, 10, 0, 0);

        assertThrows(IllegalStateException.class, () -> pipeline.execute(context));
    }

    @Test
    @DisplayName("Chain of Responsibility: Assigns CONFIRMED when seats are available")
    void testConfirmedAssignment() {
        BookingRequest request = new BookingRequest();
        request.setQuotaType(QuotaType.GENERAL);
        request.setPaymentMethod("WALLET");

        BookingRequest.PassengerInfo p = new BookingRequest.PassengerInfo();
        p.setName("Bob");
        p.setAge(32);
        p.setGender("MALE");
        request.setPassengers(List.of(p));

        // 10 booked out of 100
        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(3),
                false, 5000.0, 10, 0, 0);

        pipeline.execute(context);
        assertEquals(TicketStatus.CONFIRMED, context.getAssignedStatus());
    }

    @Test
    @DisplayName("Chain of Responsibility: Cascades to RAC when confirmed seats are full")
    void testRacAssignment() {
        BookingRequest request = new BookingRequest();
        request.setQuotaType(QuotaType.GENERAL);
        request.setPaymentMethod("WALLET");

        BookingRequest.PassengerInfo p = new BookingRequest.PassengerInfo();
        p.setName("Charlie");
        p.setAge(30);
        p.setGender("MALE");
        request.setPassengers(List.of(p));

        // 100 booked out of 100 (confirmed full), 5 RAC booked out of 20
        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(3),
                false, 5000.0, 100, 5, 0);

        pipeline.execute(context);
        assertEquals(TicketStatus.RAC, context.getAssignedStatus());
        assertEquals(6, context.getRacNumber());
    }

    @Test
    @DisplayName("Chain of Responsibility: Cascades to WAITING when confirmed and RAC are full")
    void testWaitingAssignment() {
        BookingRequest request = new BookingRequest();
        request.setQuotaType(QuotaType.GENERAL);
        request.setPaymentMethod("WALLET");

        BookingRequest.PassengerInfo p = new BookingRequest.PassengerInfo();
        p.setName("David");
        p.setAge(25);
        p.setGender("MALE");
        request.setPassengers(List.of(p));

        // 100 confirmed, 20 RAC (full), 8 WL booked
        BookingContext context = new BookingContext(request, passenger, train, LocalDate.now().plusDays(3),
                false, 5000.0, 100, 20, 8);

        pipeline.execute(context);
        assertEquals(TicketStatus.WAITING, context.getAssignedStatus());
        assertEquals(9, context.getWaitlistNumber());
    }
}
