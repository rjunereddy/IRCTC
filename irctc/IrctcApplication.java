// src/main/java/com/irctc/IrctcApplication.java
package com.irctc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IrctcApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IrctcApplication.class, args);
        System.out.println("=".repeat(50));
        System.out.println("🚂 IRCTC Railway Reservation System Started!");
        System.out.println("📍 Access at: http://localhost:8080");
        System.out.println("👤 Admin Login: admin / admin123");
        System.out.println("=".repeat(50));
    }
}