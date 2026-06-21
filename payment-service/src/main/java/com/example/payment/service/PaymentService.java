package com.example.payment.service;

import com.example.payment.dto.ChargeRequest;
import com.example.payment.dto.ChargeResponse;
import com.example.payment.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // Toggle this via PUT /api/payments/simulate-failure to demo the Circuit Breaker tripping
    private final AtomicBoolean forceFailure = new AtomicBoolean(false);

    public ChargeResponse processCharge(ChargeRequest request) {
        if (forceFailure.get()) {
            log.warn("Simulated payment gateway outage - force failure mode is ON");
            throw new PaymentProcessingException("Simulated payment gateway outage");
        }

        // Simulate processing latency
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        // Simulate occasional random failure (10%) to mimic a flaky downstream payment gateway
        if (ThreadLocalRandom.current().nextInt(100) < 10) {
            log.warn("Random simulated payment gateway error for customer {}", request.getCustomerId());
            throw new PaymentProcessingException("Random payment gateway error");
        }

        log.info("Charged {} to customer {}", request.getAmount(), request.getCustomerId());
        return new ChargeResponse(true, "Charged " + request.getAmount() + " to customer " + request.getCustomerId());
    }

    public void setForceFailure(boolean value) {
        forceFailure.set(value);
        log.info("Force failure mode set to {}", value);
    }

    public boolean isForceFailure() {
        return forceFailure.get();
    }
}
