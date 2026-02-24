
package com.cts.cart.dto.responsedto;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String name,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {}
