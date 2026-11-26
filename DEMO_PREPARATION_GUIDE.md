# DEMO PREPARATION GUIDE
## IRCTC Railway Reservation System — Viva / Defense Cheat Sheet

---

## HOW TO EXPLAIN THE CLASS DIAGRAM (Step by Step)

When your teacher asks you to explain the class diagram, follow this order. Do NOT jump randomly between classes. Start from the top (domain model), then show patterns, then show architecture.

### Step 1: Start with the Domain Model (30 seconds)

Say: "The core of our system is built around these domain entities."

User is the base entity. It stores username, password, email, phone, and role. We use JPA JOINED inheritance, so Passenger and Administrator extend User. Passenger adds fields like fullName, age, gender, and has a one-to-many relationship with Ticket meaning one passenger can have many bookings. Administrator adds employeeCode, department, and designation. This demonstrates Liskov Substitution Principle because anywhere we expect a User object, we can safely pass either a Passenger or Administrator and the system works correctly. Spring Security's authentication works identically for both.

Train stores trainNo, trainName, source and destination stations, departure and arrival times, total seats, and available seats. It has a many-to-one relationship with Station for both source and destination.

Ticket is the central entity. It connects a Passenger to a Train for a specific journey date. It stores the PNR number, travel class, quota type, fare, status (CONFIRMED, WAITING, CANCELLED, etc.), coach number, seat numbers, and refund status. Each Ticket contains one or more PassengerDetail objects representing individual travelers on that booking.

Payment stores the payment transaction details linked to a Ticket.

PassengerWallet stores the wallet balance for each Passenger, and WalletTransaction logs every credit and debit operation.

### Step 2: Show the Design Patterns (2 minutes)

Say: "Now let me show how we applied design patterns in our system."

Point to the Singleton classes (orange in the diagram). AuditLogger uses lazy initialization with synchronized keyword. It has a private constructor and a static getInstance method. Only one instance exists throughout the application. We use it because multiple instances would compete for write access to the same audit.log file, causing data corruption. DatabaseConnectionPool uses double-checked locking with volatile keyword for thread-safe singleton creation. It manages a pool of 10 reusable database connections.

Point to the Factory classes (green). FareCalculator is the interface with one method calculateFare. FareCalculatorFactory takes a QuotaType enum and returns the correct calculator. GeneralFareCalculator multiplies base fare by number of passengers. TatkalFareCalculator multiplies by 1.3 for 30 percent surcharge. SeniorCitizenFareCalculator multiplies by 0.5 for 50 percent discount. The BookingService calls fareCalculatorFactory.getCalculator(quotaType) and then calls calculateFare on whatever object is returned. It never knows which specific calculator it is using.

Point to the Strategy classes (blue). PaymentStrategy is the interface with pay and getPaymentMethod methods. CreditCardPayment, UPIPayment, and NetBankingPayment each implement it differently. The PaymentService selects the right strategy at runtime based on user's choice. This means adding a new payment method like Cryptocurrency only requires creating a new class, no existing code changes.

Point to the Observer classes (yellow). TicketObserver is the interface with an update method. EmailNotifier and SMSNotifier implement it. When a booking, cancellation, or waitlist promotion happens, both observers are notified. If we add WhatsApp notifications later, we just create a new WhatsAppNotifier class implementing TicketObserver.

### Step 3: Show the Architecture (1 minute)

Say: "The system follows a layered MVC architecture."

Controllers (BookingController, AdminController, AuthController, WalletController) handle HTTP requests. They contain ZERO business logic. They only extract parameters, call service methods, and return views. This is the GRASP Controller pattern.

Service Layer (BookingService, PaymentService, UserService, WalletService, SeatAllocationService) contains all business logic. BookingService acts as a Facade, providing a single bookTicket method that internally orchestrates 9 different subsystem calls.

Repository Layer (TicketRepository, TrainRepository, UserRepository, etc.) provides database access through Spring Data JPA interfaces. Each repository is a separate, focused interface demonstrating Interface Segregation Principle. They also serve as Indirection between services and the database.

---

## EXPECTED QUESTIONS AND ANSWERS

### Q1: Why did you use the Singleton pattern for AuditLogger?

Answer: AuditLogger writes to a single audit.log file. If we had multiple instances, they would all try to write to the same file simultaneously, causing file corruption and data loss. The Singleton pattern ensures there is exactly one instance with one PrintWriter, so all log entries are serialized through a single point. We used synchronized on getInstance to ensure thread safety because in a web application, multiple HTTP request threads could call getInstance at the same time.

### Q2: Why did you use double-checked locking in DatabaseConnectionPool instead of just synchronized?

