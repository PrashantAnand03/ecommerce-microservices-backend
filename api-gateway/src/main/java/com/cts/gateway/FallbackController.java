package com.cts.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
class FallbackController {

    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> cartFallback() {
        return createFallbackResponse("Cart Service", "CART_SERVICE_UNAVAILABLE");
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        return createFallbackResponse("Product Service", "PRODUCT_SERVICE_UNAVAILABLE");
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        return createFallbackResponse("User Service", "USER_SERVICE_UNAVAILABLE");
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        return createFallbackResponse("Order Service", "ORDER_SERVICE_UNAVAILABLE");
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminFallback() {
        return createFallbackResponse("Admin Service", "ADMIN_SERVICE_UNAVAILABLE");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("errorCode", errorCode);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}


