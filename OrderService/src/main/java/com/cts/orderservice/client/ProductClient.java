package com.cts.orderservice.client;

import com.cts.orderservice.client.fallback.ProductClientFallback;
import com.cts.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);

    @GetMapping
    List<ProductDto> getAllProducts();
}
