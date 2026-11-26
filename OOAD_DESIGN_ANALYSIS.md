# OOAD Design Principles and Patterns Analysis
## IRCTC Railway Reservation System

---

## PART A: SOLID PRINCIPLES

### 1. Single Responsibility Principle (SRP)

The Single Responsibility Principle states that a class should have one and only one reason to change, meaning it should have only one job or responsibility.

In the IRCTC Reservation System, SRP is followed rigorously across the entire project through a well-defined layered architecture. Each class in the system has a single, focused responsibility.

The BookingService class located at com.irctc.service.BookingService is responsible solely for the orchestration of the ticket booking workflow. It handles seat availability checking, fare calculation delegation, ticket creation, and triggering notifications. It does not handle how payments are processed internally, how seats are physically allocated, or how wallet balances are managed. Those responsibilities are delegated to dedicated classes.

The WalletService class at com.irctc.service.WalletService handles only wallet-related operations such as adding funds, deducting funds, and processing refunds to the wallet. It does not know anything about trains, tickets, or booking logic. If the wallet system needs to change, for example by adding a new transaction fee, only this one class needs to be modified.

The PaymentService class at com.irctc.service.PaymentService is solely responsible for processing financial transactions through external payment gateways like Credit Card, UPI, and Net Banking. It does not manage tickets or users.

The SeatAllocationService class at com.irctc.service.SeatAllocationService has the sole duty of determining which coach number and seat number a passenger gets. It uses the ClassType enum to determine coach prefixes like H for First AC, A for Second AC, B for Third AC, and S for Sleeper. This logic is completely isolated from fare calculation or payment processing.

The UserService class at com.irctc.service.UserService is responsible only for user management operations including registration, login, profile updates, password changes, and enabling or disabling users. It does not deal with trains or tickets.

Each controller class has the single responsibility of routing HTTP requests. For example, BookingController at com.irctc.controller.BookingController only maps URLs to service methods. It contains no business logic whatsoever. It receives a request, calls the appropriate service method, and returns a view.

Justification: By following SRP, if the fare calculation rules change due to a government regulation, only the FareCalculator classes need to be modified. The BookingService, PaymentService, and all controllers remain completely untouched. This makes the system highly maintainable and reduces the risk of introducing bugs when making changes.

---

### 2. Open-Closed Principle (OCP)

The Open-Closed Principle states that software entities should be open for extension but closed for modification. This means you should be able to add new functionality without changing existing, tested code.

In the IRCTC system, OCP is demonstrated primarily in two areas.

The first area is the FareCalculator interface at com.irctc.pattern.factory.FareCalculator. This interface defines a single method called calculateFare that takes a Ticket object and returns a double. Currently, the system has three concrete implementations. The GeneralFareCalculator calculates the standard fare by multiplying the base fare of the travel class by the number of passengers. The TatkalFareCalculator extends the base fare by applying a 30 percent surcharge, multiplying the result by 1.3. The SeniorCitizenFareCalculator applies a 50 percent discount by multiplying the base fare by 0.5. If the Indian Railways introduces a new quota such as a Defence Personnel Quota with a 25 percent discount, a developer only needs to create a new DefenceFareCalculator class that implements FareCalculator and add one line in the FareCalculatorFactory switch case. The GeneralFareCalculator, TatkalFareCalculator, SeniorCitizenFareCalculator, and BookingService classes remain completely unmodified.

The second area is the PaymentStrategy interface at com.irctc.pattern.strategy.PaymentStrategy. This interface defines two methods, pay and getPaymentMethod. The system currently has three implementations which are CreditCardPayment, UPIPayment, and NetBankingPayment. Each handles its own payment processing logic independently. If a new payment method like Cryptocurrency needs to be supported, a new CryptoPayment class implementing PaymentStrategy is all that is needed. No existing payment code is modified.

Justification: OCP ensures that the system can grow and adapt to new business requirements such as new quotas, new payment methods, and new notification channels without risking the stability of existing, tested features. This is critical in a railway reservation system where stability and reliability are paramount.

