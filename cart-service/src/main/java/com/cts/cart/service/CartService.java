
package com.cts.cart.service;

import com.cts.cart.dto.responsedto.CartDto;

public interface CartService {

    /** 3) Get all items of a user's cart (with computed totals) */
    CartDto getCart(Long userId);

    /** 1) Add product to a specific user's cart (qty default = 1 if null) */
    CartDto addProduct(Long userId, Long productId, Integer quantity);

    /** 2) Reduce quantity by one for a specific product */
    CartDto decrementProduct(Long userId, Long productId);

    /** 4) Delete a specific product from the cart (qty becomes 0 — remove item entirely) */
    void deleteProduct(Long userId, Long productId);
}
