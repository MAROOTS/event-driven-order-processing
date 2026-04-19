package com.orderprocessing.notificationservice.listener;

import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.common.event.OrderEvent;
import com.orderprocessing.common.event.PaymentEvent;
import com.orderprocessing.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Notification service received order event: {}", event.getEventType());

        switch (event.getEventType()) {
            case ORDER_PLACED -> notificationService.notifyOrderPlaced(event);
            case ORDER_CANCELLED -> log.info(
                    "Order {} cancelled — inventory being released",
                    event.getOrderId());
            default -> log.info("Order event noted: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Notification service received inventory event: {}", event.getEventType());

        switch (event.getEventType()) {
            case INVENTORY_RESERVED -> notificationService.notifyInventoryReserved(event);
            case INVENTORY_FAILED -> notificationService.notifyInventoryFailed(event);
            case INVENTORY_RELEASED -> log.info(
                    "Inventory released for order: {}",
                    event.getOrderId());
            default -> log.info("Inventory event noted: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Notification service received payment event: {}", event.getEventType());

        switch (event.getEventType()) {
            case PAYMENT_SUCCESS -> notificationService.notifyOrderConfirmed(event);
            case PAYMENT_FAILED -> notificationService.notifyOrderFailed(event);
            default -> log.info("Payment event noted: {}", event.getEventType());
        }
    }
}