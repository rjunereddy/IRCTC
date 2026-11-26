# IRCTC Railway Reservation System
## Comprehensive Project Documentation

---

## 1. PROJECT OVERVIEW

The IRCTC Railway Reservation System is a full-stack web application that simulates the Indian Railway Catering and Tourism Corporation's online ticket booking platform. The system allows passengers to search for trains, book tickets across multiple travel classes and quotas, manage their bookings, and download PDF e-tickets. Administrators can manage trains, handle refund requests, generate revenue and booking reports, and control user accounts. The project is built using the Spring Boot framework with a layered MVC architecture and demonstrates the practical application of Object-Oriented Analysis and Design principles including SOLID, GRASP, and multiple GoF design patterns.

---

## 2. TECHNOLOGY STACK

The backend of the application is built using Java 21 with Spring Boot 3.5.13 as the primary framework. Spring Data JPA is used for database access through repository interfaces, and Hibernate serves as the ORM (Object-Relational Mapping) provider that maps Java entity objects to database tables. Spring Security handles authentication and authorization, enforcing role-based access control for Passenger and Administrator users. The frontend uses Thymeleaf as the server-side template engine, rendering dynamic HTML pages with data passed from controllers. The database is configured to use H2, an in-memory relational database, for development and testing purposes. The application was originally designed for MySQL and can be switched back by changing configuration properties without modifying any Java source code. OpenPDF (LibrePDF) is used for generating downloadable PDF e-tickets. Apache Commons Lang3 is used for utility operations. Maven is used as the build tool and dependency manager. The application runs on an embedded Apache Tomcat server at port 9090.

---

## 3. PROJECT STRUCTURE

The project follows a standard Maven directory layout. All Java source code resides under src/main/java/com/irctc and is organized into the following packages.

The model package contains all JPA entity classes that map to database tables. These include User, Passenger, Administrator, Train, Ticket, PassengerDetail, Payment, Station, PassengerWallet, WalletTransaction, SavedPassenger, TrainTatkal, and TrainRouteStop. The model/enums subpackage contains Java enums for UserRole, ClassType, QuotaType, TicketStatus, PaymentStatus, RefundStatus, and BerthType.

The repository package contains 11 Spring Data JPA repository interfaces. Each interface extends JpaRepository and provides database access methods for its corresponding entity. These include TicketRepository, TrainRepository, UserRepository, PassengerRepository, PaymentRepository, PassengerWalletRepository, WalletTransactionRepository, StationRepository, TrainTatkalRepository, TrainRouteStopRepository, and SavedPassengerRepository.

The service package contains business logic classes. BookingService orchestrates the entire ticket booking workflow. PaymentService processes payments through different payment strategies. UserService handles user registration, login, profile management, and password changes. WalletService manages the IRCTC wallet for adding funds, deducting funds, and processing refunds. TrainService handles train CRUD operations. SeatAllocationService determines coach and seat assignments. ReportService generates revenue and booking reports. PdfGeneratorService creates downloadable PDF e-tickets. SavedPassengerService manages frequently used passenger profiles.

The controller package contains Spring MVC controllers that handle HTTP requests. BookingController manages all passenger-side operations including search, booking, cancellation, refund requests, PNR status checks, booking history, and PDF downloads. AdminController manages administrator operations including the dashboard, train management, Tatkal enabling, refund approval and rejection, user management, and report generation. AuthController handles login and registration pages. WalletController handles wallet operations. ReportController handles report-specific endpoints. StationRestController and TrainRestController provide REST API endpoints for station and train data.

The config package contains configuration classes. SecurityConfig configures Spring Security with role-based access control, custom login pages, and authentication success handlers. PasswordEncoderConfig provides the BCrypt password encoder bean. DataInitializer implements CommandLineRunner to populate the database with initial stations, trains, and an admin user on application startup.

The pattern package contains explicit implementations of design patterns organized into subpackages. The pattern/singleton subpackage contains AuditLogger and DatabaseConnectionPool. The pattern/factory subpackage contains FareCalculator interface, FareCalculatorFactory, GeneralFareCalculator, TatkalFareCalculator, and SeniorCitizenFareCalculator. The pattern/strategy subpackage contains PaymentStrategy interface, CreditCardPayment, UPIPayment, and NetBankingPayment. The pattern/observer subpackage contains TicketObserver interface, EmailNotifier, and SMSNotifier.

