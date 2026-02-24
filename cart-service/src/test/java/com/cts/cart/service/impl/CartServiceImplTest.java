package com.cts.cart.service.impl;

import com.cts.cart.client.ProductClient;
import com.cts.cart.client.UserClient;
import com.cts.cart.dto.responsedto.CartDto;
import com.cts.cart.dto.ProductDto;
import com.cts.cart.dto.UserDto;
import com.cts.cart.entity.Cart;
import com.cts.cart.entity.CartItem;
import com.cts.cart.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    private CartRepository cartRepo;
    private ProductClient productClient;
    private UserClient userClient;
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartRepo = mock(CartRepository.class);
        productClient = mock(ProductClient.class);
        userClient = mock(UserClient.class);
        cartService = new CartServiceImpl(cartRepo, productClient, userClient);
    }

    @Test
    void testGetCart_UserExists_ReturnsCart() {
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setUserId(userId);
        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userClient.getUser(userId)).thenReturn(new UserDto(userId, "John Doe", "john@example.com", "123 Street", "Visa"));

        CartDto result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        verify(cartRepo, times(1)).findByUserId(userId);
    }

    @Test
    void testGetCart_UserDoesNotExist_ThrowsException() {
        Long userId = 1L;
        when(userClient.getUser(userId)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> cartService.getCart(userId));
        verify(cartRepo, never()).findByUserId(any());
    }

    @Test
    void testAddProduct_ValidProduct_AddsToCart() {
        Long userId = 1L;
        Long productId = 100L;
        int quantity = 2;

        Cart cart = new Cart();
        cart.setUserId(userId);
        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userClient.getUser(userId)).thenReturn(new UserDto(userId, "John Doe", "john@example.com", "123 Street", "Visa"));
        when(productClient.getProduct(productId)).thenReturn(new ProductDto(productId, "Product A", BigDecimal.TEN, 10));

        CartDto result = cartService.addProduct(userId, productId, quantity);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    void testDeleteProduct_ProductExists_RemovesProduct() {
        Long userId = 1L;
        Long productId = 100L;

        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(2);
        cart.getItems().add(item);

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.deleteProduct(userId, productId);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepo, times(1)).save(cart);
    }

    @Test
    void testDeleteProduct_ProductDoesNotExist_ThrowsException() {
        Long userId = 1L;
        Long productId = 100L;

        Cart cart = new Cart();
        cart.setUserId(userId);

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(EntityNotFoundException.class, () -> cartService.deleteProduct(userId, productId));
        verify(cartRepo, never()).save(any());
    }
}
