package com.cts.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long productId;
    private String name;            // Changed from productName to match Cart Service
    private BigDecimal unitPrice;   // Changed from productPrice to match Cart Service
    private Integer quantity;
    private BigDecimal lineTotal;   // Changed from subtotal to match Cart Service
}

