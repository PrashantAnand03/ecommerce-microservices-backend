package com.cts.cart.client.fallback;

import com.cts.cart.client.ProductClient;
import com.cts.cart.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public ProductDto getProduct(Long id) {
        log.warn("Fallback: Product Service unavailable for product ID: {}", id);
        return null;
    }
}
