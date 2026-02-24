package com.cts.userservice.client;

import com.cts.userservice.dto.AddToCartRequest;
import com.cts.userservice.dto.CartDto;
import com.cts.userservice.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "CART-SERVICE", path = "/api/carts")
public interface CartServiceClient {

    @GetMapping("/{userId}")
    ResponseEntity<CartDto> getCart(@PathVariable("userId") Long userId);

    @PostMapping("/{userId}/items")
    ResponseEntity<CartDto> addToCart(@PathVariable("userId") Long userId, @RequestBody AddToCartRequest request);

    @PutMapping("/{userId}/items/{productId}/increment")
    ResponseEntity<CartDto> incrementProduct(@PathVariable("userId") Long userId, @PathVariable("productId") Long productId);

    @PutMapping("/{userId}/items/{productId}/decrement")
    ResponseEntity<CartDto> decrementProduct(@PathVariable("userId") Long userId, @PathVariable("productId") Long productId);

    @DeleteMapping("/{userId}/items/{productId}")
    ResponseEntity<String> removeFromCart(@PathVariable("userId") Long userId, @PathVariable("productId") Long productId);

    @PostMapping("/{userId}/place-order")
    ResponseEntity<OrderResponseDto> placeOrder(@PathVariable("userId") Long userId);
}

