package com.irctc;

import com.irctc.model.Passenger;
import com.irctc.model.PassengerDetail;
import com.irctc.model.Station;
import com.irctc.model.Ticket;
import com.irctc.model.Train;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import com.irctc.model.enums.TicketStatus;
import com.irctc.service.PdfGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfGeneratorServiceTest {

    private PdfGeneratorService pdfService;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        pdfService = new PdfGeneratorService();

        Station src = new Station("SBC", "KSR Bengaluru", "Bangalore", "Karnataka");
        Station dest = new Station("TVC", "Trivandrum Central", "Thiruvananthapuram", "Kerala");

        Train train = new Train("16315", "Kochuveli Express", src, dest,
                LocalTime.of(19, 30), LocalTime.of(9, 30));

        Passenger user = new Passenger();
        user.setUsername("testuser");

        ticket = new Ticket("2611213092", train, user, LocalDate.now().plusDays(3),
                ClassType.SECOND_AC, QuotaType.GENERAL, 1160.0);
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setCoachNumber("A1");
        ticket.setBaseFare(1100.0);
        ticket.setServiceFee(15.0);
        ticket.setInsuranceAmount(0.45);
        ticket.setTransactionId("WALLET-2611213092");

        PassengerDetail pd = new PassengerDetail("Babitha", 21, "FEMALE");
        pd.setBerthAllotted("A1 - 1 (Lower)");
        ticket.setAllPassengers(List.of(pd));
    }

    @Test
    @DisplayName("PDF Generator: Successfully generates valid PDF byte array for e-ticket")
    void testGenerateTicketPdf() {
        byte[] pdfBytes = pdfService.generateTicketPdf(ticket);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF should not be empty");

        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", header, "Should start with standard PDF signature");
    }

    @Test
    @DisplayName("PDF Generator: Handles RAC and Waiting tickets correctly")
    void testGenerateRacAndWlTickets() {
        ticket.setStatus(TicketStatus.RAC);
        ticket.setRacNumber(2);
        byte[] racPdf = pdfService.generateTicketPdf(ticket);
        assertNotNull(racPdf);
        assertTrue(racPdf.length > 500);

        ticket.setStatus(TicketStatus.WAITING);
        ticket.setWaitlistNumber(4);
        byte[] wlPdf = pdfService.generateTicketPdf(ticket);
        assertNotNull(wlPdf);
        assertTrue(wlPdf.length > 500);
    }

    @Test
    @DisplayName("PDF Generator: Ticket with food order generates official e-Catering coupon")
    void testTicketWithFoodOrderGeneratesFoodCoupon() {
        ticket.setFoodTotal(240.0);
        ticket.setFoodItems("Veg Standard Thali (1), Rail Neer 1L (2)");

        assertTrue(ticket.hasFoodOrder(), "Ticket should report having a food order");
        assertNotNull(ticket.getFoodCouponCode(), "Food coupon code should be generated");
        assertTrue(ticket.getFoodCouponCode().startsWith("ECAT-"), "Coupon code should start with ECAT-");

        byte[] pdfWithCoupon = pdfService.generateTicketPdf(ticket);
        assertNotNull(pdfWithCoupon);
        assertTrue(pdfWithCoupon.length > 500, "PDF with food coupon should generate valid bytes");
    }

    @Test
    @DisplayName("PDF Generator: Ticket without food order does not produce a food coupon")
    void testTicketWithoutFoodOrderHasNoFoodCoupon() {
        ticket.setFoodTotal(0.0);
        ticket.setFoodItems(null);

        assertFalse(ticket.hasFoodOrder(), "Ticket without food should not report food order");
        assertNull(ticket.getFoodCouponCode(), "No food coupon code should be generated");

        byte[] pdfWithoutCoupon = pdfService.generateTicketPdf(ticket);
        assertNotNull(pdfWithoutCoupon);
        assertTrue(pdfWithoutCoupon.length > 500);
    }
}
