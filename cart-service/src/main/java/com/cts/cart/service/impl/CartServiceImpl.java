package com.cts.cart.service.impl;

import com.cts.cart.client.ProductClient;
import com.cts.cart.client.UserClient;
import com.cts.cart.dto.responsedto.CartDto;
import com.cts.cart.dto.responsedto.CartItemDto;
import com.cts.cart.dto.ProductDto;
import com.cts.cart.dto.UserDto;
import com.cts.cart.entity.Cart;
import com.cts.cart.entity.CartItem;
import com.cts.cart.repository.CartRepository;
import com.cts.cart.service.CartService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final ProductClient productClient;
    private final UserClient userClient;


    /** Validate if user exists */
    private void validateUserExists(Long userId) {
        log.debug("Validating user exists with id: {}", userId);
        try {
            UserDto user = userClient.getUser(userId);
            if (user == null) {
                log.error("User not found with id: {}", userId);
                throw new EntityNotFoundException("User not found with id: " + userId);
            }
            log.debug("User validation successful for id: {}", userId);
        } catch (FeignException.NotFound e) {
            log.error("User not found with id: {}", userId, e);
            throw new EntityNotFoundException("User not found with id: " + userId);
        } catch (FeignException e) {
            log.error("Failed to validate user with id: {}. User service unavailable.", userId, e);
            throw new RuntimeException("Failed to validate user with id: " + userId + ". User service unavailable.", e);
        }
    }

    /** Get a cart or create one for the user if absent */
    private Cart getOrCreateCart(Long userId) {
        log.debug("Getting or creating cart for user: {}", userId);
        validateUserExists(userId); // Ensure the user exists
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            log.info("Creating new cart for user: {}", userId);
            Cart c = new Cart();
            c.setUserId(userId);
            Cart savedCart = cartRepo.save(c);
            log.info("Cart created with id: {} for user: {}", savedCart.getCartId(), userId);
            return savedCart;
        });
    }


    /** 3) Get all items of a user's cart (with computed totals) */
    @Override
    public CartDto getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        long totalQuantity = 0L;

        for (CartItem ci : cart.getItems()) {
            ProductDto p = productClient.getProduct(ci.getProductId());
            BigDecimal unitPrice = p.price();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));

            itemDtos.add(new CartItemDto(
                    ci.getProductId(),
                    p.name(),
                    unitPrice,
                    ci.getQuantity(),
                    lineTotal
            ));

            total = total.add(lineTotal);
            totalQuantity += ci.getQuantity();
        }

        log.info("Cart retrieved for user: {} with {} items, total quantity: {}, total price: {}",
                userId, itemDtos.size(), totalQuantity, total);
        return new CartDto(cart.getCartId(), cart.getUserId(), itemDtos, totalQuantity, total);
    }

    /** 1) Add product to a specific user's cart (qty default = 1 if null) */
    @Override
    public CartDto addProduct(Long userId, Long productId, Integer quantity) {
        log.info("Adding product {} to cart for user: {} with quantity: {}", productId, userId, quantity);
        // Validate user exists
        validateUserExists(userId);

        int qty = (quantity == null || quantity <= 0) ? 1 : quantity;

        // Validate product exists (and optionally stock)
        ProductDto p = productClient.getProduct(productId);
        log.debug("Product fetched: {} with stock: {}", p.name(), p.stock());

        if (p.stock() != null && p.stock() < qty) {
            log.error("Insufficient stock for product {}. Requested: {}, Available: {}", productId, qty, p.stock());
            throw new IllegalArgumentException("Insufficient stock for product " + productId);
        }

        Cart cart = getOrCreateCart(userId);
        cart.addOrIncrementItem(productId, qty);
        cartRepo.save(cart);
        log.info("Product {} added to cart for user: {} successfully", productId, userId);
        return getCart(userId);
    }

    /** 2) Reduce quantity by one for a specific product */
    @Override
    public CartDto decrementProduct(Long userId, Long productId) {
        log.info("Decrementing product {} in cart for user: {}", productId, userId);
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new EntityNotFoundException("Cart not found for user " + userId);
                });
        boolean existed = cart.decrementItem(productId);
        if (!existed) {
            log.error("Product {} not found in cart for user: {}", productId, userId);
            throw new EntityNotFoundException("Product " + productId + " not in cart for user " + userId);
        }
        cartRepo.save(cart);
        log.info("Product {} decremented in cart for user: {} successfully", productId, userId);
        return getCart(userId);
    }

    /** 4) Delete a specific product from the cart (qty becomes 0 — remove item entirely) */
    @Override
    public void deleteProduct(Long userId, Long productId) {
        log.info("Deleting product {} from cart for user: {}", productId, userId);
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new EntityNotFoundException("Cart not found for user " + userId);
                });
        boolean removed = cart.removeProduct(productId);
        if (!removed) {
            log.error("Product {} not found in cart for user: {}", productId, userId);
            throw new EntityNotFoundException("Product " + productId + " not in cart for user " + userId);
        }
        cartRepo.save(cart);
        log.info("Product {} deleted from cart for user: {} successfully", productId, userId);
    }
}