Answer: Simple synchronized on getInstance works but has a performance cost because every call to getInstance acquires a lock, even after the instance is already created. Double-checked locking first checks if instance is null without synchronization. Only if it is null, it enters the synchronized block and checks again. This means after the first creation, subsequent calls avoid the synchronization overhead entirely. The volatile keyword prevents instruction reordering that could cause another thread to see a partially constructed object.

### Q3: Why Factory pattern for fare calculation? Why not just use if-else?

Answer: If we used if-else in BookingService like "if quotaType is TATKAL then fare equals baseFare times 1.3, else if quotaType is SENIOR_CITIZEN then fare equals baseFare times 0.5", two problems arise. First, every time Indian Railways adds a new quota like Defence Personnel or Student, we would have to modify BookingService, which is the most critical class in the system. This violates the Open-Closed Principle. Second, the fare calculation logic is mixed with booking logic, violating Single Responsibility Principle. With the Factory pattern, adding a new quota means creating one new class like DefenceFareCalculator and adding one line in the factory switch case. BookingService remains completely untouched and untested code is never at risk.

### Q4: How does the Strategy pattern work in your payment system?

Answer: The PaymentStrategy interface defines a pay method and a getPaymentMethod method. We have three concrete strategies: CreditCardPayment which stores card number, expiry, and CVV; UPIPayment which stores the UPI ID; and NetBankingPayment which stores bank name and user ID. Each has its own pay method implementation with different validation and processing logic. In PaymentService, when a payment request comes in, the getPaymentStrategy method checks the payment method string and returns the appropriate strategy object. Then PaymentService calls strategy.pay(amount) without knowing which specific strategy it is using. The algorithm is selected at runtime. This is different from Factory because Factory creates objects while Strategy selects behavior.

### Q5: What is the difference between Factory and Strategy pattern in your project?

Answer: Factory pattern is about object creation. FareCalculatorFactory creates the right FareCalculator object based on quota type. The client does not know which concrete class is instantiated. Strategy pattern is about behavior selection. PaymentService selects the right payment algorithm at runtime. The key difference is intent. Factory answers the question "which object should I create?" while Strategy answers "which algorithm should I use?" In our project, Factory is used where the type of object matters (different fare formulas), and Strategy is used where the algorithm varies (different payment processing flows).

### Q6: Explain the Observer pattern in your project.

Answer: When a ticket's status changes, multiple systems need to be notified. Email system needs to send an email, SMS system needs to send a text message. Instead of BookingService having hardcoded calls to each notification system, we defined a TicketObserver interface with an update method that takes the ticket, old status, and new status. EmailNotifier and SMSNotifier each implement this interface. In BookingService, after a booking or cancellation, we call emailNotifier.update() and smsNotifier.update(). The one-to-many relationship is: one event (ticket status change) triggers multiple observers (email and SMS). If tomorrow we need to add push notifications, we create PushNotifier implementing TicketObserver, register it, and BookingService adds one line. No existing notification code changes.

### Q7: Where is the Facade pattern in your project?

Answer: BookingService is the Facade. The bookTicket method is the simplified entry point. Behind this single method call, the following complex subsystem operations happen: train details are fetched from TrainRepository, seat availability is checked from TicketRepository, Tatkal eligibility is verified from TrainTatkalRepository, fare is calculated using FareCalculatorFactory and the appropriate FareCalculator, a PNR is generated using PNRGenerator, PassengerDetail objects are created, payment is processed through WalletService or PaymentService, seats are allocated by SeatAllocationService, and notifications are sent through EmailNotifier and SMSNotifier. The controller only calls bookingService.bookTicket() and gets back a Ticket object. It has no knowledge of the 9 subsystem classes involved. Without this Facade, the controller would need to call each subsystem individually, making it extremely complex and tightly coupled.

### Q8: Where is the Proxy pattern?

Answer: The Proxy pattern is used implicitly through Spring's @Transactional annotation. Every service method annotated with @Transactional is wrapped in a dynamic proxy at runtime by Spring's AOP mechanism. When BookingController calls bookingService.cancelTicket(), it is actually calling the proxy's cancelTicket method. The proxy opens a database transaction before the method executes. If the method completes successfully, the proxy commits the transaction. If any exception is thrown, the proxy rolls back all database changes. For example, in cancelTicket, the ticket status is updated, available seats are incremented, refund status is set, and waitlisted tickets are promoted. If the waitlist promotion fails, the proxy rolls back everything, including the ticket cancellation, ensuring the database is never in an inconsistent state. This is the Proxy pattern because the proxy object controls access to the real BookingService object.