---

### 3. Liskov Substitution Principle (LSP)

The Liskov Substitution Principle states that objects of a superclass should be replaceable with objects of its subclasses without affecting the correctness of the program.

In the IRCTC system, LSP is demonstrated in multiple inheritance hierarchies.

The User and its subclasses follow LSP. The User class at com.irctc.model.User is the base entity that implements the Spring Security UserDetails interface. The Passenger class at com.irctc.model.Passenger extends User with additional fields such as fullName, age, gender, dateOfBirth, and address. The Administrator class at com.irctc.model.Administrator extends User with fields such as employeeCode, department, and designation. Anywhere in the codebase that expects a User object, such as SecurityConfig.java which builds a Spring Security user from the User entity, both Passenger and Administrator objects can be substituted without any issues. The system correctly determines whether to redirect to the admin dashboard or the passenger dashboard based on the role, but the authentication mechanism works identically for both.

The TicketObserver interface and its implementations also follow LSP. The TicketObserver interface at com.irctc.pattern.observer.TicketObserver defines a single update method. The EmailNotifier sends an email notification when called. The SMSNotifier sends an SMS notification when called. In the BookingService, both notifiers are called with the exact same method signature. The BookingService does not care which specific type of notifier it is working with. It simply calls update on whatever TicketObserver implementation is provided, and the correct notification is sent. If a new PushNotifier were added, it could replace either EmailNotifier or SMSNotifier without breaking anything.

The FareCalculator implementations also follow LSP. The BookingService calls fareCalculator.calculateFare(ticket) without knowing whether it is dealing with a GeneralFareCalculator, TatkalFareCalculator, or SeniorCitizenFareCalculator. Any subtype can be substituted, and the correct fare is returned every time.

Justification: LSP ensures that the system's polymorphic behavior is safe and predictable. When the BookingService calls emailNotifier.update() and smsNotifier.update(), it can trust that both will behave correctly because they conform to the TicketObserver contract. This eliminates runtime errors caused by unexpected subtype behavior.

---

### 4. Interface Segregation Principle (ISP)

The Interface Segregation Principle states that no client should be forced to depend on interfaces it does not use. It is better to have many small, specific interfaces than one large, general-purpose interface.

In the IRCTC system, ISP is followed through the use of multiple focused repository interfaces instead of a single monolithic database interface.

The system has 11 separate repository interfaces. TrainRepository handles only train-related queries such as finding trains by route and finding active trains. TicketRepository handles only ticket-related queries such as counting confirmed passenger seats, counting waitlisted seats, finding tickets by PNR, and finding tickets by journey date and status. UserRepository handles only user lookup operations such as finding users by username, email, or phone, and checking if a user exists. PassengerRepository handles only passenger-specific queries. PaymentRepository handles only payment-related queries. WalletTransactionRepository handles only wallet transaction retrieval. StationRepository handles only station-related queries. Each of these repositories is a separate Spring Data JPA interface.

The BookingService depends on TicketRepository, TrainRepository, and TrainTatkalRepository. It does not depend on UserRepository, StationRepository, or WalletTransactionRepository because it does not need them. The WalletService depends only on PassengerWalletRepository and WalletTransactionRepository. It does not need to know about trains or tickets.

Similarly, the PaymentStrategy interface has only two methods, pay and getPaymentMethod. It does not force implementing classes to define notification logic, fare logic, or seat allocation logic.

Justification: ISP prevents unnecessary coupling. If the StationRepository interface changes, such as adding a new search method, the BookingService is completely unaffected because it does not depend on StationRepository. This makes the system modular and easier to test because each class can be tested with mock implementations of only the interfaces it actually uses.

---

### 5. Dependency Inversion Principle (DIP)

The Dependency Inversion Principle states that high-level modules should not depend on low-level modules. Both should depend on abstractions. Abstractions should not depend on details. Details should depend on abstractions.

