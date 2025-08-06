package com.library.borrowservice.service;


import com.library.borrowservice.entity.BorrowRecord;
import com.library.borrowservice.enums.BorrowStatus;
import com.library.borrowservice.events.BorrowEvent;
import com.library.borrowservice.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowSchedulerService {
    
    private final BorrowRepository borrowRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional
    public void checkOverdueBooks() {
        log.info("Checking for overdue books...");
        
        List<BorrowRecord> overdueRecords = borrowRepository.findOverdueRecords(
            BorrowStatus.BORROWED, 
            LocalDateTime.now()
        );
        
        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowStatus.OVERDUE);
            borrowRepository.save(record);
            
            // Publish overdue event
            BorrowEvent event = new BorrowEvent(
                "BORROW_OVERDUE",
                record.getId(),
                record.getBookId(),
                record.getUserId(),
                BorrowStatus.OVERDUE.toString(),
                LocalDateTime.now(),
                "Book is overdue"
            );
            
            kafkaTemplate.send("borrow-overdue", event);
        }
        
        log.info("Found and updated {} overdue books", overdueRecords.size());
    }
    
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void sendDueSoonNotifications() {
        log.info("Checking for books due soon...");
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
        
        List<BorrowRecord> dueSoonRecords = borrowRepository.findDueSoonRecords(
            LocalDateTime.now(), 
            dayAfterTomorrow
        );
        
        for (BorrowRecord record : dueSoonRecords) {
            BorrowEvent event = new BorrowEvent(
                "BORROW_DUE_SOON",
                record.getId(),
                record.getBookId(),
                record.getUserId(),
                record.getStatus().toString(),
                LocalDateTime.now(),
                "Book is due soon - Due date: " + record.getDueDate()
            );
            
            kafkaTemplate.send("borrow-due-soon", event);
        }
        
        log.info("Sent due soon notifications for {} books", dueSoonRecords.size());
    }
}