package com.cts.cart.dto.requestdto;

import com.cts.cart.dto.responsedto.OrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private Long userid;
    private String shippingAddress;
    private List<OrderItemDto> items;
}

