package com.irctc.controller;

import com.irctc.dto.ReportCriteria;
import com.irctc.model.Train;
import com.irctc.model.TrainTatkal;
import com.irctc.model.enums.ClassType;
import com.irctc.service.BookingService;
import com.irctc.service.ReportService;
import com.irctc.service.TrainService;
import com.irctc.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final TrainService trainService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ReportService reportService;
    private final com.irctc.repository.TrainTatkalRepository trainTatkalRepository;
    
    // Manual constructor
    public AdminController(TrainService trainService, 
                           BookingService bookingService,
                           UserService userService,
                           ReportService reportService,
                           com.irctc.repository.TrainTatkalRepository trainTatkalRepository) {
        this.trainService = trainService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.reportService = reportService;
        this.trainTatkalRepository = trainTatkalRepository;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        return "admin/dashboard";
    }
    
    @GetMapping("/trains")
    public String manageTrains(Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        // Build set of train numbers that already have Tatkal enabled for tomorrow
        java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
        Set<String> tatkalEnabledTrains = trainTatkalRepository.findByJourneyDate(tomorrow)
                .stream()
                .map(t -> t.getTrain().getTrainNo())
                .collect(Collectors.toSet());
        model.addAttribute("tatkalEnabledTrains", tatkalEnabledTrains);
        model.addAttribute("tomorrowDate", tomorrow);
        return "admin/manage-trains";
    }
    
    @GetMapping("/trains/add")
    public String showAddTrainForm(Model model) {
        model.addAttribute("train", new Train());
        model.addAttribute("allClasses", com.irctc.model.enums.ClassType.values());
        return "admin/add-train";
    }
    
    @PostMapping("/trains/add")
    public String addTrain(@RequestParam String trainNo,
                          @RequestParam String trainName,
                          @RequestParam String sourceStationCode,
                          @RequestParam String destinationStationCode,
                          @RequestParam String departureTime,
                          @RequestParam String arrivalTime,
                          @RequestParam Integer totalSeats,
                          @RequestParam(required = false) String[] runningDays,
                          @RequestParam(required = false) String intermediateStations,
                          @RequestParam(required = false) String[] selectedClasses,
                          @RequestParam Map<String, String> allParams,
                          Model model) {
        try {
            Train train = new Train();
            train.setTrainNo(trainNo);
            train.setTrainName(trainName);
            train.setDepartureTime(java.time.LocalTime.parse(departureTime));
            train.setArrivalTime(java.time.LocalTime.parse(arrivalTime));
            train.setTotalSeats(totalSeats);
            if (runningDays != null) {
                train.setRunningDays(runningDays);
            } else {
                train.setRunningDays(new String[]{});
            }
            
            // Populate Classes and Fare Structure
            if (selectedClasses != null && selectedClasses.length > 0) {
                List<com.irctc.model.enums.ClassType> classesList = new ArrayList<>();
                Map<String, Double> fareMap = new HashMap<>();
                
                for (String className : selectedClasses) {
                    try {
                        com.irctc.model.enums.ClassType ct = com.irctc.model.enums.ClassType.valueOf(className);
                        classesList.add(ct);
                        
                        String fareStr = allParams.get("fare_" + className);
                        if (fareStr != null && !fareStr.isEmpty()) {
                            fareMap.put(className, Double.parseDouble(fareStr));
                        } else {
                            // Fallback to ClassType default if not provided
                            fareMap.put(className, (double) ct.getBaseFare());
                        }
                    } catch (Exception e) {
                        logger.warn("Skipping invalid class selection: {}", className);
                    }
                }
                train.setClasses(classesList);
                train.setFareStructure(fareMap);
            } else {
                // Default classes if none selected
                List<com.irctc.model.enums.ClassType> defaultClasses = List.of(
                    com.irctc.model.enums.ClassType.SLEEPER, 
                    com.irctc.model.enums.ClassType.THIRD_AC
                );
                train.setClasses(defaultClasses);
                Map<String, Double> defaultFares = new HashMap<>();
                for (com.irctc.model.enums.ClassType ct : defaultClasses) {
                    defaultFares.put(ct.name(), (double) ct.getBaseFare());
                }
                train.setFareStructure(defaultFares);
            }
            
            trainService.addTrain(train, sourceStationCode, destinationStationCode, intermediateStations);
            return "redirect:/admin/trains";
        } catch (Exception e) {
            logger.error("Error adding train: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("train", new Train()); // Reset form
            model.addAttribute("allClasses", com.irctc.model.enums.ClassType.values());
            return "admin/add-train";
        }
    }
    
    /** Enable Tatkal for ALL active trains for today + tomorrow in one click */
    @PostMapping("/trains/tatkal/enable-all")
    public String enableTatkalForAllTrains(RedirectAttributes redirectAttributes) {
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Train> trains = trainService.getAllTrains();
        int enabledCount = 0;

        for (Train train : trains) {
            String trainNo = train.getTrainNo();
            // Today
            if (!trainTatkalRepository.existsByTrain_TrainNoAndJourneyDate(trainNo, today)) {
                trainTatkalRepository.save(new TrainTatkal(train, today));
            }
            // Tomorrow
            if (!trainTatkalRepository.existsByTrain_TrainNoAndJourneyDate(trainNo, tomorrow)) {
                trainTatkalRepository.save(new TrainTatkal(train, tomorrow));
            }
            enabledCount++;
        }

        redirectAttributes.addFlashAttribute("message",
                "✅ Tatkal enabled for all " + enabledCount + " trains on " + today + " and " + tomorrow + "!");
        return "redirect:/admin/trains";
    }

    /** Disable Tatkal for ALL trains (rollback) */
    @PostMapping("/trains/tatkal/disable-all")
    public String disableTatkalForAllTrains(RedirectAttributes redirectAttributes) {
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<TrainTatkal> todayEntries    = trainTatkalRepository.findByJourneyDate(today);
        List<TrainTatkal> tomorrowEntries = trainTatkalRepository.findByJourneyDate(tomorrow);
        trainTatkalRepository.deleteAll(todayEntries);
        trainTatkalRepository.deleteAll(tomorrowEntries);

        redirectAttributes.addFlashAttribute("message",
                "🔴 Tatkal disabled for all trains on " + today + " and " + tomorrow + ".");
        return "redirect:/admin/trains";
    }
    
    @PostMapping("/trains/delete/{trainNo}")
    public String deleteTrain(@PathVariable String trainNo) {
        trainService.deleteTrain(trainNo);
        return "redirect:/admin/trains";
    }
    
    @GetMapping("/reports")
    public String showReportsForm(Model model) {
        model.addAttribute("criteria", new ReportCriteria());
        return "admin/reports";
    }
    
    @PostMapping("/reports/generate")
    public String generateReport(@ModelAttribute ReportCriteria criteria, Model model) {
        Map<String, Object> report;
        
        if ("revenue".equals(criteria.getReportType())) {
            report = reportService.generateRevenueReport(criteria);
        } else {
            report = reportService.generateBookingReport(criteria);
        }
        
        model.addAttribute("report", report);
        model.addAttribute("criteria", criteria);
        return "admin/reports";
    }
    
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/manage-users";
    }

    @GetMapping("/refunds")
    public String manageRefunds(Model model) {
        model.addAttribute("refunds", bookingService.getRefundRequests());
        return "admin/refund-requests";
    }

    @PostMapping("/refunds/approve/{pnr}")
    public String approveRefund(@PathVariable String pnr, Model model) {
        try {
            bookingService.approveRefund(pnr);
            model.addAttribute("message", "Refund approved for PNR " + pnr + ".");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("refunds", bookingService.getRefundRequests());
        return "admin/refund-requests";
    }

    @PostMapping("/refunds/reject/{pnr}")
    public String rejectRefund(@PathVariable String pnr, Model model) {
        try {
            bookingService.rejectRefund(pnr);
            model.addAttribute("message", "Refund request rejected for PNR " + pnr + ".");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("refunds", bookingService.getRefundRequests());
        return "admin/refund-requests";
    }
}