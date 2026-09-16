package com.shopsphere.notification.service;

import com.shopsphere.notification.domain.Notification;
import com.shopsphere.notification.domain.NotificationStatus;
import com.shopsphere.notification.messaging.OrderEvent;
import com.shopsphere.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private OrderEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = OrderEvent.builder()
                .orderId(UUID.randomUUID())
                .userId("user123")
                .userEmail("test@example.com")
                .totalAmount(new BigDecimal("99.99"))
                .eventType("ORDER_CREATED")
                .build();
    }

    @Test
    void handleOrderCreated_shouldSendAndSaveNotification() {
        notificationService.handleOrderCreated(testEvent);

        verify(notificationSender).sendGenericNotification(
                eq("test@example.com"),
                contains("Order Received"),
                contains("99.99")
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user123");
        assertThat(saved.getOrderId()).isEqualTo(testEvent.getOrderId());
        assertThat(saved.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SIMULATED);
    }

    @Test
    void handleOrderConfirmed_shouldSendAndSaveNotification() {
        notificationService.handleOrderConfirmed(testEvent);

        verify(notificationSender).sendGenericNotification(
                eq("test@example.com"),
                contains("Order Confirmed"),
                contains("99.99")
        );

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotifications_shouldReturnUserNotifications() {
        Notification notification = Notification.builder()
                .userId("user123")
                .eventType("ORDER_CREATED")
                .build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user123"))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotifications("user123");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user123");
    }
}
