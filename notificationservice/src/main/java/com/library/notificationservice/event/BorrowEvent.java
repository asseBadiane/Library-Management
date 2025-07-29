package com.library.notificationservice.event;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowEvent {
    private String eventType;
    private Long borrowId;
    private Long bookId;
    private Long userId;
    private String status;
    private LocalDateTime timestamp;
    private String details;
}
