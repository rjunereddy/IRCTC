package com.irctc.controller;

import com.irctc.dto.LoginRequest;
import com.irctc.dto.RegisterRequest;
import com.irctc.model.User;
import com.irctc.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        System.out.println("=== Showing login form ===");
        return "login";
    }
    
    @PostMapping("/login")
    public String login(LoginRequest request, HttpSession session, Model model) {
        System.out.println("=== Login attempt for: " + request.getUsername());
        try {
            User user = userService.loginUser(request);
            session.setAttribute("user", user);
            System.out.println("Login successful for: " + user.getUsername() + " Role: " + user.getRole());
            
            if (user.getRole().name().equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/passenger/dashboard";
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        System.out.println("=== Showing registration form ===");
        // Make sure to add a new RegisterRequest object to the model
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new RegisterRequest());
        }
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {
        System.out.println("=== Registration attempt for: " + request.getUsername());
        System.out.println("FullName: " + request.getFullName());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Phone: " + request.getPhone());
        System.out.println("Age: " + request.getAge());
        System.out.println("Gender: " + request.getGender());
        
        try {
            userService.registerUser(request);
            System.out.println("Registration successful for: " + request.getUsername());
            model.addAttribute("message", "Registration successful! Please login.");
            return "login";
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", request);  // Add the request back to model
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}