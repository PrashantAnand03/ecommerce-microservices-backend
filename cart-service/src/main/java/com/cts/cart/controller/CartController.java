package com.cts.cart.controller;

import com.cts.cart.client.OrderClient;
import com.cts.cart.client.UserClient;
import com.cts.cart.dto.responsedto.CartDto;
import com.cts.cart.service.CartService;
import com.cts.cart.util.SecurityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService service;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final SecurityValidator securityValidator;

    /** Request payload for adding an item to the cart.*/
    public record AddItemRequest(Long productId, Integer quantity) {
    }

    // ============================================================================
    // These endpoints are called by User Service (via Feign)
    // ENDPOINTS FOR USER SERVICE → CART SERVICE FLOW
    // ============================================================================

    /**
     * Retrieves the cart of a specific user.
     * <p>
     * This endpoint is used by authenticated users to view their own cart.
     * Admin users may access any user's cart.
     * @param userId ID of the cart owner
     * @param requestingUserId ID of the user making the request (from header)
     * @param role Role of the requesting user (USER / ADMIN)
     * @return The full cart details of the user
     */

    @GetMapping("/{userId}")
    public ResponseEntity<CartDto> getCart(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("USER: Fetching cart for user: {} by requesting user: {}", userId, requestingUserId);
        /* Validate authentication */
        securityValidator.validateAuthentication(requestingUserId, role);

        /* Validate user can only access their own cart (unless admin) */
        securityValidator.validateUserOwnership(requestingUserId, userId, role);

        CartDto result = service.getCart(userId);
        log.info("USER: Cart fetched for user: {} successfully", userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Adds a product to the user's cart.
     * <p>
     * If the product already exists, its quantity is increased
     * @param userId ID of the cart owner
     * @param request Product ID and quantity to add
     * @param requestingUserId ID of the requester from header
     * @param role Role of requester
     * @return Updated cart after adding the item
     */

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartDto> addToCart(
            @PathVariable Long userId,
            @RequestBody AddItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("USER: Adding product {} to cart for user: {} by requesting user: {}",
                request.productId(), userId, requestingUserId);
        /* Validate authentication */
        securityValidator.validateAuthentication(requestingUserId, role);

        /* Validate user can only modify their own cart (unless admin) */
        securityValidator.validateUserOwnership(requestingUserId, userId, role);

        CartDto result = service.addProduct(userId, request.productId(), request.quantity());
        log.info("USER: Product {} added to cart for user: {} successfully", request.productId(), userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Increments the quantity of an item in the user's cart by 1.
     * @param userId Owner of the cart
     * @param productId Product whose quantity is to be incremented
     * @param requestingUserId Header user ID of requester
     * @param role Role of requester
     * @return Updated cart after incrementing the product quantity
     */

    @PutMapping("/{userId}/items/{productId}/increment")
    public ResponseEntity<CartDto> incrementItem(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("USER: Incrementing product {} in cart for user: {} by requesting user: {}",
                productId, userId, requestingUserId);
        /* Validate authentication */
        securityValidator.validateAuthentication(requestingUserId, role);

        /* Validate user can only modify their own cart (unless admin) */
        securityValidator.validateUserOwnership(requestingUserId, userId, role);

        CartDto result = service.addProduct(userId, productId, 1);
        log.info("USER: Product {} incremented in cart for user: {} successfully", productId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Decrements the quantity of a product in the cart.
     * <p>
     * If quantity reaches zero, product may be removed (based on service logic)
     * @param userId The owner of the cart
     * @param productId The product whose quantity to reduce
     * @param requestingUserId Header user ID
     * @param role Role of requester
     * @return Updated cart after decrementing the quantity
     */

    @PutMapping("/{userId}/items/{productId}/decrement")
    public ResponseEntity<CartDto> decrementItem(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("USER: Decrementing product {} in cart for user: {} by requesting user: {}",
                productId, userId, requestingUserId);
        /* Validate authentication */
        securityValidator.validateAuthentication(requestingUserId, role);

        /* Validate user can only modify their own cart (unless admin) */
        securityValidator.validateUserOwnership(requestingUserId, userId, role);

        CartDto result = service.decrementProduct(userId, productId);
        log.info("USER: Product {} decremented in cart for user: {} successfully", productId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes a product from the user's cart entirely.
     * @param userId           Cart owner's ID
     * @param productId        Product to delete
     * @param requestingUserId Header user ID of requester
     * @param role             Role of requester
     * @return Confirmation message upon successful deletion
     */

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<String> deleteItem(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("USER: Removing product {} from cart for user: {} by requesting user: {}",
                productId, userId, requestingUserId);
        /* Validate authentication */
        securityValidator.validateAuthentication(requestingUserId, role);

        /* Validate user can only modify their own cart (unless admin) */
        securityValidator.validateUserOwnership(requestingUserId, userId, role);

        service.deleteProduct(userId, productId);
        log.info("USER: Product {} removed from cart for user: {} successfully", productId, userId);
        return ResponseEntity.ok("Item removed from cart");
    }
}