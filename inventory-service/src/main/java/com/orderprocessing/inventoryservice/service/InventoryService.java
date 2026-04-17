package com.orderprocessing.inventoryservice.service;

import com.orderprocessing.common.event.OrderEvent;
import com.orderprocessing.inventoryservice.entity.InventoryItem;
import com.orderprocessing.inventoryservice.publisher.InventoryEventPublisher;
import com.orderprocessing.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    @Transactional
    public void reserveStock(OrderEvent event) {
        List<OrderEvent.OrderItemDto> items = event.getItems();

        // First check ALL items have enough stock before reserving anything
        for (OrderEvent.OrderItemDto item : items) {
            Optional<InventoryItem> inventoryItem =
                    inventoryRepository.findByProductId(item.getProductId());

            if (inventoryItem.isEmpty()) {
                log.warn("Product not found in inventory: {}", item.getProductId());
                inventoryEventPublisher.publishInventoryFailed(
                        event.getOrderId(),
                        event.getCustomerId(),
                        "Product not found: " + item.getProductId()
                );
                return;
            }

            if (!inventoryItem.get().hasEnoughStock(item.getQuantity())) {
                log.warn("Insufficient stock for product: {}", item.getProductId());
                inventoryEventPublisher.publishInventoryFailed(
                        event.getOrderId(),
                        event.getCustomerId(),
                        "Insufficient stock for: " + item.getProductName()
                );
                return;
            }
        }

        // All items available — now reserve them all
        for (OrderEvent.OrderItemDto item : items) {
            InventoryItem inventoryItem = inventoryRepository
                    .findByProductId(item.getProductId()).get();
            inventoryItem.reserve(item.getQuantity());
            inventoryRepository.save(inventoryItem);
            log.info("Reserved {} units of product {}", item.getQuantity(), item.getProductId());
        }

        // Publish success
        inventoryEventPublisher.publishInventoryReserved(
                event.getOrderId(),
                event.getCustomerId(),
                items
        );
    }

    @Transactional
    public void releaseStock(OrderEvent event) {
        // Called when payment fails — undo the reservation (Saga compensation)
        if (event.getItems() == null) return;

        for (OrderEvent.OrderItemDto item : event.getItems()) {
            inventoryRepository.findByProductId(item.getProductId())
                    .ifPresent(inventoryItem -> {
                        inventoryItem.release(item.getQuantity());
                        inventoryRepository.save(inventoryItem);
                        log.info("Released {} units of product {}",
                                item.getQuantity(), item.getProductId());
                    });
        }

        inventoryEventPublisher.publishInventoryReleased(
                event.getOrderId(),
                event.getCustomerId(),
                event.getItems()
        );
    }

    // Seed some initial inventory data for testing
    @Transactional
    public void seedInventory() {
        if (inventoryRepository.count() == 0) {
            inventoryRepository.save(InventoryItem.builder()
                    .productId("PROD-001")
                    .productName("Laptop")
                    .availableQuantity(10)
                    .reservedQuantity(0)
                    .build());

            inventoryRepository.save(InventoryItem.builder()
                    .productId("PROD-002")
                    .productName("Phone")
                    .availableQuantity(20)
                    .reservedQuantity(0)
                    .build());

            inventoryRepository.save(InventoryItem.builder()
                    .productId("PROD-003")
                    .productName("Headphones")
                    .availableQuantity(50)
                    .reservedQuantity(0)
                    .build());

            log.info("Inventory seeded with 3 products");
        }
    }
}