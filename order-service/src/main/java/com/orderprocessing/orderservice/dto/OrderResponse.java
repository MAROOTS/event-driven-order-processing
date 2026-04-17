package com.orderprocessing.orderservice.dto;

import com.orderprocessing.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID orderId;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}