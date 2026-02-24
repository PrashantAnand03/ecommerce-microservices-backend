package com.cts.userservice.client;

import com.cts.userservice.dto.OrderRequestDto;
import com.cts.userservice.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ORDER-SERVICE", path = "/orders")
public interface OrderServiceClient {

    @PostMapping
    ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequest);

    @GetMapping("/{orderId}/track")
    ResponseEntity<OrderResponseDto> getOrderById(@PathVariable("orderId") Long orderId);

    @GetMapping("/user/{userId}")
    ResponseEntity<List<OrderResponseDto>> getOrdersByUserId(@PathVariable("userId") Long userId);

    @PatchMapping("/{orderId}/status")
    ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);

    @PatchMapping("/{orderId}/payment")
    ResponseEntity<OrderResponseDto> updatePaymentStatus(@PathVariable("orderId") Long orderId, @RequestParam("paymentStatus") String paymentStatus);
}

