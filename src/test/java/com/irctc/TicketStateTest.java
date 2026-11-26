package com.irctc;

import com.irctc.model.Passenger;
import com.irctc.model.Ticket;
import com.irctc.model.Train;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import com.irctc.model.enums.TicketStatus;
import com.irctc.pattern.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class TicketStateTest {

    private Ticket ticket;
    private Train train;

    @BeforeEach
    void setUp() {
        train = new Train();
        train.setTrainNo("12627");
        train.setTrainName("Karnataka Express");
        train.setDepartureTime(LocalTime.of(19, 20));

        ticket = new Ticket("PNR1234567890", train, new Passenger(), LocalDate.now().plusDays(5),
                ClassType.THIRD_AC, QuotaType.GENERAL, 1200.0);
    }

    @Test
    @DisplayName("State Pattern: ConfirmedState allows cancellation and calculates tiered refund")
    void testConfirmedState() {
        ticket.setStatus(TicketStatus.CONFIRMED);
        TicketState state = ticket.getState();

        assertTrue(state instanceof ConfirmedState);
        assertEquals(TicketStatus.CONFIRMED, state.getStatus());
        assertTrue(ticket.canBeCancelled());
        assertTrue(ticket.canDownloadPdf());
        assertFalse(ticket.canPromote());

        // > 48 hours should give 80% refund (20% cancellation fee)
        double refund = ticket.getRefundAmount();
        assertEquals(960.0, refund, 0.01);
    }

    @Test
    @DisplayName("State Pattern: WaitingState gives refund minus clerkage fee and allows promotion")
    void testWaitingState() {
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setWaitlistNumber(5);

        TicketState state = ticket.getState();
        assertTrue(state instanceof WaitingState);
        assertTrue(ticket.canBeCancelled());
        assertTrue(ticket.canPromote());

        // Waitlist gets fare - Rs 60 clerkage fee per passenger
        assertEquals(1140.0, ticket.getRefundAmount(), 0.01);
    }

    @Test
    @DisplayName("State Pattern: RacState allows promotion and gives refund minus clerkage fee")
    void testRacState() {
        ticket.setStatus(TicketStatus.RAC);
        ticket.setRacNumber(2);

        TicketState state = ticket.getState();
        assertTrue(state instanceof RacState);
        assertTrue(ticket.canBeCancelled());
        assertTrue(ticket.canPromote());
        assertEquals(1140.0, ticket.getRefundAmount(), 0.01);
    }

    @Test
    @DisplayName("State Pattern: CancelledState prevents further cancellations or PDF downloads")
    void testCancelledState() {
        ticket.setStatus(TicketStatus.CANCELLED);

        TicketState state = ticket.getState();
        assertTrue(state instanceof CancelledState);
        assertFalse(ticket.canBeCancelled());
        assertFalse(ticket.canDownloadPdf());
        assertFalse(ticket.canPromote());
        assertEquals(0.0, ticket.getRefundAmount(), 0.01);
    }

    @Test
    @DisplayName("State Pattern Factory: returns correct state instances")
    void testStateFactory() {
        assertTrue(TicketStateFactory.getState(TicketStatus.CONFIRMED) instanceof ConfirmedState);
        assertTrue(TicketStateFactory.getState(TicketStatus.WAITING) instanceof WaitingState);
        assertTrue(TicketStateFactory.getState(TicketStatus.RAC) instanceof RacState);
        assertTrue(TicketStateFactory.getState(TicketStatus.CANCELLED) instanceof CancelledState);
        assertTrue(TicketStateFactory.getState(TicketStatus.REFUNDED) instanceof RefundedState);
        assertTrue(TicketStateFactory.getState(TicketStatus.EXPIRED) instanceof ExpiredState);
    }
}
