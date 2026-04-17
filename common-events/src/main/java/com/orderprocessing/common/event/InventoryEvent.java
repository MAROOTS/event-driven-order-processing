package com.orderprocessing.common.event;

import com.orderprocessing.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private EventType eventType;
    private List<OrderEvent.OrderItemDto> items;
    private String failureReason;
    private LocalDateTime timestamp;
}