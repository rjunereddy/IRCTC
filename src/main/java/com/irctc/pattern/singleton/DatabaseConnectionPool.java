package com.irctc.pattern.singleton;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton Pattern (OOAD Demo) — wraps Spring's DataSource.
 * Made a @Component to avoid DevTools classloader conflicts with static singletons.
 */
@Component
public class DatabaseConnectionPool {

    private static DatabaseConnectionPool INSTANCE;

    private final DataSource dataSource;

    public DatabaseConnectionPool(DataSource dataSource) {
        this.dataSource = dataSource;
        INSTANCE = this;
        System.out.println("[DatabaseConnectionPool] Singleton initialized (Spring-managed, using H2 DataSource)");
    }

    /**
     * Legacy static access kept for backward compatibility.
     */
    public static DatabaseConnectionPool getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get DB connection from pool", e);
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // Spring DataSource handles pooling internally
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public int getPoolSize() {
        return 10; // Handled by HikariCP configured via Spring
    }
}