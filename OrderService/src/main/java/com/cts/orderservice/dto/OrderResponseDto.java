package com.cts.orderservice.dto;

import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for Order information with enriched user and product details.
 *
 * <p>This DTO contains complete order information including:
 * <ul>
 *   <li>Order details (ID, status, price)</li>
 *   <li>User details (from User-Service)</li>
 *   <li>Order items with product details (from Product-Service)</li>
 * </ul>
 *
 * @author Order Service Team
 * @version 1.0
 * @since 2026-01-08
 */
@Data
@Builder
public class OrderResponseDto {

    /**
     * Unique identifier for the order.
     */
    private Long orderId;

    /**
     * User ID who placed the order.
     */
    private Long userId;

    /**
     * Total price of the order.
     */
    private Double totalPrice;

    /**
     * Current status of the order.
     */
    private OrderStatus orderStatus;

    /**
     * Current payment status.
     */
    private PaymentStatus paymentStatus;

    /**
     * List of order items with enriched product details.
     */
    private List<OrderItemResponseDto> items;

    // User details from User-Service
    /**
     * Name of the user (fetched from User-Service).
     */
    private String userName;

    /**
     * Email of the user (fetched from User-Service).
     */
    private String userEmail;

    /**
     * Shipping address for the order.
     */
    private String shippingAddress;
}
