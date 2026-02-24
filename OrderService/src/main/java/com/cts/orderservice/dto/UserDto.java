package com.cts.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User information from User-Service.
 *
 * <p>This DTO is used to receive user details from the User-Service
 * via Feign client calls. It contains essential user information needed
 * for order processing and enrichment.
 *
 * @author Order Service Team
 * @version 1.0
 * @since 2026-01-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    /**
     * Unique identifier for the user.
     */
    private Long userId;

    /**
     * Full name of the user.
     */
    private String name;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * Default shipping address for the user.
     * Used as fallback if not provided in order.
     */
    private String shippingAddress;

    /**
     * Payment details or preferred payment method.
     * May contain masked payment information.
     */
    private String paymentDetails;
}

