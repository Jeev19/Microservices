package com.example.order.client;

import com.example.order.dto.InventoryRequest;
import com.example.order.dto.InventoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryClient(RestTemplate restTemplate,
                            @Value("${services.inventory-url}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public boolean reserveStock(String productId, Integer quantity) {
        try {
            InventoryRequest request = new InventoryRequest(productId, quantity);
            InventoryResponse response = restTemplate.postForObject(
                    inventoryServiceUrl + "/api/inventory/reserve", request, InventoryResponse.class);
            return response != null && response.isSuccess();
        } catch (RestClientException ex) {
            log.error("Failed to reserve stock for product {}: {}", productId, ex.getMessage());
            return false;
        }
    }

    public void releaseStock(String productId, Integer quantity) {
        try {
            InventoryRequest request = new InventoryRequest(productId, quantity);
            restTemplate.postForObject(
                    inventoryServiceUrl + "/api/inventory/release", request, InventoryResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to release stock for product {} during compensation: {}", productId, ex.getMessage());
        }
    }
}
