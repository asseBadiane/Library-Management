package com.library.borrowservice.controller;

import com.library.borrowservice.dto.BorrowApprovalDto;
import com.library.borrowservice.dto.BorrowRequestDto;
import com.library.borrowservice.dto.BorrowResponseDto;
import com.library.borrowservice.dto.ExtensionRequestDto;
import com.library.borrowservice.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@Tag(name = "Borrow Management", description = "APIs for managing book borrowing operations")
public class BorrowController {
    
    private final BorrowService borrowService;
    
    @PostMapping("/request")
    @Operation(summary = "Request to borrow a book")
    public ResponseEntity<BorrowResponseDto> requestBorrow(@Valid @RequestBody BorrowRequestDto request) {
        BorrowResponseDto response = borrowService.requestBorrow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/approve")
    @Operation(summary = "Approve or reject a borrow request")
    public ResponseEntity<BorrowResponseDto> approveBorrowRequest(@Valid @RequestBody BorrowApprovalDto approval) {
        BorrowResponseDto response = borrowService.approveBorrowRequest(approval);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{borrowId}/complete")
    @Operation(summary = "Complete the borrowing process")
    public ResponseEntity<BorrowResponseDto> completeBorrow(@PathVariable Long borrowId) {
        BorrowResponseDto response = borrowService.completeBorrow(borrowId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{borrowId}/return")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<BorrowResponseDto> returnBook(@PathVariable Long borrowId) {
        BorrowResponseDto response = borrowService.returnBook(borrowId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/extend")
    @Operation(summary = "Extend due date for a borrowed book")
    public ResponseEntity<BorrowResponseDto> extendDueDate(@Valid @RequestBody ExtensionRequestDto extension) {
        BorrowResponseDto response = borrowService.extendDueDate(extension);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get borrow history for a user")
    public ResponseEntity<List<BorrowResponseDto>> getUserBorrowHistory(@PathVariable Long userId) {
        List<BorrowResponseDto> history = borrowService.getUserBorrowHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/book/{bookId}")
    @Operation(summary = "Get borrow history for a book")
    public ResponseEntity<List<BorrowResponseDto>> getBookBorrowHistory(@PathVariable Long bookId) {
        List<BorrowResponseDto> history = borrowService.getBookBorrowHistory(bookId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/requests")
    @Operation(summary = "Get all pending borrow requests")
    public ResponseEntity<List<BorrowResponseDto>> getAllBorrowRequests() {
        List<BorrowResponseDto> requests = borrowService.getAllBorrowRequests();
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue books")
    public ResponseEntity<List<BorrowResponseDto>> getOverdueBooks() {
        List<BorrowResponseDto> overdueBooks = borrowService.getOverdueBooks();
        return ResponseEntity.ok(overdueBooks);
    }
}