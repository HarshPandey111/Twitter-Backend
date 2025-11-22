package com.microblog.twitterclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TweetRequestDTO {

    @NotBlank(message = "Tweet content cannot be empty")
    @Size(max = 280, message = "Tweet cannot exceed 280 characters")
    private String content;
}
