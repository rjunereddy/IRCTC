# OOAD Mini Project Report Content: IRCTC Reservation System

Here is the content you can use to fill in your `Report_Template.docx`. 

---

### **Title**
IRCTC Railway Reservation System - Object Oriented Analysis & Design Mini Project

*(Fill in your team members, SRN, Section, and Faculty Name in the template)*

---

### **Problem Statement:** 
The existing manual or legacy railway reservation systems often face challenges related to concurrency, real-time ticket availability, dynamic pricing, and secure payment processing. This project aims to design and implement a robust, scalable, and user-friendly online railway reservation system (inspired by IRCTC) utilizing Object-Oriented Analysis and Design (OOAD) principles. The system will handle train scheduling, seat allocation, passenger management, and ticket generation while applying appropriate design patterns to ensure maintainability, extensibility, and a seamless user experience.

---

### **Key Features:**
1. **User Authentication & Roles:** Secure registration and login system with Role-Based Access Control (Admin and Passenger).
2. **Train & Route Management:** Admins can dynamically add, update, and schedule trains, routes, and seat availability (including quotas like Tatkal).
3. **Search & Booking Engine:** Users can search for trains between stations on specific dates, view real-time seat availability, and book tickets for multiple passengers.
4. **Dynamic Fare Calculation:** Automatic calculation of ticket prices based on quotas (General, Tatkal, Senior Citizen).
5. **Payment Gateway Integration:** Support for multiple payment modes (UPI, Credit Card, Net Banking) and an integrated Passenger E-Wallet system.
6. **Ticket Generation & Notifications:** Automated PNR generation, downloadable PDF tickets, and automated notifications (Email/SMS).
7. **Booking History & Cancellations:** Users can view their past bookings, track PNR status, and request cancellations with automated refund processing.
8. **Admin Dashboard:** Centralized dashboard for administrators to manage users, monitor trains, and review system activities.

---

### **Models:**
The system is built upon several core domain models representing real-world entities:
*   **User / Passenger:** Manages credentials, roles, and personal profiles.
*   **Train & TrainRouteStop:** Represents trains, their schedules, routes, stops, and seat capacities (General/Tatkal).
*   **Station:** Represents physical railway stations.
*   **Ticket & PassengerDetail:** Represents booked tickets, unique PNRs, and associated passenger information.
*   **Payment, PassengerWallet & WalletTransaction:** Manages transaction histories, wallet balances, and payment records.
*   **Refund:** Handles cancellation and refund requests.

---

### **Diagram Descriptions (For your reference to add images):**
*   **Use Case Diagram:** 
    *   **Actors:** Passenger, Admin.
    *   **Use Cases:** Register, Login, Search Trains, Book Ticket, Make Payment, Download PDF, Cancel Ticket (Passenger). Manage Trains, Manage Users, View Reports (Admin).
*   **Class Diagram:** Should show the relationships between `Train`, `Ticket`, `User`, `Payment`, and highlight the interfaces used for the Strategy (`PaymentStrategy`) and Factory (`FareCalculator`) patterns.
*   **State Diagram:** Show the state transitions of a **Ticket** (`Initiated` → `Pending Payment` → `Confirmed` → `Cancelled/Refunded`).
*   **Activity Diagrams:** Show the flow of the **Ticket Booking Process**: (Start → Search Train → Select Train → Enter Details → Select Payment Mode → Process Payment → Generate PNR/Ticket → Send Notification → End).

---

### **Design Principles, and Design Patterns:**

**MVC Architecture used?** Yes

**Design Principles** 
*   **Single Responsibility Principle (SRP):** Every class is designed to handle a single part of the functionality. For instance, `PdfGeneratorService` strictly handles PDF creation, `TrainRepository` handles database interactions for trains, and Controllers only route HTTP requests.
*   **Open/Closed Principle (OCP):** The system is open for extension but closed for modification. We can easily add a new payment method or a new fare calculation logic by implementing the existing interfaces (`PaymentStrategy`, `FareCalculator`) without modifying the core booking engine.
*   **Dependency Inversion Principle (DIP):** High-level modules (like `BookingService`) depend on abstractions (interfaces like `PaymentStrategy` or Spring Data Repositories) rather than concrete implementations, promoting loose coupling.

**Design Patterns**
*   **Strategy Pattern:** Implemented in the payment processing module. The `PaymentStrategy` interface is implemented by `UPIPayment`, `CreditCardPayment`, and `NetBankingPayment`. The system dynamically selects the appropriate algorithm at runtime based on the user's chosen payment method, isolating the payment logic from the booking service.
*   **Observer Pattern:** Implemented for the notification system. The `TicketObserver` interface acts as the observer. When a ticket is successfully confirmed, the booking service notifies registered observers like `EmailNotifier` and `SMSNotifier` to dispatch messages to the user. This decouples the core booking process from the notification mechanism.
*   **Factory Pattern:** Implemented via the `FareCalculatorFactory`. Depending on the passenger type or quota (General, Senior Citizen, Tatkal), the factory instantiates and returns the appropriate fare calculator object (`GeneralFareCalculator`, `SeniorCitizenFareCalculator`, `TatkalFareCalculator`). This abstracts the complex instantiation logic from the client.
*   **Singleton Pattern:** Implemented for system-wide, shared resources. `AuditLogger` is designed as a Singleton to maintain a single, consistent logging mechanism across the application. Similarly, `DatabaseConnectionPool` uses this pattern to efficiently manage and reuse database connections without the overhead of creating new instances.

---

### **Github link to the Codebase:**
*(Insert your public GitHub repository link here)*

---

### **Screenshots**
**UI:**
*(Insert screenshots of your application here. Recommended screenshots:)*
1. Login/Registration Page
2. Passenger Dashboard & Train Search Page
3. Train Search Results & Seat Availability
4. Ticket Booking & Passenger Details Form
5. Payment Selection & Confirmation Page
6. Generated PDF Ticket / PNR Status Page
7. Admin Dashboard & Add Train Page

---

### **Individual contributions of the team members:**
*(Fill out the table with your team members' names and the specific modules they worked on, e.g., "Frontend UI & Thymeleaf Integration", "Backend Booking Service & Payments", "Database Design & Admin Module", etc.)*
