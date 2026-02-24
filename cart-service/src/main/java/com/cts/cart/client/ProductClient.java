package com.cts.cart.client;

import com.cts.cart.client.fallback.ProductClientFallback;
import com.cts.cart.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping("/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);
}
