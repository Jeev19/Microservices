package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a reserve or release operation")
public class InventoryResponse {

    @Schema(example = "true")
    private boolean success;

    @Schema(example = "Reserved successfully")
    private String message;

    @Schema(description = "Remaining available quantity after the operation", example = "48")
    private Integer remainingQuantity;

    public InventoryResponse() {}

    public InventoryResponse(boolean success, String message, Integer remainingQuantity) {
        this.success = success;
        this.message = message;
        this.remainingQuantity = remainingQuantity;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }
}
