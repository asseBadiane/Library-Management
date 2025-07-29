package com.library.bookservice.dto;


import com.library.bookservice.entity.Book;

import java.time.LocalDateTime;

public class BookEventDto {
    private String eventType;
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Book.BookStatus status;
    private Integer availableCopies;
    private LocalDateTime timestamp;
    private String correlationId;

    // Constructors
    public BookEventDto() {}

    public BookEventDto(String eventType, Long bookId, String title, String author, 
                       String isbn, Book.BookStatus status, Integer availableCopies, String correlationId) {
        this.eventType = eventType;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.status = status;
        this.availableCopies = availableCopies;
        this.timestamp = LocalDateTime.now();
        this.correlationId = correlationId;
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Book.BookStatus getStatus() { return status; }
    public void setStatus(Book.BookStatus status) { this.status = status; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}