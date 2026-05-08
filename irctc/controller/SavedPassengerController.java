package com.irctc.controller;

import com.irctc.model.Passenger;
import com.irctc.model.SavedPassenger;
import com.irctc.model.User;
import com.irctc.service.SavedPassengerService;
import com.irctc.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/passenger/saved-passengers")
public class SavedPassengerController {

    private final SavedPassengerService savedPassengerService;
    private final UserService userService;

    public SavedPassengerController(SavedPassengerService savedPassengerService, UserService userService) {
        this.savedPassengerService = savedPassengerService;
        this.userService = userService;
    }

    @GetMapping
    public String viewSavedPassengers(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        model.addAttribute("savedPassengers", savedPassengerService.getSavedPassengers(passenger));
        return "passenger/saved-passengers";
    }

    @PostMapping("/add")
    public String addSavedPassenger(@ModelAttribute SavedPassenger savedPassenger, HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            Passenger passenger = userService.getPassengerByUsername(user.getUsername());
            savedPassenger.setPassenger(passenger);
            savedPassengerService.addSavedPassenger(savedPassenger);
        }
        return "redirect:/passenger/saved-passengers";
    }

    @PostMapping("/delete/{id}")
    public String deleteSavedPassenger(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            Passenger passenger = userService.getPassengerByUsername(user.getUsername());
            savedPassengerService.deleteSavedPassenger(id, passenger);
        }
        return "redirect:/passenger/saved-passengers";
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
