package com.cts.product_service.controller;

import com.cts.product_service.entity.Product;
import com.cts.product_service.service.ProductService;
import com.cts.product_service.util.SecurityValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductControllerTest - Uses MockMvc with manual setup
 * Tests all REST endpoints for Product operations
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @Mock
    private SecurityValidator securityValidator;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        ProductController productController = new ProductController(productService, securityValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        objectMapper = new ObjectMapper();

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("iPhone 15 Pro");
        testProduct.setDescription("Apple iPhone 15 Pro with A17 Pro chip");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setCategory("Electronics");
        testProduct.setStock(50);
        testProduct.setImageUrl("https://example.com/iphone15.jpg");
    }

    // ==================== CREATE PRODUCT TESTS (ADMIN ONLY) ====================

    @Test
    void createProduct_AsAdmin_ShouldCreateProduct() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(securityValidator).validateAuthentication(1L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_WithTrailingSlash_ShouldCreateProduct() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_WithMinimalData_ShouldCreateProduct() throws Exception {
        Product minimalProduct = new Product();
        minimalProduct.setProductId(2L);
        minimalProduct.setName("Basic Laptop");
        minimalProduct.setPrice(new BigDecimal("599.99"));

        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.createProduct(any(Product.class))).thenReturn(minimalProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalProduct))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.name").value("Basic Laptop"));

        verify(productService).createProduct(any(Product.class));
    }

    // ==================== GET PRODUCT BY ID TESTS (PUBLIC) ====================

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.description").value("Apple iPhone 15 Pro with A17 Pro chip"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(50));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_WithDifferentId_ShouldReturnCorrectProduct() throws Exception {
        Product anotherProduct = new Product();
        anotherProduct.setProductId(5L);
        anotherProduct.setName("Samsung Galaxy");
        anotherProduct.setPrice(new BigDecimal("849.99"));

        when(productService.getProductById(5L)).thenReturn(anotherProduct);

        mockMvc.perform(get("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(5))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy"));

        verify(productService).getProductById(5L);
    }

    // ==================== GET ALL PRODUCTS TESTS (PUBLIC) ====================

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Samsung Galaxy S24");
        product2.setPrice(new BigDecimal("849.99"));
        product2.setCategory("Electronics");

        List<Product> products = Arrays.asList(testProduct, product2);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].name").value("Samsung Galaxy S24"));

        verify(productService).getAllProducts();
    }

    @Test
    void getAllProducts_WithTrailingSlash_ShouldReturnProductList() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getAllProducts();
    }

    @Test
    void getAllProducts_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService).getAllProducts();
    }

    // ==================== UPDATE PRODUCT TESTS (ADMIN ONLY) ====================

    @Test
    void updateProduct_AsAdmin_ShouldUpdateProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setProductId(1L);
        updatedProduct.setName("iPhone 15 Pro Max");
        updatedProduct.setDescription("Updated description");
        updatedProduct.setPrice(new BigDecimal("1199.99"));
        updatedProduct.setCategory("Electronics");
        updatedProduct.setStock(30);

        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro Max"))
                .andExpect(jsonPath("$.price").value(1199.99))
                .andExpect(jsonPath("$.stock").value(30));

        verify(securityValidator).validateAuthentication(1L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_WithDifferentId_ShouldUpdateCorrectProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setProductId(5L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(new BigDecimal("299.99"));

        doNothing().when(securityValidator).validateAuthentication(2L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.updateProduct(eq(5L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(5));

        verify(productService).updateProduct(eq(5L), any(Product.class));
    }

    // ==================== DELETE PRODUCT TESTS (ADMIN ONLY) ====================

    @Test
    void deleteProduct_AsAdmin_ShouldDeleteProduct() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(securityValidator).validateAuthentication(1L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_WithDifferentId_ShouldDeleteCorrectProduct() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(1L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        doNothing().when(productService).deleteProduct(10L);

        mockMvc.perform(delete("/api/products/10")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(10L);
    }

    // ==================== ADMIN ACCESS VERIFICATION TESTS ====================

    @Test
    void createProduct_VerifiesAdminValidation() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(5L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct))
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(securityValidator).validateAuthentication(5L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
    }

    @Test
    void updateProduct_VerifiesAdminValidation() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(3L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct))
                        .header("X-User-Id", "3")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(securityValidator).validateAuthentication(3L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
    }

    @Test
    void deleteProduct_VerifiesAdminValidation() throws Exception {
        doNothing().when(securityValidator).validateAuthentication(4L, "ADMIN");
        doNothing().when(securityValidator).validateAdminRole("ADMIN");
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1")
                        .header("X-User-Id", "4")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(securityValidator).validateAuthentication(4L, "ADMIN");
        verify(securityValidator).validateAdminRole("ADMIN");
    }
}

