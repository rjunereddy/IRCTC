package com.irctc.dto;

import com.irctc.model.Train;
import com.irctc.model.enums.ClassType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TrainAvailabilityDTO {
    private Train train;
    private LocalDate date;
    private Map<ClassType, AvailabilityInfo> classAvailMap = new HashMap<>();

    public TrainAvailabilityDTO(Train train, LocalDate date) {
        this.train = train;
        this.date = date;
    }

    public void addClassAvailability(ClassType ct, int totalSeats, int bookedConfirmed, 
                                     int bookedRac, int bookedWl, int racCapacity, int wlCapacity) {
        classAvailMap.put(ct, new AvailabilityInfo(totalSeats, bookedConfirmed, bookedRac, bookedWl, racCapacity, wlCapacity));
    }

    public Train getTrain() { return train; }
    public LocalDate getDate() { return date; }
    public Map<ClassType, AvailabilityInfo> getClassAvailMap() { return classAvailMap; }

    public static class AvailabilityInfo {
        public String badgeClass;
        public String availabilityLabel;
        public boolean fullyBooked;

        public AvailabilityInfo(int totalSeats, int bookedConfirmed, int bookedRac, int bookedWl, int racCapacity, int wlCapacity) {
            int available = totalSeats - bookedConfirmed;
            if (available > 0) {
                this.badgeClass = "avail-confirmed";
                this.availabilityLabel = "AVAILABLE-" + available;
                this.fullyBooked = false;
            } else {
                int racLeft = racCapacity - bookedRac;
                if (racLeft > 0) {
                    this.badgeClass = "avail-rac";
                    this.availabilityLabel = "RAC " + (bookedRac + 1);
                    this.fullyBooked = false;
                } else {
                    int wlLeft = wlCapacity - bookedWl;
                    if (wlLeft > 0) {
                        this.badgeClass = "avail-wl";
                        this.availabilityLabel = "WL " + (bookedWl + 1);
                        this.fullyBooked = false;
                    } else {
                        this.badgeClass = "avail-regret";
                        this.availabilityLabel = "REGRET";
                        this.fullyBooked = true;
                    }
                }
            }
        }

        public String getBadgeClass() { return badgeClass; }
        public String getAvailabilityLabel() { return availabilityLabel; }
        public boolean isFullyBooked() { return fullyBooked; }
    }
}
