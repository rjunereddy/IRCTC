package com.irctc.pattern.state;

import com.irctc.model.enums.TicketStatus;

public class TicketStateFactory {

    private static final TicketState CONFIRMED = new ConfirmedState();
    private static final TicketState WAITING = new WaitingState();
    private static final TicketState RAC = new RacState();
    private static final TicketState CANCELLED = new CancelledState();
    private static final TicketState REFUNDED = new RefundedState();
    private static final TicketState EXPIRED = new ExpiredState();

    public static TicketState getState(TicketStatus status) {
        if (status == null) {
            return CONFIRMED;
        }
        return switch (status) {
            case CONFIRMED -> CONFIRMED;
            case WAITING -> WAITING;
            case RAC -> RAC;
            case CANCELLED -> CANCELLED;
            case REFUNDED -> REFUNDED;
            case EXPIRED -> EXPIRED;
        };
    }
}
