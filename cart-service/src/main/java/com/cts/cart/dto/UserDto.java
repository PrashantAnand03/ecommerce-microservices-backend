package com.cts.cart.dto;

public record UserDto(
        Long userId,
        String name,
        String email,
        String shippingAddress,
        String paymentDetails
) {}
