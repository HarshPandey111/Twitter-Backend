package com.microblog.twitterclone.service;

import com.microblog.twitterclone.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicLong notificationIdGenerator = new AtomicLong(1);

    /**
     * Send real-time notification to specific user
     */
    public void sendNotificationToUser(Long userId, NotificationDTO notification) {
        log.info("🔔 Sending notification to user {}: {}", userId, notification.getMessage());

        // Send to user's personal notification channel
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                notification
        );
    }

    /**
     * Create and send like notification
     */
    public void createLikeNotification(Long tweetAuthorId, String likerUsername, Long tweetId) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(notificationIdGenerator.getAndIncrement())
                .userId(tweetAuthorId)
                .type("LIKE")
                .message(likerUsername + " liked your tweet")
                .relatedTweetId(tweetId)
                .actorUsername(likerUsername)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        sendNotificationToUser(tweetAuthorId, notification);
    }

    /**
     * Create and send follow notification
     */
    public void createFollowNotification(Long followedUserId, String followerUsername) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(notificationIdGenerator.getAndIncrement())
                .userId(followedUserId)
                .type("FOLLOW")
                .message(followerUsername + " started following you")
                .actorUsername(followerUsername)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        sendNotificationToUser(followedUserId, notification);
    }

    /**
     * Broadcast notification to all connected users
     */
    public void broadcastNotification(NotificationDTO notification) {
        log.info("📢 Broadcasting notification to all users");
        messagingTemplate.convertAndSend("/topic/notifications/all", notification);
    }
}
