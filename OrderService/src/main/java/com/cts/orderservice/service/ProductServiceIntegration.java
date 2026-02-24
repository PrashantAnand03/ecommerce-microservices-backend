package com.cts.orderservice.service;

import com.cts.orderservice.client.ProductClient;
import com.cts.orderservice.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage product-service integration.
 * This service acts as a bridge between OrderService and product-service.
 * It provides methods to fetch product information with error handling and optional caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceIntegration {

    private final ProductClient productClient;

    // Simple in-memory cache (for demonstration - use Redis/Caffeine in production)
    private final Map<Long, ProductDto> productCache = new HashMap<>();
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    private final Map<Long, Long> cacheTimestamps = new HashMap<>();

    /**
     * Fetch product details from product-service with caching.
     * @param productId The ID of the product
     * @return ProductDto or null if not found
     */
    public ProductDto getProductById(Long productId) {
        return getProductById(productId, true);
    }

    /**
     * Fetch product details from product-service.
     * @param productId The ID of the product
     * @param useCache Whether to use cache or fetch fresh data
     * @return ProductDto or null if not found
     */
    public ProductDto getProductById(Long productId, boolean useCache) {
        try {
            // Check cache if enabled
            if (useCache && isCacheValid(productId)) {
                log.debug("Returning cached product for ID: {}", productId);
                return productCache.get(productId);
            }

            // Fetch from product-service
            log.debug("Fetching product from product-service for ID: {}", productId);
            ProductDto product = productClient.getProductById(productId);

            if (product != null) {
                // Update cache
                productCache.put(productId, product);
                cacheTimestamps.put(productId, System.currentTimeMillis());
                log.info("Product fetched successfully from product-service: {}", productId);
            } else {
                log.warn("Product not found in product-service: {}", productId);
            }

            return product;
        } catch (Exception e) {
            log.error("Error fetching product from product-service for ID: {}", productId, e);
            // Return cached data if available, even if expired
            return productCache.getOrDefault(productId, null);
        }
    }

    /**
     * Validate product existence and availability.
     * @param productId The ID of the product
     * @param requestedQuantity The quantity requested
     * @throws IllegalArgumentException if product not found or insufficient stock
     */
    public void validateProduct(Long productId, Integer requestedQuantity) {
        ProductDto product = getProductById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }

        if (product.getStock() != null && product.getStock() < requestedQuantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock for product '%s' (ID: %d). Available: %d, Requested: %d",
                    product.getName(), productId, product.getStock(), requestedQuantity)
            );
        }
    }

    /**
     * Clear cache for a specific product.
     * @param productId The ID of the product
     */
    public void clearCache(Long productId) {
        productCache.remove(productId);
        cacheTimestamps.remove(productId);
        log.debug("Cache cleared for product ID: {}", productId);
    }

    /**
     * Clear all product cache.
     */
    public void clearAllCache() {
        productCache.clear();
        cacheTimestamps.clear();
        log.debug("All product cache cleared");
    }

    /**
     * Check if cached product is still valid.
     * @param productId The ID of the product
     * @return true if cache is valid, false otherwise
     */
    private boolean isCacheValid(Long productId) {
        if (!productCache.containsKey(productId)) {
            return false;
        }

        Long timestamp = cacheTimestamps.get(productId);
        if (timestamp == null) {
            return false;
        }

        return (System.currentTimeMillis() - timestamp) < CACHE_TTL;
    }

    /**
     * Get cache statistics.
     * @return Map with cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", productCache.size());
        stats.put("cacheTTL", CACHE_TTL);
        return stats;
    }
}

