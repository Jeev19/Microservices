package com.example.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request to charge a customer")
public class ChargeRequest {

    @Schema(description = "Customer ID being charged", example = "CUST-100")
    private String customerId;

    @Schema(description = "Amount to charge", example = "49.98")
    private BigDecimal amount;

    public ChargeRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
