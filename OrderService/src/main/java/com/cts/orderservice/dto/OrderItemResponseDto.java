package com.cts.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {
    private Long orderItemId;
    private Long productId;
    private Integer quantity;
    private Double price;

    // Product details from product-service
    private String productName;
    private String productDescription;
    private String productCategory;
    private String productImageUrl;
}
