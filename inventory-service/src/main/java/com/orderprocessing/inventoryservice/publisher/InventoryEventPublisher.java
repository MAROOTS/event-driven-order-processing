package com.orderprocessing.inventoryservice.publisher;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    public void publishInventoryReserved(String orderId, String customerId,
                                         List<OrderEvent.OrderItemDto> items) {
        InventoryEvent event = InventoryEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .eventType(EventType.INVENTORY_RESERVED)
                .items(items)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, orderId, event);
        log.info("Published INVENTORY_RESERVED event for orderId: {}", orderId);
    }

    public void publishInventoryFailed(String orderId, String customerId,
                                       String reason) {
        InventoryEvent event = InventoryEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .eventType(EventType.INVENTORY_FAILED)
                .failureReason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, orderId, event);
        log.warn("Published INVENTORY_FAILED event for orderId: {}", orderId);
    }

    public void publishInventoryReleased(String orderId, String customerId,
                                         List<OrderEvent.OrderItemDto> items) {
        InventoryEvent event = InventoryEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .eventType(EventType.INVENTORY_RELEASED)
                .items(items)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, orderId, event);
        log.info("Published INVENTORY_RELEASED event for orderId: {}", orderId);
    }
}