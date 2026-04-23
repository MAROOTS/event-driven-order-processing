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
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public void processPayment(InventoryEvent event) {

        // Idempotency check
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already processed for orderId: {}", event.getOrderId());
            return;
        }

        BigDecimal amount = calculateTotal(event);
        log.info("Processing payment of ${} for orderId: {}", amount, event.getOrderId());

        // Simulate gateway processing delay (100-300ms)
        simulateProcessingDelay();

        // Run through payment gateway simulator
        PaymentGatewayResponse gatewayResponse = simulatePaymentGateway(
                event.getOrderId(),
                event.getCustomerId(),
                amount
        );

        log.info("Gateway response for order {}: {} - {}",
                event.getOrderId(),
                gatewayResponse.isSuccess() ? "SUCCESS" : "FAILED",
                gatewayResponse.getMessage());


        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(amount)
                .status(gatewayResponse.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(gatewayResponse.getTransactionId())
                .failureReason(gatewayResponse.isSuccess() ? null : gatewayResponse.getMessage())
                .build();

        paymentRepository.save(payment);


        if (gatewayResponse.isSuccess()) {
            paymentEventPublisher.publishPaymentSuccess(payment);
        } else {
            paymentEventPublisher.publishPaymentFailed(payment);
        }
    }


    private PaymentGatewayResponse simulatePaymentGateway(
            String orderId, String customerId, BigDecimal amount) {

        return switch (determineScenario(customerId, amount)) {

            case SUCCESS -> PaymentGatewayResponse.success(
                    generateTransactionId(),
                    "Payment approved"
            );

            case INSUFFICIENT_FUNDS -> PaymentGatewayResponse.failure(
                    "INSUFFICIENT_FUNDS",
                    "Card declined: Insufficient funds"
            );

            case CARD_EXPIRED -> PaymentGatewayResponse.failure(
                    "CARD_EXPIRED",
                    "Card declined: Card has expired"
            );

            case FRAUD_DETECTED -> PaymentGatewayResponse.failure(
                    "FRAUD_DETECTED",
                    "Transaction blocked: Suspicious activity detected"
            );

            case GATEWAY_TIMEOUT -> PaymentGatewayResponse.failure(
                    "GATEWAY_TIMEOUT",
                    "Payment gateway timeout — please retry"
            );

            case BANK_DECLINED -> PaymentGatewayResponse.failure(
                    "BANK_DECLINED",
                    "Transaction declined by issuing bank"
            );
        };
    }

    private PaymentScenario determineScenario(String customerId, BigDecimal amount) {
        if (customerId != null) {
            return switch (customerId) {
                case "CUST-FAIL" -> PaymentScenario.INSUFFICIENT_FUNDS;
                case "CUST-EXPIRED" -> PaymentScenario.CARD_EXPIRED;
                case "CUST-FRAUD" -> PaymentScenario.FRAUD_DETECTED;
                case "CUST-TIMEOUT" -> PaymentScenario.GATEWAY_TIMEOUT;
                case "CUST-SUCCESS" -> PaymentScenario.SUCCESS;
                default -> randomScenario(amount);
            };
        }
        return randomScenario(amount);
    }

    private PaymentScenario randomScenario(BigDecimal amount) {
        double successRate = amount.compareTo(BigDecimal.valueOf(5000)) > 0 ? 0.6 : 0.85;

        double random = Math.random();

        if (random < successRate) return PaymentScenario.SUCCESS;
        if (random < successRate + 0.07) return PaymentScenario.INSUFFICIENT_FUNDS;
        if (random < successRate + 0.10) return PaymentScenario.CARD_EXPIRED;
        if (random < successRate + 0.12) return PaymentScenario.FRAUD_DETECTED;
        if (random < successRate + 0.13) return PaymentScenario.GATEWAY_TIMEOUT;
        return PaymentScenario.BANK_DECLINED;
    }

    private void simulateProcessingDelay() {
        try {
            long delay = 100 + (long)(Math.random() * 200);
            Thread.sleep(delay);
            log.debug("Gateway processing time: {}ms", delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
    }

    private BigDecimal calculateTotal(InventoryEvent event) {
        if (event.getItems() == null) return BigDecimal.ZERO;
        return event.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private enum PaymentScenario {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        CARD_EXPIRED,
        FRAUD_DETECTED,
        GATEWAY_TIMEOUT,
        BANK_DECLINED
    }

    private static class PaymentGatewayResponse {
        private final boolean success;
        private final String transactionId;
        private final String message;

        private PaymentGatewayResponse(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public static PaymentGatewayResponse success(String transactionId, String message) {
            return new PaymentGatewayResponse(true, transactionId, message);
        }

        public static PaymentGatewayResponse failure(String transactionId, String message) {
            return new PaymentGatewayResponse(false, transactionId, message);
        }

        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getMessage() { return message; }
    }
}