In the IRCTC system, DIP is implemented through constructor-based dependency injection, which is powered by the Spring Framework's IoC (Inversion of Control) container.

The BookingService class demonstrates DIP clearly. Its constructor accepts nine interface or abstract dependencies: TicketRepository, TrainRepository, PaymentService, FareCalculatorFactory, EmailNotifier, SMSNotifier, TrainTatkalRepository, WalletService, and SeatAllocationService. The BookingService never uses the new keyword to create any of these dependencies. It does not know which specific database driver is being used, whether it is MySQL or H2. It does not know how the FareCalculatorFactory internally selects a fare calculator. It only knows the interfaces and method signatures.

The UserService class also follows DIP. It depends on the PasswordEncoder interface, not on BCryptPasswordEncoder directly. The actual encoder implementation is configured in the PasswordEncoderConfig class. If the system needs to switch from BCrypt to Argon2 password encoding, only the configuration class changes. The UserService remains untouched.

The SecurityConfig class depends on the UserService abstraction for loading user details, not on raw database queries. It does not know or care how users are stored.

The entire application.properties configuration demonstrates DIP at the infrastructure level. By simply changing spring.datasource.url from jdbc:mysql://localhost:3306/irctc_db to jdbc:h2:mem:irctc_db and changing the dialect from MySQLDialect to H2Dialect, the entire database layer swaps from MySQL to H2 without modifying a single line of Java code. This is only possible because the service layer depends on JPA repository abstractions, not on concrete database implementations.

Justification: DIP makes the system extremely flexible. The transition from MySQL to the H2 in-memory database for development and testing purposes was achieved by changing only configuration properties. No service class, repository interface, or controller had to be modified. This proves that the high-level business logic is genuinely decoupled from the low-level database implementation.

---

## PART B: GRASP PRINCIPLES

### 1. Creator

The Creator principle states that a class B should be responsible for creating instances of class A if B aggregates A, B contains A, B closely uses A, or B has the initializing data for A.

In the IRCTC system, the BookingService is the Creator for Ticket objects. When a user books a ticket, the BookingService has all the required data to create the Ticket. It holds the Train details fetched from TrainRepository, the Passenger object passed from the controller, the journey date, the class type, and the quota type. Line 98-100 in BookingService.java shows the creation: a new Ticket object is instantiated with the generated PNR, train, passenger, journey date, class type, quota type, and fare. The BookingService also creates PassengerDetail objects for each passenger on the booking (lines 104-112).

Justification: The BookingService is the natural Creator because it aggregates all the information required to build a Ticket. Placing Ticket creation in the controller or in a utility class would violate the Creator principle because those classes do not possess the train availability data, waitlist calculations, or fare information.

---

### 2. Information Expert

The Information Expert principle states that responsibility should be assigned to the class that has the information necessary to fulfill that responsibility.

In the IRCTC system, the SeatAllocationService is the Information Expert for seat assignment. It possesses knowledge of coach naming conventions (H for First AC, A for Second AC, B for Third AC, S for Sleeper, C for AC Chair Car, D for Second Sitting), berth rotation patterns (Lower, Middle, Upper, Side Lower, Side Upper using modulo 8 arithmetic), and the seat numbering algorithm. No other class in the system has this knowledge, making SeatAllocationService the undisputed expert.

The Ticket class itself is the Information Expert for determining whether it can be cancelled. The canBeCancelled method at line 148-155 in Ticket.java checks its own status, whether the chart is prepared, and whether the journey date has passed. It also calculates its own refund amount in the getRefundAmount method at lines 157-179, using its own status, fare, and departure time data.

The ClassType enum is the Information Expert for base fare data. Each enum constant stores its own base fare value (First AC costs 1000, Second AC costs 600, Third AC costs 400, Sleeper costs 200). The fare calculators call ticket.getClassType().getBaseFare() to retrieve this information.

