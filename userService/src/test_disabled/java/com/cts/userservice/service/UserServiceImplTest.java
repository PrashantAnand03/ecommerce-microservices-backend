package com.cts.userservice.service;

import com.cts.userservice.dto.*;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.InvalidCredentialsException;
import com.cts.userservice.exception.UserAlreadyExistsException;
import com.cts.userservice.exception.UserNotFoundException;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    // private ProfileUpdateRequestDto profileUpdateDto;  // Commented out - DTO doesn't exist
    private PasswordChangeRequestDto passwordChangeDto;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setShippingAddress("123 Main St");
        user.setPaymentDetails("Card-1234");

        userRequestDto = new UserRequestDto();
        userRequestDto.setName("John Doe");
        userRequestDto.setEmail("john.doe@example.com");
        userRequestDto.setPassword("password123");
        userRequestDto.setShippingAddress("123 Main St");
        userRequestDto.setPaymentDetails("Card-1234");

        profileUpdateDto = new ProfileUpdateRequestDto();
        profileUpdateDto.setName("John Updated");
        profileUpdateDto.setShippingAddress("456 New St");
        profileUpdateDto.setPaymentDetails("Card-5678");

        passwordChangeDto = new PasswordChangeRequestDto();
        passwordChangeDto.setCurrentPassword("password123");
        passwordChangeDto.setNewPassword("newPassword456");
        passwordChangeDto.setConfirmPassword("newPassword456");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("john.doe@example.com");
        loginRequestDto.setPassword("password123");
    }

    @Test
    void createUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllUsers_Success() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserResponseDto> results = userService.getAllUsers();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertEquals("Jane Smith", results.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setName("John Updated");
        updateDto.setEmail("john.updated@example.com");
        updateDto.setPassword("newPass123");
        updateDto.setShippingAddress("789 Update St");
        updateDto.setPaymentDetails("Card-9999");

        UserResponseDto result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_EmailAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setName("John Updated");
        updateDto.setEmail("existing@example.com");
        updateDto.setPassword("newPass123");

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updateDto));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userRequestDto));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        LoginResponseDto result = userService.loginUser(loginRequestDto);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getUser());
        assertEquals("john.doe@example.com", result.getUser().getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void loginUser_InvalidEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequestDto));
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void loginUser_InvalidPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        LoginRequestDto wrongPasswordDto = new LoginRequestDto();
        wrongPasswordDto.setEmail("john.doe@example.com");
        wrongPasswordDto.setPassword("wrongPassword");

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(wrongPasswordDto));
    }

    @Test
    void updateProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.updateProfile(1L, profileUpdateDto);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateProfile(1L, profileUpdateDto));
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, passwordChangeDto);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PasswordChangeRequestDto wrongCurrentPassword = new PasswordChangeRequestDto();
        wrongCurrentPassword.setCurrentPassword("wrongPassword");
        wrongCurrentPassword.setNewPassword("newPassword456");
        wrongCurrentPassword.setConfirmPassword("newPassword456");

        assertThrows(InvalidCredentialsException.class, () -> userService.changePassword(1L, wrongCurrentPassword));
    }

    @Test
    void changePassword_PasswordsMismatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PasswordChangeRequestDto mismatchDto = new PasswordChangeRequestDto();
        mismatchDto.setCurrentPassword("password123");
        mismatchDto.setNewPassword("newPassword456");
        mismatchDto.setConfirmPassword("differentPassword");

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, mismatchDto));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("notfound@example.com"));
    }
}
