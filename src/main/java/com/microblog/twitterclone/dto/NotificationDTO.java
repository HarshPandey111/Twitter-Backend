package com.microblog.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String type; // "LIKE", "FOLLOW", "RETWEET", "COMMENT"
    private String message;
    private Long relatedTweetId;
    private String actorUsername; // Who performed the action
    private LocalDateTime timestamp;
    private Boolean read;
}
