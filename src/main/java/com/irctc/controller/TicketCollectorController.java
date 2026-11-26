package com.irctc.controller;

import com.irctc.model.PassengerDetail;
import com.irctc.model.Ticket;
import com.irctc.model.Train;
import com.irctc.model.enums.TicketStatus;
import com.irctc.pattern.singleton.AuditLogger;
import com.irctc.repository.PassengerDetailRepository;
import com.irctc.repository.TicketRepository;
import com.irctc.service.TrainService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tte")
public class TicketCollectorController {

    private final TrainService trainService;
    private final TicketRepository ticketRepository;
    private final PassengerDetailRepository passengerDetailRepository;

    public TicketCollectorController(TrainService trainService,
                                     TicketRepository ticketRepository,
                                     PassengerDetailRepository passengerDetailRepository) {
        this.trainService = trainService;
        this.ticketRepository = ticketRepository;
        this.passengerDetailRepository = passengerDetailRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        List<Train> trains = trainService.getAllTrains();
        model.addAttribute("trains", trains);
        model.addAttribute("tteName", auth != null ? auth.getName() : "TTE Staff");
        model.addAttribute("todayDate", LocalDate.now());
        return "tte/dashboard";
    }

    @GetMapping("/chart/{trainNo}")
    public String viewPassengerChart(@PathVariable String trainNo,
                                     @RequestParam(required = false) String journeyDate,
                                     @RequestParam(required = false) String coach,
                                     Model model) {
        Train train = trainService.getTrainByNumber(trainNo);
        LocalDate date = (journeyDate != null && !journeyDate.isEmpty())
                ? LocalDate.parse(journeyDate)
                : LocalDate.now();

        List<Ticket> tickets = ticketRepository.findByTrain_TrainNoAndJourneyDate(trainNo, date);
        if (tickets.isEmpty()) {
            // Also check all tickets for this train as fallback demo preview
            tickets = ticketRepository.findByTrain_TrainNoOrderByJourneyDateDesc(trainNo);
        }

        List<PassengerDetail> allPassengers = new ArrayList<>();
        Set<String> coaches = new TreeSet<>();
        int confirmedCount = 0;
        int presentCount = 0;
        int noShowCount = 0;

        List<Ticket> racTickets = new ArrayList<>();

        for (Ticket t : tickets) {
            if (t.getStatus() == TicketStatus.RAC || t.getStatus() == TicketStatus.WAITING) {
                racTickets.add(t);
            }
            if (t.getAllPassengers() != null) {
                for (PassengerDetail p : t.getAllPassengers()) {
                    allPassengers.add(p);
                    String berth = p.getBerthAllotted();
                    if (berth != null && berth.contains("-")) {
                        coaches.add(berth.split("-")[0].trim());
                    }
                    if (t.getStatus() == TicketStatus.CONFIRMED) {
                        confirmedCount++;
                        if ("PRESENT".equalsIgnoreCase(p.getAttendanceStatus())) {
                            presentCount++;
                        } else if ("NO_SHOW".equalsIgnoreCase(p.getAttendanceStatus())
                                || "NO_SHOW_VACATED".equalsIgnoreCase(p.getAttendanceStatus())) {
                            noShowCount++;
                        }
                    }
                }
            }
        }

        // Apply coach filter if selected
        List<PassengerDetail> displayedPassengers = allPassengers;
        if (coach != null && !coach.isEmpty() && !"ALL".equalsIgnoreCase(coach)) {
            displayedPassengers = allPassengers.stream()
                    .filter(p -> p.getBerthAllotted() != null && p.getBerthAllotted().toUpperCase().startsWith(coach.toUpperCase()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("train", train);
        model.addAttribute("journeyDate", date);
        model.addAttribute("passengers", displayedPassengers);
        model.addAttribute("racTickets", racTickets);
        model.addAttribute("coaches", coaches);
        model.addAttribute("selectedCoach", coach != null ? coach : "ALL");
        model.addAttribute("totalConfirmed", confirmedCount);
        model.addAttribute("totalPresent", presentCount);
        model.addAttribute("totalNoShow", noShowCount);
        model.addAttribute("vacantBerths", noShowCount);

        return "tte/chart";
    }

    @PostMapping("/attendance")
    public String markAttendance(@RequestParam Long passengerId,
                                 @RequestParam String status,
                                 @RequestParam String trainNo,
                                 @RequestParam(required = false) String journeyDate,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        PassengerDetail p = passengerDetailRepository.findById(passengerId).orElse(null);
        if (p != null) {
            p.setAttendanceStatus(status);
            p.setCheckedAt(LocalDateTime.now());
            p.setCheckedByTte(auth != null ? auth.getName() : "TTE");
            passengerDetailRepository.save(p);
            AuditLogger.getInstance().log("TTE_ATTENDANCE", auth != null ? auth.getName() : "TTE",
                    "Passenger: " + p.getPassengerName() + " marked " + status);
            redirectAttributes.addFlashAttribute("message", "Passenger " + p.getPassengerName() + " marked as " + status);
        }
        return "redirect:/tte/chart/" + trainNo + (journeyDate != null ? "?journeyDate=" + journeyDate : "");
    }

    @PostMapping("/reallocate")
    public String reallocateBerth(@RequestParam String trainNo,
                                  @RequestParam Long noShowPassengerId,
                                  @RequestParam(required = false) String journeyDate,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        PassengerDetail noShowP = passengerDetailRepository.findById(noShowPassengerId).orElse(null);
        if (noShowP == null || noShowP.getBerthAllotted() == null) {
            redirectAttributes.addFlashAttribute("error", "No-show passenger or allocated berth not found.");
            return "redirect:/tte/chart/" + trainNo;
        }

        String vacatedBerth = noShowP.getBerthAllotted();
        LocalDate date = (journeyDate != null && !journeyDate.isEmpty()) ? LocalDate.parse(journeyDate) : LocalDate.now();

        // Find RAC ticket for this train
        List<Ticket> racTickets = ticketRepository.findByTrain_TrainNoAndJourneyDateAndStatus(trainNo, date, TicketStatus.RAC);
        if (racTickets.isEmpty()) {
            racTickets = ticketRepository.findByTrain_TrainNoAndJourneyDateAndStatus(trainNo, date, TicketStatus.WAITING);
        }

        if (racTickets.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No RAC or Waiting list passengers found on this train for reallocation.");
            return "redirect:/tte/chart/" + trainNo + (journeyDate != null ? "?journeyDate=" + journeyDate : "");
        }

        Ticket upgradedTicket = racTickets.get(0);
        upgradedTicket.setStatus(TicketStatus.CONFIRMED);
        if (upgradedTicket.getAllPassengers() != null && !upgradedTicket.getAllPassengers().isEmpty()) {
            PassengerDetail luckyPassenger = upgradedTicket.getAllPassengers().get(0);
            luckyPassenger.setBerthAllotted(vacatedBerth);
            luckyPassenger.setAttendanceStatus("PRESENT");
            luckyPassenger.setCheckedByTte(auth != null ? auth.getName() : "TTE");
            luckyPassenger.setCheckedAt(LocalDateTime.now());
            passengerDetailRepository.save(luckyPassenger);
        }
        ticketRepository.save(upgradedTicket);

        noShowP.setAttendanceStatus("NO_SHOW_VACATED");
        passengerDetailRepository.save(noShowP);

        AuditLogger.getInstance().log("TTE_REALLOCATE", auth != null ? auth.getName() : "TTE",
                "Reallocated " + vacatedBerth + " to PNR " + upgradedTicket.getPnr());

        redirectAttributes.addFlashAttribute("message", "🎉 Vacated Berth " + vacatedBerth + 
                " successfully re-allocated to RAC passenger (PNR: " + upgradedTicket.getPnr() + ")!");

        return "redirect:/tte/chart/" + trainNo + (journeyDate != null ? "?journeyDate=" + journeyDate : "");
    }
}
