package com.irctc.pattern.singleton;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Pattern implementation — managed by Spring to avoid DevTools
 * classloader conflicts. The static getInstance() delegates to the Spring bean.
 */
@Component
public class AuditLogger {

    // Spring-managed singleton instance registration
    private static AuditLogger INSTANCE;

    // In-memory audit log (survives restarts within a session, easy for demo)
    private final List<String> logEntries = Collections.synchronizedList(new ArrayList<>());

    /**
     * Called by Spring after construction — registers this bean as the static getInstance() target.
     */
    public AuditLogger() {
        INSTANCE = this;
    }

    /**
     * Legacy access pattern kept for backward compatibility with all services.
     * Always returns the Spring-managed bean instance.
     */
    public static synchronized AuditLogger getInstance() {
        if (INSTANCE == null) {
            // Fallback: create a temporary instance if Spring context not ready
            INSTANCE = new AuditLogger();
        }
        return INSTANCE;
    }

    public void log(String action, String user, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] USER: %-15s | ACTION: %-25s | %s",
                timestamp, user, action, details);
        System.out.println("[AUDIT] " + logEntry);
        logEntries.add(logEntry);
        // Keep only last 500 entries to avoid memory leak
        if (logEntries.size() > 500) {
            logEntries.remove(0);
        }
    }

    public List<String> getRecentLogs(int count) {
        int size = logEntries.size();
        int from = Math.max(0, size - count);
        return new ArrayList<>(logEntries.subList(from, size));
    }

    public List<String> getAllLogs() {
        return new ArrayList<>(logEntries);
    }
}