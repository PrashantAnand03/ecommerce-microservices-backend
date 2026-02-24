package com.cts.userservice.controller;

import com.cts.userservice.client.CartServiceClient;
import com.cts.userservice.client.OrderServiceClient;
import com.cts.userservice.client.ProductServiceClient;
import com.cts.userservice.dto.*;
import com.cts.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified User Controller
 *
 * Endpoint Structure:
 * - /api/user/**  → USER only endpoints (Role validated by Gateway)
 * - /api/admin/** → ADMIN only endpoints (Role validated by Gateway)
 * - /api/both/**  → Both USER and ADMIN can access (Role validated by Gateway)
 *
 * Authentication Flow:
 * 1. Gateway validates JWT token (not expired, valid signature)
 * 2. Gateway extracts userId, role, email from token
 * 3. Gateway validates role-based access (/api/user=USER, /api/admin=ADMIN, /api/both=ANY)
 * 4. Gateway passes X-User-Id, X-User-Role, X-User-Email headers to this service
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final ProductServiceClient productClient;
    private final CartServiceClient cartClient;
    private final OrderServiceClient orderClient;

    // ════════════════════════════════════════════════════════════════════════
    // USER ONLY ENDPOINTS (/api/user/**) - Role: USER
    // ════════════════════════════════════════════════════════════════════════

    // -------------------- PROFILE ENDPOINTS --------------------

    // PATCH /api/user/profile/name - Update user name
    @PatchMapping("/api/user/profile/name")
    public ResponseEntity<UserResponseDto> updateName(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateNameDto dto) {
        UserResponseDto updated = service.updateName(userId, dto);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/user/profile/email - Update user email
    @PatchMapping("/api/user/profile/email")
    public ResponseEntity<UserResponseDto> updateEmail(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateEmailDto dto) {
        UserResponseDto updated = service.updateEmail(userId, dto);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/user/profile/password - Change password
    @PatchMapping("/api/user/profile/password")
    public ResponseEntity<String> updatePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PasswordChangeRequestDto dto) {
        service.updatePassword(userId, dto);
        return ResponseEntity.ok("Password updated successfully");
    }

    // PATCH /api/user/profile/address - Update shipping address
    @PatchMapping("/api/user/profile/address")
    public ResponseEntity<UserResponseDto> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateAddressDto dto) {
        UserResponseDto updated = service.updateAddress(userId, dto);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/user/profile/payment - Update payment details
    @PatchMapping("/api/user/profile/payment")
    public ResponseEntity<UserResponseDto> updatePaymentDetails(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePaymentDto dto) {
        UserResponseDto updated = service.updatePaymentDetails(userId, dto);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/user/profile - Delete user account
    @DeleteMapping("/api/user/profile")
    public ResponseEntity<String> deleteAccount(@RequestHeader("X-User-Id") Long userId) {
        service.deleteAccount(userId);
        return ResponseEntity.ok("Account deleted");
    }

    // -------------------- CART ENDPOINTS --------------------

    // POST /api/user/cart/add/{productId} - Add product to cart
    @PostMapping("/api/user/cart/add/{productId}")
    public ResponseEntity<CartDto> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        AddToCartRequest request = new AddToCartRequest(productId, quantity);
        return cartClient.addToCart(userId, request);
    }

    // GET /api/user/cart - Get my cart
    @GetMapping("/api/user/cart")
    public ResponseEntity<CartDto> getMyCart(@RequestHeader("X-User-Id") Long userId) {
        return cartClient.getCart(userId);
    }

    // PUT /api/user/cart/{productId}/increment - Increment quantity by 1
    @PutMapping("/api/user/cart/{productId}/increment")
    public ResponseEntity<CartDto> incrementQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        return cartClient.incrementProduct(userId, productId);
    }

    // PUT /api/user/cart/{productId}/decrement - Decrement quantity by 1
    @PutMapping("/api/user/cart/{productId}/decrement")
    public ResponseEntity<CartDto> decrementQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        return cartClient.decrementProduct(userId, productId);
    }

    // DELETE /api/user/cart/{productId} - Remove product from cart
    @DeleteMapping("/api/user/cart/{productId}")
    public ResponseEntity<String> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        return cartClient.removeFromCart(userId, productId);
    }
    // -------------------- USER ORDER ENDPOINTS --------------------

    // GET /api/user/orders - Get my orders
    @GetMapping("/api/user/orders")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        return orderClient.getOrdersByUserId(userId);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN ONLY ENDPOINTS (/api/admin/**) - Role: ADMIN
    // ════════════════════════════════════════════════════════════════════════

    // -------------------- USER MANAGEMENT ENDPOINTS --------------------

    // GET /api/admin/users - Get all users
    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = service.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // GET /api/admin/users/{id} - Get user by ID
    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = service.getUser(id);
        return ResponseEntity.ok(user);
    }

    // DELETE /api/admin/users/{id} - Delete user by ID
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // -------------------- PRODUCT MANAGEMENT ENDPOINTS --------------------

    // POST /api/admin/products - Create new product
    @PostMapping("/api/admin/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        return productClient.createProduct(productDto);
    }

    // PUT /api/admin/products/{id} - Update product
    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        return productClient.updateProduct(id, productDto);
    }

    // DELETE /api/admin/products/{id} - Delete product
    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        return productClient.deleteProduct(id);
    }

    // -------------------- ORDER MANAGEMENT ENDPOINTS --------------------

    // GET /api/admin/orders/user/{userId} - Get orders by user ID
    @GetMapping("/api/admin/orders/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUserId(@PathVariable Long userId) {
        return orderClient.getOrdersByUserId(userId);
    }

    // PATCH /api/admin/orders/{orderId}/status - Update order status
    @PatchMapping("/api/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return orderClient.updateOrderStatus(orderId, status);
    }

    // PATCH /api/admin/orders/{orderId}/payment - Update payment status
    @PatchMapping("/api/admin/orders/{orderId}/payment")
    public ResponseEntity<OrderResponseDto> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam String paymentStatus) {
        return orderClient.updatePaymentStatus(orderId, paymentStatus);
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOTH USER AND ADMIN ENDPOINTS (/api/both/**) - Role: USER or ADMIN
    // ════════════════════════════════════════════════════════════════════════

    // GET /api/both/products - Get all products
    @GetMapping("/api/both/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return productClient.getAllProducts();
    }

    // GET /api/both/products/{productId} - Get product by ID
    @GetMapping("/api/both/products/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
        return productClient.getProductById(productId);
    }

    // GET /api/both/orders/{orderId} - Track order by ID
    @GetMapping("/api/both/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> trackOrder(@PathVariable Long orderId) {
        return orderClient.getOrderById(orderId);
    }
}

