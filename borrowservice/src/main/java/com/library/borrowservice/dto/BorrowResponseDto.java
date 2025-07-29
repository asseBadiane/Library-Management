package com.library.borrowservice.dto;

import java.time.LocalDateTime;

import com.library.borrowservice.enums.BorrowStatus;

import lombok.Data;

@Data
public class BorrowResponseDto {
    private Long id;
    private Long bookId;
    private Long userId;
    private BorrowStatus status;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LocalDateTime extendedDueDate;
    private Double fineAmount;
    private LocalDateTime createdAt;
    private LocalDateTime requestDate;
    private Long approvedBy;
    private LocalDateTime approvalDate;
    private String rejectionReason;
    
    // Book and User details from external services
    private BookDto book;
    private UserDto user;
}