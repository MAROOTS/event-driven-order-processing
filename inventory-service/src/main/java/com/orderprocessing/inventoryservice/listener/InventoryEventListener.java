package com.orderprocessing.inventoryservice.listener;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.event.OrderEvent;
import com.orderprocessing.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for orderId: {}",
                event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case ORDER_PLACED -> inventoryService.reserveStock(event);
            case ORDER_CANCELLED -> inventoryService.releaseStock(event);
            default -> log.info("Ignoring event type: {}", event.getEventType());
        }
    }
}