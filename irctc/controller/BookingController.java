package com.irctc.controller;

import com.irctc.dto.BookingRequest;
import com.irctc.dto.TrainAvailabilityDTO;
import com.irctc.model.Passenger;
import com.irctc.model.Ticket;
import com.irctc.model.Train;
import com.irctc.model.User;
import com.irctc.repository.StationRepository;
import com.irctc.service.BookingService;
import com.irctc.service.TrainService;
import com.irctc.service.UserService;
import com.irctc.util.PNRGenerator;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.irctc.service.PdfGeneratorService;
import com.irctc.service.SavedPassengerService;
import com.irctc.service.WalletService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/passenger")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    private final BookingService bookingService;
    private final UserService userService;
    private final TrainService trainService;
    private final StationRepository stationRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final SavedPassengerService savedPassengerService;
    private final WalletService walletService;
    
    // Manual constructor
    public BookingController(BookingService bookingService, UserService userService, TrainService trainService, StationRepository stationRepository, PdfGeneratorService pdfGeneratorService, SavedPassengerService savedPassengerService, WalletService walletService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.trainService = trainService;
        this.stationRepository = stationRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.savedPassengerService = savedPassengerService;
        this.walletService = walletService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        model.addAttribute("passenger", passenger);

        // Booking stats for dashboard cards
        try {
            List<com.irctc.model.Ticket> allTickets = bookingService.getPassengerBookings(passenger);
            long confirmed  = allTickets.stream().filter(t -> t.getStatus() == com.irctc.model.enums.TicketStatus.CONFIRMED).count();
            long cancelled  = allTickets.stream().filter(t -> t.getStatus() == com.irctc.model.enums.TicketStatus.CANCELLED).count();
            long pending    = allTickets.stream().filter(t -> t.getStatus() == com.irctc.model.enums.TicketStatus.RAC
                                                           || t.getStatus() == com.irctc.model.enums.TicketStatus.WAITING).count();
            model.addAttribute("totalBookings",     allTickets.size());
            model.addAttribute("confirmedBookings", confirmed);
            model.addAttribute("cancelledBookings", cancelled);
            model.addAttribute("pendingBookings",   pending);

            // Last 5 bookings for the recent bookings table
            List<com.irctc.model.Ticket> recent = allTickets.stream()
                    .sorted((a, b) -> {
                        if (a.getBookingDate() == null) return 1;
                        if (b.getBookingDate() == null) return -1;
                        return b.getBookingDate().compareTo(a.getBookingDate());
                    })
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("recentTickets", recent);
        } catch (Exception e) {
            model.addAttribute("totalBookings", 0);
            model.addAttribute("confirmedBookings", 0);
            model.addAttribute("cancelledBookings", 0);
            model.addAttribute("pendingBookings", 0);
            model.addAttribute("recentTickets", java.util.Collections.emptyList());
        }

        // Wallet balance
        try {
            double walletBalance = walletService.getWalletBalance(passenger);
            model.addAttribute("walletBalance", String.format("%.2f", walletBalance));
        } catch (Exception e) {
            model.addAttribute("walletBalance", "0.00");
        }

        return "passenger/dashboard";
    }

    
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("source", "");
        model.addAttribute("destination", "");
        model.addAttribute("date", LocalDate.now().plusDays(1).toString());
        model.addAttribute("stations", stationRepository.findAll());
        model.addAttribute("trains", null); // No results on initial load
        return "passenger/search";
    }
    
    @PostMapping("/search")
    public String searchTrains(@RequestParam String source, 
                               @RequestParam String destination,
                               @RequestParam String date,
                               Model model) {
        LocalDate journeyDate = LocalDate.parse(date);
        List<TrainAvailabilityDTO> trains = bookingService.searchTrains(source, destination, journeyDate);
        model.addAttribute("trains", trains);
        model.addAttribute("source", source);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("stations", stationRepository.findAll());
        return "passenger/search";
    }
    
    @GetMapping("/book/{trainNo}")
    public String showBookingForm(@PathVariable String trainNo, 
                                  @RequestParam(required = false) String date,
                                  @RequestParam(required = false) String classType,
                                  HttpSession session,
                                  Model model) {
        // Validate date parameter
        if (date == null || date.trim().isEmpty()) {
            return "redirect:/passenger/search";
        }
        try {
            LocalDate.parse(date); // Validate date format
        } catch (Exception e) {
            return "redirect:/passenger/search";
        }
        
        model.addAttribute("trainNo", trainNo);
        model.addAttribute("journeyDate", date);
        model.addAttribute("selectedClassType", classType); // Pre-select class from search results
        model.addAttribute("bookingRequest", new BookingRequest());
        
        LocalDate parsedDate = LocalDate.parse(date);
        
        // Pass train object so we can list its available classes in the form
        try {
            Train train = trainService.getTrainByNumber(trainNo);
            model.addAttribute("train", train);
            // Compute live availability totals for the booking form header
            int confirmedBooked = bookingService.getConfirmedSeatCount(trainNo, parsedDate);
            int totalSeats = train.getTotalSeats() != null ? train.getTotalSeats() : 0;
            model.addAttribute("availableSeatsOnForm", Math.max(0, totalSeats - confirmedBooked));
            model.addAttribute("racBooked", bookingService.getRacSeatCount(trainNo, parsedDate));
            model.addAttribute("wlBooked", bookingService.getWlSeatCount(trainNo, parsedDate));
        } catch (Exception ignored) {}
        
        boolean isTatkalEnabled = bookingService.isTatkalEnabled(trainNo, parsedDate);
        model.addAttribute("isTatkalEnabled", isTatkalEnabled);
        
        User user = getCurrentUser(session);
        if (user != null) {
            Passenger passenger = userService.getPassengerByUsername(user.getUsername());
            model.addAttribute("savedPassengers", savedPassengerService.getSavedPassengers(passenger));
            // Show wallet balance on booking form
            double walletBalance = walletService.getWalletBalance(passenger);
            model.addAttribute("walletBalance", walletBalance);
        }
        
        return "passenger/book";
    }

    
    @PostMapping("/book")
    public String bookTicket(@ModelAttribute BookingRequest request, 
                             HttpSession session,
                             Model model) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return "redirect:/auth/login";
            }
            Passenger passenger = userService.getPassengerByUsername(user.getUsername());
            
            // Validate journey date
            if (request.getJourneyDate() == null || request.getJourneyDate().trim().isEmpty()) {
                throw new IllegalArgumentException("Journey date is required");
            }
            
            LocalDate journeyDate = LocalDate.parse(request.getJourneyDate());
            Ticket ticket = bookingService.bookTicket(request, passenger, journeyDate);
            model.addAttribute("ticket", ticket);
            return "passenger/confirmation";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trainNo", request.getTrainNo());
            model.addAttribute("journeyDate", request.getJourneyDate());
            model.addAttribute("bookingRequest", request);
            return "passenger/book";
        }
    }
    
    @GetMapping("/pnr-status")
    public String showPNRForm() {
        return "passenger/pnr-status";
    }
    
    @PostMapping("/pnr-status")
    public String getPNRStatus(@RequestParam String pnr, Model model) {
        try {
            if (!PNRGenerator.validate(pnr)) {
                model.addAttribute("error", "Invalid PNR format. Please enter a valid 10-digit PNR number.");
                return "passenger/pnr-status";
            }
            Ticket ticket = bookingService.getTicketByPNR(pnr);
            model.addAttribute("ticket", ticket);
        } catch (Exception e) {
            model.addAttribute("error", "Ticket not found");
        }
        return "passenger/pnr-status";
    }
    
    @GetMapping("/booking-history")
    public String viewBookingHistory(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        List<Ticket> tickets = bookingService.getPassengerBookings(passenger);
        model.addAttribute("tickets", tickets);
        return "passenger/booking-history";
    }
    
    @PostMapping("/cancel/{pnr}")
    public String cancelTicket(@PathVariable String pnr, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        try {
            bookingService.cancelTicket(pnr, passenger);
            model.addAttribute("message", "Ticket cancelled successfully. You can now request a refund.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cancellation failed for PNR " + pnr + ": " + e.getMessage());
            model.addAttribute("error", e.getMessage());
        }
        List<Ticket> tickets = bookingService.getPassengerBookings(passenger);
        model.addAttribute("tickets", tickets);
        return "passenger/booking-history";
    }



    @PostMapping("/refund-request/{pnr}")
    public String requestRefund(@PathVariable String pnr, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        try {
            Ticket ticket = bookingService.requestRefund(pnr, passenger);
            model.addAttribute("message", "Refund request submitted for PNR " + ticket.getPnr() + ".");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        List<Ticket> tickets = bookingService.getPassengerBookings(passenger);
        model.addAttribute("tickets", tickets);
        return "passenger/booking-history";
    }

    @GetMapping("/ticket/download/{pnr}")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable String pnr, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Ticket ticket = bookingService.getTicketByPNR(pnr);
            byte[] pdfBytes = pdfGeneratorService.generateTicketPdf(ticket);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Ticket_" + pnr + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        model.addAttribute("passenger", passenger);
        return "passenger/dashboard";
    }

    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            return userService.getUserByUsername(authentication.getName());
        }

        return null;
    }
}