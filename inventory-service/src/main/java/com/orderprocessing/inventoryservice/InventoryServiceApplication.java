package com.orderprocessing.inventoryservice;

import com.orderprocessing.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class InventoryServiceApplication implements CommandLineRunner {
    private final InventoryService inventoryService;
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Override
    public void run(String... args){
        inventoryService.seedInventory();
    }
}