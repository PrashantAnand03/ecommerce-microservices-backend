package com.cts.gateway.filter;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    /**
     * List of endpoints that don't require authentication
     * ONLY registration and login endpoints are open
     * ALL other endpoints require valid JWT token
     */
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register",       // User registration
            "/api/auth/login",          // User login
            "/api/auth/admin/register", // Admin registration (requires secret key)
            "/api/auth/admin/login",    // Admin login
            "/actuator",                // Health check endpoints
            "/eureka"                   // Eureka endpoints
    );

    /**
     * Check if the endpoint is open (doesn't require authentication)
     * ALL endpoints except registration and login require token
     */
    public boolean isOpenEndpoint(String path) {
        return OPEN_ENDPOINTS.stream()
                .anyMatch(endpoint -> path.equals(endpoint) || path.startsWith(endpoint + "/"));
    }

    /**
     * Check if the endpoint requires ADMIN role
     */
    public boolean isAdminOnlyEndpoint(String path) {
        return path.startsWith("/api/admin/");
    }

    /**
     * Check if the endpoint is accessible by authenticated users
     */
    public boolean isAuthenticatedEndpoint(String path) {
        // All endpoints except open endpoints require authentication
        return !isOpenEndpoint(path);
    }
}

