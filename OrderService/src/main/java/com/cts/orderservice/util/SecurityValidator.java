package com.cts.orderservice.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Security validator for authentication and authorization checks
 */
@Component
public class SecurityValidator {

    /**
     * Validate that the user is authenticated (has valid userId and role in headers)
     */
    public void validateAuthentication(Long userId, String role) {
        if (userId == null || role == null || role.trim().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required. Missing or invalid token."
            );
        }
    }

    /**
     * Validate that the user has USER role
     */
    public void validateUserRole(String role) {
        if (!"USER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. USER role required."
            );
        }
    }

    /**
     * Validate that the user has ADMIN role
     */
    public void validateAdminRole(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. ADMIN role required."
            );
        }
    }

    /**
     * Validate that the user has either USER or ADMIN role
     */
    public void validateUserOrAdminRole(String role) {
        if (!"USER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. USER or ADMIN role required."
            );
        }
    }

    /**
     * Validate that the requesting user can only access their own resources
     * (unless they are an admin)
     */
    public void validateUserOwnership(Long requestingUserId, Long resourceUserId, String role) {
        // Admin can access any resource
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        // User can only access their own resources
        if (!requestingUserId.equals(resourceUserId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. You can only access your own resources."
            );
        }
    }
}