Justification: Placing the cancellation eligibility check in BookingService or a controller would mean those classes need to access and understand ticket internals. By keeping it in the Ticket class, the logic stays with the data it operates on, reducing coupling and improving maintainability.

---

### 3. Low Coupling

Low Coupling means that classes should have minimal dependencies on each other. Changes in one class should have minimal impact on other classes.

In the IRCTC system, low coupling is achieved through interfaces and dependency injection. The BookingService does not directly create EmailNotifier or SMSNotifier objects. They are injected through the constructor. If the email notification system changes from console printing to actual SMTP, only the EmailNotifier class changes. BookingService is unaffected.

The PaymentService achieves low coupling through the PaymentStrategy interface. It does not hardcode payment logic. It delegates to whichever PaymentStrategy implementation is selected at runtime. The UserService uses the PasswordEncoder interface instead of depending on a specific encoder implementation.

Justification: Low coupling enables independent development, testing, and modification of components. The wallet system, payment system, notification system, and seat allocation system can all evolve independently without cascading changes through the codebase.

---

### 4. High Cohesion

High Cohesion means that the responsibilities within a single class should be closely related and focused on a single purpose.

In the IRCTC system, each service class is highly cohesive. WalletService contains only three methods: addFunds, deductFunds, and refundFunds. All three are directly related to wallet balance management. SeatAllocationService contains only allocateSeats, getCoachPrefix, and getBerthType. All three are directly related to the physical seat assignment process. The AuditLogger class contains only log and close methods, both focused on audit trail management.

Justification: High cohesion makes each class easy to understand, test, and maintain. A developer looking at WalletService immediately knows it handles wallet operations and nothing else.

---

### 5. Controller (GRASP)

The Controller pattern assigns responsibility for handling system events to a class that represents the overall system or a use case scenario.

In the IRCTC system, the MVC Controller layer consists of dedicated controller classes. BookingController handles all passenger-side booking operations including search, booking, cancellation, refund requests, and ticket PDF downloads. AdminController handles all administrator operations including train management, refund approvals, and user management. AuthController handles authentication operations including login and registration. WalletController handles wallet-related HTTP requests.

These controllers contain zero business logic. They only extract parameters from HTTP requests, call the appropriate service methods, and return views with model data. For example, in BookingController line 127-153, the bookTicket method extracts the BookingRequest from the form, gets the current user, calls bookingService.bookTicket(), and returns either a confirmation view or an error view.

Justification: Separating controllers from services ensures that business logic is reusable. The same BookingService could be used by a REST API controller, a mobile app backend, or a scheduled batch process without any duplication.

---

### 6. Polymorphism

The Polymorphism principle states that responsibility for defining the variation of behaviors based on type should be assigned to the types for which this variation happens, using polymorphic operations.

In the IRCTC system, polymorphism is used extensively in the fare calculation system. The FareCalculatorFactory at com.irctc.pattern.factory.FareCalculatorFactory receives a QuotaType enum and returns the appropriate FareCalculator implementation. The BookingService at line 95-96 calls fareCalculatorFactory.getCalculator(request.getQuotaType()) and then calls fareCalculator.calculateFare(ticket). It does not use if-else checks to determine which calculation formula to apply. The polymorphic dispatch handles this automatically.

Similarly, the PaymentStrategy interface enables polymorphic behavior in payment processing. The PaymentService at line 37-39 calls strategy.pay(amount), and the correct payment processing logic executes based on whether the strategy is CreditCardPayment, UPIPayment, or NetBankingPayment.

Justification: Polymorphism eliminates complex conditional logic. Instead of writing a massive if-else block such as if quotaType equals TATKAL then multiply by 1.3, else if quotaType equals SENIOR_CITIZEN then multiply by 0.5, the system delegates to the correct object. This is cleaner, more extensible, and less error-prone.

---

### 7. Pure Fabrication

Pure Fabrication states that when no appropriate real-world domain class exists to assign a responsibility, a new class should be fabricated to support high cohesion, low coupling, and reuse.

