package com.orderprocessing.notificationservice.service;

import com.orderprocessing.common.event.InventoryEvent;
import com.orderprocessing.common.event.OrderEvent;
import com.orderprocessing.common.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void notifyOrderPlaced(OrderEvent event) {
        log.info("================================================");
        log.info("NOTIFICATION: Order Placed");
        log.info("   Order ID   : {}", event.getOrderId());
        log.info("   Customer   : {}", event.getCustomerId());
        log.info("   Amount     : ${}", event.getTotalAmount());
        log.info("   Items      : {} item(s)", event.getItems().size());
        log.info("   Status     : {}", event.getOrderStatus());
        log.info("===============================================");
        // In production: send email/SMS via SendGrid, Twilio, etc.
    }

    public void notifyOrderConfirmed(PaymentEvent event) {
        log.info("==============================================");
        log.info("✅ NOTIFICATION: Order Confirmed!");
        log.info("   Order ID      : {}", event.getOrderId());
        log.info("   Customer      : {}", event.getCustomerId());
        log.info("   Amount Paid   : ${}", event.getAmount());
        log.info("   Transaction ID: {}", event.getTransactionId());
        log.info("=============================================");
    }

    public void notifyOrderFailed(PaymentEvent event) {
        log.info("============================================");
        log.info("NOTIFICATION: Order Failed - Payment Issue");
        log.info("   Order ID : {}", event.getOrderId());
        log.info("   Customer : {}", event.getCustomerId());
        log.info("   Reason   : {}", event.getFailureReason());
        log.info("===========================================");
    }

    public void notifyInventoryFailed(InventoryEvent event) {
        log.info("==========================================");
        log.info("NOTIFICATION: Order Failed - Stock Issue");
        log.info("   Order ID : {}", event.getOrderId());
        log.info("   Customer : {}", event.getCustomerId());
        log.info("   Reason   : {}", event.getFailureReason());
        log.info("=========================================");
    }

    public void notifyInventoryReserved(InventoryEvent event) {
        log.info("========================================");
        log.info("NOTIFICATION: Stock Reserved - Processing Payment");
        log.info("   Order ID : {}", event.getOrderId());
        log.info("   Customer : {}", event.getCustomerId());
        log.info("   Items    : {} item(s)", event.getItems().size());
        log.info("=================================================");
    }
}