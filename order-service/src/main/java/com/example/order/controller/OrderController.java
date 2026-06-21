package com.example.order.controller;

import com.example.order.dto.OrderRequest;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderSagaOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Saga orchestration endpoints for placing and tracking orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderSagaOrchestrator orderSagaOrchestrator;
    private final OrderRepository orderRepository;

    public OrderController(OrderSagaOrchestrator orderSagaOrchestrator, OrderRepository orderRepository) {
        this.orderSagaOrchestrator = orderSagaOrchestrator;
        this.orderRepository = orderRepository;
    }

    @Operation(
            summary = "Place a new order",
            description = "Runs the full Saga: reserves inventory, then charges payment "
                    + "(through a Circuit Breaker). If any step fails, compensating actions "
                    + "are triggered automatically and the order ends up COMPENSATED or FAILED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed (check 'status' field for outcome)",
                    content = @Content(schema = @Schema(implementation = Order.class)))
    })
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        Order order = orderSagaOrchestrator.processOrder(request);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get an order by ID", description = "Fetch a single order and its current Saga status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List all orders", description = "Returns every order ever placed, regardless of Saga outcome.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders")
    })
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }
}