In the IRCTC system, several classes are pure fabrications because they do not represent real-world railway domain objects.

The AuditLogger at com.irctc.pattern.singleton.AuditLogger is a pure fabrication. There is no real-world entity called an AuditLogger in the Indian Railways domain. It was fabricated purely to provide a centralized, thread-safe logging mechanism for tracking all user actions such as REGISTER, LOGIN, BOOK_TICKET, CANCEL_TICKET, and CHANGE_PASSWORD.

The DatabaseConnectionPool at com.irctc.pattern.singleton.DatabaseConnectionPool is a pure fabrication. It is a technical infrastructure class that has no counterpart in the railway domain. It was fabricated to manage a pool of reusable database connections efficiently.

The PNRGenerator at com.irctc.util.PNRGenerator is a pure fabrication. It is a utility class that generates unique 10-digit PNR numbers. The concept of a PNR exists in the real world, but a dedicated generator class is a software construct.

Justification: Without these fabricated classes, their responsibilities would have been scattered across domain classes like Ticket or User, reducing cohesion. By fabricating dedicated classes, the system maintains clean separation of concerns.

---

### 8. Indirection

The Indirection principle states that an intermediate object can be assigned to mediate between components to reduce direct coupling.

In the IRCTC system, the Spring Data JPA Repository interfaces serve as indirection between the service layer and the database. The BookingService never writes raw SQL queries or calls JDBC methods. It calls ticketRepository.save(ticket) or trainRepository.findById(trainNo). The repository interface acts as an intermediary that translates these method calls into actual database operations. If the database engine changes from H2 to MySQL to PostgreSQL, the service layer remains unchanged because the repository abstraction absorbs the variation.

The FareCalculatorFactory also serves as indirection. The BookingService does not directly instantiate fare calculators. It asks the factory to provide one based on the quota type. This factory acts as a middleman that decouples the booking logic from the fare calculation instantiation logic.

Justification: Indirection enables the database to be swapped without modifying business logic. This was proven in the project when MySQL was replaced with H2 for local development, and zero Java source code changes were required.

---

### 9. Protected Variations

Protected Variations states that design elements should be created to protect against variations in other elements by wrapping the focus of instability with an interface.

In the IRCTC system, the PaymentStrategy interface protects the system against variations in payment gateway APIs. UPI payment APIs, credit card processing APIs, and net banking APIs all have different authentication flows, request formats, and response structures. By wrapping them behind the PaymentStrategy interface with a simple pay(double amount) method, the PaymentService is completely shielded from these variations. If the UPI protocol version changes, only UPIPayment.java is modified. PaymentService and BookingService remain protected.

The TicketObserver interface protects the booking system from variations in notification delivery mechanisms. Whether notifications are sent via email, SMS, push notifications, or WhatsApp, the BookingService always calls the same update(ticket, oldStatus, newStatus) method. Changes in email SMTP configuration or SMS gateway APIs do not propagate into the booking logic.

Justification: Protected Variations is essential in a railway reservation system because external services like payment gateways and notification providers change frequently. By encapsulating these external dependencies behind stable interfaces, the core booking engine remains stable and reliable.

---

## PART C: CREATIONAL DESIGN PATTERNS

### 1. Singleton Pattern

The Singleton Pattern ensures that a class has only one instance and provides a global point of access to that instance.

In the IRCTC system, the Singleton pattern is implemented in two classes.

The AuditLogger class at com.irctc.pattern.singleton.AuditLogger uses lazy initialization with the synchronized keyword. It has a private static field called instance that holds the single AuditLogger object. The constructor is private, preventing external instantiation. The getInstance() method is synchronized to ensure thread safety. When called for the first time, it creates the instance. On subsequent calls, it returns the same instance. The AuditLogger uses a PrintWriter to write log entries to an audit.log file, with each entry containing a timestamp, the username, the action performed, and additional details. Throughout the system, classes like BookingService, UserService, and others call AuditLogger.getInstance().log() to record actions such as BOOK_TICKET, CANCEL_TICKET, REGISTER, LOGIN, UPDATE_PROFILE, CHANGE_PASSWORD, DISABLE_USER, and ENABLE_USER.

