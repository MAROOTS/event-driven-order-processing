package com.orderprocessing.common.event;

import com.orderprocessing.common.enums.EventType;
import com.orderprocessing.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventId;
    private String orderId;
    private String customerId;
    private EventType eventType;
    private OrderStatus orderStatus;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
