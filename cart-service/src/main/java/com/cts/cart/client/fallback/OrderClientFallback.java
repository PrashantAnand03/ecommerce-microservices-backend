package com.cts.cart.client.fallback;

import com.cts.cart.client.OrderClient;
import com.cts.cart.dto.requestdto.OrderRequestDto;
import com.cts.cart.dto.responsedto.OrderResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderClientFallback implements OrderClient {

    private static final Logger log = LoggerFactory.getLogger(OrderClientFallback.class);

    @Override
    public ResponseEntity<OrderResponseDto> createOrder(OrderRequestDto orderRequest) {
        log.warn("Fallback: Order Service unavailable for order creation");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}

