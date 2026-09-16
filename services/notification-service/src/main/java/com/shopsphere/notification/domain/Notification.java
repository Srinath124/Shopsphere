package com.shopsphere.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_order_id", columnList = "order_id"),
        @Index(name = "idx_notifications_event_type", columnList = "event_type"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "channel", nullable = false, length = 20)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "recipient", length = 200)
    private String recipient;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.SENT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
