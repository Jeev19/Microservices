package com.example.order.dto;

import java.math.BigDecimal;

public class ChargeRequest {
    private String customerId;
    private BigDecimal amount;

    public ChargeRequest() {}

    public ChargeRequest(String customerId, BigDecimal amount) {
        this.customerId = customerId;
        this.amount = amount;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
