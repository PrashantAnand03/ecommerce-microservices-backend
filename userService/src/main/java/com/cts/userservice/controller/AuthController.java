package com.cts.userservice.controller;

import com.cts.userservice.dto.*;
import com.cts.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - Public endpoints (no token required)
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;

    @Value("${admin.secret-key:ADMIN_SECRET_KEY_2024}")
    private String adminSecretKey;

    // ==================== USER AUTH ENDPOINTS ====================

    // POST /api/auth/register - User Registration
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto dto) {
        UserResponseDto response = service.registerUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // POST /api/auth/login - User Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto response = service.loginUser(dto);
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN AUTH ENDPOINTS ====================

    // POST /api/auth/admin/register - Admin Registration (requires secret key)
    @PostMapping("/admin/register")
    public ResponseEntity<UserResponseDto> registerAdmin(@Valid @RequestBody AdminRegistrationDto dto) {
        // Validate admin secret key
        if (!adminSecretKey.equals(dto.getAdminSecretKey())) {
            throw new IllegalArgumentException("Invalid admin secret key. Admin registration denied.");
        }

        // Create user request for admin registration
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName(dto.getName());
        userRequest.setEmail(dto.getEmail());
        userRequest.setPassword(dto.getPassword());

        UserResponseDto response = service.registerAdmin(userRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // POST /api/auth/admin/login - Admin Login
    @PostMapping("/admin/login")
    public ResponseEntity<LoginResponseDto> loginAdmin(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto response = service.loginAdmin(dto);
        return ResponseEntity.ok(response);
    }
}

