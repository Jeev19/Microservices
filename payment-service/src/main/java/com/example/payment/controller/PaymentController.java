package com.example.payment.controller;

import com.example.payment.dto.ChargeRequest;
import com.example.payment.dto.ChargeResponse;
import com.example.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments", description = "Simulated payment gateway, called by Order Service through a Circuit Breaker")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Charge a customer",
            description = "Simulates charging a payment method. Has a 10% random failure rate, "
                    + "plus a forced-failure mode toggleable via /simulate-failure for demos. "
                    + "Failures return HTTP 503 so the caller's Circuit Breaker counts them correctly."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charge succeeded",
                    content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
            @ApiResponse(responseCode = "503", description = "Payment gateway failure (simulated)", content = @Content)
    })
    @PostMapping("/charge")
    public ResponseEntity<ChargeResponse> charge(@RequestBody ChargeRequest request) {
        ChargeResponse response = paymentService.processCharge(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Toggle forced failure mode",
            description = "When enabled, every charge attempt fails with HTTP 503. "
                    + "Use this to demo the Order Service's Circuit Breaker tripping open."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failure mode updated")
    })
    @PutMapping("/simulate-failure")
    public ResponseEntity<String> simulateFailure(
            @Parameter(description = "true to force all charges to fail", example = "true")
            @RequestParam boolean enabled) {
        paymentService.setForceFailure(enabled);
        return ResponseEntity.ok("Force failure mode set to " + enabled);
    }

    @Operation(summary = "Get current forced-failure mode state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current state of the failure toggle")
    })
    @GetMapping("/simulate-failure")
    public ResponseEntity<Boolean> getFailureMode() {
        return ResponseEntity.ok(paymentService.isForceFailure());
    }
}
