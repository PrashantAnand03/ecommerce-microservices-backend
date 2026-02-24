package com.cts.userservice.exception;

public class CartEmptyException extends RuntimeException {

    public CartEmptyException() {
        super("Cart is empty. Add products before placing order.");
    }

    public CartEmptyException(String message) {
        super(message);
    }
}

