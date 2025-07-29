package com.library.notificationservice.service;

import com.library.notificationservice.client.BookServiceClient;
import com.library.notificationservice.client.UserServiceClient;
import com.library.notificationservice.dto.BookDto;
import com.library.notificationservice.dto.NotificationRequestDto;
import com.library.notificationservice.dto.UserDto;
import com.library.notificationservice.entity.Notification;
import com.library.notificationservice.enums.NotificationStatus;
import com.library.notificationservice.enums.NotificationType;
import com.library.notificationservice.event.BorrowEvent;
import com.library.notificationservice.event.UserEvent;
import com.library.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final NotificationTemplateService templateService;
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;
    
    @Transactional
    public Notification createNotification(NotificationRequestDto request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setEventType(request.getEventType());
        notification.setEventData(request.getEventData());
        
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public void sendNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (notification.getStatus() == NotificationStatus.SENT) {
            log.info("Notification {} already sent", notificationId);
            return;
        }
        
        try {
            UserDto user = userServiceClient.getUserById(notification.getUserId());
            
            switch (notification.getType()) {
                case EMAIL:
                    sendEmailNotification(notification, user);
                    break;
                case SMS:
                    sendSmsNotification(notification, user);
                    break;
                default:
                    log.warn("Unsupported notification type: {}", notification.getType());
            }
            
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notificationId, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }
        
        notificationRepository.save(notification);
    }
    
    private void sendEmailNotification(Notification notification, UserDto user) {
        String emailContent;
        
        switch (notification.getEventType()) {
            case "USER_CREATED":
                emailContent = templateService.generateUserWelcomeEmail(user);
                break;
            default:
                emailContent = notification.getMessage();
        }
        
        emailService.sendHtmlEmail(user.getEmail(), notification.getSubject(), emailContent);
        notification.setEmailSent(true);
    }
    
    private void sendSmsNotification(Notification notification, UserDto user) {
        log.info("SMS would be sent to user {} with message: {}", user.getId(), notification.getMessage());
        notification.setSmsSent(true);
    }
    
    // Event handlers
    
    public void handleUserEvent(UserEvent event) {
        log.info("Processing user event: {}", event.getEventType());
        
        switch (event.getEventType()) {
            case "USER_CREATED":
                createWelcomeNotification(event);
                break;
            case "USER_UPDATED":
                createUserUpdateNotification(event);
                break;
            default:
                log.debug("No notification handler for user event: {}", event.getEventType());
        }
    }
    
    public void handleBorrowEvent(BorrowEvent event) {
        log.info("Processing borrow event: {}", event.getEventType());
        
        try {
            UserDto user = userServiceClient.getUserById(event.getUserId());
            BookDto book = bookServiceClient.getBookById(event.getBookId());
            
            switch (event.getEventType()) {
                case "BORROW_APPROVED":
                    createBorrowApprovalNotification(event, user, book);
                    break;
                case "BORROW_OVERDUE":
                    createOverdueNotification(event, user, book);
                    break;
                case "BORROW_DUE_SOON":
                    createDueSoonNotification(event, user, book);
                    break;
                case "BOOK_RETURNED":
                    createReturnConfirmationNotification(event, user, book);
                    break;
                default:
                    log.debug("No notification handler for borrow event: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process borrow event {}: {}", event.getEventType(), e.getMessage());
        }
    }
    
    // Private notification creation methods
    
    private void createWelcomeNotification(UserEvent event) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Welcome to Library Management System");
        request.setMessage("Welcome to our library! Your account has been created successfully.");
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    private void createUserUpdateNotification(UserEvent event) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Account Updated");
        request.setMessage("Your account information has been updated successfully.");
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    private void createBorrowApprovalNotification(BorrowEvent event, UserDto user, BookDto book) {
        String emailContent = templateService.generateBorrowApprovalEmail(user, book, event);
        
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Borrow Request Approved - " + book.getTitle());
        request.setMessage(emailContent);
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    private void createOverdueNotification(BorrowEvent event, UserDto user, BookDto book) {
        String emailContent = templateService.generateOverdueNotificationEmail(user, book, event);
        
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Overdue Book - " + book.getTitle());
        request.setMessage(emailContent);
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    private void createDueSoonNotification(BorrowEvent event, UserDto user, BookDto book) {
        String emailContent = templateService.generateDueSoonNotificationEmail(user, book, event);
        
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Book Due Soon - " + book.getTitle());
        request.setMessage(emailContent);
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    private void createReturnConfirmationNotification(BorrowEvent event, UserDto user, BookDto book) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setUserId(event.getUserId());
        request.setSubject("Book Returned Successfully - " + book.getTitle());
        request.setMessage("Thank you! You have successfully returned '" + book.getTitle() + "' by " + book.getAuthor());
        request.setType(NotificationType.EMAIL);
        request.setEventType(event.getEventType());
        
        Notification notification = createNotification(request);
        sendNotification(notification.getId());
    }
    
    // Additional utility methods
    
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.PENDING);
    }
    
    public List<Notification> getFailedNotificationsForRetry() {
        return notificationRepository.findFailedNotificationsForRetry(NotificationStatus.FAILED, 3);
    }
    
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = getFailedNotificationsForRetry();
        log.info("Retrying {} failed notifications", failedNotifications.size());
        
        for (Notification notification : failedNotifications) {
            sendNotification(notification.getId());
        }
    }
}
