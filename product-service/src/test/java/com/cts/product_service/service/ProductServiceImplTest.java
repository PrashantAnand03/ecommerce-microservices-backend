package com.cts.product_service.service;

import com.cts.product_service.entity.Product;
import com.cts.product_service.exception.ProductNotFoundException;
import com.cts.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProductServiceImplTest - Unit tests for ProductServiceImpl
 * Tests all business logic operations for Product management
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("iPhone 15 Pro");
        testProduct.setDescription("Apple iPhone 15 Pro with A17 Pro chip");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setCategory("Electronics");
        testProduct.setStock(50);
        testProduct.setImageUrl("https://example.com/iphone15.jpg");
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    void createProduct_WithValidData_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getStock()).isEqualTo(50);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WithMinimalData_ShouldSaveProduct() {
        Product minimalProduct = new Product();
        minimalProduct.setProductId(2L);
        minimalProduct.setName("Basic Laptop");
        minimalProduct.setPrice(new BigDecimal("599.99"));

        when(productRepository.save(any(Product.class))).thenReturn(minimalProduct);

        Product result = productService.createProduct(minimalProduct);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Basic Laptop");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("599.99"));

        verify(productRepository).save(minimalProduct);
    }

    @Test
    void createProduct_ShouldCallRepositorySave() {
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        productService.createProduct(testProduct);

        verify(productRepository, times(1)).save(testProduct);
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getDescription()).isEqualTo("Apple iPhone 15 Pro with A17 Pro chip");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(result.getCategory()).isEqualTo("Electronics");

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository).findById(99L);
    }

    @Test
    void getProductById_WithDifferentId_ShouldReturnCorrectProduct() {
        Product anotherProduct = new Product();
        anotherProduct.setProductId(5L);
        anotherProduct.setName("Samsung Galaxy");
        anotherProduct.setPrice(new BigDecimal("849.99"));

        when(productRepository.findById(5L)).thenReturn(Optional.of(anotherProduct));

        Product result = productService.getProductById(5L);

        assertThat(result.getProductId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Samsung Galaxy");

        verify(productRepository).findById(5L);
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    void getAllProducts_WhenProductsExist_ShouldReturnProductList() {
        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Samsung Galaxy S24");
        product2.setPrice(new BigDecimal("849.99"));

        List<Product> products = Arrays.asList(testProduct, product2);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.get(1).getName()).isEqualTo("Samsung Galaxy S24");

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_WhenNoProductsExist_ShouldReturnEmptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<Product> result = productService.getAllProducts();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_WithMultipleProducts_ShouldReturnAll() {
        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Product 2");

        Product product3 = new Product();
        product3.setProductId(3L);
        product3.setName("Product 3");

        Product product4 = new Product();
        product4.setProductId(4L);
        product4.setName("Product 4");

        List<Product> products = Arrays.asList(testProduct, product2, product3, product4);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(4);

        verify(productRepository).findAll();
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        Product updatedData = new Product();
        updatedData.setName("iPhone 15 Pro Max");
        updatedData.setDescription("Updated description");
        updatedData.setPrice(new BigDecimal("1199.99"));
        updatedData.setCategory("Premium Electronics");
        updatedData.setStock(30);
        updatedData.setImageUrl("https://example.com/iphone15promax.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updatedData);

        assertThat(result).isNotNull();
        assertThat(testProduct.getName()).isEqualTo("iPhone 15 Pro Max");
        assertThat(testProduct.getDescription()).isEqualTo("Updated description");
        assertThat(testProduct.getPrice()).isEqualByComparingTo(new BigDecimal("1199.99"));
        assertThat(testProduct.getCategory()).isEqualTo("Premium Electronics");
        assertThat(testProduct.getStock()).isEqualTo(30);

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowProductNotFoundException() {
        Product updatedData = new Product();
        updatedData.setName("Updated Name");

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, updatedData))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_ShouldUpdateAllFields() {
        Product updatedData = new Product();
        updatedData.setName("New Name");
        updatedData.setDescription("New Description");
        updatedData.setPrice(new BigDecimal("555.55"));
        updatedData.setCategory("New Category");
        updatedData.setStock(100);
        updatedData.setImageUrl("https://newurl.com/image.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateProduct(1L, updatedData);

        assertThat(testProduct.getName()).isEqualTo("New Name");
        assertThat(testProduct.getDescription()).isEqualTo("New Description");
        assertThat(testProduct.getPrice()).isEqualByComparingTo(new BigDecimal("555.55"));
        assertThat(testProduct.getCategory()).isEqualTo("New Category");
        assertThat(testProduct.getStock()).isEqualTo(100);
        assertThat(testProduct.getImageUrl()).isEqualTo("https://newurl.com/image.jpg");
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowProductNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository).existsById(99L);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void deleteProduct_ShouldCallRepositoryDeleteById() {
        when(productRepository.existsById(5L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(5L);

        productService.deleteProduct(5L);

        verify(productRepository, times(1)).deleteById(5L);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void createProduct_WithZeroStock_ShouldSaveProduct() {
        Product zeroStockProduct = new Product();
        zeroStockProduct.setProductId(3L);
        zeroStockProduct.setName("Out of Stock Product");
        zeroStockProduct.setPrice(new BigDecimal("199.99"));
        zeroStockProduct.setStock(0);

        when(productRepository.save(any(Product.class))).thenReturn(zeroStockProduct);

        Product result = productService.createProduct(zeroStockProduct);

        assertThat(result.getStock()).isEqualTo(0);

        verify(productRepository).save(zeroStockProduct);
    }

    @Test
    void createProduct_WithHighPrice_ShouldSaveProduct() {
        Product expensiveProduct = new Product();
        expensiveProduct.setProductId(4L);
        expensiveProduct.setName("Luxury Item");
        expensiveProduct.setPrice(new BigDecimal("99999.99"));

        when(productRepository.save(any(Product.class))).thenReturn(expensiveProduct);

        Product result = productService.createProduct(expensiveProduct);

        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99999.99"));

        verify(productRepository).save(expensiveProduct);
    }

    @Test
    void updateProduct_WithNullValues_ShouldUpdateToNull() {
        Product updatedData = new Product();
        updatedData.setName("Updated Name");
        updatedData.setDescription(null);
        updatedData.setPrice(new BigDecimal("100.00"));
        updatedData.setCategory(null);
        updatedData.setStock(null);
        updatedData.setImageUrl(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateProduct(1L, updatedData);

        assertThat(testProduct.getName()).isEqualTo("Updated Name");
        assertThat(testProduct.getDescription()).isNull();
        assertThat(testProduct.getCategory()).isNull();
        assertThat(testProduct.getStock()).isNull();
        assertThat(testProduct.getImageUrl()).isNull();
    }
}

