package com.cts.orderservice.controller;

import com.cts.orderservice.dto.OrderRequestDto;
import com.cts.orderservice.dto.OrderResponseDto;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;
import com.cts.orderservice.service.OrderService;
import com.cts.orderservice.util.SecurityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final SecurityValidator securityValidator;

    // USER: Create a new order (requires authentication + USER role)
    @PostMapping
    public ResponseEntity<OrderResponseDto> create(
            @RequestBody OrderRequestDto orderRequestDto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication and USER role
        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateUserRole(role);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDto));
    }

    // USER & ADMIN: Track/view order by orderId (requires authentication)
    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication
        securityValidator.validateAuthentication(userId, role);

        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrderById(orderId));
    }

    // USER: Get own orders (requires authentication + USER role)
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication and USER role
        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateUserRole(role);

        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrderByUserId(userId));
    }

    // ADMIN: Get orders by specific userId (requires authentication + ADMIN role)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication and ADMIN role
        securityValidator.validateAuthentication(requestingUserId, role);
        securityValidator.validateAdminRole(role);

        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrderByUserId(userId));
    }

    // ADMIN: Update order status (requires authentication + ADMIN role)
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication and ADMIN role
        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateAdminRole(role);

        return ResponseEntity.status(HttpStatus.OK).body(orderService.updateOrderStatus(orderId, status));
    }

    // ADMIN: Update payment status (requires authentication + ADMIN role)
    @PatchMapping("/{orderId}/payment")
    public ResponseEntity<OrderResponseDto> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Validate authentication and ADMIN role
        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateAdminRole(role);

        return ResponseEntity.status(HttpStatus.OK).body(orderService.updatePaymentStatus(orderId, paymentStatus));
    }

}
