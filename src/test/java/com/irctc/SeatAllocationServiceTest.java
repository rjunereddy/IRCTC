package com.irctc;

import com.irctc.model.PassengerDetail;
import com.irctc.model.Ticket;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.TicketStatus;
import com.irctc.service.SeatAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SeatAllocationServiceTest {

    private SeatAllocationService service;

    @BeforeEach
    void setUp() {
        service = new SeatAllocationService();
    }

    @Test
    @DisplayName("Seat Allocation: Correct coach prefixes for all travel classes")
    void testCoachPrefixes() {
        assertEquals("H", service.getCoachPrefix(ClassType.FIRST_AC));
        assertEquals("A", service.getCoachPrefix(ClassType.SECOND_AC));
        assertEquals("B", service.getCoachPrefix(ClassType.THIRD_AC));
        assertEquals("S", service.getCoachPrefix(ClassType.SLEEPER));
        assertEquals("C", service.getCoachPrefix(ClassType.AC_CHAIR_CAR));
        assertEquals("D", service.getCoachPrefix(ClassType.SECOND_SITTING));
    }

    @Test
    @DisplayName("Seat Allocation: Standard Indian Railway 8-Berth layout mapping (SL/3AC)")
    void testBerthTypesSleeper() {
        // mod 8: 1 Lower, 2 Middle, 3 Upper, 4 Lower, 5 Middle, 6 Upper, 7 Side Lower, 0 Side Upper
        assertEquals("Lower", service.getBerthType(1, ClassType.SLEEPER));
        assertEquals("Middle", service.getBerthType(2, ClassType.SLEEPER));
        assertEquals("Upper", service.getBerthType(3, ClassType.SLEEPER));
        assertEquals("Lower", service.getBerthType(4, ClassType.SLEEPER));
        assertEquals("Middle", service.getBerthType(5, ClassType.SLEEPER));
        assertEquals("Upper", service.getBerthType(6, ClassType.SLEEPER));
        assertEquals("Side Lower", service.getBerthType(7, ClassType.SLEEPER));
        assertEquals("Side Upper", service.getBerthType(8, ClassType.SLEEPER));
    }

    @Test
    @DisplayName("Seat Allocation: Chair Car Window vs Aisle layout")
    void testBerthTypesChairCar() {
        assertEquals("Aisle", service.getBerthType(1, ClassType.AC_CHAIR_CAR));
        assertEquals("Aisle", service.getBerthType(2, ClassType.AC_CHAIR_CAR));
        assertEquals("Window", service.getBerthType(3, ClassType.AC_CHAIR_CAR));
        assertEquals("Window", service.getBerthType(6, ClassType.AC_CHAIR_CAR));
    }

    @Test
    @DisplayName("Seat Allocation: Multi-passenger booking receives consecutive seats")
    void testConsecutiveSeatAllocation() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setClassType(ClassType.THIRD_AC);

        List<PassengerDetail> passengers = new ArrayList<>();
        passengers.add(new PassengerDetail("Passenger 1", 30, "MALE"));
        passengers.add(new PassengerDetail("Passenger 2", 28, "FEMALE"));
        passengers.add(new PassengerDetail("Passenger 3", 5, "MALE"));
        ticket.setAllPassengers(passengers);

        // Allocate with 10 existing confirmed seats
        service.allocateSeats(ticket, 10);

        assertEquals("B1", ticket.getCoachNumber());
        assertEquals(3, ticket.getSeatNumbers().size());
        assertEquals("B1-11", ticket.getSeatNumbers().get(0));
        assertEquals("B1-12", ticket.getSeatNumbers().get(1));
        assertEquals("B1-13", ticket.getSeatNumbers().get(2));
    }

    @Test
    @DisplayName("Seat Allocation: RAC passengers receive shared side lower berths")
    void testRacAllocation() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RAC);
        ticket.setRacNumber(3);
        ticket.setClassType(ClassType.SLEEPER);

        List<PassengerDetail> passengers = new ArrayList<>();
        passengers.add(new PassengerDetail("RAC Passenger", 40, "MALE"));
        ticket.setAllPassengers(passengers);

        service.allocateSeats(ticket);

        assertTrue(passengers.get(0).getBerthAllotted().contains("RAC-3"));
        assertTrue(passengers.get(0).getBerthAllotted().contains("Shared Side Lower"));
    }
}
