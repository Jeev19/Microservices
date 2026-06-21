package com.example.inventory.config;

import com.example.inventory.model.Stock;
import com.example.inventory.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final StockRepository stockRepository;

    public DataLoader(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(String... args) {
        stockRepository.save(new Stock("PROD-001", "Wireless Mouse", 50));
        stockRepository.save(new Stock("PROD-002", "Mechanical Keyboard", 30));
        stockRepository.save(new Stock("PROD-003", "USB-C Hub", 5));
        stockRepository.save(new Stock("PROD-004", "27-inch Monitor", 10));
    }
}
