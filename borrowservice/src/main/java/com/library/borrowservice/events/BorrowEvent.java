package com.library.borrowservice.events;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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