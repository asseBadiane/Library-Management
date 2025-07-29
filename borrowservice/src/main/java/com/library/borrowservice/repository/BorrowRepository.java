package com.library.borrowservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.borrowservice.entity.BorrowRecord;
import com.library.borrowservice.enums.BorrowStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {
    
    List<BorrowRecord> findByUserId(Long userId);
    
    List<BorrowRecord> findByBookId(Long bookId);
    
    List<BorrowRecord> findByStatus(BorrowStatus status);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = :status AND br.dueDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(@Param("status") BorrowStatus status, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.bookId = :bookId AND br.status IN ('BORROWED', 'OVERDUE')")
    Optional<BorrowRecord> findActiveBorrowByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.status IN ('BORROWED', 'OVERDUE')")
    List<BorrowRecord> findActiveBorrowsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.userId = :userId AND br.status IN ('BORROWED', 'OVERDUE')")
    Long countActiveBorrowsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate BETWEEN :start AND :end AND br.status = 'BORROWED'")
    List<BorrowRecord> findDueSoonRecords(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}