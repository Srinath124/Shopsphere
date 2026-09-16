package com.shopsphere.notification.service;

import com.shopsphere.notification.messaging.OrderEvent;

/**
 * Notification sender abstraction.
 * Allows swapping between simulated email (default) and real SMTP/SES.
 */
public interface NotificationSender {

    void sendOrderConfirmation(String recipient, String subject, String body);

    void sendOrderCancellation(String recipient, String subject, String body);

    void sendPaymentFailure(String recipient, String subject, String body);

    void sendGenericNotification(String recipient, String subject, String body);
}
