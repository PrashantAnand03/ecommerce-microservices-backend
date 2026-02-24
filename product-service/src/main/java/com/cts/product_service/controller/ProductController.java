package com.cts.product_service.controller;

import com.cts.product_service.entity.Product;
import com.cts.product_service.service.ProductService;
import com.cts.product_service.util.SecurityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Product operations
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService service;
    private final SecurityValidator securityValidator;

    /** ADMIN ONLY: Create product (requires authentication + ADMIN role) */
    @PostMapping(value = {"", "/"})
    public ResponseEntity<Product> create(
            @RequestBody Product product,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("POST /api/products - UserId: {}, Role: {}", userId, role);

        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateAdminRole(role);

        Product createdProduct = service.createProduct(product);
        log.info("Product created successfully with id: {}", createdProduct.getProductId());
        return ResponseEntity.ok(createdProduct);
    }

    /** PUBLIC: Get product by ID (no authentication required) */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        Product product = service.getProductById(id);
        log.info("Product retrieved successfully with id: {}", id);
        return ResponseEntity.ok(product);
    }

    /** PUBLIC: Get all products (no authentication required) */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<Product>> getAll() {
        log.info("GET /api/products");
        List<Product> products = service.getAllProducts();
        log.info("Retrieved {} products successfully", products != null ? products.size() : 0);
        return ResponseEntity.ok(products);
    }

    /** ADMIN ONLY: Update product (requires authentication + ADMIN role) */
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("PUT /api/products/{} - UserId: {}, Role: {}", id, userId, role);

        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateAdminRole(role);

        Product updatedProduct = service.updateProduct(id, product);
        log.info("Product updated successfully with id: {}", id);
        return ResponseEntity.ok(updatedProduct);
    }

    /** ADMIN ONLY: Delete product (requires authentication + ADMIN role) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("DELETE /api/products/{} - UserId: {}, Role: {}", id, userId, role);

        securityValidator.validateAuthentication(userId, role);
        securityValidator.validateAdminRole(role);

        service.deleteProduct(id);
        log.info("Product deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}