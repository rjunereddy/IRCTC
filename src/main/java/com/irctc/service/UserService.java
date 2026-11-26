package com.irctc.service;

import com.irctc.dto.LoginRequest;
import com.irctc.dto.RegisterRequest;
import com.irctc.model.Passenger;
import com.irctc.model.User;
import com.irctc.pattern.singleton.AuditLogger;
import com.irctc.repository.PassengerRepository;
import com.irctc.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;  // Changed from BCryptPasswordEncoder
    
    public UserService(UserRepository userRepository, 
                       PassengerRepository passengerRepository,
                       PasswordEncoder passwordEncoder) {  // Changed to PasswordEncoder
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null");
        this.passengerRepository = Objects.requireNonNull(passengerRepository, "PassengerRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder must not be null");
    }
    
    @Transactional
    public Passenger registerUser(RegisterRequest request) {
        if (request == null) {
            throw new RuntimeException("Registration request is required");
        }
        
        AuditLogger logger = AuditLogger.getInstance();
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }
        
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        if (request.getAge() != null && (request.getAge() < 1 || request.getAge() > 120)) {
            throw new RuntimeException("Invalid age. Age must be between 1 and 120");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        int age = request.getAge() != null ? request.getAge() : 18;
        String gender = request.getGender() != null ? request.getGender() : "MALE";
        
        Passenger passenger = new Passenger(
            request.getUsername(),
            encodedPassword,
            request.getEmail(),
            request.getPhone(),
            request.getFullName(),
            age,
            gender
        );
        
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            passenger.setAddress(request.getAddress());
        }
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            passenger.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().trim().isEmpty()) {
            passenger.setState(request.getState());
        }
        if (request.getPincode() != null && !request.getPincode().trim().isEmpty()) {
            passenger.setPincode(request.getPincode());
        }
        
        passenger.setEnabled(true);
        passenger.setCreatedAt(LocalDateTime.now());
        
        Passenger savedPassenger = passengerRepository.save(passenger);
        logger.log("REGISTER", request.getUsername(), "User registered successfully");
        
        return savedPassenger;
    }
    
    public User loginUser(LoginRequest request) {
        if (request == null) {
            throw new RuntimeException("Login request is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with username: " + request.getUsername());
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled. Please contact administrator");
        }
        
        user.setLastLogin(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        AuditLogger.getInstance().log("LOGIN", request.getUsername(), "Login successful");
        return savedUser;
    }
    
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    public Passenger getPassengerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        return passengerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Passenger not found: " + username));
    }
    
    public Passenger getPassengerById(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        return passengerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Passenger not found with ID: " + userId));
    }
    
    @Transactional
    public Passenger updateProfile(Long userId, RegisterRequest request) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        if (request == null) {
            throw new RuntimeException("Profile update request is required");
        }
        
        Passenger passenger = passengerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Passenger not found with ID: " + userId));
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            passenger.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                throw new RuntimeException("Email already registered to another user");
            }
            passenger.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                throw new RuntimeException("Phone number already registered to another user");
            }
            passenger.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            passenger.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            passenger.setCity(request.getCity());
        }
        if (request.getState() != null) {
            passenger.setState(request.getState());
        }
        if (request.getPincode() != null) {
            passenger.setPincode(request.getPincode());
        }
        
        Passenger savedPassenger = Objects.requireNonNull(passengerRepository.save(passenger), "Saved passenger must not be null");
        AuditLogger.getInstance().log("UPDATE_PROFILE", passenger.getUsername(), "Profile updated");
        
        return savedPassenger;
    }
    
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new RuntimeException("Old password is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password is required");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        AuditLogger.getInstance().log("CHANGE_PASSWORD", user.getUsername(), "Password changed");
    }
    
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users != null ? users : List.of();
    }
    
    @Transactional
    public void disableUser(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(false);
        userRepository.save(user);
        
        AuditLogger.getInstance().log("DISABLE_USER", "ADMIN", "User disabled: " + user.getUsername());
    }
    
    @Transactional
    public void enableUser(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(true);
        userRepository.save(user);
        
        AuditLogger.getInstance().log("ENABLE_USER", "ADMIN", "User enabled: " + user.getUsername());
    }

    @Transactional
    public void updateUserRole(Long userId, com.irctc.model.enums.UserRole role) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        AuditLogger.getInstance().log("UPDATE_USER_ROLE", "ADMIN", "User: " + user.getUsername() + " new role: " + role);
    }
}