# TEAM CONTRIBUTION DIVISION
## IRCTC Railway Reservation System — 4 Members

---

## USE CASES OVERVIEW

### 4 Major Use Cases
1. Train Search & Ticket Booking (with fare calculation and payment)
2. Ticket Cancellation & Waitlist Promotion
3. Admin Train Management & Tatkal Enabling
4. User Registration, Authentication & Role-Based Access

### 4 Minor Use Cases
5. IRCTC Wallet (Add Funds, Pay via Wallet, Refund to Wallet)
6. PDF E-Ticket Download
7. Refund Request & Admin Approval/Rejection
8. Admin Reports (Revenue & Booking Reports)

---

## MEMBER-WISE CONTRIBUTION

---

### MEMBER 1 — Booking Module & Factory Pattern

**Use Cases Handled:**
- Major: Train Search & Ticket Booking
- Minor: PDF E-Ticket Download

**Design Patterns Owned:**
- Factory Method Pattern (FareCalculatorFactory)
- Facade Pattern (BookingService)

**SOLID Principles to Explain:**
- Open-Closed Principle (new fare calculators without changing existing code)
- Single Responsibility Principle (each fare calculator has one job)

**GRASP Principles to Explain:**
- Creator (BookingService creates Ticket objects)
- Information Expert (ClassType enum stores base fare data)
- Polymorphism (fareCalculator.calculateFare() dispatches to correct formula)

**Files Owned:**
- pattern/factory/FareCalculator.java (interface)
- pattern/factory/FareCalculatorFactory.java
- pattern/factory/GeneralFareCalculator.java
- pattern/factory/TatkalFareCalculator.java
- pattern/factory/SeniorCitizenFareCalculator.java
- service/BookingService.java (bookTicket method, searchTrains method)
- service/PdfGeneratorService.java
- service/SeatAllocationService.java
- controller/BookingController.java (search, book, download endpoints)
- dto/BookingRequest.java

**What to Say in Demo:**
"I built the core booking engine. When a passenger books a ticket, BookingService acts as a Facade — it coordinates train lookup, seat availability check, fare calculation, payment, seat allocation, and notifications through a single bookTicket() method. For fare calculation, I implemented the Factory Pattern. FareCalculatorFactory receives the quota type and returns the right calculator — GeneralFareCalculator for standard fare, TatkalFareCalculator for 30% surcharge, SeniorCitizenFareCalculator for 50% discount. This follows the Open-Closed Principle because adding a new quota like Defence Personnel only requires creating one new class. BookingService stays untouched. I also built the PDF e-ticket generation using OpenPDF library."

---

### MEMBER 2 — Payment Module & Strategy Pattern

**Use Cases Handled:**
- Major: Ticket Cancellation & Waitlist Promotion
- Minor: IRCTC Wallet (Add Funds, Deduct, Refund)

**Design Patterns Owned:**
- Strategy Pattern (PaymentStrategy)
- Iterator Pattern (waitlist promotion)

**SOLID Principles to Explain:**
- Dependency Inversion Principle (services depend on interfaces, not implementations)
- Interface Segregation Principle (11 separate repository interfaces)

**GRASP Principles to Explain:**
- Low Coupling (PaymentService depends on PaymentStrategy interface, not concrete classes)
- Protected Variations (PaymentStrategy shields from payment gateway changes)
- Indirection (Repository interfaces between service and database)

**Files Owned:**
- pattern/strategy/PaymentStrategy.java (interface)
- pattern/strategy/CreditCardPayment.java
- pattern/strategy/UPIPayment.java
- pattern/strategy/NetBankingPayment.java
- service/PaymentService.java
- service/WalletService.java
- service/BookingService.java (cancelTicket method, promoteWaitlist method)
- controller/WalletController.java
- model/Payment.java
- model/PassengerWallet.java
- model/WalletTransaction.java

**What to Say in Demo:**
"I built the payment and cancellation system. For payments, I implemented the Strategy Pattern. PaymentStrategy is the interface with a pay() method. CreditCardPayment, UPIPayment, and NetBankingPayment each implement it with their own logic. PaymentService selects the right strategy at runtime — the algorithm varies but the interface stays the same. This follows DIP because PaymentService depends on the PaymentStrategy abstraction, not concrete classes. For cancellation, I built the waitlist promotion algorithm that uses the Iterator Pattern — when a confirmed ticket is cancelled, the system iterates through waitlisted tickets sorted by booking date and promotes them to CONFIRMED. I also built the wallet system for instant payments and refunds."

---

### MEMBER 3 — Notification Module & Observer + Singleton Patterns

**Use Cases Handled:**
- Major: User Registration, Authentication & Role-Based Access
- Minor: Refund Request & Admin Approval/Rejection

**Design Patterns Owned:**
- Observer Pattern (TicketObserver, EmailNotifier, SMSNotifier)
- Singleton Pattern (AuditLogger, DatabaseConnectionPool)