The dto package contains Data Transfer Objects used to transfer data between the frontend and controllers. These include BookingRequest, LoginRequest, RegisterRequest, and ReportCriteria.

The util package contains utility classes including PNRGenerator for generating and validating 10-digit PNR numbers, and DateUtils for date formatting operations.

The templates directory under src/main/resources contains Thymeleaf HTML templates organized into subdirectories for passenger views, admin views, and authentication views.

---

## 4. DATABASE DESIGN

The system uses 14 database tables mapped through JPA entities.

The users table stores common user information including userId (auto-generated primary key), username (unique), password (BCrypt encoded), email (unique), phone, registration date, last login timestamp, active status, and role (ADMIN or PASSENGER). This table uses JPA's JOINED inheritance strategy, meaning subclass-specific data is stored in separate child tables.

The passengers table extends the users table and stores fullName, age, gender, dateOfBirth, idProofType, idProofNumber, address, city, state, pincode, isSeniorCitizen, isStudent, and concessionType.

The administrators table extends the users table and stores employeeCode (unique), department, designation, and accessLevel.

The stations table stores stationCode (primary key, e.g., SBC, NDLS, MAS), stationName, city, state, zone, pincode, platformCount, and isJunction flag. Fifty major Indian railway stations are pre-loaded at startup covering 20 states and union territories including KSR Bengaluru (SBC), Mysuru (MYS), Chennai Central (MAS), Chennai Egmore (MS), New Delhi (NDLS), Old Delhi (DLI), Mumbai Central (BCT), CSMT Mumbai (CSTM), LTT Mumbai (LTT), Pune (PNQ), Nagpur (NGP), Hyderabad (HYB), Secunderabad (SC), Vijayawada (BZA), Visakhapatnam (VSKP), Howrah (HWH), Sealdah (SDAH), Patna (PNBE), Ranchi (RNC), Bhubaneswar (BBS), Lucknow (LKO), Kanpur (CNB), Agra (AGC), Varanasi (BSB), Gorakhpur (GKP), Jaipur (JP), Ajmer (AII), Udaipur (UDZ), Jodhpur (JU), Ahmedabad (ADI), Vadodara (BRC), Trivandrum (TVC), Ernakulam (ERS), Calicut (CLT), Coimbatore (CBE), Madurai (MDU), Bhopal (BPL), Jabalpur (JBP), Chandigarh (CDG), Amritsar (ASR), Jammu Tawi (JAT), Katra (SVDK), Dehradun (DDN), Haridwar (HW), Guwahati (GHY), Dibrugarh (DGR), Hubballi (UBL), and Rajkot (RJT).

The trains table stores trainNo (primary key, e.g., 12627), trainName, source and destination station codes (foreign keys to stations), departureTime, arrivalTime, totalSeats, availableSeats, waitlistCapacity, trainType, isActive flag, and runningDays. One hundred and five trains are pre-loaded across multiple categories: 10 Rajdhani Express trains (Howrah, Mumbai, Chennai, Trivandrum, Bengaluru, Dibrugarh, Secunderabad, Patna Rajdhani), 9 Shatabdi Express trains (Bhopal, Lucknow, Chennai, Kalka, Pune, Amritsar Shatabdi), 6 Duronto Express trains (Mumbai, Chennai, Howrah, Bengaluru, Secunderabad Duronto), 4 Garib Rath Express trains (budget AC services), 3 Vande Bharat Express trains (Delhi-Varanasi, Bengaluru-Chennai, Delhi-Jaipur), and 73 Superfast/Mail/Express trains covering all major routes across India including Tamil Nadu Express, Kerala Express, Coromandel Express, Grand Trunk Express, Telangana Express, Golden Temple Mail, Gitanjali Express, Gujarat Mail, Punjab Mail, Deccan Queen, and many more.

The train_fares table is a collection table that stores fare structures as a map with the train number as a foreign key and class type as the key and fare amount as the value.

The train_classes table is a collection table that stores which travel classes are available for each train.

