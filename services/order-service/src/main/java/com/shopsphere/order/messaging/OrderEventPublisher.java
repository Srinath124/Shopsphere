package com.shopsphere.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.orders-exchange}")
    private String ordersExchange;

    public void publishOrderCreated(OrderEvent event) {
        publishEvent(event, "order.created");
    }

    public void publishOrderConfirmed(OrderEvent event) {
        publishEvent(event, "order.confirmed");
    }

    public void publishOrderCancelled(OrderEvent event) {
        publishEvent(event, "order.cancelled");
    }

    public void publishPaymentSuccess(OrderEvent event) {
        publishEvent(event, "payment.success");
    }

    public void publishPaymentFailed(OrderEvent event) {
        publishEvent(event, "payment.failed");
    }

    private void publishEvent(OrderEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(ordersExchange, routingKey, event);
            log.info("Published {} event for order {}", event.getEventType(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish {} event for order {}: {}",
                event.getEventType(), event.getOrderId(), e.getMessage());
            // Don't propagate — order is already created, notification is best-effort
        }
    }
}
