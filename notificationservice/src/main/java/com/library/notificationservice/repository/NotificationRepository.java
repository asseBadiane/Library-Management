package com.library.notificationservice.repository;

import com.library.notificationservice.entity.Notification;
import com.library.notificationservice.enums.NotificationStatus;
import com.library.notificationservice.enums.NotificationType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByStatus(NotificationStatus status);
    
    List<Notification> findByType(NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < :maxRetries")
    List<Notification> findFailedNotificationsForRetry(@Param("status") NotificationStatus status, @Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt >= :since")
    Long countNotificationsByStatusSince(@Param("status") NotificationStatus status, @Param("since") LocalDateTime since);
}