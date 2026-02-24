package com.cts.orderservice.repository;

import com.cts.orderservice.entity.OrderItem;
import com.cts.orderservice.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Find order items by the Order entity relationship
    List<OrderItem> findByOrder(Orders order);

    // Find order items by order ID using the nested property
    List<OrderItem> findByOrder_OrderId(Long orderId);
}
