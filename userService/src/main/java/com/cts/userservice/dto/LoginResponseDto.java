package com.cts.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String tokenType = "Bearer";
    private UserResponseDto user;
    
    public LoginResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }
}
