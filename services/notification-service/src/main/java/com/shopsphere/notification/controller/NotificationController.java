package com.shopsphere.notification.controller;

import com.shopsphere.notification.dto.NotificationResponse;
import com.shopsphere.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification history endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notification history for current authenticated user")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> responses = notificationService.getNotifications(userDetails.getUsername())
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
