package com.library.notificationservice.dto;

import com.library.notificationservice.enums.NotificationType;
import lombok.Data;

@Data
public class NotificationRequestDto {
    private Long userId;
    private String subject;
    private String message;
    private NotificationType type;
    private String eventType;
    private String eventData;
}