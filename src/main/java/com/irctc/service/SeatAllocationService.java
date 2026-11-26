package com.irctc.service;

import com.irctc.model.PassengerDetail;
import com.irctc.model.Ticket;
import com.irctc.model.enums.BerthType;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.TicketStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatAllocationService {

    public void allocateSeats(Ticket ticket) {
        allocateSeats(ticket, 0);
    }

    public void allocateSeats(Ticket ticket, int currentConfirmedCount) {
        if (ticket.getStatus() == TicketStatus.WAITING) {
            int wlNum = ticket.getWaitlistNumber() != null ? ticket.getWaitlistNumber() : 1;
            List<String> seats = new ArrayList<>();
            for (PassengerDetail pd : ticket.getAllPassengers()) {
                String wlTag = "WL-" + wlNum;
                pd.setBerthAllotted(wlTag);
                seats.add(wlTag);
            }
            ticket.setSeatNumbers(seats);
            ticket.setCoachNumber("WAITLIST");
            return;
        }

        if (ticket.getStatus() == TicketStatus.RAC) {
            int racNum = ticket.getRacNumber() != null ? ticket.getRacNumber() : 1;
            String coach = getCoachPrefix(ticket.getClassType()) + "1";
            ticket.setCoachNumber(coach);
            List<String> seats = new ArrayList<>();
            for (PassengerDetail pd : ticket.getAllPassengers()) {
                String racTag = coach + " - RAC-" + racNum + " (Shared Side Lower)";
                pd.setBerthAllotted(racTag);
                seats.add("RAC-" + racNum);
            }
            ticket.setSeatNumbers(seats);
            return;
        }

        // CONFIRMED Booking Allocation
        ClassType classType = ticket.getClassType();
        int seatsPerCoach = getSeatsPerCoach(classType);
        String coachPrefix = getCoachPrefix(classType);

        // Compute coach index based on existing booking volume
        int coachNum = (currentConfirmedCount / seatsPerCoach) + 1;
        String coach = coachPrefix + coachNum;
        ticket.setCoachNumber(coach);

        int startSeatInCoach = (currentConfirmedCount % seatsPerCoach) + 1;

        List<PassengerDetail> passengers = ticket.getAllPassengers();
        List<String> seatNumbers = new ArrayList<>();

        for (int i = 0; i < passengers.size(); i++) {
            PassengerDetail pd = passengers.get(i);
            int seatNum = startSeatInCoach + i;

            // Handle coach rollover if group spans across coach boundary
            if (seatNum > seatsPerCoach) {
                coachNum++;
                coach = coachPrefix + coachNum;
                seatNum = seatNum % seatsPerCoach;
                if (seatNum == 0)
                    seatNum = seatsPerCoach;
            }

            String berthType = getBerthType(seatNum, classType);

            // Preference indicator
            String prefNote = "";
            if (pd.getBerthPreference() != null && pd.getBerthPreference() != BerthType.NO_PREFERENCE) {
                if (berthType.toUpperCase().contains(pd.getBerthPreference().name())) {
                    prefNote = " [Matched]";
                }
            }

            String allottedString = coach + " - " + seatNum + " (" + berthType + ")" + prefNote;
            pd.setBerthAllotted(allottedString);
            seatNumbers.add(coach + "-" + seatNum);
        }

        ticket.setSeatNumbers(seatNumbers);
    }

    public String getCoachPrefix(ClassType classType) {
        if (classType == null)
            return "GN";
        if (ClassType.FIRST_AC.equals(classType))
            return "H";
        if (ClassType.SECOND_AC.equals(classType))
            return "A";
        if (ClassType.THIRD_AC.equals(classType))
            return "B";
        if (ClassType.SLEEPER.equals(classType))
            return "S";
        if (ClassType.AC_CHAIR_CAR.equals(classType))
            return "C";
        if (ClassType.SECOND_SITTING.equals(classType))
            return "D";
        return "GN";
    }

    public int getSeatsPerCoach(ClassType classType) {
        if (classType == null)
            return 72;
        if (ClassType.FIRST_AC.equals(classType))
            return 24;
        if (ClassType.SECOND_AC.equals(classType))
            return 54;
        if (ClassType.THIRD_AC.equals(classType))
            return 72;
        if (ClassType.SLEEPER.equals(classType))
            return 72;
        if (ClassType.AC_CHAIR_CAR.equals(classType))
            return 78;
        if (ClassType.SECOND_SITTING.equals(classType))
            return 100;
        return 72;
    }

    public String getBerthType(int seatNum, ClassType classType) {
        if (classType == ClassType.AC_CHAIR_CAR || classType == ClassType.SECOND_SITTING) {
            return (seatNum % 3 == 0) ? "Window" : "Aisle";
        }
        if (classType == ClassType.FIRST_AC) {
            return (seatNum % 4 <= 2 && seatNum % 4 != 0) ? "Coupe" : "Cabin";
        }
        if (classType == ClassType.SECOND_AC) {
            int mod = seatNum % 6;
            if (mod == 1 || mod == 3)
                return "Lower";
            if (mod == 2 || mod == 4)
                return "Upper";
            if (mod == 5)
                return "Side Lower";
            if (mod == 0)
                return "Side Upper";
            return "Lower";
        }

        // Sleeper (SL) & Third AC (3A) Standard 8-Berth Bay
        int mod = seatNum % 8;
        if (mod == 1 || mod == 4)
            return "Lower";
        if (mod == 2 || mod == 5)
            return "Middle";
        if (mod == 3 || mod == 6)
            return "Upper";
        if (mod == 7)
            return "Side Lower";
        if (mod == 0)
            return "Side Upper";
        return "Lower";
    }
}
