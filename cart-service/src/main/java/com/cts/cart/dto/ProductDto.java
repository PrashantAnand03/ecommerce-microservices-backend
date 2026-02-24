
package com.cts.cart.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long productId,
        String name,
        BigDecimal price,
        Integer stock
) {}