The DatabaseConnectionPool class at com.irctc.pattern.singleton.DatabaseConnectionPool uses double-checked locking with the volatile keyword for even more robust thread safety. The instance field is declared as static volatile, and the getInstance() method first checks if instance is null without synchronization for performance, then enters a synchronized block and checks again before creating the instance. The pool maintains a list of up to 10 pre-initialized database connections. The getConnection() method removes and returns a connection from the pool, while releaseConnection() returns a used connection back to the pool if the pool has not reached maximum capacity.

Additionally, all Spring Service classes annotated with @Service such as BookingService, PaymentService, WalletService, UserService, and TrainService are implicitly singletons. The Spring IoC container creates exactly one instance of each service bean and shares it across the entire application.

Justification: The AuditLogger must be a Singleton because multiple instances would compete for write access to the same audit.log file, causing file corruption and data loss. A single instance ensures all audit entries are serialized through one PrintWriter. The DatabaseConnectionPool must be a Singleton because creating multiple pools would waste database connections and system memory. A single pool manages all connections efficiently.

---

### 2. Factory Method Pattern

The Factory Pattern defines an interface for creating an object but lets subclasses or factory classes decide which class to instantiate.

In the IRCTC system, the Factory pattern is implemented in the FareCalculatorFactory class at com.irctc.pattern.factory.FareCalculatorFactory. This class is annotated as a Spring @Component and has three autowired dependencies: GeneralFareCalculator, TatkalFareCalculator, and SeniorCitizenFareCalculator. Its getCalculator method takes a QuotaType enum as input and returns the appropriate FareCalculator implementation using a switch statement. For TATKAL quota, it returns the TatkalFareCalculator which multiplies the base fare by 1.3, adding a 30 percent surcharge. For SENIOR_CITIZEN quota, it returns the SeniorCitizenFareCalculator which multiplies the base fare by 0.5, giving a 50 percent discount. For all other quotas including GENERAL, LADIES, and FOREIGN_TOURIST, it returns the GeneralFareCalculator which calculates the standard fare.

The BookingService uses this factory at line 95: FareCalculator fareCalculator = fareCalculatorFactory.getCalculator(request.getQuotaType()). It then calls fareCalculator.calculateFare(ticket) at line 116. The BookingService does not know or care which specific calculator class it receives. It works entirely with the FareCalculator interface.

The PaymentService at com.irctc.service.PaymentService also uses a factory-like method called getPaymentStrategy. This private method at line 67-78 takes a payment method string as input and returns the appropriate PaymentStrategy implementation. For "creditcard", it returns the CreditCardPayment strategy. For "upi", it returns the UPIPayment strategy. For "netbanking", it returns the NetBankingPayment strategy.

Justification: The Factory pattern centralizes object creation logic in one place. If a new fare calculator for a Defence Quota is needed, only the factory class and the new calculator class are created or modified. The BookingService, which is the most critical class in the system, remains completely unchanged. This protects the system's most important business logic from unnecessary modification risk.

---

## PART D: STRUCTURAL DESIGN PATTERNS

### 1. Facade Pattern

The Facade Pattern provides a simplified interface to a complex subsystem of classes.

In the IRCTC system, the BookingService class at com.irctc.service.BookingService acts as a Facade. The bookTicket method at line 58-165 is the single simplified entry point for the entire ticket booking process. Behind this single method call, the following complex subsystem operations are orchestrated: the train is fetched from the TrainRepository, seat availability is checked by counting confirmed and waitlisted passenger seats, Tatkal eligibility is verified through the TrainTatkalRepository, the fare is calculated using the FareCalculatorFactory and the appropriate FareCalculator, passenger details are assembled and attached to the ticket, the ticket is persisted to the database, payment is processed either through the WalletService or the PaymentService depending on the payment method, seats and coaches are allocated by the SeatAllocationService, and email and SMS notifications are sent through the observer pattern.

