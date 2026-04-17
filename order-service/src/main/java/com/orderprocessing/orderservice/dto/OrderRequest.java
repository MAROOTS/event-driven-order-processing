package com.orderprocessing.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank(message = "Product name is required")
        private String productName;

        @NotNull
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull
        @Positive(message = "Price must be positive")
        private BigDecimal price;
    }
}