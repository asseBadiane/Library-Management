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
            
            // Determine the correct topic based on event type
            String topic;
            switch (eventType) {
                case "BOOK_ADDED":
                    topic = "book-added";
                    break;
                case "BOOK_UPDATED":
                    topic = "book-updated";
                    break;
                case "BOOK_DELETED":
                    topic = "book-deleted";
                    break;
                case "BOOK_STATUS_CHANGED":
                    topic = "book-status-changed";
                    break;
                default:
                    topic = "book-events";
                    break;
            }
            
            logger.info("Publishing book event: {} for book ID: {} to topic: {}", eventType, book.getId(), topic);
            
            // Use book ID as partition key for ordering
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                topic,
                book.getId().toString(),
                eventJson
            );
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish book event: {} for book ID: {} to topic: {}",
                               eventType, book.getId(), topic, ex);
                } else {
                    logger.info("Book event published successfully: {} for book ID: {} to topic: {} partition: {}",
                               eventType, book.getId(), topic, result.getRecordMetadata().partition());
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