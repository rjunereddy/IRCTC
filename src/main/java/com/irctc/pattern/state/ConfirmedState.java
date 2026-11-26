package com.irctc.pattern.state;

import com.irctc.model.Ticket;
import com.irctc.model.enums.QuotaType;
import com.irctc.model.enums.TicketStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConfirmedState implements TicketState {

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.CONFIRMED;
    }

    @Override
    public boolean canCancel(Ticket ticket) {
        if (ticket.isChartPrepared()) return false;
        if (ticket.getJourneyDate() == null || ticket.getJourneyDate().isBefore(LocalDate.now())) return false;
        if (ticket.getTrain() == null || ticket.getTrain().getDepartureTime() == null) return false;
        return true;
    }

    @Override
    public boolean canPromote(Ticket ticket) {
        return false;
    }

    @Override
    public boolean canDownloadPdf(Ticket ticket) {
        return true;
    }

    @Override
    public double calculateRefund(Ticket ticket) {
        if (ticket.getTotalFare() == null || ticket.getTotalFare() <= 0) return 0.0;

        // Confirmed Tatkal tickets have ZERO refund as per IRCTC rules
        if (ticket.getQuotaType() == QuotaType.TATKAL) {
            return 0.0;
        }

        long hours = getHoursBeforeDeparture(ticket);

        if (hours > 48) {
            // More than 48 hours: 80% refund (20% cancellation charge)
            return Math.round(ticket.getTotalFare() * 0.80 * 100.0) / 100.0;
        } else if (hours > 24) {
            // Between 24 and 48 hours: 75% refund (25% cancellation charge)
            return Math.round(ticket.getTotalFare() * 0.75 * 100.0) / 100.0;
        } else if (hours > 10) {
            // Between 10 and 24 hours: 50% refund (50% cancellation charge)
            return Math.round(ticket.getTotalFare() * 0.50 * 100.0) / 100.0;
        } else if (hours > 4) {
            // Between 4 and 10 hours: 25% refund (75% cancellation charge)
            return Math.round(ticket.getTotalFare() * 0.25 * 100.0) / 100.0;
        }
        // Within 4 hours: 0% refund
        return 0.0;
    }

    @Override
    public String getRefundExplanation(Ticket ticket) {
        if (ticket.getQuotaType() == QuotaType.TATKAL) {
            return "No refund permissible on confirmed Tatkal tickets as per IRCTC policy.";
        }

        long hours = getHoursBeforeDeparture(ticket);
        if (hours > 48) {
            return "80% refund credited (20% cancellation fee deducted for cancellation > 48h before departure).";
        } else if (hours > 24) {
            return "75% refund credited (25% cancellation fee deducted for cancellation between 24h and 48h before departure).";
        } else if (hours > 10) {
            return "50% refund credited (50% cancellation fee deducted for cancellation between 10h and 24h before departure).";
        } else if (hours > 4) {
            return "25% refund credited (75% cancellation fee deducted for cancellation between 4h and 10h before departure).";
        }
        return "No refund permissible for cancellation within 4 hours of scheduled departure as per IRCTC policy.";
    }

    private long getHoursBeforeDeparture(Ticket ticket) {
        if (ticket.getTrain() == null || ticket.getTrain().getDepartureTime() == null || ticket.getJourneyDate() == null) {
            return 72; // Default assume future date
        }
        LocalDateTime depTime = ticket.getJourneyDate().atTime(ticket.getTrain().getDepartureTime());
        return Duration.between(LocalDateTime.now(), depTime).toHours();
    }
}
