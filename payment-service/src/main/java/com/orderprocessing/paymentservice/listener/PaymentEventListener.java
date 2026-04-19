package com.orderprocessing.paymentservice.listener;

import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final PaymentService paymentService;


    @KafkaListener(topics = "inventory-events", groupId = "payment-group")
    public void handleInventoryEvent(InventoryEvent event){
        log.info("Received inventory event: {} for orderId: {}",
                event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case INVENTORY_RESERVED -> paymentService.processPayment(event);
            default -> log.info("Ignoring inventory event: {}", event.getEventType());
        }

    }

}