The controllers and front-end only interact with the BookingService Facade. They call bookingService.bookTicket(request, passenger, journeyDate) and receive a completed Ticket object. They have no knowledge of the 9 different subsystem classes involved in this process.

Justification: Without the Facade, the BookingController would need to individually call TrainRepository, TicketRepository, FareCalculatorFactory, PaymentService or WalletService, SeatAllocationService, EmailNotifier, and SMSNotifier. This would make the controller extremely complex and tightly coupled to every subsystem class. The Facade pattern keeps the controller simple and the subsystem interactions manageable.

---

### 2. Proxy Pattern

The Proxy Pattern provides a surrogate or placeholder for another object to control access to it.

In the IRCTC system, the Proxy pattern is used implicitly through Spring Framework's AOP (Aspect-Oriented Programming) mechanism. Every method in the service layer annotated with @Transactional is wrapped in a dynamic proxy at runtime. When the application starts, Spring generates a proxy class for BookingService, WalletService, PaymentService, and UserService. When a controller calls bookingService.bookTicket(), it is actually calling the proxy's bookTicket method. The proxy opens a database transaction before the actual method executes and commits or rolls back the transaction after the method completes or throws an exception.

For example, the cancelTicket method in BookingService is annotated with @Transactional. If the method successfully cancels the ticket, updates the train's available seats, sets the refund status, and promotes waitlisted tickets, the proxy commits all these changes as a single atomic transaction. If any step throws an exception, the proxy rolls back all changes, ensuring data consistency.

Justification: The Proxy pattern is essential for transactional integrity. Without it, a failure in the middle of the cancellation process could leave the database in an inconsistent state, for example with a cancelled ticket but unreleased seats. The proxy ensures all-or-nothing execution.

---

## PART E: BEHAVIORAL DESIGN PATTERNS

### 1. Observer Pattern

The Observer Pattern defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.

In the IRCTC system, the Observer pattern is implemented in the com.irctc.pattern.observer package. The TicketObserver interface defines the contract with a single method: update(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus).

The EmailNotifier class implements TicketObserver. When its update method is called, it retrieves the passenger's email address from the ticket and prints a notification message containing the PNR number and the status change. The SMSNotifier class also implements TicketObserver. When its update method is called, it retrieves the passenger's phone number and prints an SMS notification with the PNR and the new status display name.

In the BookingService, both observers are called at multiple points. After a successful booking at line 135-136, both emailNotifier.update() and smsNotifier.update() are called to notify the passenger of the booking confirmation. After a cancellation at line 197-198, both are called to notify about the cancellation. After a refund approval at line 247-248, both are called to notify about the refund. During waitlist promotion at line 358-359, both are called to notify the passenger that their waitlisted ticket has been automatically confirmed.

Justification: The Observer pattern decouples the notification mechanism from the booking logic. The BookingService does not need to know the details of how notifications are sent. If a new notification channel like WhatsApp is added, only a new WhatsAppNotifier class implementing TicketObserver is needed. The BookingService just adds one more line calling the new notifier's update method.

---

### 2. Strategy Pattern

The Strategy Pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable. It lets the algorithm vary independently from the clients that use it.

In the IRCTC system, the Strategy pattern is implemented in the com.irctc.pattern.strategy package. The PaymentStrategy interface defines two methods: pay(double amount) which returns a boolean indicating success or failure, and getPaymentMethod() which returns the name of the payment method.

The CreditCardPayment class implements PaymentStrategy. It stores card details including the card number, expiry date, and CVV. Its pay method processes the credit card transaction and uses a maskCardNumber method to display only the last four digits for security. The UPIPayment class implements PaymentStrategy and stores a UPI ID. Its pay method processes the UPI transaction. The NetBankingPayment class implements PaymentStrategy and stores the bank name and user ID.

