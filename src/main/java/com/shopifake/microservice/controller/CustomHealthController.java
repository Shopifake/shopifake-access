package com.shopifake.microservice.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Custom health controller that adds service information to the health endpoint.
 */
@RestController
public class CustomHealthController {

    /**
     * Timeout for database connection check in seconds.
     */
    private static final int CONNECTION_TIMEOUT = 3;

    /**
     * Service name from application configuration.
     */
    private final String serviceName;

    /**
     * DataSource to check database health.
     */
    private final DataSource dataSource;

    /**
     * Constructor.
     *
     * @param serviceName the service name from configuration
     * @param dataSource the data source to check
     */
    public CustomHealthController(
            @Value("${spring.application.name:unknown}") final String serviceName,
            final DataSource dataSource) {
        this.serviceName = serviceName;
        this.dataSource = dataSource;
    }

    /**
     * Custom health endpoint that includes service name and database status.
     *
     * @return health response with service name and database status
     */
    @GetMapping("/actuator/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        
        // Check database health
        String dbStatus;
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(CONNECTION_TIMEOUT)) {
                dbStatus = "UP";
            } else {
                dbStatus = "DOWN";
            }
        } catch (SQLException e) {
            dbStatus = "DOWN";
        }
        
        // Overall status is UP only if database is UP
        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DOWN";
        
        health.put("status", overallStatus);
        health.put("service", serviceName);
        health.put("database", dbStatus);
        
        return health;
    }
}