### Q9: Explain the Iterator pattern usage.

Answer: The Iterator pattern is used in the promoteWaitlist method. When a confirmed ticket is cancelled, seats are released. The system retrieves all waitlisted tickets for that train and date, sorts them by booking date (first-come-first-served using Comparator), and then iterates through them sequentially using a for-each loop. For each waitlisted ticket, it checks if enough seats are available, promotes the ticket from WAITING to CONFIRMED, allocates seats, and sends notifications. The iteration continues until either all released seats are used or all waitlisted tickets are processed. Java's for-each loop internally uses the Iterator interface. We do not use array indices. The iterator provides a clean, sequential traversal without exposing the underlying List structure.

### Q10: Explain SOLID principles with one example each from your project.

Answer:

SRP (Single Responsibility): WalletService has only three methods — addFunds, deductFunds, refundFunds. It handles only wallet operations. If wallet rules change, only this class changes.

OCP (Open-Closed): FareCalculator interface is open for extension. We can add a DefenceFareCalculator without modifying existing calculators or BookingService. Closed for modification.

LSP (Liskov Substitution): Passenger and Administrator both extend User. Spring Security authentication works with both. Anywhere the system expects a User, either subtype works correctly.

ISP (Interface Segregation): We have 11 separate repository interfaces instead of one big DatabaseInterface. BookingService depends on TicketRepository and TrainRepository only. It does not depend on StationRepository or WalletTransactionRepository that it does not need.

DIP (Dependency Inversion): BookingService depends on the PasswordEncoder interface, not on BCryptPasswordEncoder directly. The service layer depends on JPA repository abstractions, not on specific database implementations. We switched from MySQL to H2 by changing only application.properties. Zero Java code changes.

### Q11: Explain GRASP principles with examples.

Answer:

Creator: BookingService creates Ticket objects because it has all the required data — train, passenger, fare, date, class type, quota type.

Information Expert: Ticket class has canBeCancelled() and getRefundAmount() methods because it owns the status, journey date, and fare data needed for those decisions.

Low Coupling: BookingService does not create EmailNotifier objects directly. They are injected through the constructor. If email implementation changes, BookingService is unaffected.

High Cohesion: Each service class is focused. WalletService does only wallet operations. SeatAllocationService does only seat assignment. AuditLogger does only logging.

Controller: BookingController, AdminController handle HTTP requests but contain zero business logic. They delegate everything to services.

Polymorphism: FareCalculatorFactory returns different FareCalculator implementations. BookingService calls calculateFare() without if-else checks. The correct formula runs automatically.

Pure Fabrication: AuditLogger, DatabaseConnectionPool, and PNRGenerator do not represent real-world railway objects. They are software constructs created for separation of concerns.

Indirection: JPA Repository interfaces act as intermediaries between services and the database. FareCalculatorFactory acts as intermediary between BookingService and fare calculators.

Protected Variations: PaymentStrategy interface shields PaymentService from variations in payment gateway APIs. TicketObserver interface shields BookingService from changes in notification mechanisms.

### Q12: What anti-patterns did you avoid?

Answer:

God Object: BookingService at 364 lines could have become a God Object but it delegates fare calculation to FareCalculatorFactory, payment to PaymentService, wallet to WalletService, seat allocation to SeatAllocationService, and notifications to EmailNotifier and SMSNotifier.

Magic Numbers: We use ClassType enum with named constants like FIRST_AC(1000) instead of hardcoding 1000 everywhere. The only magic numbers remaining are 1.3 and 0.5 in fare calculators, but the class names TatkalFareCalculator and SeniorCitizenFareCalculator provide context.

Analysis Paralysis: We used iterative development. Started with basic booking, then added wallet, then Tatkal, then seat allocation, then waitlist promotion, then PDF generation. We did not try to design everything perfectly upfront.

### Q13: Can you show me the code for Singleton pattern?

Answer: Open AuditLogger.java. Show:
- Private static instance field
- Private constructor (prevents new AuditLogger() from outside)
- Public static synchronized getInstance() method
- First call creates instance, subsequent calls return same instance
- log() method writes timestamp, username, action, details to audit.log file

### Q14: What happens when a ticket is cancelled? Walk me through the flow.

