package com.librar_management.user_service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.librar_management.user_service.enums.UserRole;
import com.librar_management.user_service.event.UserEvent;
import com.librar_management.user_service.model.User;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class UserEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topic.user-created}")
    private String userCreatedTopic;
    
    @Value("${kafka.topic.user-updated}")
    private String userUpdatedTopic;
    
    @Value("${kafka.topic.user-deactivated}")
    private String userDeactivatedTopic;

    public void sendUserCreatedEvent(User user) {
        UserEvent event = new UserEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setRole(user.getRole().name());
        event.setTimestamp(LocalDateTime.now());
        event.setDetails("User account created");
        
        kafkaTemplate.send(userCreatedTopic, event);
    }

    public void sendUserUpdatedEvent(User user) {
        UserEvent event = new UserEvent();
        event.setEventType("USER_UPDATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setRole(user.getRole().name());
        event.setTimestamp(LocalDateTime.now());
        event.setDetails("User account updated");
        
        kafkaTemplate.send(userUpdatedTopic, event);
    }

    public void sendUserDeactivatedEvent(User user) {
        UserEvent event = new UserEvent();
        event.setEventType("USER_DEACTIVATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setRole(user.getRole().name());
        event.setTimestamp(LocalDateTime.now());
        event.setDetails("User account deactivated");
        
        kafkaTemplate.send(userDeactivatedTopic, event);
    }

    public void publishUserDeactivated(User user) {
        sendUserDeactivatedEvent(user);
    }

    public void publishUserUpdated(User user, UserRole oldRole) {
        sendUserUpdatedEvent(user);
    }
}