package com.cts.cart.dto.responsedto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        Long cartId,
        Long userId,
        List<CartItemDto> items,
        Long totalQuantity,  // total quantity across all items
        BigDecimal total       // total price
) {}
