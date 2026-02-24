package com.cts.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private String shippingAddress;
    private String paymentDetails;
}
