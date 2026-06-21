package com.example.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "An order and its current Saga status")
@Entity
@Table(name = "orders")
public class Order {

    @Schema(description = "Order ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "CUST-100")
    private String customerId;

    @Schema(example = "PROD-001")
    private String productId;

    @Schema(example = "2")
    private Integer quantity;

    @Schema(example = "49.98")
    private BigDecimal amount;

    @Schema(description = "Current step/outcome of the Saga", example = "CONFIRMED")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Schema(description = "Populated when status is FAILED or COMPENSATED",
            example = "Payment declined")
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
