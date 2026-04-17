package com.orderprocessing.orderservice.service;

import com.orderprocessing.common.enums.OrderStatus;
import com.orderprocessing.orderservice.dto.OrderRequest;
import com.orderprocessing.orderservice.dto.OrderResponse;
import com.orderprocessing.orderservice.entity.Order;
import com.orderprocessing.orderservice.entity.OrderItem;
import com.orderprocessing.orderservice.publisher.OrderEventPublisher;
import com.orderprocessing.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderEventPublisher orderEventPublisher;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request){
        List<OrderItem> items = request.getItems().stream()
                .map(i->OrderItem.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build())
                .collect(Collectors.toList());
        BigDecimal total = items.stream()
                .map(i->i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .items(items)

                .build();
        items.forEach(item->item.setOrder(order));
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        orderEventPublisher.publishOrderPlaced(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status){
        orderRepository.findById(UUID.fromString(orderId)).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            log.info("Order {} status updated to {}", orderId, status);
        });
    }
    public OrderResponse getOrder(String orderId){
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(()->new RuntimeException("Order not found: "+ orderId));
        return mapToResponse(order);
    }
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(i -> OrderResponse.OrderItemResponse.builder()
                                .productId(i.getProductId())
                                .productName(i.getProductName())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}
