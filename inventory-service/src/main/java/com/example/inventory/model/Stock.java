package com.example.inventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Schema(description = "Current stock level for a product")
@Entity
@Table(name = "stock")
public class Stock {

    @Schema(description = "Product ID", example = "PROD-001")
    @Id
    private String productId;

    @Schema(example = "Wireless Mouse")
    private String productName;

    @Schema(example = "50")
    private Integer availableQuantity;

    public Stock() {}

    public Stock(String productId, String productName, Integer availableQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.availableQuantity = availableQuantity;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
}
