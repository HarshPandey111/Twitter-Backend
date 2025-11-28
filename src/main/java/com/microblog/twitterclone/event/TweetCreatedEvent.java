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
public class TweetCreatedEvent {
    private Long tweetId;
    private Long authorId;
    private String authorUsername;
    private String content;
    private LocalDateTime createdAt;
}
