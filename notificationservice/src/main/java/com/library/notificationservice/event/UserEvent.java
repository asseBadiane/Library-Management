package com.library.notificationservice.event;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private String eventType;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime timestamp;
    private String details;
}


