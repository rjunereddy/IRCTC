package com.irctc.controller;

import com.irctc.model.Passenger;
import com.irctc.model.PassengerWallet;
import com.irctc.model.User;
import com.irctc.service.UserService;
import com.irctc.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/passenger/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public String viewWallet(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Passenger passenger = userService.getPassengerByUsername(user.getUsername());
        PassengerWallet wallet = walletService.getWallet(passenger);
        
        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions", walletService.getTransactions(wallet));
        return "passenger/wallet";
    }

    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Double amount, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            Passenger passenger = userService.getPassengerByUsername(user.getUsername());
            walletService.addFunds(passenger, amount);
            model.addAttribute("message", "Successfully added ₹" + amount + " to your wallet.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return viewWallet(session, model);
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
