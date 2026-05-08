package com.irctc.controller;

import com.irctc.dto.ReportCriteria;
import com.irctc.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/report")
public class ReportController {
    
    private final ReportService reportService;
    
    // Manual constructor
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @GetMapping("/revenue")
    public String showRevenueReport(Model model) {
        model.addAttribute("criteria", new ReportCriteria());
        return "report/revenue";
    }
    
    @PostMapping("/revenue/generate")
    public String generateRevenueReport(@ModelAttribute ReportCriteria criteria, Model model) {
        model.addAttribute("report", reportService.generateRevenueReport(criteria));
        return "report/revenue";
    }
    
    @GetMapping("/booking")
    public String showBookingReport(Model model) {
        model.addAttribute("criteria", new ReportCriteria());
        return "report/booking";
    }
    
    @PostMapping("/booking/generate")
    public String generateBookingReport(@ModelAttribute ReportCriteria criteria, Model model) {
        model.addAttribute("report", reportService.generateBookingReport(criteria));
        return "report/booking";
    }
}