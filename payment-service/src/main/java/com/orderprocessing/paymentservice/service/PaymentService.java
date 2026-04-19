package com.orderprocessing.paymentservice.service;

import com.orderprocessing.common.enums.PaymentStatus;
import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.paymentservice.entity.Payment;
import com.orderprocessing.paymentservice.publisher.PaymentEventPublisher;
import com.orderprocessing.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    @Transactional
    public void processPayment(InventoryEvent event){
        if (paymentRepository.existsByOrderId(event.getOrderId())){
            log.warn("Payment already processed for orderId: {}", event.getOrderId());
        return;
        }

        boolean paymentSuccessful = simulatePaymentGateway(event.getOrderId());

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(calculateTotal(event))
                .status(paymentSuccessful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(paymentSuccessful ? UUID.randomUUID().toString() : null)
                .failureReason(paymentSuccessful ? null : "Payment declined by gateway")
                .build();
        paymentRepository.save(payment);

        if (paymentSuccessful){
            paymentEventPublisher.publishPaymentSuccess(payment);
        }else {
            paymentEventPublisher.publishPaymentFailed(payment);
        }
    }
    private boolean simulatePaymentGateway(String orderId){
        double random = Math.random();
        boolean success = random > 0.2;
        log.info("Payment simulation for order {}: {} (random={})",
                orderId, success ? "SUCCESS" : "FAILED", String.format("%.2f", random));
        return success;
    }
    private java.math.BigDecimal calculateTotal(InventoryEvent event){
        if (event.getItems()==null) return BigDecimal.ZERO;
        return event.getItems().stream()
                .map(i->i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
