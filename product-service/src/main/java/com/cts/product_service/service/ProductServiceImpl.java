package com.cts.product_service.service;

import com.cts.product_service.exception.ProductNotFoundException;
import com.cts.product_service.entity.Product;
import com.cts.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of ProductService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    /** Create a new product */
    @Override
    public Product createProduct(Product product) {
        log.info("Creating new product with name: {}", product.getName());
        Product savedProduct = repository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getProductId());
        return savedProduct;
    }

    /** Get product by ID */
    @Override
    public Product getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new ProductNotFoundException(id);
                });
        log.info("Product retrieved successfully with id: {}", id);
        return product;
    }

    /** Update an existing product */
    @Override
    public Product updateProduct(Long id, Product product) {
        log.info("Updating product with id: {}", id);
        Product existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new ProductNotFoundException(id);
                });

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());

        Product updatedProduct = repository.save(existing);
        log.info("Product updated successfully with id: {}", id);
        return updatedProduct;
    }

    /** Delete a product by ID */
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!repository.existsById(id)) {
            log.error("Product not found with id: {}", id);
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    /** Get all products */
    @Override
    public @Nullable List<Product> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = repository.findAll();
        log.info("Retrieved {} products", products.size());
        return products;
    }
}