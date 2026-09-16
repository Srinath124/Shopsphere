package com.shopsphere.notification.service;

import com.shopsphere.notification.domain.Notification;
import com.shopsphere.notification.domain.NotificationStatus;
import com.shopsphere.notification.messaging.OrderEvent;
import com.shopsphere.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    @Transactional
    public void handleOrderCreated(OrderEvent event) {
        String subject = "Order Received - #" + event.getOrderId();
        String body = String.format("Your order #%s has been received and is being processed. Total: $%.2f",
                event.getOrderId(), event.getTotalAmount());

        sendAndSave(event, subject, body, "ORDER_CREATED");
    }

    @Transactional
    public void handleOrderConfirmed(OrderEvent event) {
        String subject = "Order Confirmed! - #" + event.getOrderId();
        String body = String.format(
                "Great news! Your order #%s has been confirmed and your payment of $%.2f was successful.",
                event.getOrderId(), event.getTotalAmount());

        sendAndSave(event, subject, body, "ORDER_CONFIRMED");
    }

    @Transactional
    public void handleOrderCancelled(OrderEvent event) {
        String subject = "Order Cancelled - #" + event.getOrderId();
        String body = String.format("Your order #%s has been cancelled.", event.getOrderId());

        sendAndSave(event, subject, body, "ORDER_CANCELLED");
    }

    @Transactional
    public void handlePaymentSuccess(OrderEvent event) {
        String subject = "Payment Successful - #" + event.getOrderId();
        String body = String.format("Payment of $%.2f for order #%s was successful.",
                event.getTotalAmount(), event.getOrderId());

        sendAndSave(event, subject, body, "PAYMENT_SUCCESS");
    }

    @Transactional
    public void handlePaymentFailed(OrderEvent event) {
        String subject = "Payment Failed - #" + event.getOrderId();
        String body = String.format("We could not process your payment for order #%s. Please try again.",
                event.getOrderId());

        sendAndSave(event, subject, body, "PAYMENT_FAILED");
    }

    public List<Notification> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private void sendAndSave(OrderEvent event, String subject, String body, String eventType) {
        String recipient = event.getUserEmail() != null
                ? event.getUserEmail()
                : event.getUserId() + "@shopsphere.local";

        notificationSender.sendGenericNotification(recipient, subject, body);

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .eventType(eventType)
                .channel("EMAIL")
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .status(NotificationStatus.SIMULATED)
                .build();

        notificationRepository.save(notification);
        log.info("Notification saved for user {} event {}", event.getUserId(), eventType);
    }
}
