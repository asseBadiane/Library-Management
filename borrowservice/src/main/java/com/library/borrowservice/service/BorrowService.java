package com.library.borrowservice.service;

import com.library.borrowservice.client.BookServiceClient;
import com.library.borrowservice.client.UserServiceClient;
import com.library.borrowservice.dto.BookDto;
import com.library.borrowservice.dto.BorrowApprovalDto;
import com.library.borrowservice.dto.BorrowRequestDto;
import com.library.borrowservice.dto.BorrowResponseDto;
import com.library.borrowservice.dto.ExtensionRequestDto;
import com.library.borrowservice.dto.UserDto;
import com.library.borrowservice.entity.BorrowRecord;
import com.library.borrowservice.enums.BorrowStatus;
import com.library.borrowservice.events.BorrowEvent;
import com.library.borrowservice.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {
    
    private final BorrowRepository borrowRepository;
    private final BookServiceClient bookServiceClient;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final int MAX_BORROW_LIMIT = 5;
    private static final int DEFAULT_BORROW_DAYS = 14;
    
    @Transactional
    public BorrowResponseDto requestBorrow(BorrowRequestDto request) {
        log.info("Processing borrow request for user {} and book {}", request.getUserId(), request.getBookId());
        
        // Validate user exists and can borrow
        UserDto user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check borrow limit
        Long activeBorrows = borrowRepository.countActiveBorrowsByUserId(request.getUserId());
        if (activeBorrows >= MAX_BORROW_LIMIT) {
            throw new RuntimeException("User has reached maximum borrow limit");
        }
        
        // Validate book exists and is available
        BookDto book = bookServiceClient.getBookById(request.getBookId());
        if (book == null || !"AVAILABLE".equals(book.getStatus())) {
            throw new RuntimeException("Book is not available for borrowing");
        }
        
        // Check if book is already borrowed
        borrowRepository.findActiveBorrowByBookId(request.getBookId())
            .ifPresent(br -> {
                throw new RuntimeException("Book is already borrowed");
            });
        
        // Create borrow record
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBookId(request.getBookId());
        borrowRecord.setUserId(request.getUserId());
        borrowRecord.setStatus(BorrowStatus.REQUESTED);
        borrowRecord.setRequestDate(request.getRequestDate());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(DEFAULT_BORROW_DAYS));
        
        BorrowRecord saved = borrowRepository.save(borrowRecord);
        
        // Publish event
        publishBorrowEvent("BORROW_REQUESTED", saved, "Borrow request created");
        
        return mapToResponseDto(saved);
    }
    
    @Transactional
    public BorrowResponseDto approveBorrowRequest(BorrowApprovalDto approval) {
        log.info("Processing approval for borrow {}", approval.getBorrowId());
        
        BorrowRecord borrowRecord = borrowRepository.findById(approval.getBorrowId())
            .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        
        if (borrowRecord.getStatus() != BorrowStatus.REQUESTED) {
            throw new RuntimeException("Borrow request is not in REQUESTED status");
        }
        
        if (approval.getApproved()) {
            borrowRecord.setStatus(BorrowStatus.APPROVED);
            borrowRecord.setApprovedBy(approval.getApproverId());
            borrowRecord.setApprovalDate(LocalDateTime.now());
            
            // Update book status to BORROWED
            bookServiceClient.updateBookStatus(borrowRecord.getBookId(), "BORROWED");
            
            publishBorrowEvent("BORROW_APPROVED", borrowRecord, "Borrow request approved");
        } else {
            borrowRecord.setStatus(BorrowStatus.REJECTED);
            borrowRecord.setRejectionReason(approval.getRejectionReason());
            borrowRecord.setApprovalDate(LocalDateTime.now());
            
            publishBorrowEvent("BORROW_REJECTED", borrowRecord, "Borrow request rejected: " + approval.getRejectionReason());
        }
        
        BorrowRecord saved = borrowRepository.save(borrowRecord);
        return mapToResponseDto(saved);
    }
    
    @Transactional
    public BorrowResponseDto completeBorrow(Long borrowId) {
        BorrowRecord borrowRecord = borrowRepository.findById(borrowId)
            .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        
        if (borrowRecord.getStatus() != BorrowStatus.APPROVED) {
            throw new RuntimeException("Borrow request is not approved");
        }
        
        borrowRecord.setStatus(BorrowStatus.BORROWED);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        
        BorrowRecord saved = borrowRepository.save(borrowRecord);
        
        publishBorrowEvent("BOOK_BORROWED", saved, "Book borrowed successfully");
        
        return mapToResponseDto(saved);
    }
    
    @Transactional
    public BorrowResponseDto returnBook(Long borrowId) {
        BorrowRecord borrowRecord = borrowRepository.findById(borrowId)
            .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        
        if (borrowRecord.getStatus() != BorrowStatus.BORROWED && borrowRecord.getStatus() != BorrowStatus.OVERDUE) {
            throw new RuntimeException("Book is not currently borrowed");
        }
        
        borrowRecord.setStatus(BorrowStatus.RETURNED);
        borrowRecord.setReturnDate(LocalDateTime.now());
        
        // Calculate fine if overdue
        LocalDateTime effectiveDueDate = borrowRecord.getExtendedDueDate() != null ? 
            borrowRecord.getExtendedDueDate() : borrowRecord.getDueDate();
        
        if (LocalDateTime.now().isAfter(effectiveDueDate)) {
            long overdueDays = java.time.Duration.between(effectiveDueDate, LocalDateTime.now()).toDays();
            borrowRecord.setFineAmount(overdueDays * 1.0); // $1 per day fine
        }
        
        // Update book status back to AVAILABLE
        bookServiceClient.updateBookStatus(borrowRecord.getBookId(), "AVAILABLE");
        
        BorrowRecord saved = borrowRepository.save(borrowRecord);
        
        publishBorrowEvent("BOOK_RETURNED", saved, "Book returned successfully");
        
        return mapToResponseDto(saved);
    }
    
    @Transactional
    public BorrowResponseDto extendDueDate(ExtensionRequestDto extension) {
        BorrowRecord borrowRecord = borrowRepository.findById(extension.getBorrowId())
            .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        
        if (borrowRecord.getStatus() != BorrowStatus.BORROWED) {
            throw new RuntimeException("Book is not currently borrowed");
        }
        
        borrowRecord.setExtendedDueDate(extension.getNewDueDate());
        BorrowRecord saved = borrowRepository.save(borrowRecord);
        
        publishBorrowEvent("DUE_DATE_EXTENDED", saved, "Due date extended to " + extension.getNewDueDate());
        
        return mapToResponseDto(saved);
    }
    
    public List<BorrowResponseDto> getUserBorrowHistory(Long userId) {
        List<BorrowRecord> records = borrowRepository.findByUserId(userId);
        return records.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }
    
    public List<BorrowResponseDto> getBookBorrowHistory(Long bookId) {
        List<BorrowRecord> records = borrowRepository.findByBookId(bookId);
        return records.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }
    
    public List<BorrowResponseDto> getAllBorrowRequests() {
        List<BorrowRecord> records = borrowRepository.findByStatus(BorrowStatus.REQUESTED);
        return records.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }
    
    public List<BorrowResponseDto> getOverdueBooks() {
        List<BorrowRecord> records = borrowRepository.findOverdueRecords(BorrowStatus.BORROWED, LocalDateTime.now());
        return records.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }
    
    private BorrowResponseDto mapToResponseDto(BorrowRecord record) {
        BorrowResponseDto dto = new BorrowResponseDto();
        dto.setId(record.getId());
        dto.setBookId(record.getBookId());
        dto.setUserId(record.getUserId());
        dto.setStatus(record.getStatus());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setExtendedDueDate(record.getExtendedDueDate());
        dto.setFineAmount(record.getFineAmount());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setRequestDate(record.getRequestDate());
        dto.setApprovedBy(record.getApprovedBy());
        dto.setApprovalDate(record.getApprovalDate());
        dto.setRejectionReason(record.getRejectionReason());
        
        // Fetch additional details
        try {
            BookDto book = bookServiceClient.getBookById(record.getBookId());
            dto.setBook(book);
        } catch (Exception e) {
            log.warn("Failed to fetch book details for book {}: {}", record.getBookId(), e.getMessage());
        }
        
        try {
            UserDto user = userServiceClient.getUserById(record.getUserId());
            dto.setUser(user);
        } catch (Exception e) {
            log.warn("Failed to fetch user details for user {}: {}", record.getUserId(), e.getMessage());
        }
        
        return dto;
    }
    
    private void publishBorrowEvent(String eventType, BorrowRecord record, String details) {
        BorrowEvent event = new BorrowEvent(
            eventType,
            record.getId(),
            record.getBookId(),
            record.getUserId(),
            record.getStatus().toString(),
            LocalDateTime.now(),
            details
        );
        
        kafkaTemplate.send("borrow-events", event);
        log.info("Published borrow event: {}", eventType);
    }
}