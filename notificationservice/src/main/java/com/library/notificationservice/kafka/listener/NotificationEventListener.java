package com.library.notificationservice.kafka.listener;

import com.library.notificationservice.event.BorrowEvent;
import com.library.notificationservice.event.UserEvent;
import com.library.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvent(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received user event from topic: {}, partition: {}, offset: {}, event: {}", 
                topic, partition, offset, event.getEventType());
        
        try {
            notificationService.handleUserEvent(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed user event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to process user event: {}, error: {}", event.getEventType(), e.getMessage());
            // In a real scenario, you might want to send to a dead letter queue
            acknowledgment.acknowledge(); // Acknowledge to avoid infinite retry
        }
    }
    
    @KafkaListener(topics = "borrow-events", groupId = "notification-service-group")
    public void handleBorrowEvent(
            @Payload BorrowEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received borrow event from topic: {}, partition: {}, offset: {}, event: {}", 
                topic, partition, offset, event.getEventType());
        
        try {
            notificationService.handleBorrowEvent(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed borrow event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to process borrow event: {}, error: {}", event.getEventType(), e.getMessage());
            acknowledgment.acknowledge();
        }
    }
    
    @KafkaListener(topics = "book-events", groupId = "notification-service-group")
    public void handleBookEvent(
            @Payload String bookEventJson,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received book event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            // Process book events if needed for notifications
            log.info("Book event processed: {}", bookEventJson);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process book event, error: {}", e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}