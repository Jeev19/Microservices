package com.example.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Payload to place a new order")
public class OrderRequest {

    @Schema(description = "ID of the customer placing the order", example = "CUST-100")
    private String customerId;

    @Schema(description = "Product to order (must exist in Inventory Service)", example = "PROD-001")
    private String productId;

    @Schema(description = "Quantity to order", example = "2")
    private Integer quantity;

    @Schema(description = "Total amount to charge", example = "49.98")
    private BigDecimal amount;

    public OrderRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
