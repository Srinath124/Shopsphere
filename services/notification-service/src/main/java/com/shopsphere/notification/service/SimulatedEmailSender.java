package com.shopsphere.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simulated email notification sender.
 * Logs the notification instead of actually sending an email.
 *
 * To add real email: implement NotificationSender with Spring Mail/AWS SES
 * and wire it instead of this implementation via @Primary or @Profile.
 */
@Slf4j
@Component
public class SimulatedEmailSender implements NotificationSender {

    @Override
    public void sendOrderConfirmation(String recipient, String subject, String body) {
        log.info("[EMAIL SIMULATED] To: {} | Subject: {} | Body: {}", recipient, subject, body);
    }

    @Override
    public void sendOrderCancellation(String recipient, String subject, String body) {
        log.info("[EMAIL SIMULATED] To: {} | Subject: {} | Body: {}", recipient, subject, body);
    }

    @Override
    public void sendPaymentFailure(String recipient, String subject, String body) {
        log.warn("[EMAIL SIMULATED] To: {} | Subject: {} | Body: {}", recipient, subject, body);
    }

    @Override
    public void sendGenericNotification(String recipient, String subject, String body) {
        log.info("[EMAIL SIMULATED] To: {} | Subject: {} | Body: {}", recipient, subject, body);
    }
}
