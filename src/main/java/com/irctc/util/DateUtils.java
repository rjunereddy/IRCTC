// src/main/java/com/irctc/util/DateUtils.java
package com.irctc.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    
    public static long getHoursDifference(LocalDateTime date1, LocalDateTime date2) {
        return ChronoUnit.HOURS.between(date1, date2);
    }
    
    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return date.format(formatter);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return dateTime.format(formatter);
    }
    
    public static boolean isPastDate(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }
    
    public static boolean isWithinBookingWindow(LocalDate date) {
        LocalDate maxDate = LocalDate.now().plusDays(120);
        return date.isAfter(LocalDate.now()) && date.isBefore(maxDate);
    }
}