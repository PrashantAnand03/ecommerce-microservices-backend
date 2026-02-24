package com.cts.cart.client;

import com.cts.cart.client.fallback.OrderClientFallback;
import com.cts.cart.dto.requestdto.OrderRequestDto;
import com.cts.cart.dto.responsedto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ORDER-SERVICE", path = "/orders", fallback = OrderClientFallback.class)
public interface OrderClient {

    @PostMapping
    ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequest);
}

