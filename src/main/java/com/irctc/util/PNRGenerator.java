package com.irctc.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class PNRGenerator {
    
    private static final Random random = new Random();
    
    public static String generate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePrefix = LocalDateTime.now().format(formatter);
        int randomNum = 1000 + random.nextInt(9000);
        return datePrefix + randomNum;
    }
    
    public static boolean validate(String pnr) {
        return pnr != null && pnr.matches("\\d{10}");
    }
}