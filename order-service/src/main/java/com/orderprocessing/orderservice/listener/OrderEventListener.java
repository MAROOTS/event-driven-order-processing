package com.orderprocessing.orderservice.listener;

import com.orderprocessing.common.enums.OrderStatus;
import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.common.event.PaymentEvent;
import com.orderprocessing.orderservice.publisher.OrderEventPublisher;
import com.orderprocessing.orderservice.repository.OrderRepository;
import com.orderprocessing.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @KafkaListener(topics = "inventory-events",groupId = "order-group")
    public void handleInventoryEvent(InventoryEvent event){
        log.info("Received inventory event: {} for order: {}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()){
            case INVENTORY_FAILED -> {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
                log.warn("Order {} failed due to inventory issue: {}", event.getOrderId(), event.getFailureReason());
            }
            default -> log.info("Inventory event {} handled downstream", event.getEventType());
        }
    }
    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {} for order: {}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case PAYMENT_SUCCESS -> {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
                log.info("Order {} confirmed after successful payment", event.getOrderId());
            }
            case PAYMENT_FAILED -> {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
                // Trigger compensating transaction — release inventory
                orderRepository.findById(UUID.fromString(event.getOrderId()))
                        .ifPresent(orderEventPublisher::publishOrderCancelled);
                log.warn("Order {} failed due to payment failure", event.getOrderId());
            }
            default -> log.info("Payment event {} noted", event.getEventType());
        }
    }
}
