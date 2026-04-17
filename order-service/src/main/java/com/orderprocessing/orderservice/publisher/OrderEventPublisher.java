package com.orderprocessing.orderservice.publisher;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.event.OrderEvent;
import com.orderprocessing.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    public void publishOrderPlaced(Order order){
        OrderEvent event = buildEvent(order, EventType.ORDER_PLACED);
        kafkaTemplate.send(ORDER_EVENTS_TOPIC,order.getId().toString(),event);
        log.info("Published ORDER_PLACED event for orderId: {}", order.getId());
    }
    public void publishOrderCancelled(Order order){
        OrderEvent event = buildEvent(order, EventType.ORDER_CANCELLED);
        kafkaTemplate.send(ORDER_EVENTS_TOPIC,order.getId().toString(),event);
        log.info("Published ORDER_CANCELLED event for orderId: {}", order.getId());
    }

    private OrderEvent buildEvent(Order order, EventType eventType) {
        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId().toString())
                .customerId(order.getCustomerId())
                .eventType(eventType)
                .orderStatus(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .items(order.getItems().stream()
                        .map(item->OrderEvent.OrderItemDto.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
