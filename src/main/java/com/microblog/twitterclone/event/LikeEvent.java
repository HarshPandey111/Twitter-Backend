package com.microblog.twitterclone.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent {
    private Long tweetId;
    private Long userId;
    private String username;
    private LocalDateTime timestamp;
    private String eventType; // "LIKE" or "UNLIKE"
}
