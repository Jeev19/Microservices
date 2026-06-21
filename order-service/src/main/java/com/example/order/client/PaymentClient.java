package com.example.order.client;

import com.example.order.dto.ChargeRequest;
import com.example.order.dto.ChargeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    public PaymentClient(RestTemplate restTemplate,
                          @Value("${services.payment-url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "chargeFallback")
    @Retry(name = "paymentService")
    public boolean charge(String customerId, BigDecimal amount) {
        ChargeRequest request = new ChargeRequest(customerId, amount);
        ChargeResponse response = restTemplate.postForObject(
                paymentServiceUrl + "/api/payments/charge", request, ChargeResponse.class);
        return response != null && response.isSuccess();
    }

    // Fallback signature MUST match the original method's args + a Throwable
    private boolean chargeFallback(String customerId, BigDecimal amount, Throwable t) {
        log.warn("Circuit breaker fallback triggered for customer {} amount {}: {}",
                customerId, amount, t.getMessage());
        return false;
    }
}
