package com.cts.product_service.service;

import com.cts.product_service.entity.Product;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Service interface for Product operations
 */
public interface ProductService {

    /** Create a new product */
    Product createProduct(Product product);

    /** Get product by ID */
    Product getProductById(Long id);

    /** Update an existing product */
    Product updateProduct(Long id, Product product);

    /** Delete a product by ID */
    void deleteProduct(Long id);

    /** Get all products */
    @Nullable List<Product> getAllProducts();
}