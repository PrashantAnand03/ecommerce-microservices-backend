package com.cts.userservice.service.impl;

import com.cts.userservice.dto.*;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.InvalidCredentialsException;
import com.cts.userservice.exception.UserAlreadyExistsException;
import com.cts.userservice.exception.UserNotFoundException;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.UserService;
import com.cts.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final JwtUtil jwtUtil;

    // Authentication Operations
    @Override
    public UserResponseDto registerUser(UserRequestDto dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail(), "Email");
        }
        
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setShippingAddress(dto.getShippingAddress());
        user.setPaymentDetails(dto.getPaymentDetails());
        user.setRole("USER"); // Default role for new users

        User saved = repository.save(user);
        return toResponseDto(saved);
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto dto) {
        User user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());
        
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Include role in JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setUser(toResponseDto(user));
        
        return response;
    }

    @Override
    public Long validateTokenAndGetUserId(String token) {
        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new InvalidCredentialsException("Token has expired");
            }
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }

    // Admin Authentication Operations
    @Override
    public UserResponseDto registerAdmin(UserRequestDto dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail(), "Email");
        }

        User admin = new User();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
        admin.setShippingAddress(dto.getShippingAddress());
        admin.setPaymentDetails(dto.getPaymentDetails());
        admin.setRole("ADMIN"); // Set role as ADMIN

        User saved = repository.save(admin);
        return toResponseDto(saved);
    }

    @Override
    public LoginResponseDto loginAdmin(LoginRequestDto dto) {
        User user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        // Check if user is an admin
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new InvalidCredentialsException("Access denied. Admin credentials required.");
        }

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Include role in JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setUser(toResponseDto(user));

        return response;
    }

    // User Profile Update Operations
    @Override
    public UserResponseDto updateName(Long userId, UpdateNameDto dto) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setName(dto.getName());
        User updated = repository.save(user);
        return toResponseDto(updated);
    }

    @Override
    public UserResponseDto updateEmail(Long userId, UpdateEmailDto dto) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.getEmail().equals(dto.getEmail()) && repository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail(), "Email");
        }

        user.setEmail(dto.getEmail());
        User updated = repository.save(user);
        return toResponseDto(updated);
    }

    @Override
    public void updatePassword(Long userId, PasswordChangeRequestDto dto) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.getPassword().equals(dto.getCurrentPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        user.setPassword(dto.getNewPassword());
        repository.save(user);
    }

    @Override
    public UserResponseDto updateAddress(Long userId, UpdateAddressDto dto) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setShippingAddress(dto.getShippingAddress());
        User updated = repository.save(user);
        return toResponseDto(updated);
    }

    @Override
    public UserResponseDto updatePaymentDetails(Long userId, UpdatePaymentDto dto) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setPaymentDetails(dto.getPaymentDetails());
        User updated = repository.save(user);
        return toResponseDto(updated);
    }

    @Override
    public void deleteAccount(Long userId) {
        if (!repository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        repository.deleteById(userId);
    }

    // Internal Operations (for Admin Service)
    @Override
    public UserResponseDto getUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return repository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(dto.getEmail()) && repository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail(), "Email");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setShippingAddress(dto.getShippingAddress());
        user.setPaymentDetails(dto.getPaymentDetails());

        User updated = repository.save(user);
        return toResponseDto(updated);
    }

    @Override
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // Helper method
    private UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setShippingAddress(user.getShippingAddress());
        dto.setPaymentDetails(user.getPaymentDetails());
        dto.setRole(user.getRole());
        return dto;
    }
}
