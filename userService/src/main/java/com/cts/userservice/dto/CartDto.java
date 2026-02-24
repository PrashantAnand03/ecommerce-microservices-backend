package com.cts.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long cartId;
    private Long userId;
    private List<CartItemDto> items;
    private Long totalQuantity;  // Changed from totalAmount to match Cart Service
    private BigDecimal total;     // Added to match Cart Service
}

