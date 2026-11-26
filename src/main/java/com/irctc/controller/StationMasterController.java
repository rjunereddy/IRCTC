package com.irctc.controller;

import com.irctc.model.Station;
import com.irctc.model.TrainRouteStop;
import com.irctc.pattern.singleton.AuditLogger;
import com.irctc.service.StationService;
import com.irctc.service.TrainService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/station-master")
public class StationMasterController {

    private final StationService stationService;
    private final TrainService trainService;

    public StationMasterController(StationService stationService, TrainService trainService) {
        this.stationService = stationService;
        this.trainService = trainService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "KLBG") String stationCode,
                            Model model, Authentication auth) {
        Station currentStation = stationService.getStation(stationCode);
        if (currentStation == null) {
            currentStation = stationService.getStation("KLBG");
        }
        if (currentStation == null) {
            List<Station> all = stationService.getAllStations();
            if (!all.isEmpty()) currentStation = all.get(0);
        }

        List<TrainRouteStop> stops = currentStation != null 
                ? trainService.getStationStops(currentStation.getStationCode()) 
                : List.of();

        model.addAttribute("currentStation", currentStation);
        model.addAttribute("allStations", stationService.getAllStations());
        model.addAttribute("stops", stops);
        model.addAttribute("smName", auth != null ? auth.getName() : "Station Master");

        return "stationmaster/dashboard";
    }

    @PostMapping("/update-platform")
    public String updatePlatform(@RequestParam Long stopId,
                                 @RequestParam String platformNumber,
                                 @RequestParam String stationCode,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            TrainRouteStop stop = trainService.updateStopPlatform(stopId, platformNumber);
            AuditLogger.getInstance().log("SM_UPDATE_PLATFORM", auth != null ? auth.getName() : "SM",
                    "Stop " + stopId + " platform set to " + platformNumber);
            redirectAttributes.addFlashAttribute("message", "✅ Platform assigned to Platform " + platformNumber + 
                    " for Train " + stop.getTrain().getTrainNo() + " (" + stop.getTrain().getTrainName() + ")!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating platform: " + e.getMessage());
        }
        return "redirect:/station-master/dashboard?stationCode=" + stationCode;
    }

    @PostMapping("/update-delay")
    public String updateDelay(@RequestParam Long stopId,
                              @RequestParam Integer delayMinutes,
                              @RequestParam String stationCode,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            TrainRouteStop stop = trainService.updateStopDelay(stopId, delayMinutes);
            AuditLogger.getInstance().log("SM_UPDATE_DELAY", auth != null ? auth.getName() : "SM",
                    "Stop " + stopId + " delay set to " + delayMinutes + " mins");
            String delayText = delayMinutes > 0 ? "Delayed by " + delayMinutes + " mins" : "Running On Time";
            redirectAttributes.addFlashAttribute("message", "⏱️ Schedule updated: Train " + 
                    stop.getTrain().getTrainNo() + " is now " + delayText + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating delay: " + e.getMessage());
        }
        return "redirect:/station-master/dashboard?stationCode=" + stationCode;
    }

    @PostMapping("/prepare-chart")
    public String prepareChart(@RequestParam String trainNo,
                               @RequestParam String stationCode,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        AuditLogger.getInstance().log("SM_PREPARE_CHART", auth != null ? auth.getName() : "SM",
                "Reservation chart frozen for Train " + trainNo + " at station " + stationCode);
        redirectAttributes.addFlashAttribute("message", "📋 Reservation Chart prepared & frozen for Train " + trainNo + "!");
        return "redirect:/station-master/dashboard?stationCode=" + stationCode;
    }
}
