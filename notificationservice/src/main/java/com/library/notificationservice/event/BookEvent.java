package com.library.notificationservice.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookEvent {
    private String eventType;
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private String status;
    private LocalDateTime timestamp;
    private String details;
}