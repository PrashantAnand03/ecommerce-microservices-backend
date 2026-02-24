package com.cts.userservice.service;

import com.cts.userservice.dto.*;

import java.util.List;

public interface UserService {
    
    // Authentication Operations
    UserResponseDto registerUser(UserRequestDto dto);
    LoginResponseDto loginUser(LoginRequestDto dto);
    Long validateTokenAndGetUserId(String token);

    // Admin Authentication Operations
    UserResponseDto registerAdmin(UserRequestDto dto);
    LoginResponseDto loginAdmin(LoginRequestDto dto);

    // User Profile Update Operations (for logged-in user)
    UserResponseDto updateName(Long userId, UpdateNameDto dto);
    UserResponseDto updateEmail(Long userId, UpdateEmailDto dto);
    void updatePassword(Long userId, PasswordChangeRequestDto dto);
    UserResponseDto updateAddress(Long userId, UpdateAddressDto dto);
    UserResponseDto updatePaymentDetails(Long userId, UpdatePaymentDto dto);
    void deleteAccount(Long userId);

    // Internal Operations (for Admin Service via Feign)
    UserResponseDto getUser(Long id);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserRequestDto dto);
    void deleteUser(Long id);
}
