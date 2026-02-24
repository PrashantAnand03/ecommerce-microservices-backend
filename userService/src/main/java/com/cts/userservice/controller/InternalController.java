package com.cts.userservice.controller;

import com.cts.userservice.dto.UserResponseDto;
import com.cts.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal Controller for Service-to-Service Communication
 *
 * These endpoints are NOT exposed through the API Gateway.
 * They are only accessible by other microservices (Cart, Order, Product services).
 *
 * Purpose: Allow other services to validate users and fetch user details
 * without going through the Gateway authentication flow.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalController {

    private final UserService service;

    /**
     * Get user by ID (for internal service validation)
     * Used by: Cart Service, Order Service, Product Service
     *
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = service.getUser(id);
        return ResponseEntity.ok(user);
    }
}

