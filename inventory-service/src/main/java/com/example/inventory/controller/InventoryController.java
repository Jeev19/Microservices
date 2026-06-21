package com.example.inventory.controller;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.InventoryResponse;
import com.example.inventory.model.Stock;
import com.example.inventory.repository.StockRepository;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Stock reservation and release — the Saga participant invoked by Order Service")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockRepository stockRepository;

    public InventoryController(InventoryService inventoryService, StockRepository stockRepository) {
        this.inventoryService = inventoryService;
        this.stockRepository = stockRepository;
    }

    @Operation(
            summary = "Reserve stock",
            description = "Decrements available quantity for a product. Returns success=false if "
                    + "stock is insufficient or the product doesn't exist (causes the Saga to FAIL early)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation attempt result",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class)))
    })
    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponse> reserve(@RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.reserve(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Release stock (compensating transaction)",
            description = "Adds quantity back to available stock. Called by the Order Service's Saga "
                    + "orchestrator when a later step (Payment) fails, to undo the earlier reservation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Release result",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class)))
    })
    @PostMapping("/release")
    public ResponseEntity<InventoryResponse> release(@RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.release(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all stock", description = "Returns current available quantity for every product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of stock items")
    })
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStock() {
        return ResponseEntity.ok(stockRepository.findAll());
    }
}