The tickets table stores pnr (primary key, 10-digit generated string), bookingDate, journeyDate, train number (foreign key), passenger ID (foreign key), classType, quotaType, coachNumber, totalFare, status (CONFIRMED, WAITING, RAC, CANCELLED, REFUNDED, EXPIRED), waitlistNumber, racNumber, chartPrepared flag, transactionId, and refundStatus (NONE, REQUESTED, APPROVED, REJECTED).

The ticket_seats table stores seat numbers associated with each ticket.

The passenger_details table stores individual passenger information within a booking including detailId (auto-generated), ticket PNR (foreign key), name, age, gender, idProofType, idProofNumber, berthPreference, berthAllotted, and mealPreference.

The payments table stores paymentId, ticket PNR (foreign key), amount, paymentMethod, status (SUCCESS, FAILED, REFUNDED), transactionId, failureReason, and retryCount.

The passenger_wallets table stores walletId, passenger ID (foreign key), and balance.

The wallet_transactions table stores transaction records for each wallet including transactionId, wallet ID (foreign key), amount, type (CREDIT or DEBIT), description, and transactionDate.

The saved_passengers table stores frequently used passenger profiles associated with a registered passenger, including name, age, gender, idProofType, and idProofNumber.

The train_tatkal table stores which trains have Tatkal booking enabled for which dates, with a train number foreign key and journeyDate.

---

## 5. FEATURE MODULES

### 5.1 User Registration and Authentication

The system supports two roles: PASSENGER and ADMIN. New users register through the registration form providing username, password (minimum 6 characters), email, phone, full name, age, gender, and optional address details. Passwords are encoded using BCrypt before storage. The system validates that username, email, and phone are unique across all users. Upon successful registration, a Passenger entity is created in the database. Login is handled through Spring Security's form-based authentication. After successful login, the AuthenticationSuccessHandler checks the user's role and redirects Admin users to /admin/dashboard and Passenger users to /passenger/dashboard. The AuditLogger Singleton records every registration and login event with timestamps.

### 5.2 Train Search and Availability

Passengers can search for trains by selecting a source station, destination station, and journey date. The system queries the TrainRepository to find all active trains matching the route. For each matching train, the system checks whether the train runs on the selected day of the week using the runningDays array. It then calculates real-time seat availability by counting confirmed passenger seats from the TicketRepository and subtracting from the total seats. If confirmed seats are available, the train is displayed with CONFIRMED status and the number of available seats. If confirmed seats are exhausted but waitlist capacity remains, the train is displayed with WAITING status and the number of available waitlist spots. If both confirmed and waitlist capacity are exhausted, the train is not displayed in results.

### 5.3 Ticket Booking

The booking process begins when a passenger selects a train and journey date. The booking form allows the passenger to choose the travel class (First AC, Second AC, Third AC, Sleeper, AC Chair Car, or Second Sitting), the quota type (General, Tatkal, Senior Citizen, Ladies, Premium Tatkal, or Foreign Tourist), the payment method (Wallet or Payment Gateway), and berth preference. The passenger adds one or more traveler details including name, age, gender, ID proof type, ID proof number, and meal preference. They can also auto-fill details from their saved passenger profiles.

