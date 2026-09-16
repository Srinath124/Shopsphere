package com.shopsphere.notification.messaging;

import com.shopsphere.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order events from the notifications.queue.
 * Handles: ORDER_CREATED, ORDER_CONFIRMED, ORDER_CANCELLED, PAYMENT_SUCCESS, PAYMENT_FAILED
 *
 * Dead-letter queue (notifications.dlq) receives messages that fail after all retries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.notifications-queue}")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received {} event for order {}", event.getEventType(), event.getOrderId());

        try {
            switch (event.getEventType()) {
                case "ORDER_CREATED"   -> notificationService.handleOrderCreated(event);
                case "ORDER_CONFIRMED" -> notificationService.handleOrderConfirmed(event);
                case "ORDER_CANCELLED" -> notificationService.handleOrderCancelled(event);
                case "PAYMENT_SUCCESS" -> notificationService.handlePaymentSuccess(event);
                case "PAYMENT_FAILED"  -> notificationService.handlePaymentFailed(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing {} event for order {}: {}",
                event.getEventType(), event.getOrderId(), e.getMessage());
            throw e; // Re-throw to trigger RabbitMQ retry/DLQ
        }
    }
}
