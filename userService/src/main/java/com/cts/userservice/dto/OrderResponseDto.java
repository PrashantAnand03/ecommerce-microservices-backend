package com.cts.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private Long userId;
    private String shippingAddress;
    private List<OrderItemDto> items;
    private Double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}

