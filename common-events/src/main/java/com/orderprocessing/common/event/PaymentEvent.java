package com.orderprocessing.common.event;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private EventType eventType;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
}