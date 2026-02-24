package com.cts.product_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Security validator for authentication and authorization checks
 */
@Component
@Slf4j
public class SecurityValidator {

    /** Validate that the user is authenticated (has valid userId and role in headers) */
    public void validateAuthentication(Long userId, String role) {
        log.debug("Validating authentication for userId: {}, role: {}", userId, role);
        if (userId == null || role == null || role.trim().isEmpty()) {
            log.warn("Authentication failed - Missing or invalid userId: {}, role: {}", userId, role);
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required. Missing or invalid token."
            );
        }
        log.debug("Authentication validated successfully for userId: {}", userId);
    }

    /** Validate that the user has USER role */
    public void validateUserRole(String role) {
        log.debug("Validating USER role: {}", role);
        if (!"USER".equalsIgnoreCase(role)) {
            log.warn("Access denied - USER role required, but got: {}", role);
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. USER role required."
            );
        }
        log.debug("USER role validation successful");
    }

    /** Validate that the user has ADMIN role */
    public void validateAdminRole(String role) {
        log.debug("Validating ADMIN role: {}", role);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            log.warn("Access denied - ADMIN role required, but got: {}", role);
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. ADMIN role required."
            );
        }
        log.debug("ADMIN role validation successful");
    }

    /** Validate that the user has either USER or ADMIN role */
    public void validateUserOrAdminRole(String role) {
        log.debug("Validating USER or ADMIN role: {}", role);
        if (!"USER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            log.warn("Access denied - USER or ADMIN role required, but got: {}", role);
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. USER or ADMIN role required."
            );
        }
        log.debug("USER/ADMIN role validation successful");
    }
}

