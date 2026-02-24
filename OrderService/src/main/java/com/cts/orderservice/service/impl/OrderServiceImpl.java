package com.cts.orderservice.service.impl;

import com.cts.orderservice.dto.OrderItemResponseDto;
import com.cts.orderservice.dto.OrderRequestDto;
import com.cts.orderservice.dto.OrderResponseDto;
import com.cts.orderservice.dto.ProductDto;
import com.cts.orderservice.dto.UserDto;
import com.cts.orderservice.entity.OrderItem;
import com.cts.orderservice.entity.Orders;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;
import com.cts.orderservice.exception.OrderNotFoundException;
import com.cts.orderservice.repository.OrderItemRepository;
import com.cts.orderservice.repository.OrderRepository;
import com.cts.orderservice.service.OrderService;
import com.cts.orderservice.service.ProductServiceIntegration;
import com.cts.orderservice.service.UserServiceIntegration;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceIntegration productServiceIntegration;
    private final UserServiceIntegration userServiceIntegration;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        log.info("Creating order for user: {}", orderRequestDto.getUserid());

        // Validate user exists in User-Service
        userServiceIntegration.validateUser(orderRequestDto.getUserid());
        log.debug("User {} validated successfully", orderRequestDto.getUserid());

        // Validate products exist and calculate total price from product-service
        double totalPrice = 0.0;

        for (var itemDto : orderRequestDto.getItems()) {
            // Validate product exists and has sufficient stock
            productServiceIntegration.validateProduct(itemDto.getProductId(), itemDto.getQuantity());

            // Fetch product details for pricing
            ProductDto product = productServiceIntegration.getProductById(itemDto.getProductId());

            // Use the price from product-service for accurate pricing
            double itemTotal = product.getPrice().doubleValue() * itemDto.getQuantity();
            totalPrice += itemTotal;

            log.debug("Product {} validated: quantity={}, price={}, subtotal={}",
                    product.getName(), itemDto.getQuantity(), product.getPrice(), itemTotal);
        }

        // Create order entity with calculated total price
        Orders order = Orders.builder()
                .userId(orderRequestDto.getUserid())
                .shippingAddress(orderRequestDto.getShippingAddress())
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        // Create order items and establish bidirectional relationship
        for (var itemDto : orderRequestDto.getItems()) {
            ProductDto product = productServiceIntegration.getProductById(itemDto.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .price(product.getPrice().doubleValue())
                    .build();

            // Add order item using helper method (establishes bidirectional relationship)
            order.addOrderItem(orderItem);
        }

        // Save order with cascade - this will automatically save all order items
        Orders savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and {} items", savedOrder.getOrderId(), savedOrder.getItems().size());

        // Simulate payment processing
        processPayment(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Override
    public List<OrderResponseDto> getOrderByUserId(Long userId) {
        List<Orders> orders=orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Orders order=orderRepository.findById(orderId).orElseThrow(()->new OrderNotFoundException("Order Not found"));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {
        Orders order=orderRepository.findById(orderId).orElseThrow(()->new OrderNotFoundException("Order not found with this id "+orderId));
        order.setOrderStatus(status);
        Orders updatedOrder=orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Orders order=orderRepository.findById(orderId).orElseThrow(()->new OrderNotFoundException("Order not found with this id "+orderId));
        order.setPaymentStatus(paymentStatus);
        if (paymentStatus == PaymentStatus.PAID && order.getOrderStatus() == OrderStatus.CREATED) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }
        Orders updatedOrder=orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }


    private void processPayment(Orders order) {
        try {
            // Simulate payment gateway call
            // In real scenario: paymentServiceClient.processPayment(paymentRequest)
            boolean paymentSuccessful = true;

            if (paymentSuccessful) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setOrderStatus(OrderStatus.CONFIRMED);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }

            orderRepository.save(order);
        } catch (Exception e) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
        }
    }

    private OrderResponseDto mapToResponse(Orders order) {
        // Fetch order items using the bidirectional relationship
        // This will trigger a query if items are not loaded yet (lazy fetch)
        List<OrderItem> orderItems = order.getItems();

        // Fetch user details from User-Service to enrich response
        UserDto user = userServiceIntegration.getUserById(order.getUserId());

        List<OrderItemResponseDto> itemDtos = orderItems.stream()
                .map(item -> {
                    // Fetch product details from product-service to enrich response
                    ProductDto product = productServiceIntegration.getProductById(item.getProductId());

                    OrderItemResponseDto.OrderItemResponseDtoBuilder builder = OrderItemResponseDto.builder()
                            .orderItemId(item.getOrderItemId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice());

                    // Add product details if available
                    if (product != null) {
                        builder.productName(product.getName())
                                .productDescription(product.getDescription())
                                .productCategory(product.getCategory())
                                .productImageUrl(product.getImageUrl());

                        log.debug("Enriched order item {} with product details: {}",
                                item.getOrderItemId(), product.getName());
                    } else {
                        log.warn("Could not fetch product details for productId: {} in order item: {}",
                                item.getProductId(), item.getOrderItemId());
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());

        OrderResponseDto.OrderResponseDtoBuilder responseBuilder = OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .items(itemDtos);

        // Add user details if available
        if (user != null) {
            responseBuilder.userName(user.getName())
                    .userEmail(user.getEmail());

            log.debug("Enriched order {} with user details: {}", order.getOrderId(), user.getName());
        } else {
            log.warn("Could not fetch user details for userId: {} in order: {}",
                    order.getUserId(), order.getOrderId());
        }

        return responseBuilder.build();
    }
}

