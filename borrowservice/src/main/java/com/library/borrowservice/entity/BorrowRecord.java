package com.library.borrowservice.entity;


import jakarta.persistence.*;
import lombok. *;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.library.borrowservice.enums.BorrowStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long bookId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;
    
    @Column(name = "borrow_date")
    @CreationTimestamp
    private LocalDateTime borrowDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
    
    @Column(name = "return_date")
    private LocalDateTime returnDate;
    
    @Column(name = "extended_due_date")
    private LocalDateTime extendedDueDate;
    
    @Column(name = "fine_amount")
    private Double fineAmount = 0.0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "request_date")
    private LocalDateTime requestDate;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
}