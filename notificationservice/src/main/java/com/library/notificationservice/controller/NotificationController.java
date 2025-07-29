package com.library.notificationservice.controller;

import com.library.notificationservice.dto.NotificationRequestDto;
import com.library.notificationservice.entity.Notification;
import com.library.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequestDto request) {
        Notification notification = notificationService.createNotification(request);
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/{id}/send")
    @Operation(summary = "Send a specific notification")
    public ResponseEntity<Void> sendNotification(@PathVariable Long id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user ID")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get all pending notifications")
    public ResponseEntity<List<Notification>> getPendingNotifications() {
        List<Notification> notifications = notificationService.getPendingNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed notifications")
    public ResponseEntity<Void> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok().build();
    }
}