When the booking is submitted, the BookingService orchestrates the following steps. First, it fetches the train details from the database. Second, it checks seat availability by counting currently booked confirmed and waitlisted seats. If the train is completely full including waitlist, it throws an error. If seats are available, the ticket status is set to CONFIRMED. If only waitlist spots are available, the status is set to WAITING with a waitlist number. Third, if the quota is TATKAL, it verifies that Tatkal has been enabled by an administrator for that specific train and date. Fourth, it uses the FareCalculatorFactory to get the appropriate FareCalculator based on the quota type. The GeneralFareCalculator multiplies the base fare by the number of passengers. The TatkalFareCalculator adds a 30 percent surcharge. The SeniorCitizenFareCalculator applies a 50 percent discount. Fifth, it generates a unique 10-digit PNR number using the PNRGenerator utility. Sixth, it creates PassengerDetail objects for each traveler and attaches them to the ticket. Seventh, it processes payment either through the WalletService (deducting from the passenger's IRCTC wallet balance) or through the PaymentService (which uses the Strategy pattern to select CreditCardPayment, UPIPayment, or NetBankingPayment). Eighth, upon successful payment, it calls the SeatAllocationService to assign coach numbers (e.g., H1 for First AC, A3 for Second AC, B2 for Third AC, S4 for Sleeper) and berth types (Lower, Middle, Upper, Side Lower, Side Upper for sleeper classes or Window and Aisle for chair car classes). Ninth, it sends notifications through the Observer pattern by calling update on both the EmailNotifier and SMSNotifier. Tenth, the AuditLogger records the booking event. The booking confirmation page displays all ticket details, and the passenger can download a PDF e-ticket.

### 5.4 PNR Status Check

Passengers can check the status of any ticket by entering the 10-digit PNR number. The system validates the PNR format using the PNRGenerator.validate method and then retrieves the full ticket details from the database, displaying the train information, journey date, passenger details, berth allocations, fare, and current status.

### 5.5 Ticket Cancellation

Passengers can cancel their tickets from the booking history page. The Ticket entity's canBeCancelled method checks eligibility by verifying that the ticket is not already cancelled or refunded, the chart is not prepared, and the journey date has not passed. Upon cancellation, the ticket status is changed to CANCELLED, the train's available seats are incremented, the refund status is set to REQUESTED so the admin can process it, and both email and SMS notifications are sent. Critically, the system then runs the promoteWaitlist algorithm. This method retrieves all waitlisted tickets for the same train and date, sorts them by booking date (first-come-first-served), and iterates through them. For each waitlisted ticket, if enough seats are available to accommodate all passengers on that ticket, it promotes the ticket from WAITING to CONFIRMED, allocates seats through SeatAllocationService, and sends promotion notifications via email and SMS.

### 5.6 Refund Management

Passengers can request a refund for cancelled tickets. The system checks that the ticket is cancelled and no refund has been previously requested. The Ticket entity calculates the refund amount based on how many hours remain before departure: more than 48 hours results in a 50 percent refund, 12 to 48 hours results in a 25 percent refund, 4 to 12 hours results in a 10 percent refund, and less than 4 hours results in no refund. Waitlisted and RAC tickets receive a full refund. Administrators view all pending refund requests on the refund management page and can approve or reject each one. When approved, if the original payment was through the wallet, the refund amount is credited back to the wallet via WalletService.refundFunds. If the original payment was through a payment gateway, the refund is processed through PaymentService.processRefund. Notifications are sent for both approval and rejection.

### 5.7 IRCTC Wallet

Each passenger has a digital wallet for quick payments. The wallet module supports adding funds, viewing the current balance, and viewing transaction history. When booking a ticket using the wallet, the WalletService.deductFunds method checks that the balance is sufficient and creates a DEBIT transaction record. When a refund is approved for a wallet-paid ticket, WalletService.refundFunds credits the amount back and creates a CREDIT transaction record. All wallet operations are transactional, ensuring consistency.

### 5.8 Saved Passengers

Passengers can save frequently used traveler profiles (name, age, gender, ID proof details) to avoid re-entering information for every booking. During the booking process, these saved profiles can be loaded to auto-fill the passenger detail fields.

### 5.9 PDF E-Ticket Generation

After a successful booking, passengers can download a PDF e-ticket. The PdfGeneratorService uses the OpenPDF library to generate a formatted PDF document containing the IRCTC header, PNR number, train details (number and name), journey date, travel class, ticket status with waitlist number if applicable, total fare, and a passenger details table with columns for name, age, gender, and allotted berth.

### 5.10 Admin Train Management

Administrators can view all active trains, add new trains by specifying the train number, name, source and destination stations, departure and arrival times, total seats, and running days. They can also soft-delete trains by setting the isActive flag to false, which hides them from passenger searches without removing data. Administrators can enable Tatkal booking for specific trains on specific dates. When Tatkal is enabled, passengers searching for that train and date will see the Tatkal quota option on the booking form.

### 5.11 Admin Reports

Administrators can generate two types of reports by specifying a date range. The Revenue Report shows total revenue collected from successful payments and total number of bookings in the period. The Booking Report shows total bookings broken down by train number, allowing administrators to identify the most popular routes.

### 5.12 Admin User Management

Administrators can view all registered users and disable or enable user accounts. Disabling an account prevents the user from logging in until an administrator re-enables it.

### 5.13 Audit Logging

Every significant user action is recorded by the AuditLogger Singleton to the audit.log file. Logged events include REGISTER, LOGIN, BOOK_TICKET, BOOKING_SUCCESS, CANCEL_TICKET, CANCELLATION_SUCCESS, UPDATE_PROFILE, CHANGE_PASSWORD, ADD_TRAIN, UPDATE_TRAIN, DELETE_TRAIN, DISABLE_USER, and ENABLE_USER. Each log entry includes a timestamp, the username performing the action, and descriptive details.

### 5.14 Security

The SecurityConfig class configures URL-based access control. All URLs under /admin/ require the ADMIN role. All URLs under /passenger/ require the PASSENGER role. Authentication pages under /auth/, static resources under /css/ and /js/, and the root path are publicly accessible. The system uses a custom login page at /auth/login with a custom AuthenticationSuccessHandler that redirects users to the appropriate dashboard based on their role. CSRF protection is disabled for simplicity in development. Session timeout is set to 30 minutes.

---

## 6. DATA INITIALIZATION

On application startup, the DataInitializer class (implementing CommandLineRunner) checks if the database is empty and populates it with seed data. Fifty railway stations are created spanning 20 states and union territories across India, covering all major railheads and junction stations. One hundred and five trains are created across 7 categories with realistic routes, departure and arrival times, travel classes, and fare structures. The train categories include 10 Rajdhani Express (premium fully AC long-distance trains with First AC, Second AC, and Third AC classes, fares ranging from 1600 to 4800 rupees), 9 Shatabdi Express (premium daytime intercity trains with AC Chair Car and Second AC classes, fares ranging from 350 to 2500 rupees), 6 Duronto Express (non-stop long-distance trains with First AC, Second AC, Third AC, and Sleeper classes), 4 Garib Rath Express (budget Third AC-only trains with fares below 1000 rupees), 3 Vande Bharat Express (semi-high-speed trains with AC Chair Car and Second AC classes), and 73 Superfast/Mail/Express trains covering diverse routes across every major corridor of Indian Railways. Seat capacities range from 300 to 800 depending on the train type. One administrator account is created with username "admin" and password "admin123" (BCrypt encoded).

---

## 7. HOW TO RUN THE APPLICATION

Step 1: Ensure Java 21 (JDK) is installed on your system. Verify by running java -version in your terminal.

Step 2: Open a terminal or command prompt and navigate to the project root directory where pom.xml is located.

Step 3: Run the following command to compile the project and execute tests:
.\mvnw.cmd clean test

Step 4: Start the Spring Boot application by running:
.\mvnw.cmd spring-boot:run

Step 5: Wait until you see the message "IRCTC Railway Reservation System Started!" in the console output.

Step 6: Open a web browser and navigate to http://localhost:9090 to access the application.

Step 7: To log in as an administrator, use username "admin" and password "admin123". To use the system as a passenger, first register a new account through the registration page.

Step 8: To access the H2 database console for inspecting data, navigate to http://localhost:9090/h2-console. Use JDBC URL "jdbc:h2:mem:irctc_db", username "sa", and leave the password field blank.

---

## 8. SUMMARY OF FILES AND CLASS COUNT

The project contains 73 Java source files distributed across 9 packages:
- 14 entity classes in the model package
- 7 enum types in the model/enums package
- 11 repository interfaces in the repository package
- 9 service classes in the service package
- 8 controller classes in the controller package
- 3 configuration classes in the config package
- 4 DTO classes in the dto package
- 2 utility classes in the util package
- 11 design pattern implementation classes in the pattern package (2 Singleton, 5 Factory, 3 Strategy, 3 Observer)
- 4 remaining miscellaneous files

The application uses a total of 14 database tables, manages 105 pre-loaded trains across 50 stations covering all major Indian railway corridors, and supports 6 travel classes, 6 quota types, 6 ticket statuses, 3 payment statuses, and 4 refund statuses.
