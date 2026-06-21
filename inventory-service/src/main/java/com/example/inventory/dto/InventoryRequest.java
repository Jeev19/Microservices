package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to reserve or release stock for a product")
public class InventoryRequest {

    @Schema(description = "Product ID", example = "PROD-001")
    private String productId;

    @Schema(description = "Quantity to reserve or release", example = "2")
    private Integer quantity;

    public InventoryRequest() {}

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
