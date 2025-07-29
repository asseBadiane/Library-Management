package com.librar_management.user_service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.librar_management.user_service.enums.UserRole;
import com.librar_management.user_service.model.User;

import lombok.RequiredArgsConstructor;


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

    public void sendUserCreatedEvent(Object event) {
        kafkaTemplate.send(userCreatedTopic, event);
    }

    public void sendUserUpdatedEvent(Object event) {
        kafkaTemplate.send(userUpdatedTopic, event);
    }

    public void sendUserDeactivatedEvent(Object event) {
        kafkaTemplate.send(userDeactivatedTopic, event);
    }

    public void publishUserDeactivated(User savedUser) {
     kafkaTemplate.send(userDeactivatedTopic, savedUser);
    }

    public void publishUserUpdated(User updatedUser, UserRole oldRole) {
        kafkaTemplate.send(userUpdatedTopic, updatedUser);
    }
}