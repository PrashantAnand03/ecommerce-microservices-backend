package com.cts.orderservice.client.fallback;

import com.cts.orderservice.client.ProductClient;
import com.cts.orderservice.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public ProductDto getProductById(Long id) {
        log.warn("Fallback: Product Service unavailable for product ID: {}", id);
        return null;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        log.warn("Fallback: Product Service unavailable for getting all products");
        return Collections.emptyList();
    }
}