Answer: 
1. Passenger clicks Cancel on the booking history page
2. BookingController receives the request and calls bookingService.cancelTicket(pnr, passenger)
3. BookingService finds the ticket by PNR
4. Ticket.canBeCancelled() checks if status is valid, chart is not prepared, and journey date has not passed
5. Ticket status is set to CANCELLED
6. Train's available seats are incremented by the number of passengers on that ticket
7. Refund status is set to REQUESTED
8. EmailNotifier.update() and SMSNotifier.update() are called (Observer pattern)
9. promoteWaitlist() is called — retrieves all waitlisted tickets for same train and date, sorts by booking date, promotes them to CONFIRMED one by one if seats are available, allocates seats via SeatAllocationService, and notifies each promoted passenger
10. AuditLogger.getInstance().log() records the cancellation event
11. All of this happens inside a @Transactional method (Proxy pattern), so if any step fails, everything rolls back

### Q15: Why did you use H2 instead of MySQL?

Answer: This demonstrates the Dependency Inversion Principle. Our service layer depends on JPA repository abstractions, not on any specific database. We switched from MySQL to H2 by changing only three lines in application.properties — the JDBC URL, driver class, and Hibernate dialect. Zero Java source files were modified. This proves our architecture correctly separates high-level business logic from low-level database details.

---

## DEMO FLOW (Recommended Order for Presentation)

### Step 1: Architecture Overview (2 minutes)
Open the class diagram. Walk through domain model, then patterns, then architecture as described above.

### Step 2: Show Running Application (1 minute)
Open browser, go to http://localhost:9090/auth/login. Show the custom login page. Login as admin/admin123. Show the admin dashboard with 100 trains.

### Step 3: Show Booking Flow (3 minutes)
Logout. Register a new passenger account. Login as passenger. Search for trains (e.g., SBC to NDLS). Select a train. Choose class and quota. Add passenger details. Book with wallet. Show the confirmation page with PNR. Download the PDF ticket.

### Step 4: Show Cancellation and Waitlist Promotion (2 minutes)
Cancel the ticket. Show the refund request. Login as admin. Approve the refund. Show how wallet balance is updated.

### Step 5: Show Code for Patterns (2 minutes)
Open AuditLogger.java (Singleton). Open FareCalculatorFactory.java (Factory). Open PaymentStrategy.java and CreditCardPayment.java (Strategy). Open TicketObserver.java and EmailNotifier.java (Observer). Show BookingService.bookTicket() (Facade). Point out @Transactional annotation (Proxy).

### Step 6: Answer Questions
Use the Q&A section above.

---

## QUICK REFERENCE: WHERE EACH PATTERN IS IN CODE

| Pattern | File Location | Key Method/Field |
|---------|--------------|-----------------|
| Singleton | pattern/singleton/AuditLogger.java | getInstance(), private constructor |
| Singleton | pattern/singleton/DatabaseConnectionPool.java | volatile instance, double-checked locking |
| Factory | pattern/factory/FareCalculatorFactory.java | getCalculator(QuotaType) |
| Factory | pattern/factory/FareCalculator.java | calculateFare(Ticket) interface |
| Strategy | pattern/strategy/PaymentStrategy.java | pay(amount), getPaymentMethod() |
| Strategy | pattern/strategy/CreditCardPayment.java | pay() implementation |
| Observer | pattern/observer/TicketObserver.java | update(ticket, oldStatus, newStatus) |
| Observer | pattern/observer/EmailNotifier.java | update() sends email notification |
| Facade | service/BookingService.java | bookTicket() method (lines 58-165) |
| Proxy | service/BookingService.java | @Transactional annotation on cancelTicket() |
| Iterator | service/BookingService.java | promoteWaitlist() method (lines 343-362) |

## QUICK REFERENCE: WHERE EACH PRINCIPLE IS IN CODE

| Principle | Example | File |
|-----------|---------|------|
| SRP | WalletService does only wallet ops | service/WalletService.java |
| OCP | New fare calculators without modifying existing | pattern/factory/ |
| LSP | Passenger and Admin both work as User | model/User.java, Passenger.java |
| ISP | 11 separate focused repository interfaces | repository/ |
| DIP | Service depends on interfaces, not implementations | service/BookingService.java constructor |
| Creator | BookingService creates Ticket objects | service/BookingService.java line 98 |
| Information Expert | Ticket.canBeCancelled() | model/Ticket.java line 148 |
| Low Coupling | Constructor injection, interfaces | All service classes |
| High Cohesion | WalletService has only 3 related methods | service/WalletService.java |
| Controller | BookingController has zero business logic | controller/BookingController.java |
| Polymorphism | fareCalculator.calculateFare() dispatches correctly | service/BookingService.java line 116 |
| Pure Fabrication | AuditLogger, PNRGenerator | pattern/singleton/, util/ |
| Indirection | JPA Repositories between services and DB | repository/ |
| Protected Variations | PaymentStrategy shields from gateway changes | pattern/strategy/ |
