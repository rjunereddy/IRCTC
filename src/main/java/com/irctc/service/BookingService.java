package com.irctc.service;

import com.irctc.dto.BookingRequest;
import com.irctc.dto.TrainAvailabilityDTO;
import com.irctc.model.*;
import com.irctc.model.enums.*;
import com.irctc.pattern.chain.BookingContext;
import com.irctc.pattern.chain.BookingValidationPipeline;
import com.irctc.pattern.factory.FareCalculator;
import com.irctc.pattern.factory.FareCalculatorFactory;
import com.irctc.pattern.observer.EmailNotifier;
import com.irctc.pattern.observer.SMSNotifier;
import com.irctc.pattern.singleton.AuditLogger;
import com.irctc.repository.*;
import com.irctc.util.PNRGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    private final TicketRepository ticketRepository;
    private final TrainRepository trainRepository;
    private final PaymentService paymentService;
    private final FareCalculatorFactory fareCalculatorFactory;
    private final EmailNotifier emailNotifier;
    private final SMSNotifier smsNotifier;
    private final TrainTatkalRepository trainTatkalRepository;
    private final WalletService walletService;
    private final SeatAllocationService seatAllocationService;
    private final TrainRouteStopRepository trainRouteStopRepository;
    private final StationRepository stationRepository;
    private final DistanceFareService distanceFareService;
    
    // Manual constructor to initialize all dependencies
    public BookingService(TicketRepository ticketRepository, 
                          TrainRepository trainRepository,
                          PaymentService paymentService,
                          FareCalculatorFactory fareCalculatorFactory,
                          EmailNotifier emailNotifier,
                          SMSNotifier smsNotifier,
                          TrainTatkalRepository trainTatkalRepository,
                          WalletService walletService,
                          SeatAllocationService seatAllocationService,
                          TrainRouteStopRepository trainRouteStopRepository,
                          StationRepository stationRepository,
                          DistanceFareService distanceFareService) {
        this.ticketRepository = ticketRepository;
        this.trainRepository = trainRepository;
        this.paymentService = paymentService;
        this.fareCalculatorFactory = fareCalculatorFactory;
        this.emailNotifier = emailNotifier;
        this.smsNotifier = smsNotifier;
        this.trainTatkalRepository = trainTatkalRepository;
        this.walletService = walletService;
        this.seatAllocationService = seatAllocationService;
        this.trainRouteStopRepository = trainRouteStopRepository;
        this.stationRepository = stationRepository;
        this.distanceFareService = distanceFareService;
    }
    
    @Transactional
    @SuppressWarnings("null")
    public Ticket bookTicket(BookingRequest request, Passenger passenger, LocalDate journeyDate) {
        AuditLogger logger = AuditLogger.getInstance();
        logger.log("BOOK_TICKET", passenger.getUsername(), 
                   "Train: " + request.getTrainNo() + ", Passengers: " + 
                   request.getPassengers().size());
        
        // 1. Get train details with Pessimistic Write Lock to prevent double-booking race condition
        Train train = trainRepository.findByIdWithLock(request.getTrainNo())
            .orElseThrow(() -> new RuntimeException("Train not found: " + request.getTrainNo()));
        
        // 2. Fetch live seat counts
        int bookedSeats = ticketRepository.countConfirmedPassengerSeats(train.getTrainNo(), journeyDate);
        int racSeats = ticketRepository.countRacPassengerSeats(train.getTrainNo(), journeyDate);
        int wlSeats = ticketRepository.countWaitlistedPassengerSeats(train.getTrainNo(), journeyDate);
        double walletBal = walletService.getWalletBalance(passenger);

        // 2.5 Run Chain of Responsibility Validation Pipeline
        BookingContext validationContext = new BookingContext(
            request, passenger, train, journeyDate,
            isTatkalEnabled(train.getTrainNo(), journeyDate),
            walletBal, bookedSeats, racSeats, wlSeats
        );
        new BookingValidationPipeline().execute(validationContext);

        TicketStatus ticketStatus = validationContext.getAssignedStatus();
        int wlNum = validationContext.getWaitlistNumber();
        int racNum = validationContext.getRacNumber();
        
        // 3. Calculate fare using Factory Pattern
        FareCalculator fareCalculator = fareCalculatorFactory.getCalculator(request.getQuotaType());
        
        // 4. Create ticket
        String pnr = PNRGenerator.generate();
        Ticket ticket = new Ticket(pnr, train, passenger, journeyDate,
                                   request.getClassType(), request.getQuotaType(), 0.0);
        
        // 4.1 Set Journey Stations & Compute Distance-Based Segment Fare
        com.irctc.model.Station srcStation = null;
        com.irctc.model.Station destStation = null;
        if (request.getSourceStationCode() != null && !request.getSourceStationCode().trim().isEmpty()) {
            srcStation = stationRepository.findById(request.getSourceStationCode().trim().toUpperCase()).orElse(null);
        }
        if (request.getDestinationStationCode() != null && !request.getDestinationStationCode().trim().isEmpty()) {
            destStation = stationRepository.findById(request.getDestinationStationCode().trim().toUpperCase()).orElse(null);
        }
        if (srcStation == null) srcStation = train.getSourceStation();
        if (destStation == null) destStation = train.getDestinationStation();

        ticket.setSourceStation(srcStation);
        ticket.setDestinationStation(destStation);

        List<com.irctc.model.TrainRouteStop> stops = trainRouteStopRepository.findByTrain_TrainNoOrderByStopOrderAsc(train.getTrainNo());
        int segDist = distanceFareService.calculateSegmentDistance(stops, srcStation.getStationCode(), destStation.getStationCode());
        int totDist = distanceFareService.calculateTotalRouteDistance(stops);
        if (segDist <= 0) segDist = totDist;
        ticket.setDistanceKm(segDist);

        double segBaseFare = distanceFareService.calculateSegmentFare(train, request.getClassType(), segDist, totDist);
        ticket.setSegmentBaseFare(segBaseFare);
        
        // 5. Add passenger details
        List<PassengerDetail> passengerDetails = new ArrayList<>();
        for (BookingRequest.PassengerInfo info : request.getPassengers()) {
            PassengerDetail detail = new PassengerDetail(info.getName(), info.getAge(), info.getGender());
            detail.setTicket(ticket);
            detail.setIdProofType(info.getIdProofType());
            detail.setIdProofNumber(info.getIdProofNumber());
            detail.setMealPreference(info.getMealPreference());
            detail.setBerthPreference(request.getBerthPreference());
            passengerDetails.add(detail);
        }
        ticket.setAllPassengers(passengerDetails);
        
        // 6. Calculate breakdown components
        // We calculate baseAndGst using the strategy, then add per-ticket fees
        double baseAndGst = fareCalculator.calculateFare(ticket); // Note: calculators update baseFare and gstAmount in ticket
        
        // 6.1 Parse Food Total & Items
        String foodItemsStr = parseFoodItems(request.getFoodOrderDetails());
        ticket.setFoodItems(foodItemsStr);
        double foodTotal = calculateFoodTotal(request.getFoodOrderDetails());
        ticket.setFoodTotal(foodTotal);
        if (foodTotal > 0 && foodItemsStr != null && !foodItemsStr.isEmpty()) {
            ticket.setFoodCouponCode("ECAT-" + ticket.getPnr() + "-" + (1000 + (int)(Math.random() * 9000)));
        }
        
        // 6.2 Service Fee
        double serviceFee = 15.0; // Flat fee
        ticket.setServiceFee(serviceFee);
        
        // 6.3 Insurance
        double insuranceAmount = 0.0;
        if (request.isInsuranceOpted()) {
            insuranceAmount = request.getPassengers().size() * 0.35;
        }
        ticket.setInsuranceAmount(insuranceAmount);
        
        // Final Total
        double finalFare = baseAndGst + foodTotal + serviceFee + insuranceAmount;
        finalFare = Math.round(finalFare * 100.0) / 100.0;
        ticket.setTotalFare(finalFare);
        
        // 7. Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        double fare = finalFare;
        
        // 9. Process payment
        if ("WALLET".equals(request.getPaymentMethod())) {
            try {
                // Pre-check wallet balance before attempting deduction
                double walletBalance = walletService.getWalletBalance(passenger);
                if (walletBalance < fare) {
                    savedTicket.setStatus(TicketStatus.CANCELLED);
                    ticketRepository.save(savedTicket);
                    throw new RuntimeException(String.format(
                        "Insufficient wallet balance. Required: \u20b9%.2f, Available: \u20b9%.2f. " +
                        "Please add \u20b9%.2f to your wallet before booking.",
                        fare, walletBalance, (fare - walletBalance)));
                }
                walletService.deductFunds(passenger, fare, "Ticket booking: " + pnr);
                savedTicket.setStatus(ticketStatus);
                if (ticketStatus == TicketStatus.WAITING) {
                    savedTicket.setWaitlistNumber(wlNum);
                } else if (ticketStatus == TicketStatus.RAC) {
                    savedTicket.setRacNumber(racNum);
                }
                savedTicket.setTransactionId("WALLET-" + pnr);
                seatAllocationService.allocateSeats(savedTicket, bookedSeats);
                ticketRepository.save(savedTicket);
                
                // 10. Send notifications
                emailNotifier.update(savedTicket, null, ticketStatus);
                smsNotifier.update(savedTicket, null, ticketStatus);
            } catch (RuntimeException e) {
                // Only cancel the ticket if it hasn't already been cancelled above
                if (savedTicket.getStatus() != TicketStatus.CANCELLED) {
                    savedTicket.setStatus(TicketStatus.CANCELLED);
                    ticketRepository.save(savedTicket);
                }
                throw e;
            } catch (Exception e) {
                savedTicket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(savedTicket);
                throw new RuntimeException("Wallet Payment failed: " + e.getMessage());
            }
        } else {
            Payment payment = paymentService.processPayment(savedTicket, fare, request.getPaymentMethod(), request);
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                savedTicket.setStatus(ticketStatus);
                if (ticketStatus == TicketStatus.WAITING) {
                    savedTicket.setWaitlistNumber(wlNum);
                } else if (ticketStatus == TicketStatus.RAC) {
                    savedTicket.setRacNumber(racNum);
                }
                savedTicket.setTransactionId(payment.getTransactionId());
                seatAllocationService.allocateSeats(savedTicket, bookedSeats);
                ticketRepository.save(savedTicket);
                
                // 10. Send notifications
                emailNotifier.update(savedTicket, null, ticketStatus);
                smsNotifier.update(savedTicket, null, ticketStatus);
            } else {
                savedTicket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(savedTicket);
                throw new RuntimeException("Payment failed: " + payment.getFailureReason());
            }
        }
        
        logger.log("BOOKING_SUCCESS", passenger.getUsername(), "PNR: " + pnr);
        return savedTicket;
    }
    
    public Ticket getTicketByPNR(String pnr) {
        return ticketRepository.findByPnr(pnr)
            .orElseThrow(() -> new RuntimeException("Ticket not found with PNR: " + pnr));
    }
    
    @Transactional
    public Ticket cancelTicket(String pnr, Passenger passenger) {
        AuditLogger logger = AuditLogger.getInstance();
        logger.log("CANCEL_TICKET", passenger.getUsername(), "PNR: " + pnr);
        
        Ticket ticket = getTicketByPNR(pnr);
        
        if (!ticket.canBeCancelled()) {
            throw new RuntimeException("Ticket cannot be cancelled");
        }
        
        TicketStatus oldStatus = ticket.getStatus();
        double refund = ticket.getRefundAmount();
        double charge = ticket.getCancellationCharge();
        String explanation = ticket.getRefundExplanation();

        ticket.setStatus(TicketStatus.CANCELLED);
        
        // Immediate refund credit to user's IRCTC eWallet
        if (refund > 0) {
            walletService.refundFunds(ticket.getPassenger(), refund,
                    "Instant refund for cancelled ticket PNR: " + pnr + " (" + explanation + ")");
            ticket.setRefundStatus(RefundStatus.APPROVED);
        } else {
            ticket.setRefundStatus(RefundStatus.NONE);
        }
        
        ticketRepository.save(ticket);
        
        // Waitlist will be promoted and take up released seats dynamically.
        // No need to persist availableSeats to the generic Train object.

        // Send notifications
        emailNotifier.update(ticket, oldStatus, TicketStatus.CANCELLED);
        smsNotifier.update(ticket, oldStatus, TicketStatus.CANCELLED);
        
        // Promote Waitlist
        if (oldStatus == TicketStatus.CONFIRMED || oldStatus == TicketStatus.RAC) {
            promoteWaitlist(ticket.getTrain().getTrainNo(), ticket.getJourneyDate(), ticket.getAllPassengers().size(), oldStatus);
        }
        
        logger.log("CANCELLATION_SUCCESS", passenger.getUsername(), "PNR: " + pnr + ", Refund: ₹" + refund + ", Charge: ₹" + charge);
        return ticket;
    }
    
    @Transactional
    public Ticket requestRefund(String pnr, Passenger passenger) {
        Ticket ticket = getTicketByPNR(pnr);
        if (!ticket.getPassenger().getUserId().equals(passenger.getUserId())) {
            throw new RuntimeException("Refund request can only be made by the ticket owner");
        }
        if (ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new RuntimeException("Refund can only be requested for cancelled tickets");
        }
        if (ticket.getRefundStatus() != RefundStatus.NONE) {
            throw new RuntimeException("Refund request already submitted");
        }
        ticket.setRefundStatus(RefundStatus.REQUESTED);
        ticketRepository.save(ticket);
        return ticket;
    }
    
    public List<Ticket> getRefundRequests() {
        return ticketRepository.findByRefundStatus(RefundStatus.REQUESTED);
    }
    
    @Transactional
    public Ticket approveRefund(String pnr) {
        Ticket ticket = getTicketByPNR(pnr);
        if (ticket.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new RuntimeException("No refund request found for this ticket");
        }
        double refundAmount = ticket.getRefundAmount();
        
        if (ticket.getTransactionId() != null && ticket.getTransactionId().startsWith("WALLET-")) {
            walletService.refundFunds(ticket.getPassenger(), refundAmount, "Refund for ticket: " + pnr);
        } else {
            paymentService.processRefund(ticket, refundAmount);
        }
        
        ticket.setRefundStatus(RefundStatus.APPROVED);
        ticket.setStatus(TicketStatus.REFUNDED);
        ticketRepository.save(ticket);
        emailNotifier.update(ticket, TicketStatus.CANCELLED, TicketStatus.REFUNDED);
        smsNotifier.update(ticket, TicketStatus.CANCELLED, TicketStatus.REFUNDED);
        return ticket;
    }
    
    @Transactional
    public Ticket rejectRefund(String pnr) {
        Ticket ticket = getTicketByPNR(pnr);
        if (ticket.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new RuntimeException("No refund request found for this ticket");
        }
        ticket.setRefundStatus(RefundStatus.REJECTED);
        ticketRepository.save(ticket);
        return ticket;
    }
    
    public List<Ticket> getPassengerBookings(Passenger passenger) {
        return ticketRepository.findByPassenger_UserId(passenger.getUserId());
    }
    
    /**
     * IRCTC-style per-class real-time availability search.
     * Returns a list of TrainAvailabilityDTO — one per matching train.
     * Each DTO contains per-class AVAILABLE/RAC/WL counts computed live from the ticket DB.
     * @Transactional required so lazy @ElementCollection (classes, fareStructure) loads within session.
     */
    @Transactional(readOnly = true)
    public List<TrainAvailabilityDTO> searchTrains(String source, String destination, LocalDate date) {
        List<Train> trains = trainRepository.findTrainsByRoute(source, destination);
        List<TrainAvailabilityDTO> results = new ArrayList<>();

        String dayOfWeek = date.getDayOfWeek().name();

        for (Train train : trains) {
            // Check running days
            if (train.getRunningDays() != null && train.getRunningDays().length > 0) {
                boolean runs = false;
                for (String day : train.getRunningDays()) {
                    if (day.equalsIgnoreCase(dayOfWeek)) { runs = true; break; }
                }
                if (!runs) continue;
            }

            // Total booked confirmed (all classes) - used to know if any seat is left
            int totalConfirmed = ticketRepository.countConfirmedPassengerSeats(train.getTrainNo(), date);
            int totalRac      = ticketRepository.countRacPassengerSeats(train.getTrainNo(), date);
            int totalWl       = ticketRepository.countWaitlistedPassengerSeats(train.getTrainNo(), date);

            // Load classes (lazy @ElementCollection) — safe within @Transactional
            List<ClassType> classes = train.getClasses();
            // Fallback: if classes still empty (legacy data), use a standard set
            if (classes == null || classes.isEmpty()) {
                classes = List.of(ClassType.SLEEPER, ClassType.THIRD_AC, ClassType.SECOND_AC, ClassType.FIRST_AC);
            }

            int seatsPerClass = Math.max(1, (train.getTotalSeats() == null ? 100 : train.getTotalSeats()) / classes.size());
            int racPerClass   = Math.max(1, (train.getRacCapacity() == null ? 20  : train.getRacCapacity()) / classes.size());
            int wlPerClass    = Math.max(1, (train.getWaitlistCapacity() == null ? 50 : train.getWaitlistCapacity()) / classes.size());

            TrainAvailabilityDTO dto = new TrainAvailabilityDTO(train, date);

            // Compute route stops, timings, and distance for searched segment
            List<com.irctc.model.TrainRouteStop> allStops = trainRouteStopRepository.findByTrain_TrainNoOrderByStopOrderAsc(train.getTrainNo());
            int segDist = distanceFareService.calculateSegmentDistance(allStops, source, destination);
            int totDist = distanceFareService.calculateTotalRouteDistance(allStops);
            if (segDist <= 0) segDist = totDist;

            java.time.LocalTime depAtSrc = train.getDepartureTime();
            java.time.LocalTime arrAtDest = train.getArrivalTime();
            String srcName = train.getSourceStation() != null ? train.getSourceStation().getStationName() : source;
            String destName = train.getDestinationStation() != null ? train.getDestinationStation().getStationName() : destination;

            for (com.irctc.model.TrainRouteStop st : allStops) {
                if (st.getStation() != null) {
                    if (st.getStation().getStationCode().equalsIgnoreCase(source)) {
                        if (st.getDepartureTime() != null) depAtSrc = st.getDepartureTime();
                        srcName = st.getStation().getStationName();
                    } else if (st.getStation().getStationCode().equalsIgnoreCase(destination)) {
                        if (st.getArrivalTime() != null) arrAtDest = st.getArrivalTime();
                        destName = st.getStation().getStationName();
                    }
                }
            }

            dto.setSourceStationCode(source);
            dto.setDestinationStationCode(destination);
            dto.setSourceStationName(srcName);
            dto.setDestinationStationName(destName);
            dto.setDepartureTimeAtSource(depAtSrc);
            dto.setArrivalTimeAtDestination(arrAtDest);
            dto.setDistanceKm(segDist);

            for (ClassType ct : classes) {
                int bookedConfirmed = ticketRepository.countConfirmedByClass(train.getTrainNo(), date, ct);
                int bookedRac       = ticketRepository.countRacByClass(train.getTrainNo(), date, ct);
                int bookedWl        = ticketRepository.countWaitingByClass(train.getTrainNo(), date, ct);

                dto.addClassAvailability(ct, seatsPerClass, bookedConfirmed, bookedRac, bookedWl, racPerClass, wlPerClass);
                double fareForClass = distanceFareService.calculateSegmentFare(train, ct, segDist, totDist);
                dto.addClassFare(ct, fareForClass);
            }

            results.add(dto);
        }
        return results;
    }

    public List<Ticket> getAllTrainTickets(LocalDate date) {
        List<Train> trains = trainRepository.findByIsActiveTrue();
        List<Ticket> tickets = new ArrayList<>();
        String dayOfWeek = date.getDayOfWeek().name();
        
        for (Train train : trains) {
            boolean runsOnDate = false;
            if (train.getRunningDays() != null) {
                for (String day : train.getRunningDays()) {
                    if (day.equalsIgnoreCase(dayOfWeek)) {
                        runsOnDate = true;
                        break;
                    }
                }
            }
            if (!runsOnDate && train.getRunningDays() != null && train.getRunningDays().length > 0) {
                continue;
            }
            
            Ticket ticket = new Ticket(null, train, null, date, null, null, 0.0);
            tickets.add(ticket);
        }
        return tickets;
    }
    
    public int getConfirmedSeatCount(String trainNo, LocalDate date) {
        return ticketRepository.countConfirmedPassengerSeats(trainNo, date);
    }

    public int getRacSeatCount(String trainNo, LocalDate date) {
        return ticketRepository.countRacPassengerSeats(trainNo, date);
    }

    public int getWlSeatCount(String trainNo, LocalDate date) {
        return ticketRepository.countWaitlistedPassengerSeats(trainNo, date);
    }

    public boolean isTatkalEnabled(String trainNo, LocalDate date) {
        if (trainNo == null) return false;
        String trimmedTrainNo = trainNo.trim();
        boolean enabled = trainTatkalRepository.existsByTrain_TrainNoAndJourneyDate(trimmedTrainNo, date);
        logger.debug("Tatkal check for train: [{}], date: {} -> Result: {}", trimmedTrainNo, date, enabled);
        return enabled;
    }

    private void promoteWaitlist(String trainNo, LocalDate date, int releasedSeats, TicketStatus oldStatus) {
        // Fetch tickets sorted by booking date
        List<Ticket> racTickets = new ArrayList<>(ticketRepository.findTicketsByDateAndStatus(date, TicketStatus.RAC));
        racTickets.removeIf(t -> !t.getTrain().getTrainNo().equals(trainNo));
        racTickets.sort((t1, t2) -> t1.getBookingDate().compareTo(t2.getBookingDate()));
        
        List<Ticket> wlTickets = new ArrayList<>(ticketRepository.findTicketsByDateAndStatus(date, TicketStatus.WAITING));
        wlTickets.removeIf(t -> !t.getTrain().getTrainNo().equals(trainNo));
        wlTickets.sort((t1, t2) -> t1.getBookingDate().compareTo(t2.getBookingDate()));
        
        int availableConfirmed = oldStatus == TicketStatus.CONFIRMED ? releasedSeats : 0;
        int availableRac = oldStatus == TicketStatus.RAC ? releasedSeats : 0;

        // Phase 1: Promote RAC to CONFIRMED
        for (Ticket t : new ArrayList<>(racTickets)) {
            if (availableConfirmed <= 0) break;
            int paxCount = Math.max(1, t.getAllPassengers().size());
            if (availableConfirmed >= paxCount) {
                t.setStatus(TicketStatus.CONFIRMED);
                t.setRacNumber(null);
                seatAllocationService.allocateSeats(t);
                ticketRepository.save(t);
                availableConfirmed -= paxCount;
                availableRac += paxCount; // Old RAC spots open up
                racTickets.remove(t);

                emailNotifier.update(t, TicketStatus.RAC, TicketStatus.CONFIRMED);
                smsNotifier.update(t, TicketStatus.RAC, TicketStatus.CONFIRMED);
            }
        }

        // Phase 2: If availableConfirmed > 0 still (e.g. no RAC tickets existed or racTickets exhausted),
        // directly promote WL to CONFIRMED
        for (Ticket t : new ArrayList<>(wlTickets)) {
            if (availableConfirmed <= 0) break;
            int paxCount = Math.max(1, t.getAllPassengers().size());
            if (availableConfirmed >= paxCount) {
                t.setStatus(TicketStatus.CONFIRMED);
                t.setWaitlistNumber(null);
                seatAllocationService.allocateSeats(t);
                ticketRepository.save(t);
                availableConfirmed -= paxCount;
                wlTickets.remove(t);

                emailNotifier.update(t, TicketStatus.WAITING, TicketStatus.CONFIRMED);
                smsNotifier.update(t, TicketStatus.WAITING, TicketStatus.CONFIRMED);
            }
        }

        // Phase 3: Promote remaining WL to RAC (using availableRac seats)
        for (Ticket t : new ArrayList<>(wlTickets)) {
            if (availableRac <= 0) break;
            int paxCount = Math.max(1, t.getAllPassengers().size());
            if (availableRac >= paxCount) {
                t.setStatus(TicketStatus.RAC);
                t.setWaitlistNumber(null);
                seatAllocationService.allocateSeats(t);
                ticketRepository.save(t);
                availableRac -= paxCount;
                racTickets.add(t);
                wlTickets.remove(t);

                emailNotifier.update(t, TicketStatus.WAITING, TicketStatus.RAC);
                smsNotifier.update(t, TicketStatus.WAITING, TicketStatus.RAC);
            }
        }

        // Phase 4: Sequential Renumbering of RAC and WL
        racTickets.sort((t1, t2) -> t1.getBookingDate().compareTo(t2.getBookingDate()));
        int currentRacSeq = 1;
        for (Ticket t : racTickets) {
            t.setRacNumber(currentRacSeq++);
            seatAllocationService.allocateSeats(t);
            ticketRepository.save(t);
        }

        wlTickets.sort((t1, t2) -> t1.getBookingDate().compareTo(t2.getBookingDate()));
        int currentWlSeq = 1;
        for (Ticket t : wlTickets) {
            t.setWaitlistNumber(currentWlSeq++);
            seatAllocationService.allocateSeats(t);
            ticketRepository.save(t);
        }
    }

    private double calculateFoodTotal(String foodOrderJson) {
        if (foodOrderJson == null || foodOrderJson.trim().isEmpty() || foodOrderJson.equals("[]")) {
            return 0.0;
        }
        
        double total = 0.0;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(foodOrderJson);
            if (root.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : root) {
                    int qty = node.path("qty").asInt(0);
                    double price = node.path("price").asDouble(0.0);
                    total += (qty * price);
                }
                return total;
            }
        } catch (Exception ignored) {}

        try {
            Pattern pattern = Pattern.compile("\"qty\":(\\d+),\"price\":([0-9.]+)");
            Matcher matcher = pattern.matcher(foodOrderJson);
            
            while (matcher.find()) {
                int qty = Integer.parseInt(matcher.group(1));
                double price = Double.parseDouble(matcher.group(2));
                total += (qty * price);
            }
        } catch (Exception e) {
            logger.error("Error parsing food order JSON (total): {}", e.getMessage());
        }
        return total;
    }

    private String parseFoodItems(String foodOrderJson) {
        if (foodOrderJson == null || foodOrderJson.trim().isEmpty() || foodOrderJson.equals("[]")) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(foodOrderJson);
            if (root.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : root) {
                    String name = node.path("name").asText("");
                    int qty = node.path("qty").asInt(0);
                    if (!name.isEmpty() && qty > 0) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(name).append(" (").append(qty).append(")");
                    }
                }
                if (sb.length() > 0) return sb.toString();
            }
        } catch (Exception ignored) {}

        try {
            Pattern pattern = Pattern.compile("\"name\":\"([^\"]+)\",\"qty\":(\\d+)");
            Matcher matcher = pattern.matcher(foodOrderJson);
            
            while (matcher.find()) {
                String name = matcher.group(1);
                String qty = matcher.group(2);
                if (sb.length() > 0) sb.append(", ");
                sb.append(name).append(" (").append(qty).append(")");
            }
        } catch (Exception e) {
            logger.error("Error parsing food order JSON (items): {}", e.getMessage());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}