**SOLID Principles to Explain:**
- Liskov Substitution Principle (Passenger and Administrator both extend User safely)
- Single Responsibility Principle (UserService handles only user operations)

**GRASP Principles to Explain:**
- High Cohesion (each service class is focused on one area)
- Pure Fabrication (AuditLogger and DatabaseConnectionPool are software constructs, not domain objects)
- Information Expert (Ticket.canBeCancelled() and Ticket.getRefundAmount())

**Files Owned:**
- pattern/observer/TicketObserver.java (interface)
- pattern/observer/EmailNotifier.java
- pattern/observer/SMSNotifier.java
- pattern/singleton/AuditLogger.java
- pattern/singleton/DatabaseConnectionPool.java
- service/UserService.java
- controller/AuthController.java
- config/SecurityConfig.java
- config/PasswordEncoderConfig.java
- model/User.java
- model/Passenger.java
- model/Administrator.java
- dto/LoginRequest.java
- dto/RegisterRequest.java

**What to Say in Demo:**
"I built the user authentication system and the notification framework. For authentication, Spring Security handles login with role-based access — Passengers go to /passenger/dashboard, Admins go to /admin/dashboard. This uses LSP because both Passenger and Administrator extend User, and Spring Security authenticates both identically. For notifications, I implemented the Observer Pattern. TicketObserver is the interface. EmailNotifier and SMSNotifier implement it. When a booking, cancellation, or waitlist promotion happens, both observers are notified. Adding WhatsApp notifications just needs one new class. For audit logging, I implemented the Singleton Pattern with synchronized getInstance() to ensure thread-safe, centralized logging. DatabaseConnectionPool uses double-checked locking with volatile for even better performance."

---

### MEMBER 4 — Admin Module & Proxy Pattern + Database Design

**Use Cases Handled:**
- Major: Admin Train Management & Tatkal Enabling
- Minor: Admin Reports (Revenue & Booking Reports)

**Design Patterns Owned:**
- Proxy Pattern (@Transactional — Spring AOP dynamic proxies)
- Facade Pattern (BookingService as simplified interface to subsystems)

**SOLID Principles to Explain:**
- Dependency Inversion Principle (switched MySQL to H2 with zero Java code changes)
- Open-Closed Principle (new train types, new report types without modifying existing code)

**GRASP Principles to Explain:**
- Controller (AdminController handles HTTP requests with zero business logic)
- Creator (DataInitializer creates Station, Train, and Admin objects)
- Indirection (JPA Repositories as intermediaries between service and database)

**Files Owned:**
- controller/AdminController.java
- service/TrainService.java
- service/ReportService.java
- config/DataInitializer.java (50 stations, 105 trains)
- model/Train.java
- model/Station.java
- model/TrainTatkal.java
- model/Ticket.java (entity with canBeCancelled, getRefundAmount)
- model/enums/ (all 7 enum types)
- dto/ReportCriteria.java
- All 11 repository interfaces (repository package)
- templates/admin/ (all admin HTML templates)

**What to Say in Demo:**
"I built the admin module and the database layer. Administrators can manage trains — add, delete, and enable Tatkal for specific dates. I also built the reporting system that generates revenue and booking reports by date range. For the database, I designed 14 tables and created 11 repository interfaces following ISP — each interface is focused on one entity. The Proxy Pattern is used through @Transactional annotation. Spring generates dynamic proxy classes at runtime that wrap service methods. The proxy opens a transaction before execution and commits or rolls back after. This ensures data consistency — for example, if cancellation succeeds but wallet refund fails, everything rolls back. I also initialized the system with 50 real Indian railway stations and 105 trains across 7 categories."

---

## SUMMARY TABLE

| Member | Major Use Case | Minor Use Case | Design Patterns | SOLID | GRASP |
|--------|---------------|----------------|-----------------|-------|-------|
| 1 | Train Search & Booking | PDF E-Ticket | Factory, Facade | OCP, SRP | Creator, Info Expert, Polymorphism |
| 2 | Cancellation & Waitlist | Wallet | Strategy, Iterator | DIP, ISP | Low Coupling, Protected Variations, Indirection |
| 3 | Registration & Auth | Refund Management | Observer, Singleton | LSP, SRP | High Cohesion, Pure Fabrication, Info Expert |
| 4 | Admin Train Mgmt | Reports | Proxy, Facade | DIP, OCP | Controller, Creator, Indirection |

---

## TOTAL COUNT

| Category | Minimum Required | We Have |
|----------|-----------------|---------|
| Design Patterns | 4 | 7 (Singleton, Factory, Strategy, Observer, Facade, Proxy, Iterator) |
| Major Use Cases | 4 | 4 (Booking, Cancellation, Admin Train Mgmt, Auth) |
| Minor Use Cases | 4 | 4 (Wallet, PDF, Refund, Reports) |
| SOLID Principles | — | All 5 demonstrated |
| GRASP Principles | — | All 9 demonstrated |
| Anti-Patterns | — | 3 identified and avoided |
