package com.example.inventory.service;

import com.example.inventory.dto.InventoryResponse;
import com.example.inventory.model.Stock;
import com.example.inventory.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final StockRepository stockRepository;

    public InventoryService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public InventoryResponse reserve(String productId, Integer quantity) {
        Optional<Stock> stockOpt = stockRepository.findById(productId);
        if (stockOpt.isEmpty()) {
            return new InventoryResponse(false, "Product not found: " + productId, 0);
        }

        Stock stock = stockOpt.get();
        if (stock.getAvailableQuantity() < quantity) {
            log.warn("Insufficient stock for {}: requested {}, available {}",
                    productId, quantity, stock.getAvailableQuantity());
            return new InventoryResponse(false, "Insufficient stock", stock.getAvailableQuantity());
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stockRepository.save(stock);
        log.info("Reserved {} units of {}. Remaining: {}", quantity, productId, stock.getAvailableQuantity());
        return new InventoryResponse(true, "Reserved successfully", stock.getAvailableQuantity());
    }

    @Transactional
    public InventoryResponse release(String productId, Integer quantity) {
        Optional<Stock> stockOpt = stockRepository.findById(productId);
        if (stockOpt.isEmpty()) {
            return new InventoryResponse(false, "Product not found: " + productId, 0);
        }

        Stock stock = stockOpt.get();
        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        stockRepository.save(stock);
        log.info("Released {} units of {} back to stock (compensation). New total: {}",
                quantity, productId, stock.getAvailableQuantity());
        return new InventoryResponse(true, "Released successfully", stock.getAvailableQuantity());
    }
}
