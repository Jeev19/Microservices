package com.example.order.service;

import com.example.order.client.InventoryClient;
import com.example.order.client.PaymentClient;
import com.example.order.dto.OrderRequest;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderSagaOrchestrator(OrderRepository orderRepository,
                                  InventoryClient inventoryClient,
                                  PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public Order processOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.save(order);

        log.info("Saga started for order {}", order.getId());

        // Step 1: Reserve inventory
        boolean reserved = inventoryClient.reserveStock(order.getProductId(), order.getQuantity());
        if (!reserved) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Inventory reservation failed - insufficient stock");
            log.warn("Order {} failed at inventory reservation step", order.getId());
            return orderRepository.save(order);
        }

        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);
        log.info("Order {} - inventory reserved", order.getId());

        // Step 2: Charge payment (protected by Circuit Breaker + Retry)
        boolean charged;
        String failureReason = null;
        try {
            charged = paymentClient.charge(order.getCustomerId(), order.getAmount());
        } catch (Exception ex) {
            charged = false;
            failureReason = "Payment call failed: " + ex.getMessage();
        }

        if (!charged) {
            // Compensating transaction: release the inventory we reserved earlier
            log.warn("Order {} - payment failed, compensating by releasing inventory", order.getId());
            inventoryClient.releaseStock(order.getProductId(), order.getQuantity());
            order.setStatus(OrderStatus.COMPENSATED);
            order.setFailureReason(failureReason != null ? failureReason : "Payment declined");
            return orderRepository.save(order);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        log.info("Order {} - confirmed successfully", order.getId());
        return orderRepository.save(order);
    }
}
