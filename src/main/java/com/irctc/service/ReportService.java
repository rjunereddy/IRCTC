package com.irctc.service;

import com.irctc.dto.ReportCriteria;
import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;
import com.irctc.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;

    public ReportService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Revenue Report: sums totalFare of all CONFIRMED + RAC tickets in the date range.
     * Works for both wallet and card payments since totalFare is always set on the Ticket.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateRevenueReport(ReportCriteria criteria) {
        Map<String, Object> report = new LinkedHashMap<>();

        List<Ticket> confirmed = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(
                criteria.getStartDate(), criteria.getEndDate(), TicketStatus.CONFIRMED));
        List<Ticket> rac = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(
                criteria.getStartDate(), criteria.getEndDate(), TicketStatus.RAC));

        List<Ticket> allPaid = new ArrayList<>();
        allPaid.addAll(confirmed);
        allPaid.addAll(rac);

        double totalRevenue = allPaid.stream()
                .mapToDouble(t -> t.getTotalFare() != null ? t.getTotalFare() : 0.0)
                .sum();

        // Revenue by class (key = class name, value = formatted string "₹1,234.00")
        Map<String, String> revenueByClass = new LinkedHashMap<>();
        allPaid.stream()
                .filter(t -> t.getClassType() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getClassType().name(),
                        Collectors.summingDouble(t -> t.getTotalFare() != null ? t.getTotalFare() : 0.0)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> revenueByClass.put(e.getKey(), formatRupees(e.getValue())));

        // Revenue by train (key = "12951 - Rajdhani", value = formatted string)
        Map<String, String> revenueByTrain = new LinkedHashMap<>();
        allPaid.stream()
                .filter(t -> t.getTrain() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTrain().getTrainNo() + " - " + t.getTrain().getTrainName(),
                        Collectors.summingDouble(t -> t.getTotalFare() != null ? t.getTotalFare() : 0.0)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> revenueByTrain.put(e.getKey(), formatRupees(e.getValue())));

        report.put("reportType", "Revenue Report");
        report.put("startDate", criteria.getStartDate().toString());
        report.put("endDate", criteria.getEndDate().toString());
        report.put("totalRevenue", formatRupees(totalRevenue));
        report.put("totalBookings", allPaid.size());
        report.put("revenueByClass", revenueByClass);
        report.put("revenueByTrain", revenueByTrain);

        return report;
    }

    /**
     * Booking Report: count of bookings by train, by status, by class.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateBookingReport(ReportCriteria criteria) {
        Map<String, Object> report = new LinkedHashMap<>();

        List<Ticket> confirmed  = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(criteria.getStartDate(), criteria.getEndDate(), TicketStatus.CONFIRMED));
        List<Ticket> rac        = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(criteria.getStartDate(), criteria.getEndDate(), TicketStatus.RAC));
        List<Ticket> waiting    = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(criteria.getStartDate(), criteria.getEndDate(), TicketStatus.WAITING));
        List<Ticket> cancelled  = safeFind(ticketRepository.findTicketsBetweenDatesAndStatus(criteria.getStartDate(), criteria.getEndDate(), TicketStatus.CANCELLED));

        List<Ticket> allTickets = new ArrayList<>();
        allTickets.addAll(confirmed); allTickets.addAll(rac); allTickets.addAll(waiting); allTickets.addAll(cancelled);

        // Bookings by train (key = "12951 - Rajdhani", value = count as int)
        Map<String, Integer> bookingsByTrain = new LinkedHashMap<>();
        allTickets.stream()
                .filter(t -> t.getTrain() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTrain().getTrainNo() + " - " + t.getTrain().getTrainName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> bookingsByTrain.put(e.getKey(), e.getValue()));

        // Bookings by class (key = class name, value = count as int)
        Map<String, Integer> bookingsByClass = new LinkedHashMap<>();
        allTickets.stream()
                .filter(t -> t.getClassType() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getClassType().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> bookingsByClass.put(e.getKey(), e.getValue()));

        // Total revenue from confirmed+RAC
        List<Ticket> paid = new ArrayList<>(confirmed); paid.addAll(rac);
        double totalRevenue = paid.stream().mapToDouble(t -> t.getTotalFare() != null ? t.getTotalFare() : 0.0).sum();

        report.put("reportType", "Booking Report");
        report.put("startDate", criteria.getStartDate().toString());
        report.put("endDate", criteria.getEndDate().toString());
        report.put("totalBookings", allTickets.size());
        report.put("confirmedCount", confirmed.size());
        report.put("racCount", rac.size());
        report.put("waitingCount", waiting.size());
        report.put("cancelledCount", cancelled.size());
        report.put("totalRevenue", formatRupees(totalRevenue));
        report.put("bookingsByTrain", bookingsByTrain);
        report.put("bookingsByClass", bookingsByClass);

        return report;
    }

    private static String formatRupees(double amount) {
        return String.format("%,.2f", amount);
    }

    private static List<Ticket> safeFind(List<Ticket> list) {
        return list != null ? list : Collections.emptyList();
    }
}