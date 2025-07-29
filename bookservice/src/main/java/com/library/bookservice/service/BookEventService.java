package com.library.bookservice.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bookservice.dto.BookEventDto;
import com.library.bookservice.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BookEventService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookEventService.class);
    private static final String BOOK_EVENTS_TOPIC = "book-events";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public BookEventService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publishBookEvent(String eventType, Book book, String correlationId) {
        try {
            // Set correlation ID in MDC for logging
            MDC.put("correlationId", correlationId);
            
            BookEventDto eventDto = new BookEventDto(
                eventType,
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getStatus(),
                book.getAvailableCopies(),
                correlationId
            );
            
            String eventJson = objectMapper.writeValueAsString(eventDto);
            
            logger.info("Publishing book event: {} for book ID: {}", eventType, book.getId());
            
            // Use book ID as partition key for ordering
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                BOOK_EVENTS_TOPIC, 
                book.getId().toString(), 
                eventJson
            );
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish book event: {} for book ID: {}", 
                               eventType, book.getId(), ex);
                } else {
                    logger.info("Book event published successfully: {} for book ID: {} to partition: {}", 
                               eventType, book.getId(), result.getRecordMetadata().partition());
                }
            });
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize book event: {} for book ID: {}", 
                        eventType, book.getId(), e);
        } finally {
            MDC.clear();
        }
    }
}