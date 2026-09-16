package com.shopsphere.notification.dto;

import com.shopsphere.notification.domain.Notification;
import com.shopsphere.notification.domain.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String userId;
    private UUID orderId;
    private String eventType;
    private String channel;
    private String recipient;
    private String subject;
    private String body;
    private NotificationStatus status;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .orderId(entity.getOrderId())
                .eventType(entity.getEventType())
                .channel(entity.getChannel())
                .recipient(entity.getRecipient())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
