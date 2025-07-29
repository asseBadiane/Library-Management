package com.library.notificationservice.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.library.notificationservice.service.NotificationService;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    
    private final NotificationService notificationService;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void retryFailedNotifications() {
        log.info("Starting scheduled retry of failed notifications");
        notificationService.retryFailedNotifications();
    }
}