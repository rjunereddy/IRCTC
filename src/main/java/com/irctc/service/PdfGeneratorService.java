package com.irctc.service;

import com.irctc.model.PassengerDetail;
import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    // IRCTC Official Color Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(13, 60, 114);    // #0D3C72
    private static final Color COLOR_SECONDARY_BLUE = new Color(24, 90, 157); // #185A9D
    private static final Color COLOR_IRCTC_ORANGE  = new Color(227, 100, 20); // #E36414
    private static final Color COLOR_LIGHT_BG      = new Color(244, 247, 250); // Soft grey-blue
    private static final Color COLOR_BORDER        = new Color(195, 207, 220); // Table border
    private static final Color COLOR_TEXT_DARK     = new Color(30, 41, 59);    // Slate dark
    private static final Color COLOR_TEXT_MUTED    = new Color(100, 116, 139); // Slate muted
    private static final Color COLOR_SUCCESS_GREEN = new Color(21, 128, 61);   // Green
    private static final Color COLOR_DANGER_RED    = new Color(185, 28, 28);   // Red
    private static final Color COLOR_AMBER_RAC     = new Color(180, 83, 9);    // Amber

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public byte[] generateTicketPdf(Ticket ticket) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 20pt margins for a clean, full-page layout
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_PRIMARY_NAVY);
            Font fSubtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_IRCTC_ORANGE);
            Font fSmall = FontFactory.getFont(FontFactory.HELVETICA, 7, COLOR_TEXT_MUTED);
            Font fSmallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, COLOR_TEXT_DARK);
            Font fLabel = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_TEXT_MUTED);
            Font fVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_TEXT_DARK);
            Font fPnr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY_NAVY);
            Font fTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fTd = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_DARK);
            Font fTdBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXT_DARK);
            Font fInstructHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_PRIMARY_NAVY);
            Font fInstructText = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, COLOR_TEXT_DARK);

            // ================================================================
            // 1. TOP HEADER BANNER (Indian Railways & IRCTC ERS)
            // ================================================================
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{35f, 35f, 30f});

            // Cell 1: Indian Railways Branding
            PdfPCell c1 = new PdfPCell();
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setPadding(2);
            c1.addElement(new Paragraph("INDIAN RAILWAYS / भारतीय रेल", fTitle));
            c1.addElement(new Paragraph("IRCTC e-Ticketing System (National Rail Portal)", fSubtitle));
            c1.addElement(new Paragraph("Official Electronic Reservation Slip (ERS)", fSmall));
            headerTable.addCell(c1);

            // Cell 2: Document Title
            PdfPCell c2 = new PdfPCell();
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setPadding(2);
            Paragraph ersTitle = new Paragraph("ELECTRONIC RESERVATION SLIP", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, COLOR_PRIMARY_NAVY));
            ersTitle.setAlignment(Element.ALIGN_CENTER);
            c2.addElement(ersTitle);
            Paragraph ersSub = new Paragraph("(Valid with original photo identity proof)", fSmallBold);
            ersSub.setAlignment(Element.ALIGN_CENTER);
            c2.addElement(ersSub);
            Paragraph ersMode = new Paragraph("Booked via: IRCTC Official Web Portal", fSmall);
            ersMode.setAlignment(Element.ALIGN_CENTER);
            c2.addElement(ersMode);
            headerTable.addCell(c2);

            // Cell 3: Barcode & PNR
            PdfPCell c3 = new PdfPCell();
            c3.setBorder(Rectangle.NO_BORDER);
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c3.setPadding(2);

            try {
                Barcode128 barcode = new Barcode128();
                barcode.setCode(ticket.getPnr());
                barcode.setCodeType(Barcode128.CODE128);
                barcode.setBarHeight(18f);
                barcode.setX(0.85f);
                barcode.setFont(null); // Do not print raw text inside barcode image itself
                Image barcodeImg = barcode.createImageWithBarcode(writer.getDirectContent(), Color.BLACK, Color.BLACK);
                barcodeImg.setAlignment(Element.ALIGN_RIGHT);
                c3.addElement(barcodeImg);
            } catch (Exception ignored) {}

            Paragraph pnrPara = new Paragraph("PNR: " + ticket.getPnr(), fPnr);
            pnrPara.setAlignment(Element.ALIGN_RIGHT);
            c3.addElement(pnrPara);

            String bookTimeStr = ticket.getBookingDate() != null ? ticket.getBookingDate().format(DT_FMT) : "Confirmed";
            Paragraph bookTimePara = new Paragraph("Booked: " + bookTimeStr, fSmall);
            bookTimePara.setAlignment(Element.ALIGN_RIGHT);
            c3.addElement(bookTimePara);

            headerTable.addCell(c3);
            document.add(headerTable);

            // Divider Line (IRCTC Navy & Orange)
            PdfPTable divider = new PdfPTable(1);
            divider.setWidthPercentage(100);
            divider.setSpacingBefore(3);
            divider.setSpacingAfter(4);
            PdfPCell divCell = new PdfPCell();
            divCell.setBackgroundColor(COLOR_PRIMARY_NAVY);
            divCell.setFixedHeight(2f);
            divCell.setBorder(Rectangle.NO_BORDER);
            divider.addCell(divCell);
            document.add(divider);

            // ================================================================
            // 2. KEY JOURNEY HIGHLIGHT STRIP (Navy Background)
            // ================================================================
            PdfPTable stripTable = new PdfPTable(4);
            stripTable.setWidthPercentage(100);
            stripTable.setWidths(new float[]{38f, 20f, 22f, 20f});
            stripTable.setSpacingAfter(4);

            String trainInfo = (ticket.getTrain() != null) 
                    ? ticket.getTrain().getTrainNo() + " / " + ticket.getTrain().getTrainName() 
                    : "16315 / KOCHUVELI EXPRESS";
            String quotaInfo = (ticket.getQuotaType() != null) ? ticket.getQuotaType().name() : "GENERAL (GN)";
            String classInfo = (ticket.getClassType() != null) ? ticket.getClassType().name() : "SECOND_AC";
            String jDateStr = (ticket.getJourneyDate() != null) ? ticket.getJourneyDate().format(DATE_FMT) : "Upcoming";

            addStripCell(stripTable, "TRAIN NO. & NAME", trainInfo);
            addStripCell(stripTable, "QUOTA", quotaInfo);
            addStripCell(stripTable, "CLASS", classInfo);
            addStripCell(stripTable, "DATE OF JOURNEY", jDateStr);
            document.add(stripTable);

            // ================================================================
            // 3. DETAILED ROUTE, STATIONS & TIMINGS GRID
            // ================================================================
            PdfPTable routeTable = new PdfPTable(4);
            routeTable.setWidthPercentage(100);
            routeTable.setWidths(new float[]{30f, 25f, 25f, 20f});
            routeTable.setSpacingAfter(5);

            String srcName = (ticket.getFromStation() != null)
                    ? ticket.getFromStation().getStationName() + " (" + ticket.getFromStation().getStationCode() + ")"
                    : (ticket.getTrain() != null && ticket.getTrain().getSourceStation() != null 
                        ? ticket.getTrain().getSourceStation().getStationName() + " (" + ticket.getTrain().getSourceStation().getStationCode() + ")"
                        : "KSR Bengaluru (SBC)");
            String destName = (ticket.getToStation() != null)
                    ? ticket.getToStation().getStationName() + " (" + ticket.getToStation().getStationCode() + ")"
                    : (ticket.getTrain() != null && ticket.getTrain().getDestinationStation() != null
                        ? ticket.getTrain().getDestinationStation().getStationName() + " (" + ticket.getTrain().getDestinationStation().getStationCode() + ")"
                        : "Kochuveli (KCVL)");

            String depTime = (ticket.getTrain() != null && ticket.getTrain().getDepartureTime() != null)
                    ? ticket.getTrain().getDepartureTime().format(TIME_FMT) + " hrs" : "19:30 hrs";
            String arrTime = (ticket.getTrain() != null && ticket.getTrain().getArrivalTime() != null)
                    ? ticket.getTrain().getArrivalTime().format(TIME_FMT) + " hrs" : "09:30 hrs";

            int paxCount = (ticket.getAllPassengers() != null) ? ticket.getAllPassengers().size() : 1;
            String distStr = (ticket.getDistanceKm() != null && ticket.getDistanceKm() > 0) ? ticket.getDistanceKm() + " KM" : "—";

            // Row 1
            addGridCell(routeTable, "From Station", srcName, fLabel, fVal);
            addGridCell(routeTable, "To Station", destName, fLabel, fVal);
            addGridCell(routeTable, "Scheduled Departure", jDateStr + " " + depTime, fLabel, fVal);
            addGridCell(routeTable, "Scheduled Arrival", arrTime, fLabel, fVal);

            // Row 2
            addGridCell(routeTable, "Boarding Station", srcName, fLabel, fVal);
            addGridCell(routeTable, "Reservation Upto", destName, fLabel, fVal);
            addGridCell(routeTable, "Distance", distStr, fLabel, fVal);
            addGridCell(routeTable, "Total Passengers", paxCount + " Adult(s), 0 Child", fLabel, fVal);
            
            String chartStatus = ticket.isChartPrepared() ? "CHART PREPARED" : "CHART NOT PREPARED";
            Font fChart = ticket.isChartPrepared() ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, COLOR_SUCCESS_GREEN) 
                                                   : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, COLOR_TEXT_MUTED);
            addGridCell(routeTable, "Chart Status", chartStatus, fLabel, fChart);

            document.add(routeTable);

            // ================================================================
            // 4. PASSENGER DETAILS TABLE (Official IRCTC Style)
            // ================================================================
            Paragraph pHeader = new Paragraph("PASSENGER DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_PRIMARY_NAVY));
            pHeader.setSpacingAfter(3);
            document.add(pHeader);

            PdfPTable paxTable = new PdfPTable(7);
            paxTable.setWidthPercentage(100);
            paxTable.setWidths(new float[]{6f, 32f, 8f, 10f, 14f, 14f, 16f});
            paxTable.setSpacingAfter(5);

            addThCell(paxTable, "#");
            addThCell(paxTable, "Passenger Name");
            addThCell(paxTable, "Age");
            addThCell(paxTable, "Gender");
            addThCell(paxTable, "Booking Status");
            addThCell(paxTable, "Current Status");
            addThCell(paxTable, "Coach / Berth");

            List<PassengerDetail> passengers = ticket.getAllPassengers();
            int idx = 1;
            boolean alternate = false;

            if (passengers != null && !passengers.isEmpty()) {
                for (PassengerDetail pd : passengers) {
                    Color rowBg = alternate ? COLOR_LIGHT_BG : Color.WHITE;
                    alternate = !alternate;

                    String bookingStatus;
                    String currentStatus;
                    Color statusColor = COLOR_TEXT_DARK;

                    if (ticket.getStatus() == TicketStatus.CONFIRMED) {
                        bookingStatus = "CNF";
                        currentStatus = "CNF";
                        statusColor = COLOR_SUCCESS_GREEN;
                    } else if (ticket.getStatus() == TicketStatus.RAC) {
                        bookingStatus = "RAC " + (ticket.getRacNumber() != null ? ticket.getRacNumber() : 1);
                        currentStatus = "RAC";
                        statusColor = COLOR_AMBER_RAC;
                    } else if (ticket.getStatus() == TicketStatus.WAITING) {
                        bookingStatus = "WL " + (ticket.getWaitlistNumber() != null ? ticket.getWaitlistNumber() : 1);
                        currentStatus = "WL";
                        statusColor = COLOR_TEXT_MUTED;
                    } else if (ticket.getStatus() == TicketStatus.CANCELLED) {
                        bookingStatus = "CAN";
                        currentStatus = "CANCELLED";
                        statusColor = COLOR_DANGER_RED;
                    } else {
                        bookingStatus = ticket.getStatus().name();
                        currentStatus = ticket.getStatus().name();
                    }

                    String berth = (pd.getBerthAllotted() != null && !pd.getBerthAllotted().isEmpty())
                            ? pd.getBerthAllotted() : (ticket.getCoachNumber() != null ? ticket.getCoachNumber() : "-");

                    addTdCell(paxTable, String.valueOf(idx++), fTd, rowBg, Element.ALIGN_CENTER);
                    addTdCell(paxTable, pd.getPassengerName().toUpperCase(), fTdBold, rowBg, Element.ALIGN_LEFT);
                    addTdCell(paxTable, String.valueOf(pd.getAge()), fTd, rowBg, Element.ALIGN_CENTER);
                    addTdCell(paxTable, pd.getGender() != null ? pd.getGender().toUpperCase() : "-", fTd, rowBg, Element.ALIGN_CENTER);
                    addTdCell(paxTable, bookingStatus, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, statusColor), rowBg, Element.ALIGN_CENTER);
                    addTdCell(paxTable, currentStatus, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, statusColor), rowBg, Element.ALIGN_CENTER);
                    addTdCell(paxTable, berth, fTdBold, rowBg, Element.ALIGN_CENTER);
                }
            } else {
                addTdCell(paxTable, "1", fTd, Color.WHITE, Element.ALIGN_CENTER);
                addTdCell(paxTable, "PASSENGER 1", fTdBold, Color.WHITE, Element.ALIGN_LEFT);
                addTdCell(paxTable, "28", fTd, Color.WHITE, Element.ALIGN_CENTER);
                addTdCell(paxTable, "MALE", fTd, Color.WHITE, Element.ALIGN_CENTER);
                addTdCell(paxTable, ticket.getStatus().name(), fTdBold, Color.WHITE, Element.ALIGN_CENTER);
                addTdCell(paxTable, ticket.getStatus().name(), fTdBold, Color.WHITE, Element.ALIGN_CENTER);
                addTdCell(paxTable, ticket.getCoachNumber() != null ? ticket.getCoachNumber() : "CNF", fTdBold, Color.WHITE, Element.ALIGN_CENTER);
            }

            document.add(paxTable);

            // ================================================================
            // 5. PAYMENT & FARE BREAKDOWN BOX (Itemized)
            // ================================================================
            Paragraph fareHeader = new Paragraph("PAYMENT & FARE BREAKDOWN", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_PRIMARY_NAVY));
            fareHeader.setSpacingAfter(3);
            document.add(fareHeader);

            PdfPTable fareTable = new PdfPTable(4);
            fareTable.setWidthPercentage(100);
            fareTable.setWidths(new float[]{25f, 25f, 25f, 25f});
            fareTable.setSpacingAfter(5);

            double baseFare = ticket.getBaseFare() != null && ticket.getBaseFare() > 0 ? ticket.getBaseFare() : (ticket.getTotalFare() != null ? ticket.getTotalFare() : 0.0);
            double serviceFee = ticket.getServiceFee() != null ? ticket.getServiceFee() : 15.0;
            double insurance = ticket.getInsuranceAmount() != null ? ticket.getInsuranceAmount() : 0.45;
            double foodTotal = ticket.getFoodTotal() != null ? ticket.getFoodTotal() : 0.0;
            double totalFare = ticket.getTotalFare() != null ? ticket.getTotalFare() : 0.0;

            String txnId = ticket.getTransactionId() != null ? ticket.getTransactionId() : "WALLET-" + ticket.getPnr();
            String payMode = txnId.startsWith("WALLET-") ? "IRCTC eWallet" : (txnId.startsWith("UPI-") ? "UPI / BHIM" : "Online Gateway");

            addGridCell(fareTable, "Ticket Base Fare", String.format("₹ %.2f", baseFare), fLabel, fVal);
            addGridCell(fareTable, "IRCTC Convenience Fee", String.format("₹ %.2f (+GST)", serviceFee), fLabel, fVal);
            addGridCell(fareTable, "Travel Insurance", String.format("₹ %.2f", insurance), fLabel, fVal);
            addGridCell(fareTable, "Catering / Food Charges", String.format("₹ %.2f", foodTotal), fLabel, fVal);

            addGridCell(fareTable, "Payment Mode", payMode, fLabel, fVal);
            addGridCell(fareTable, "Transaction ID", txnId, fLabel, fVal);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setColspan(2);
            totalCell.setBackgroundColor(new Color(236, 245, 238)); // Soft green
            totalCell.setBorderColor(COLOR_SUCCESS_GREEN);
            totalCell.setPadding(4);
            Paragraph lblTotal = new Paragraph("TOTAL FARE (INCL. ALL TAXES)", FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_TEXT_MUTED));
            Paragraph valTotal = new Paragraph(String.format("₹ %.2f", totalFare), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f, COLOR_SUCCESS_GREEN));
            totalCell.addElement(lblTotal);
            totalCell.addElement(valTotal);
            fareTable.addCell(totalCell);

            document.add(fareTable);

            // ================================================================
            // 5.1 IRCTC e-CATERING FOOD COUPON & MEAL VOUCHER (Only if food ordered)
            // ================================================================
            if (ticket.hasFoodOrder()) {
                PdfPTable voucherTable = new PdfPTable(1);
                voucherTable.setWidthPercentage(100);
                voucherTable.setSpacingAfter(5);

                PdfPCell vCell = new PdfPCell();
                vCell.setBackgroundColor(new Color(255, 248, 225)); // Warm ivory/cream
                vCell.setBorderColor(new Color(230, 81, 0)); // IRCTC e-Catering Orange
                vCell.setBorderWidth(1.2f);
                vCell.setPadding(5);

                // Voucher Header
                PdfPTable vHeadTable = new PdfPTable(2);
                vHeadTable.setWidthPercentage(100);
                vHeadTable.setWidths(new float[]{65f, 35f});

                PdfPCell vTitleCell = new PdfPCell();
                vTitleCell.setBorder(Rectangle.NO_BORDER);
                Paragraph vTitle = new Paragraph("IRCTC e-CATERING MEAL VOUCHER / खान-पान कूपन", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, new Color(230, 81, 0)));
                Paragraph vSub = new Paragraph("Authorized On-Board Catering Service • Seat Delivery Coupon", 
                        FontFactory.getFont(FontFactory.HELVETICA, 7f, COLOR_TEXT_MUTED));
                vTitleCell.addElement(vTitle);
                vTitleCell.addElement(vSub);
                vHeadTable.addCell(vTitleCell);

                PdfPCell vCodeCell = new PdfPCell();
                vCodeCell.setBorder(Rectangle.NO_BORDER);
                vCodeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                Paragraph vCodeLbl = new Paragraph("VOUCHER / COUPON CODE", FontFactory.getFont(FontFactory.HELVETICA, 6.5f, COLOR_TEXT_MUTED));
                Paragraph vCodeVal = new Paragraph(ticket.getFoodCouponCode(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, new Color(211, 47, 47)));
                vCodeLbl.setAlignment(Element.ALIGN_RIGHT);
                vCodeVal.setAlignment(Element.ALIGN_RIGHT);
                vCodeCell.addElement(vCodeLbl);
                vCodeCell.addElement(vCodeVal);
                vHeadTable.addCell(vCodeCell);

                vCell.addElement(vHeadTable);

                // Voucher Detail Grid
                PdfPTable vGrid = new PdfPTable(4);
                vGrid.setWidthPercentage(100);
                vGrid.setWidths(new float[]{25f, 25f, 30f, 20f});
                vGrid.setSpacingBefore(3);

                String coachBerth = (ticket.getCoachNumber() != null ? ticket.getCoachNumber() : "") + 
                        (ticket.getAllPassengers() != null && !ticket.getAllPassengers().isEmpty() && ticket.getAllPassengers().get(0).getBerthAllotted() != null 
                         ? " (" + ticket.getAllPassengers().get(0).getBerthAllotted() + ")" : "");
                if (coachBerth.isEmpty()) coachBerth = "As per Boarding Pass";

                String primaryPax = (ticket.getAllPassengers() != null && !ticket.getAllPassengers().isEmpty()) 
                        ? ticket.getAllPassengers().get(0).getPassengerName().toUpperCase() : "PASSENGER";

                addGridCell(vGrid, "Delivery Seat / Berth", coachBerth, fLabel, fVal);
                addGridCell(vGrid, "Primary Passenger", primaryPax, fLabel, fVal);
                addGridCell(vGrid, "Booked Meal Items", ticket.getFoodItems(), fLabel, fVal);
                addGridCell(vGrid, "Meal Charges", String.format("₹ %.2f (PAID)", ticket.getFoodTotal()), fLabel, 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_SUCCESS_GREEN));

                vCell.addElement(vGrid);

                // Redemption note
                Paragraph vNotice = new Paragraph("★ REDEMPTION INSTRUCTION: Show this coupon code to the On-Board Pantry Car Attendant upon train departure. Hot meals will be delivered directly to your berth. No additional payment required. Helpline: 1323.",
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.5f, new Color(110, 50, 0)));
                vNotice.setSpacingBefore(3);
                vCell.addElement(vNotice);

                voucherTable.addCell(vCell);
                document.add(voucherTable);
            }

            // ================================================================
            // 6. IMPORTANT INSTRUCTIONS & ADVISORY (IRCTC Standard)
            // ================================================================
            PdfPTable instructTable = new PdfPTable(1);
            instructTable.setWidthPercentage(100);
            instructTable.setSpacingAfter(4);

            PdfPCell inCell = new PdfPCell();
            inCell.setBackgroundColor(COLOR_LIGHT_BG);
            inCell.setBorderColor(COLOR_BORDER);
            inCell.setPadding(5);

            Paragraph inHead = new Paragraph("IMPORTANT INSTRUCTIONS FOR PASSENGERS:", fInstructHeader);
            inCell.addElement(inHead);

            String[] instructions = {
                "1. One of the passengers booked on this E-ticket must carry original valid Photo Identity Card (Aadhaar Card, Voter ID, Passport, Driving Licence, Pan Card, Student ID, Nationalized Bank Passbook with photo) during the entire journey.",
                "2. Valid with original ID proof. Passengers travelling without valid ID proof will be treated as travelling without ticket and charged penalty as per railway rules.",
                "3. E-ticket cancellations are permitted online only through IRCTC web portal / app prior to chart preparation. No refund is permissible once the chart is prepared.",
                "4. Confirmed Tatkal tickets are non-refundable as per Indian Railway rules.",
                "5. In case of train cancellation by Indian Railways, full refund is automatically credited back to your IRCTC eWallet / original payment method.",
                "6. Railway Security Helpline: 139  |  IRCTC Customer Support: 0755-6610661  |  Email: care@irctc.co.in"
            };

            for (String ins : instructions) {
                Paragraph pIns = new Paragraph(ins, fInstructText);
                pIns.setLeading(8.5f);
                inCell.addElement(pIns);
            }
            instructTable.addCell(inCell);
            document.add(instructTable);

            // Footer note
            Paragraph footer = new Paragraph("IRCTC E-Ticketing System • Safe & Secure Journey • Save Paper, Save Trees (Show ERS/SMS on Mobile)", 
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.5f, COLOR_TEXT_MUTED));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // Helper: Top Strip Cells (Train, Quota, Class, Date)
    private void addStripCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_PRIMARY_NAVY);
        cell.setBorderColor(Color.WHITE);
        cell.setPadding(4);
        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 6.5f, new Color(200, 220, 245)));
        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE));
        cell.addElement(pLabel);
        cell.addElement(pValue);
        table.addCell(cell);
    }

    // Helper: Standard Grid Cells (From/To, Departure/Arrival, etc.)
    private void addGridCell(PdfPTable table, String label, String value, Font fLabel, Font fVal) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        cell.addElement(new Paragraph(label, fLabel));
        cell.addElement(new Paragraph(value, fVal));
        table.addCell(cell);
    }

    // Helper: Table Header Cells
    private void addThCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        cell.setBackgroundColor(COLOR_SECONDARY_BLUE);
        cell.setBorderColor(COLOR_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }

    // Helper: Table Data Cells
    private void addTdCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(COLOR_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3.5f);
        table.addCell(cell);
    }
}
