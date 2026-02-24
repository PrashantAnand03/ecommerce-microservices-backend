package com.cts.cart.service;

import com.cts.cart.client.ProductClient;
import com.cts.cart.client.UserClient;
import com.cts.cart.dto.responsedto.CartDto;
import com.cts.cart.dto.ProductDto;
import com.cts.cart.dto.UserDto;
import com.cts.cart.entity.Cart;
import com.cts.cart.entity.CartItem;
import com.cts.cart.repository.CartRepository;
import com.cts.cart.service.impl.CartServiceImpl;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserDto testUser;
    private ProductDto testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new UserDto(1L, "John Doe", "john@example.com", "123 Main St", "VISA-1234");
        testProduct = new ProductDto(100L, "Test Product", new BigDecimal("29.99"), 50);

        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setUserId(1L);
    }

    // ==================== GET CART TESTS ====================

    @Test
    void getCart_WhenCartExists_ShouldReturnCartDto() {
        // Arrange
        CartItem item = new CartItem();
        item.setCartItemId(1L);
        item.setCart(testCart);
        item.setProductId(100L);
        item.setQuantity(2);
        testCart.getItems().add(item);

        when(userClient.getUser(1L)).thenReturn(testUser);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productClient.getProduct(100L)).thenReturn(testProduct);

        // Act
        CartDto result = cartService.getCart(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalQuantity()).isEqualTo(2L);
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("59.98"));

        verify(cartRepository).findByUserId(1L);
        verify(productClient).getProduct(100L);
    }

    @Test
    void getCart_WhenCartDoesNotExist_ShouldCreateNewCart() {
        // Arrange
        Cart newCart = new Cart();
        newCart.setCartId(1L);
        newCart.setUserId(1L);

        when(userClient.getUser(1L)).thenReturn(testUser);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // Act
        CartDto result = cartService.getCart(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.items()).isEmpty();
        assertThat(result.totalQuantity()).isEqualTo(0L);
        assertThat(result.total()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCart_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(userClient.getUser(1L)).thenThrow(FeignException.NotFound.class);

        // Act & Assert
        assertThatThrownBy(() -> cartService.getCart(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 1");
    }

    // ==================== ADD PRODUCT TESTS ====================

    @Test
    void addProduct_WhenProductIsValid_ShouldAddToCart() {
        // Arrange
        when(userClient.getUser(1L)).thenReturn(testUser);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productClient.getProduct(100L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartDto result = cartService.addProduct(1L, 100L, 2);

        // Assert
        assertThat(result).isNotNull();
        verify(cartRepository, times(2)).save(any(Cart.class)); // once for add, once in getCart
        verify(productClient, atLeastOnce()).getProduct(100L);
    }

    @Test
    void addProduct_WhenQuantityIsNull_ShouldDefaultToOne() {
        // Arrange
        when(userClient.getUser(1L)).thenReturn(testUser);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productClient.getProduct(100L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartDto result = cartService.addProduct(1L, 100L, null);

        // Assert
        assertThat(result).isNotNull();
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
    }

    @Test
    void addProduct_WhenInsufficientStock_ShouldThrowIllegalArgumentException() {
        // Arrange
        ProductDto lowStockProduct = new ProductDto(100L, "Test Product", new BigDecimal("29.99"), 1);

        when(userClient.getUser(1L)).thenReturn(testUser);
        when(productClient.getProduct(100L)).thenReturn(lowStockProduct);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addProduct(1L, 100L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void addProduct_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(userClient.getUser(1L)).thenThrow(FeignException.NotFound.class);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addProduct(1L, 100L, 2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addProduct_WhenProductExistsInCart_ShouldIncrementQuantity() {
        // Arrange
        CartItem existingItem = new CartItem();
        existingItem.setCartItemId(1L);
        existingItem.setCart(testCart);
        existingItem.setProductId(100L);
        existingItem.setQuantity(2);
        testCart.getItems().add(existingItem);

        when(userClient.getUser(1L)).thenReturn(testUser);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productClient.getProduct(100L)).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.addProduct(1L, 100L, 3);

        // Assert
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
    }

    // ==================== DECREMENT PRODUCT TESTS ====================

    @Test
    void decrementProduct_WhenQuantityGreaterThanOne_ShouldReduceQuantity() {
        // Arrange
        CartItem item = new CartItem();
        item.setCartItemId(1L);
        item.setCart(testCart);
        item.setProductId(100L);
        item.setQuantity(3);
        testCart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(userClient.getUser(1L)).thenReturn(testUser);
        when(productClient.getProduct(100L)).thenReturn(testProduct);

        // Act
        CartDto result = cartService.decrementProduct(1L, 100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void decrementProduct_WhenQuantityIsOne_ShouldRemoveItem() {
        // Arrange
        CartItem item = new CartItem();
        item.setCartItemId(1L);
        item.setCart(testCart);
        item.setProductId(100L);
        item.setQuantity(1);
        testCart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(userClient.getUser(1L)).thenReturn(testUser);

        // Act
        CartDto result = cartService.decrementProduct(1L, 100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
    }

    @Test
    void decrementProduct_WhenProductNotInCart_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.decrementProduct(1L, 100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product 100 not in cart");
    }

    @Test
    void decrementProduct_WhenCartNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.decrementProduct(1L, 100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cart not found for user 1");
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    void deleteProduct_WhenProductExists_ShouldRemoveFromCart() {
        // Arrange
        CartItem item = new CartItem();
        item.setCartItemId(1L);
        item.setCart(testCart);
        item.setProductId(100L);
        item.setQuantity(3);
        testCart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.deleteProduct(1L, 100L);

        // Assert
        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }

    @Test
    void deleteProduct_WhenProductNotInCart_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.deleteProduct(1L, 100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product 100 not in cart");
    }

    @Test
    void deleteProduct_WhenCartNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.deleteProduct(1L, 100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cart not found for user 1");
    }

    // ==================== USER VALIDATION TESTS ====================

    @Test
    void validateUserExists_WhenUserServiceUnavailable_ShouldThrowRuntimeException() {
        // Arrange
        when(userClient.getUser(1L)).thenThrow(FeignException.ServiceUnavailable.class);

        // Act & Assert
        assertThatThrownBy(() -> cartService.getCart(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User service unavailable");
    }
}

