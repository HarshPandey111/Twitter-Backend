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
public class TweetResponseDTO {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String content;
    private Integer likeCount;
    private Integer retweetCount;
    private LocalDateTime createdAt;
}
