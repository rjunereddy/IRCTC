# 📜 IRCTC Project: Detailed OOAD Implementation Report

This report provides a comprehensive breakdown of the **Object-Oriented Analysis and Design (OOAD)** principles and **Design Patterns** used in the IRCTC Reservation System. Every entry includes the specific location in the code and a technical justification.

---

## 1. GoF Design Patterns (Structural & Behavioral)

### 🧩 Singleton Pattern
*   **Location**: `com.irctc.pattern.singleton.AuditLogger` ([AuditLogger.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/pattern/singleton/AuditLogger.java))
*   **How it is used**: The class has a `static INSTANCE` and a `static synchronized AuditLogger getInstance()` method.
*   **Justification**: The `AuditLogger` is used across every service (`BookingService`, `TrainService`, `AdminController`) to log system events. If we had multiple instances, our audit history would be split across different memory regions, making reliable logging impossible. The Singleton ensures a **single centralized log stream**.

### 🧩 Strategy Pattern
*   **Location**: `com.irctc.pattern.strategy.PaymentStrategy` ([PaymentStrategy.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/pattern/strategy/PaymentStrategy.java)) and implementations like `CreditCardPayment`.
*   **How it is used**: The `PaymentService` accepts a `PaymentStrategy` interface and calls `.process()` without knowing if the user chose a Credit Card, Wallet, or NetBanking.
*   **Justification**: It allows for **pluggable algorithms**. We can add a "BitcoinPayment" strategy tomorrow just by implementing the interface, without modifying a single line of the `BookingService`.

### 🧩 Factory Pattern
*   **Location**: `com.irctc.pattern.factory.FareCalculatorFactory` ([FareCalculatorFactory.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/pattern/factory/FareCalculatorFactory.java))
*   **How it is used**: Method `getCalculator(QuotaType quota)` returns a specific instance of `GeneralFareCalculator`, `TatkalFareCalculator`, or `SeniorCitizenFareCalculator`.
*   **Justification**: This **encapsulates object creation**. The complexity of deciding *which* pricing logic applies shouldn't live in the booking flow. The Factory takes that responsibility, simplifying the main system logic.

### 🧩 Observer Pattern
*   **Location**: `com.irctc.pattern.observer.BookingObserver` ([BookingObserver.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/pattern/observer/BookingObserver.java))
*   **How it is used**: `EmailNotifier` and `SMSNotifier` "listen" for status updates in `BookingService`.
*   **Justification**: Enables **Loose Coupling**. The `BookingService` doesn't need to know how to send an SMS or format an email. It just broadcasts a change, and any number of observers can react independently.

---

## 2. SOLID Principles

### ✅ S - Single Responsibility Principle (SRP)
*   **Location**: `com.irctc.service.SeatAllocationService` ([SeatAllocationService.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/service/SeatAllocationService.java))
*   **Justification**: Originally, seat allocation logic was inside `BookingService`. We extracted it to `SeatAllocationService`.
    *   **Class Goal**: To map a ticket to physical coaches/seats (e.g., S1-12).
    *   **Reasoning**: If the coach layout changes (e.g., moving from 72 to 80 seats per coach), we only change one class, leaving the payment/booking logic untouched.

### ✅ O - Open/Closed Principle (OCP)
*   **Location**: `com.irctc.pattern.factory.FareCalculator`
*   **Justification**: The `BookingService` uses the `FareCalculator` interface.
    *   **Verification**: When we added the **Tatkal Quota**, we didn't "open" the `bookTicket` method to add `if(tatkal)`. instead, we created `TatkalFareCalculator`.
    *   **Benefit**: High system stability during feature addition.

### ✅ L - Liskov Substitution Principle (LSP)
*   **Location**: User Inheritance (`Passenger` and `Administrator` extending `User`)
*   **Justification**: The `UserService.java` provides a method `loadUserByUsername()`. This method returns a `User`.
    *   **Verification**: The controllers can treat the result as a `User` for security checks, whether the person behind it is a daily traveler or an admin. Substitutability is maintained without side effects.

### ✅ I - Interface Segregation Principle (ISP)
*   **Location**: `com.irctc.repository.TicketRepository`
*   **Justification**: We use specialized Query methods like `countConfirmedByClass()` rather than one "fat" method that returns everything. Specific services only "see" the data methods they need to perform their focused jobs.

### ✅ D - Dependency Inversion Principle (DIP)
*   **Location**: Spring `@Autowired` in Services ([BookingService.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/service/BookingService.java))
*   **Justification**: Services depend on **Repository Interfaces**, not the concrete `SimpleJpaRepository` implementation. We can swap H2 for MySQL or Postgres just by changing the `application.properties` without touching the Java code.

---

## 3. GRASP Principles

### 🧠 Information Expert
*   **Location**: `Train.java` method `getFare(ClassType, QuotaType)` ([Train.java](file:///G:/My%20Drive/ooad%20mini%20project/irctc-reservation-system/src/main/java/com/irctc/model/Train.java))
*   **Justification**: The `Train` entity is the **Information Expert** for its own pricing. It holds the `fareStructure` map, so it is the most logical place to provide the "base fare" before taxes.

### 🧠 Creator
*   **Location**: `BookingService.bookTicket()`
*   **Justification**: `BookingService` is the **Creator** for the `Ticket` object. 
    1. It uses and records the ticket.
    2. It has the identifying information (PNR, Journey Date) needed to initialize it.
    3. It manages the ticket's life cycle.

### 🧠 Controller
*   **Location**: `com.irctc.controller.AdminController`
*   **Justification**: It handles high-level "System Events" (like `addTrain`). It doesn't perform train logic itself but coordinates between the Web UI and the `TrainService`.

### 🧠 High Cohesion
*   **Location**: `com.irctc.service.ReportService`
*   **Justification**: This service *only* handles data aggregation for analytics and revenue. It does not handle authentication or route search. This makes it highly cohesive and easy to test.

---

## 4. Specific Feature Implementation Justification

### 🍲 E-Catering (Itemized Display)
*   **Used Principle**: **Don't Repeat Yourself (DRY)**.
*   **Implementation**: Logic for parsing food JSON strings was placed in `BookingService.parseFoodItems()`. 
*   **Justification**: This single helper method is used to populate the `Ticket` entity and also to display the summary on the `confirmation.html` page, avoiding duplicate parsing logic.

### 💰 Wallet System
*   **Used Principle**: **Encapsulation**.
*   **Implementation**: `WalletService` encapsulates all internal arithmetic for balance deductions and refunds.
*   **Justification**: By hiding the "balance check and deduct" logic inside a service, we prevent accidental "double-spending" or direct database manipulation from the controllers.

---

### 🎓 Summary for Evaluation:
Your project successfully demonstrates these principles by moving from a **Monolithic Script** to a **Modular, Pattern-Oriented Architecture**. This ensures that as your "IRCTC System" grows (e.g., adding more trains, more food items, or new payment gateways), the code remains clean, bug-free, and easy to scale.
