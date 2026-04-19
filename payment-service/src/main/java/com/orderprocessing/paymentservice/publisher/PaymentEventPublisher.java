package com.orderprocessing.paymentservice.publisher;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.enums.PaymentStatus;
import com.orderprocessing.common.event.PaymentEvent;
import com.orderprocessing.paymentservice.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    public void publishPaymentSuccess(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .eventType(EventType.PAYMENT_SUCCESS)
                .paymentStatus(PaymentStatus.SUCCESS)
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, payment.getOrderId(), event);
        log.info("Published PAYMENT_SUCCESS for orderId: {}", payment.getOrderId());
    }

    public void publishPaymentFailed(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .eventType(EventType.PAYMENT_FAILED)
                .paymentStatus(PaymentStatus.FAILED)
                .amount(payment.getAmount())
                .failureReason(payment.getFailureReason())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, payment.getOrderId(), event);
        log.warn("Published PAYMENT_FAILED for orderId: {}", payment.getOrderId());
    }
}