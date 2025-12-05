package com.microblog.twitterclone.controller;

import com.microblog.twitterclone.dto.NotificationDTO;
import com.microblog.twitterclone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Test endpoint to send notification to specific user
     */
    @PostMapping("/test/{userId}")
    public ResponseEntity<String> sendTestNotification(@PathVariable Long userId) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(999L)
                .userId(userId)
                .type("TEST")
                .message("This is a test notification!")
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        notificationService.sendNotificationToUser(userId, notification);

        return ResponseEntity.ok("Test notification sent to user " + userId);
    }
}
