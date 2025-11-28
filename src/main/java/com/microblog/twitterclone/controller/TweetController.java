package com.microblog.twitterclone.controller;

import com.microblog.twitterclone.dto.TweetRequestDTO;
import com.microblog.twitterclone.dto.TweetResponseDTO;
import com.microblog.twitterclone.service.FeedService;
import com.microblog.twitterclone.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tweets")
@RequiredArgsConstructor
public class TweetController {

    private final TweetService tweetService;
    private final FeedService feedService;
    // Create tweet
    @PostMapping("/user/{userId}")
    public ResponseEntity<TweetResponseDTO> createTweet(
            @PathVariable Long userId,
            @Valid @RequestBody TweetRequestDTO dto
    ) {
        TweetResponseDTO tweet = tweetService.createTweet(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tweet);
    }

    // Get single tweet
    @GetMapping("/{tweetId}")
    public ResponseEntity<TweetResponseDTO> getTweet(@PathVariable Long tweetId) {
        TweetResponseDTO tweet = tweetService.getTweetById(tweetId);
        return ResponseEntity.ok(tweet);
    }

    // Get all tweets (with pagination)
    @GetMapping
    public ResponseEntity<List<TweetResponseDTO>> getAllTweets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<TweetResponseDTO> tweets = tweetService.getAllTweets(page, size);
        return ResponseEntity.ok(tweets);
    }

    // Get user's tweets (timeline)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TweetResponseDTO>> getUserTweets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<TweetResponseDTO> tweets = tweetService.getUserTweets(userId, page, size);
        return ResponseEntity.ok(tweets);
    }

    // Update tweet
    @PutMapping("/{tweetId}/user/{userId}")
    public ResponseEntity<TweetResponseDTO> updateTweet(
            @PathVariable Long tweetId,
            @PathVariable Long userId,
            @Valid @RequestBody TweetRequestDTO dto
    ) {
        TweetResponseDTO tweet = tweetService.updateTweet(tweetId, userId, dto);
        return ResponseEntity.ok(tweet);
    }

    // Delete tweet
    @DeleteMapping("/{tweetId}/user/{userId}")
    public ResponseEntity<String> deleteTweet(
            @PathVariable Long tweetId,
            @PathVariable Long userId
    ) {
        tweetService.deleteTweet(tweetId, userId);
        return ResponseEntity.ok("Tweet deleted successfully");
    }

    // Like tweet
    // Update this endpoint
    @PostMapping("/{tweetId}/like")
    public ResponseEntity<TweetResponseDTO> likeTweet(@PathVariable Long tweetId) {
        TweetResponseDTO tweet = tweetService.likeTweet(tweetId);
        return ResponseEntity.ok(tweet);
    }


    // Unlike tweet
    @DeleteMapping("/{tweetId}/unlike")
    public ResponseEntity<TweetResponseDTO> unlikeTweet(@PathVariable Long tweetId) {
        TweetResponseDTO tweet = tweetService.unlikeTweet(tweetId);
        return ResponseEntity.ok(tweet);
    }
    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<TweetResponseDTO>> getUserFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<TweetResponseDTO> feed = feedService.getUserFeed(userId, page, size);
        return ResponseEntity.ok(feed);
    }

}
