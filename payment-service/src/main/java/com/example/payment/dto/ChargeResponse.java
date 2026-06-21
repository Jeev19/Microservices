package com.example.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a charge attempt")
public class ChargeResponse {

    @Schema(example = "true")
    private boolean success;

    @Schema(example = "Charged 49.98 to customer CUST-100")
    private String message;

    public ChargeResponse() {}

    public ChargeResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