The PaymentService dynamically selects the appropriate strategy at runtime based on the payment method string received from the user interface. The getPaymentStrategy private method at line 67-78 performs this selection. Once selected, the strategy's pay method is called at line 39, and the result determines whether the payment record is marked as SUCCESS or FAILED.

Justification: The Strategy pattern allows the payment processing algorithm to be selected at runtime without modifying the PaymentService. Each payment method has completely different validation rules, API calls, and security requirements. Encapsulating each in its own class keeps the code clean and maintainable.

---

### 3. Iterator Pattern

The Iterator Pattern provides a way to access the elements of an aggregate object sequentially without exposing its underlying representation.

In the IRCTC system, the Iterator pattern is used through Java's built-in Collections framework. The most significant usage is in the promoteWaitlist method of BookingService at lines 343-362. When a confirmed ticket is cancelled, this method retrieves all waitlisted tickets for the same train and date. It sorts them by booking date using Comparator and then iterates through them sequentially, promoting them from WAITING to CONFIRMED status as seats become available. The for-each loop iterates through the List of Ticket objects, allocating seats and sending notifications for each promoted ticket until either all released seats are allocated or all waitlisted tickets have been processed.

The searchTrains method at lines 267-308 also uses iteration to loop through all trains on a route, checking availability for each one and building a list of available options.

Justification: The Iterator pattern is fundamental to processing collections of domain objects. The waitlist promotion algorithm must process tickets in a specific order (first-come-first-served by booking date), and the iterator provides a clean, sequential traversal mechanism.

---

## PART F: ANTI-PATTERNS IDENTIFIED AND AVOIDED

### 1. Architecture Anti-Pattern: God Object

A God Object is a class that knows too much or does too much. It concentrates a disproportionate amount of responsibility in a single class, making it extremely difficult to maintain, test, and extend.

In the IRCTC system, the BookingService at 364 lines of code was at risk of becoming a God Object. It orchestrates booking, cancellation, refund processing, train searching, waitlist promotion, and Tatkal verification. However, the design deliberately avoids the God Object anti-pattern by delegating specialized tasks to dedicated classes. Fare calculation is delegated to FareCalculatorFactory and its FareCalculator implementations. Payment processing is delegated to PaymentService. Wallet operations are delegated to WalletService. Seat allocation is delegated to SeatAllocationService. Notifications are delegated to EmailNotifier and SMSNotifier. Without this delegation, BookingService would easily exceed 1000 lines and become unmaintainable.

### 2. Development Anti-Pattern: Magic Numbers

Magic Numbers are unexplained numeric values embedded directly in source code. They make the code difficult to understand and modify because their meaning is not immediately apparent.

In the IRCTC system, the TatkalFareCalculator uses the value 1.3 directly (representing a 30 percent surcharge), and the SeniorCitizenFareCalculator uses 0.5 (representing a 50 percent discount). The Ticket class uses 0.5, 0.25, and 0.1 for refund percentage calculations based on hours before departure. While these are technically magic numbers, the system mitigates their impact by encapsulating them within small, focused classes with clear names. The class name TatkalFareCalculator itself provides context for the 1.3 multiplier. Additionally, base fare values are stored in the ClassType enum with named constants like FIRST_AC(1000), SECOND_AC(600), and THIRD_AC(400), avoiding magic numbers for fare data.

### 3. Project Management Anti-Pattern: Analysis Paralysis

Analysis Paralysis occurs when a team spends so much time analyzing and designing that no actual development progress is made.

The IRCTC project avoids this anti-pattern by using an iterative development approach. Rather than trying to design the perfect system upfront, the project started with core functionality such as booking and cancellation and progressively added features like wallet integration, Tatkal booking, seat allocation, waitlist promotion, and PDF ticket generation. The initial deployment used MySQL, and the database was later transitioned to H2 for simplified local development, proving that the architecture was flexible enough to accommodate changes without requiring a complete redesign.
