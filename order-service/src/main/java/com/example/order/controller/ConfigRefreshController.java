package com.example.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @RefreshScope: when you POST to /actuator/refresh, Spring re-injects all
 * @Value fields in this bean from the Config Server without restarting the service.
 * Great for toggling log levels, timeouts, feature flags at runtime.
 */
@RefreshScope
@Tag(name = "Config", description = "Shows live config values from the Config Server")
@RestController
@RequestMapping("/api/config")
public class ConfigRefreshController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${services.inventory-url}")
    private String inventoryUrl;

    @Value("${services.payment-url}")
    private String paymentUrl;

    @Value("${resilience4j.circuitbreaker.instances.paymentService.failure-rate-threshold}")
    private int cbFailureRateThreshold;

    @Value("${resilience4j.circuitbreaker.instances.paymentService.sliding-window-size}")
    private int cbSlidingWindowSize;

    @Operation(
            summary = "Show active config values",
            description = "Returns key properties currently loaded from Config Server. "
                    + "POST to /actuator/refresh to hot-reload without restart."
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> showConfig() {
        return ResponseEntity.ok(Map.of(
                "service", appName,
                "inventoryUrl", inventoryUrl,
                "paymentUrl", paymentUrl,
                "circuitBreaker.failureRateThreshold", cbFailureRateThreshold,
                "circuitBreaker.slidingWindowSize", cbSlidingWindowSize
        ));
    }
}
